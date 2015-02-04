/*
 * Copyright © 2015 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.internal.app.runtime.schedule;

import co.cask.cdap.api.schedule.SchedulableProgramType;
import co.cask.cdap.api.schedule.Schedule;
import co.cask.cdap.app.runtime.Arguments;
import co.cask.cdap.app.runtime.ProgramRuntimeService;
import co.cask.cdap.app.store.Store;
import co.cask.cdap.app.store.StoreFactory;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.stream.notification.StreamSizeNotification;
import co.cask.cdap.config.PreferencesStore;
import co.cask.cdap.internal.app.runtime.BasicArguments;
import co.cask.cdap.internal.app.runtime.ProgramOptionConstants;
import co.cask.cdap.notifications.feeds.NotificationFeed;
import co.cask.cdap.notifications.feeds.NotificationFeedException;
import co.cask.cdap.notifications.service.NotificationContext;
import co.cask.cdap.notifications.service.NotificationHandler;
import co.cask.cdap.notifications.service.NotificationService;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.ProgramType;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.twill.common.Cancellable;
import org.apache.twill.common.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * {@link Scheduler} that triggers program executions based on data availability.
 */
@Singleton
public class DataScheduler implements Scheduler {
  private static final Logger LOG = LoggerFactory.getLogger(DataScheduler.class);

  private final NotificationService notificationService;
  private final StoreFactory storeFactory;
  private final ProgramRuntimeService programRuntimeService;
  private final PreferencesStore preferencesStore;
  private final ConcurrentMap<String, StreamSizeNotificationSchedule> streamSizeSchedules;

  private Store store;
  private Executor notificationExecutor;

  @Inject
  public DataScheduler(NotificationService notificationService, StoreFactory storeFactory,
                       ProgramRuntimeService programRuntimeService, PreferencesStore preferencesStore) {
    this.notificationService = notificationService;
    this.programRuntimeService = programRuntimeService;
    this.preferencesStore = preferencesStore;
    this.storeFactory = storeFactory;
    this.streamSizeSchedules = Maps.newConcurrentMap();
    this.store = null;
  }

  public void start() {
    notificationExecutor = Executors.newCachedThreadPool(Threads.createDaemonThreadFactory("data-scheduler-%d"));
  }

  public void stop() {
    for (StreamSizeNotificationSchedule streamSizeNotificationSchedule : streamSizeSchedules.values()) {
      streamSizeNotificationSchedule.cancel();
    }
  }

