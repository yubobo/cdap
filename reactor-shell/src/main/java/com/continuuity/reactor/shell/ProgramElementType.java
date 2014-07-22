/*
 * Copyright 2014 Continuuity, Inc.
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

package com.continuuity.reactor.shell;

import com.continuuity.reactor.metadata.ProgramType;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Represents types of programs and their elements.
 */
public enum ProgramElementType {

  FLOW("flow", "flows", ProgramType.FLOW, null,
       Capability.HISTORY, Capability.LOGS, Capability.LIVE_INFO, Capability.STATUS, Capability.START_STOP),

  WORKFLOW("workflow", "workflows", ProgramType.WORKFLOW, null,
           Capability.HISTORY, Capability.STATUS, Capability.START_STOP),

  FLOWLET("flowlet", "flowlets", null, ProgramType.FLOW,
          Capability.SCALE),

  PROCEDURE("procedure", "procedures", ProgramType.PROCEDURE, null,
            Capability.HISTORY, Capability.SCALE, Capability.LOGS, Capability.LIVE_INFO, Capability.STATUS,
            Capability.START_STOP),

  SERVICE("service", "services", ProgramType.SERVICE, null, Capability.START_STOP, Capability.STATUS),

  RUNNABLE("runnable", "runnables", null, ProgramType.SERVICE,
           Capability.SCALE, Capability.HISTORY, Capability.LOGS),

  MAPREDUCE("mapreduce", "mapreduce", ProgramType.MAPREDUCE, null,
            Capability.LOGS, Capability.HISTORY, Capability.STATUS, Capability.START_STOP);

  private final String name;
  private final String pluralName;
  private final ProgramType programType;
  private final ProgramType parentType;
  private final Set<Capability> capabilities;

  ProgramElementType(String name, String pluralName, ProgramType programType, ProgramType parentType,
                     Capability... capabilities) {
    this.name = name;
    this.pluralName = pluralName;
    this.programType = programType;
    this.parentType = parentType;
    this.capabilities = Sets.newHashSet(capabilities);
  }

  public boolean isTopLevel() {
    return parentType == null;
  }

  public String getName() {
    return name;
  }

  public String getPluralName() {
    return pluralName;
  }

  public ProgramType getProgramType() {
    return programType;
  }

  public ProgramType getParentType() {
    return parentType;
  }

  public boolean canScale() {
    return capabilities.contains(Capability.SCALE);
  }

  public boolean hasHistory() {
    return capabilities.contains(Capability.HISTORY);
  }

  public boolean hasLogs() {
    return capabilities.contains(Capability.LOGS);
  }

  public boolean hasLiveInfo() {
    return capabilities.contains(Capability.LIVE_INFO);
  }

  public boolean hasStatus() {
    return capabilities.contains(Capability.STATUS);
  }

  public boolean canStartStop() {
    return capabilities.contains(Capability.START_STOP);
  }

  private enum Capability {
    SCALE, HISTORY, LOGS, LIVE_INFO, STATUS, START_STOP
  }
}
