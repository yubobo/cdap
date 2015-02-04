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

package co.cask.cdap.api.schedule;


import com.google.common.base.Objects;

/**
 * Defines a cron-based schedule for running a program. 
 */
public class Schedule {
  /**
   * Schedule Type.
   */
  public enum ScheduleType { TIME, DATA }

  /**
   * Source type, in case the {@link ScheduleType} of this schedule is {@code DATA}.
   */
  public enum SourceType { STREAM, DATASET }

  private final String name;

  private final String description;

  private final String cronEntry;

  private final String sourceNamespaceId;

  private final String sourceName;

  private final SourceType sourceType;

  private final Integer dataTriggerMB;

  private final ScheduleType scheduleType;

  public Schedule(String name, String description, String cronEntry) {
    this.name = name;
    this.description = description;
    this.cronEntry = cronEntry;
    this.sourceNamespaceId = null;
    this.sourceName = null;
    this.sourceType = null;
    this.dataTriggerMB = null;
    this.scheduleType = ScheduleType.TIME;
  }

  public Schedule(String name, String description, String sourceNamespaceId, String sourceName, SourceType sourceType,
                  Integer dataTriggerMB) {
    this.name = name;
    this.description = description;
    this.cronEntry = null;
    this.sourceNamespaceId = sourceNamespaceId;
    this.sourceName = sourceName;
    this.sourceType = sourceType;
    this.dataTriggerMB = dataTriggerMB;
    this.scheduleType = ScheduleType.DATA;
  }

  /**
   * @return Name of the schedule.
   */
  public String getName() {
    return name;
  }

  /**
   * @return Schedule description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return Cron expression for the schedule, if this schedule is a time based schedule.
   */
  public String getCronEntry() {
    return cronEntry;
  }

  /**
   * @return the namespace ID of the source of the schedule, if this schedule is a data based schedule.
   */
  public String getSourceNamespaceId() {
    return sourceNamespaceId;
  }

  /**
   * @return the name of the source of the schedule, if this schedule is a data based schedule.
   */
  public String getSourceName() {
    return sourceName;
  }

  /**
   * @return the type of source of the schedule, if this schedule is a data based schedule.
   */
  public SourceType getSourceType() {
    return sourceType;
  }

  /**
   * @return the amount of data triggering , if this schedule is a data based schedule.
   */
  public Integer getDataTriggerMB() {
    return dataTriggerMB;
  }

  /**
   * @return the type of schedule.
   */
  public ScheduleType getScheduleType() {
    return scheduleType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Schedule that = (Schedule) o;

    return Objects.equal(name, that.name) &&
      Objects.equal(description, that.description) &&
      Objects.equal(cronEntry, that.cronEntry) &&
      Objects.equal(sourceNamespaceId, that.sourceNamespaceId) &&
      Objects.equal(sourceName, that.sourceName) &&
      Objects.equal(sourceType, that.sourceType) &&
      Objects.equal(dataTriggerMB, that.dataTriggerMB) &&
      Objects.equal(scheduleType, that.scheduleType);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, description, cronEntry, sourceNamespaceId, sourceName,
                            sourceType, dataTriggerMB, scheduleType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Schedule{");
    sb.append("name='").append(name).append('\'');
    sb.append(", description='").append(description).append('\'');
    sb.append(", cronEntry='").append(cronEntry).append('\'');
    sb.append(", sourceNamespaceId='").append(sourceNamespaceId).append('\'');
    sb.append(", sourceName='").append(sourceName).append('\'');
    sb.append(", sourceType='").append(sourceType).append('\'');
    sb.append(", dataTriggerMB='").append(dataTriggerMB).append('\'');
    sb.append(", scheduleType='").append(scheduleType).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