  @Override
  public void schedule(Id.Program program, SchedulableProgramType programType, Schedule schedule) {
    Preconditions.checkArgument(schedule.getScheduleType().equals(Schedule.ScheduleType.DATA),
                                "Schedule task should be of DATA type");
    Preconditions.checkArgument(schedule.getSourceType().equals(Schedule.SourceType.STREAM),
                                "Can only schedule based on data in streams for now");
    try {
      StreamSizeNotificationSchedule streamSizeNotificationSchedule =
        new StreamSizeNotificationSchedule(program, ProgramType.valueOf(programType.name()), schedule);
      StreamSizeNotificationSchedule existing =
        streamSizeSchedules.putIfAbsent(getScheduleId(program, programType, schedule.getName()),
                                        streamSizeNotificationSchedule);
      if (existing == null) {
        streamSizeNotificationSchedule.startOrResume();
      }
    } catch (NotificationFeedException e) {
      LOG.error("Notification feed does not exist for schedule {}", schedule);
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void schedule(Id.Program program, SchedulableProgramType programType, Iterable<Schedule> schedules) {
    for (Schedule s : schedules) {
      schedule(program, programType, s);
    }
  }

  @Override
  public List<ScheduledRuntime> nextScheduledRuntime(Id.Program program, SchedulableProgramType programType) {
    return ImmutableList.of();
  }

  @Override
  public List<String> getScheduleIds(Id.Program program, SchedulableProgramType programType) {
    return ImmutableList.copyOf(streamSizeSchedules.keySet());
  }

  @Override
  public void suspendSchedule(Id.Program program, SchedulableProgramType programType, String scheduleName) {
    StreamSizeNotificationSchedule notificationSchedule =
      streamSizeSchedules.get(getScheduleId(program, programType, scheduleName));
    if (notificationSchedule == null) {
      return;
    }

    notificationSchedule.cancel();
  }

  @Override
  public void resumeSchedule(Id.Program program, SchedulableProgramType programType, String scheduleName) {
    StreamSizeNotificationSchedule notificationSchedule =
      streamSizeSchedules.get(getScheduleId(program, programType, scheduleName));
    if (notificationSchedule == null) {
      return;
    }

    try {
      notificationSchedule.startOrResume();
    } catch (NotificationFeedException e) {
      LOG.error("Notification feed does not exist for schedule {}", scheduleName);
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void deleteSchedule(Id.Program programId, SchedulableProgramType programType, String scheduleName) {
    StreamSizeNotificationSchedule schedule =
      streamSizeSchedules.remove(getScheduleId(programId, programType, scheduleName));
    if (schedule != null) {
      schedule.cancel();
    }
  }

  @Override
  public void deleteSchedules(Id.Program programId, SchedulableProgramType programType) {
    String programScheduleId = getProgramScheduleId(programId, programType);
    synchronized (this) {
      Iterator<Map.Entry<String, StreamSizeNotificationSchedule>> it = streamSizeSchedules.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<String, StreamSizeNotificationSchedule> entry = it.next();
        if (entry.getKey().startsWith(programScheduleId)) {
          entry.getValue().cancel();
          it.remove();
        }
      }
    }
  }

  @Override
  public ScheduleState scheduleState(Id.Program program, SchedulableProgramType programType, String scheduleName) {
    StreamSizeNotificationSchedule notificationSchedule =
      streamSizeSchedules.get(getScheduleId(program, programType, scheduleName));
    if (notificationSchedule == null) {
      return ScheduleState.NOT_FOUND;
    }

    if (notificationSchedule.isRunning()) {
      return ScheduleState.SCHEDULED;
    } else {
      return ScheduleState.SUSPENDED;
    }
  }

  private String getScheduleId(Id.Program program, SchedulableProgramType programType, String scheduleName) {
    return String.format("%s:%s", getProgramScheduleId(program, programType), scheduleName);
  }

  private String getProgramScheduleId(Id.Program program, SchedulableProgramType programType) {
    return String.format("%s:%s:%s:%s", program.getNamespaceId(), program.getApplicationId(),
                         programType.name(), program.getId());
  }

  private Store getStore() {
    if (store == null) {
      store = storeFactory.create();
    }
    return store;
  }

  /**
   * Schedule that uses size notifications from stream to trigger a program execution.
   */
  private final class StreamSizeNotificationSchedule
    implements Cancellable, NotificationHandler<StreamSizeNotification> {
    private final Id.Program programId;
    private final ProgramType programType;
    private final Schedule schedule;

    private Cancellable notificationSubscription;

    private long baseSize;
    private boolean running;

    public StreamSizeNotificationSchedule(Id.Program programId, ProgramType programType, Schedule schedule) {
      Preconditions.checkNotNull(schedule.getSourceType());
      Preconditions.checkArgument(schedule.getSourceType().equals(Schedule.SourceType.STREAM));
      this.programId = programId;
      this.programType = programType;
      this.schedule = schedule;
      this.running = false;
      this.baseSize = 0;
    }

    /**
     * Start triggering execution of a program, based on the data available in a stream. We use a combination
     * of notifications and polling to get that information.
     *
     * @throws NotificationFeedException in case the notification feed corresponding to the stream sizes does
     * not exist
     */
    public void startOrResume() throws NotificationFeedException {
      notificationSubscription = notificationService.subscribe(getFeed(), this, notificationExecutor);
      running = true;

      // TODO poll the stream and get the updated base size here, potentially triggering execution
    }

    private NotificationFeed getFeed() {
      return new NotificationFeed.Builder()
        .setNamespace(Constants.DEFAULT_NAMESPACE)
        .setCategory(Constants.Notification.Stream.STREAM_FEED_CATEGORY)
        .setName(String.format("%sSize", schedule.getSourceName()))
        .build();
    }

    public boolean isRunning() {
      return running;
    }

    @Override
    public void cancel() {
      notificationSubscription.cancel();
      running = false;
    }

    @Override
    public Type getNotificationFeedType() {
      return StreamSizeNotification.class;
    }

    @Override
    public void received(StreamSizeNotification notification, NotificationContext notificationContext) {
      if (notification.getSize() < baseSize + toBytes(schedule.getDataTriggerMB())) {
        return;
      }

      while (true) {
        ScheduleTaskRunner taskRunner = new ScheduleTaskRunner(getStore(), programRuntimeService, preferencesStore);

        Arguments args = new BasicArguments(ImmutableMap.of(
          ProgramOptionConstants.LOGICAL_START_TIME, Long.toString(System.currentTimeMillis()),
          ProgramOptionConstants.SCHEDULE_NAME, schedule.getName(),
          ProgramOptionConstants.TOTAL_STREAM_SIZE, Long.toString(notification.getSize()),
          ProgramOptionConstants.SCHEDULE_NAME, schedule.getName()
        ));

        try {
          LOG.info("About to start schedule {}", schedule);
          taskRunner.run(programId, programType, args);
          break;
        } catch (TaskExecutionException e) {
          LOG.error("Execution exception while running schedule {}", schedule.getName(), e);
          if (e.isRefireImmediately()) {
            LOG.info("Retrying execution for schedule {}", schedule.getName());
          } else {
            break;
          }
        }
      }

      baseSize = notification.getSize();
      LOG.debug("Base size updated to {} for schedule {}", baseSize, schedule);
    }

    private long toBytes(int mb) {
      return ((long) mb) * 1024 * 1024;
    }
  }
}
