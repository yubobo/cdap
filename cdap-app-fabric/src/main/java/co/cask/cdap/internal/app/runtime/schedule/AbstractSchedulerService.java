/*
 * Copyright © 2014-2015 Cask Data, Inc.
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
import co.cask.cdap.api.schedule.StreamSizeSchedule;
import co.cask.cdap.api.schedule.TimeSchedule;
import co.cask.cdap.app.runtime.ProgramRuntimeService;
import co.cask.cdap.app.store.StoreFactory;
import co.cask.cdap.config.PreferencesStore;
import co.cask.cdap.proto.Id;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractIdleService;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Abstract scheduler service common scheduling functionality. For each {@link Schedule} implementation, there is
 * a scheduler that this class will delegate the work to.
 * The extending classes should implement prestart and poststop hooks to perform any action before starting all
 * underlying schedulers and after stopping them.
 */
public abstract class AbstractSchedulerService extends AbstractIdleService implements SchedulerService {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSchedulerService.class);
  private final TimeScheduler timeScheduler;
  private final StreamSizeScheduler streamSizeScheduler;

  // This map is only here to know to which scheduler to route each operation on a schedule. It doesn't matter
  // if it contains more schedule names than actually existing - in particular the call to deleteSchedules() does not
  // modify the map.
  private final ConcurrentMap<String, Class<? extends Schedule>> scheduleNames;

  public AbstractSchedulerService(Supplier<org.quartz.Scheduler> schedulerSupplier,
                                  StreamSizeScheduler streamSizeScheduler,
                                  StoreFactory storeFactory, ProgramRuntimeService programRuntimeService,
                                  PreferencesStore preferencesStore) {
    this.timeScheduler = new TimeScheduler(schedulerSupplier, storeFactory, programRuntimeService, preferencesStore);
    this.streamSizeScheduler = streamSizeScheduler;
    this.scheduleNames = Maps.newConcurrentMap();
  }

  /**
   * Start the quartz scheduler service.
   */
  protected final void startScheduler() {
    try {
      timeScheduler.start();
      streamSizeScheduler.start();
      LOG.info("Started scheduler");
    } catch (SchedulerException e) {
      LOG.error("Error starting scheduler {}", e.getCause(), e);
      throw Throwables.propagate(e);
    }
  }

  /**
   * Stop the quartz scheduler service.
   */
  protected final void stopScheduler() {
    try {
      timeScheduler.stop();
      streamSizeScheduler.stop();
      LOG.info("Stopped scheduler");
    } catch (SchedulerException e) {
      LOG.error("Error stopping scheduler {}", e.getCause(), e);
      throw Throwables.propagate(e);
    }
  }

  // TODO remove this annotation once Schedule becomes an abstract class
  @SuppressWarnings("deprecation")
  @Override
  public void schedule(Id.Program programId, SchedulableProgramType programType, Schedule schedule) {
    if (schedule.isTimeSchedule()) {
      timeScheduler.schedule(programId, programType,
                             new TimeSchedule(schedule.getName(), schedule.getDescription(), schedule.getCronEntry()));
      scheduleNames.put(schedule.getName(), TimeSchedule.class);
    } else if (schedule instanceof TimeSchedule) {
      timeScheduler.schedule(programId, programType, schedule);
      scheduleNames.put(schedule.getName(), TimeSchedule.class);
    } else if (schedule instanceof StreamSizeSchedule) {
      streamSizeScheduler.schedule(programId, programType, schedule);
      scheduleNames.put(schedule.getName(), StreamSizeSchedule.class);
    }
  }

  // TODO remove this annotation once Schedule becomes an abstract class
  @SuppressWarnings("deprecation")
  @Override
  public void schedule(Id.Program programId, SchedulableProgramType programType, Iterable<Schedule> schedules) {
    Set<Schedule> timeSchedules = Sets.newHashSet();
    Set<Schedule> streamSizeSchedules = Sets.newHashSet();
    for (Schedule schedule : schedules) {
      if (schedule.isTimeSchedule()) {
        timeSchedules.add(new TimeSchedule(schedule.getName(), schedule.getDescription(), schedule.getCronEntry()));
        scheduleNames.put(schedule.getName(), TimeSchedule.class);
      } else if (schedule instanceof TimeSchedule) {
        timeSchedules.add(schedule);
        scheduleNames.put(schedule.getName(), TimeSchedule.class);
      } else if (schedule instanceof StreamSizeSchedule) {
        streamSizeSchedules.add(schedule);
        scheduleNames.put(schedule.getName(), StreamSizeSchedule.class);
      }
    }
    if (!timeSchedules.isEmpty()) {
      timeScheduler.schedule(programId, programType, timeSchedules);
    }
    if (!streamSizeSchedules.isEmpty()) {
      streamSizeScheduler.schedule(programId, programType, streamSizeSchedules);
    }
  }

  @Override
  public List<ScheduledRuntime> nextScheduledRuntime(Id.Program program, SchedulableProgramType programType) {
   return timeScheduler.nextScheduledRuntime(program, programType);
  }

  @Override
  public List<String> getScheduleIds(Id.Program program, SchedulableProgramType programType) {
    return ImmutableList.<String>builder()
      .addAll(timeScheduler.getScheduleIds(program, programType))
      .addAll(streamSizeScheduler.getScheduleIds(program, programType))
      .build();
  }

  @Override
  public void suspendSchedule(Id.Program program, SchedulableProgramType programType, String scheduleName) {
    Class<? extends Schedule> clz = scheduleNames.get(scheduleName);
    if (clz == TimeSchedule.class) {
      timeScheduler.suspendSchedule(program, programType, scheduleName);
    } else if (clz == StreamSizeSchedule.class) {
      streamSizeScheduler.suspendSchedule(program, programType, scheduleName);
    }
  }

  @Override
  public void resumeSchedule(Id.Program program, SchedulableProgramType programType, String scheduleName) {
    Class<? extends Schedule> clz = scheduleNames.get(scheduleName);
    if (clz == TimeSchedule.class) {
      timeScheduler.resumeSchedule(program, programType, scheduleName);
    } else if (clz == StreamSizeSchedule.class) {
      streamSizeScheduler.resumeSchedule(program, programType, scheduleName);
    }
  }

  @Override
  public void deleteSchedule(Id.Program program, SchedulableProgramType programType, String scheduleName) {
    Class<? extends Schedule> clz = scheduleNames.remove(scheduleName);
    if (clz == TimeSchedule.class) {
      timeScheduler.deleteSchedule(program, programType, scheduleName);
    } else if (clz == StreamSizeSchedule.class) {
      streamSizeScheduler.deleteSchedule(program, programType, scheduleName);
    }
  }

  @Override
  public void deleteSchedules(Id.Program program, SchedulableProgramType programType) {
    timeScheduler.deleteSchedules(program, programType);
    streamSizeScheduler.deleteSchedules(program, programType);
  }

  @Override
  public ScheduleState scheduleState(Id.Program program, SchedulableProgramType programType, String scheduleName) {
    Class<? extends Schedule> clz = scheduleNames.get(scheduleName);
    if (clz == TimeSchedule.class) {
      return timeScheduler.scheduleState(program, programType, scheduleName);
    } else if (clz == StreamSizeSchedule.class) {
      return streamSizeScheduler.scheduleState(program, programType, scheduleName);
    } else {
      throw new UnsupportedOperationException("Type of schedule unknown: " + clz.toString());
    }
  }
}
