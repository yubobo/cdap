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

import co.cask.cdap.api.schedule.SchedulableProgramType;
import co.cask.cdap.api.schedule.StreamSizeSchedule;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.proto.Id;
import co.cask.cdap.test.internal.AppFabricTestHelper;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class DatasetBasedStreamSizeScheduleStoreTest {

  public static DatasetBasedStreamSizeScheduleStore scheduleStore;

  private static final Id.Namespace NAMESPACE = new Id.Namespace(Constants.DEFAULT_NAMESPACE);
  private static final Id.Application APP_ID = new Id.Application(NAMESPACE, "AppWithStreamSizeSchedule");
  private static final Id.Program PROGRAM_ID = new Id.Program(APP_ID, "SampleWorkflow");
  private static final Id.Stream STREAM_ID = Id.Stream.from(NAMESPACE, "stream");
  private static final StreamSizeSchedule STREAM_SCHEDULE_1 = new StreamSizeSchedule("Schedule1", "Every 1M",
                                                                                 STREAM_ID.getName(), 1);
  private static final StreamSizeSchedule STREAM_SCHEDULE_2 = new StreamSizeSchedule("Schedule2", "Every 10M",
                                                                                     STREAM_ID.getName(), 10);
  private static final String SCHEDULE_NAME_1 = "Schedule1";
  private static final String SCHEDULE_NAME_2 = "Schedule2";
  private static final SchedulableProgramType PROGRAM_TYPE = SchedulableProgramType.WORKFLOW;

  @BeforeClass
  public static void set() throws Exception {
    scheduleStore = AppFabricTestHelper.getInjector().getInstance(DatasetBasedStreamSizeScheduleStore.class);
  }

  @Test
  public void testStreamSizeSchedule() throws Exception {
    scheduleStore.persistStreamSizeSchedule(PROGRAM_ID, PROGRAM_TYPE, STREAM_SCHEDULE_1, 0, 0, true);
    scheduleStore.persistStreamSizeSchedule(PROGRAM_ID, PROGRAM_TYPE, STREAM_SCHEDULE_2, 1000, 10,
                                            false);

    // List all schedules
    Assert.assertEquals(ImmutableList.of(
                          new DatasetBasedStreamSizeScheduleStore.StreamSizeScheduleState(
                            PROGRAM_ID, PROGRAM_TYPE, STREAM_SCHEDULE_1, 0, 0, true
                          ),
                          new DatasetBasedStreamSizeScheduleStore.StreamSizeScheduleState(
                            PROGRAM_ID, PROGRAM_TYPE, STREAM_SCHEDULE_2, 1000, 10, false
                          )
                        ),
                        scheduleStore.listStreamSizeSchedules());

    // Suspend a schedule and check that this is reflected
    scheduleStore.suspendStreamSizeSchedule(PROGRAM_ID, PROGRAM_TYPE, SCHEDULE_NAME_1);
    Assert.assertEquals(ImmutableList.of(
                          new DatasetBasedStreamSizeScheduleStore.StreamSizeScheduleState(
                            PROGRAM_ID, PROGRAM_TYPE, STREAM_SCHEDULE_1, 0, 0, false
                          ),
                          new DatasetBasedStreamSizeScheduleStore.StreamSizeScheduleState(
                            PROGRAM_ID, PROGRAM_TYPE, STREAM_SCHEDULE_2, 1000, 10, false
                          )
                        ),
                        scheduleStore.listStreamSizeSchedules());

    // Resume a schedule and check that this is reflected
    scheduleStore.resumeStreamSizeSchedule(PROGRAM_ID, PROGRAM_TYPE, SCHEDULE_NAME_2);
    Assert.assertEquals(ImmutableList.of(
                          new DatasetBasedStreamSizeScheduleStore.StreamSizeScheduleState(
                            PROGRAM_ID, PROGRAM_TYPE, STREAM_SCHEDULE_1, 0, 0, false
                          ),
                          new DatasetBasedStreamSizeScheduleStore.StreamSizeScheduleState(
                            PROGRAM_ID, PROGRAM_TYPE, STREAM_SCHEDULE_2, 1000, 10, true
                          )
                        ),
                        scheduleStore.listStreamSizeSchedules());

    // Update schedule run info
    scheduleStore.updateStreamSizeScheduleBaseRun(PROGRAM_ID, PROGRAM_TYPE, SCHEDULE_NAME_2, 10000l, 100l);
    Assert.assertEquals(ImmutableList.of(
                          new DatasetBasedStreamSizeScheduleStore.StreamSizeScheduleState(
                            PROGRAM_ID, PROGRAM_TYPE, STREAM_SCHEDULE_1, 0, 0, false
                          ),
                          new DatasetBasedStreamSizeScheduleStore.StreamSizeScheduleState(
                            PROGRAM_ID, PROGRAM_TYPE, STREAM_SCHEDULE_2, 10000l, 100l, true
                          )
                        ),
                        scheduleStore.listStreamSizeSchedules());

    // Delete schedules
    scheduleStore.deleteStreamSizeSchedule(PROGRAM_ID, PROGRAM_TYPE, SCHEDULE_NAME_1);
    Assert.assertEquals(ImmutableList.of(
                          new DatasetBasedStreamSizeScheduleStore.StreamSizeScheduleState(
                            PROGRAM_ID, PROGRAM_TYPE, STREAM_SCHEDULE_2, 10000l, 100l, true
                          )
                        ),
                        scheduleStore.listStreamSizeSchedules());
    scheduleStore.deleteStreamSizeSchedule(PROGRAM_ID, PROGRAM_TYPE, SCHEDULE_NAME_2);
    Assert.assertEquals(ImmutableList.<DatasetBasedStreamSizeScheduleStore.StreamSizeScheduleState>of(),
                        scheduleStore.listStreamSizeSchedules());
  }
}
