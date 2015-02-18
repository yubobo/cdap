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

package co.cask.cdap.internal.app.runtime.schedule.store;

import co.cask.cdap.api.dataset.table.OrderedTable;
import co.cask.cdap.api.schedule.SchedulableProgramType;
import co.cask.cdap.api.schedule.StreamSizeSchedule;
import co.cask.cdap.proto.Id;
import co.cask.tephra.TransactionExecutorFactory;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Persists {@link StreamSizeSchedule} schedule information into datasets.
 */
public class DatasetBasedStreamSizeScheduleStore {

  // TODO also store the last timestamp, and size of the last run. So that when restarting an active schedule,
  // we know when to start the base from

  // TODO store: row key is schedule id: pgmid.scheduleid
  // value is: in one column, something, in another, another thing, etc

  private static final Logger LOG = LoggerFactory.getLogger(DatasetBasedStreamSizeScheduleStore.class);

  private final TransactionExecutorFactory factory;
  private final ScheduleStoreTableUtil tableUtil;
  private OrderedTable table;

  @Inject
  public DatasetBasedStreamSizeScheduleStore(TransactionExecutorFactory factory, ScheduleStoreTableUtil tableUtil) {
    this.tableUtil = tableUtil;
    this.factory = factory;
  }

  public void persistStreamSizeSchedule(Id.Program programId, SchedulableProgramType programType,
                                        StreamSizeSchedule schedule, long baseRunSize, long baseRunTs,
                                        boolean running) {

  }

  public void suspendStreamSizeSchedule(Id.Program programId, SchedulableProgramType programType, String scheduleName) {

  }

  public void resumeStreamSizeSchedule(Id.Program programId, SchedulableProgramType programType, String scheduleName) {

  }

  public void deleteStreamSizeSchedule(Id.Program programId, SchedulableProgramType programType,
                                       String scheduleName) {

  }

  public void updateStreamSizeScheduleBaseRun(Id.Program programId, SchedulableProgramType programType,
                                              String scheduleName, long newBaseRunSize, long newBaseRunTs) {

  }

  public List<StreamSizeScheduleState> listStreamSizeSchedules() {
    return ImmutableList.of();
  }

  /**
   *
   */
  public static class StreamSizeScheduleState {
    private final Id.Program programId;
    private final SchedulableProgramType programType;
    private final StreamSizeSchedule streamSizeSchedule;
    private final long baseRunSize;
    private final long baseRunTs;
    private final boolean running;

    private StreamSizeScheduleState(Id.Program programId, SchedulableProgramType programType,
                                    StreamSizeSchedule streamSizeSchedule, long baseRunSize,
                                    long baseRunTs, boolean running) {
      this.programId = programId;
      this.programType = programType;
      this.streamSizeSchedule = streamSizeSchedule;
      this.baseRunSize = baseRunSize;
      this.baseRunTs = baseRunTs;
      this.running = running;
    }

    public Id.Program getProgramId() {
      return programId;
    }

    public SchedulableProgramType getProgramType() {
      return programType;
    }

    public StreamSizeSchedule getStreamSizeSchedule() {
      return streamSizeSchedule;
    }

    public long getBaseRunSize() {
      return baseRunSize;
    }

    public long getBaseRunTs() {
      return baseRunTs;
    }

    public boolean isRunning() {
      return running;
    }
  }

}
