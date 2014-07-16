/*
 * Copyright 2012-2014 Continuuity, Inc.
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

package com.continuuity.api.metadata;

import com.continuuity.api.ProgramSpecification;
import com.continuuity.api.flow.FlowSpecification;
import com.continuuity.api.mapreduce.MapReduceSpecification;
import com.continuuity.api.procedure.ProcedureSpecification;
import com.continuuity.api.service.ServiceSpecification;
import com.continuuity.api.webapp.WebappSpecification;
import com.continuuity.api.workflow.WorkflowSpecification;
import com.google.gson.annotations.SerializedName;

/**
 * Defines types of programs supported by the system.
 */
public enum ProgramType {

  // @SerializedName to maintain backwards-compatibility

  @SerializedName("Flow")
  FLOW(1, "flows", "Flow", true, FlowSpecification.class),

  @SerializedName("Procedure")
  PROCEDURE(2, "procedures", "Procedure", true, ProcedureSpecification.class),

  @SerializedName("Mapreduce")
  MAPREDUCE(3, "mapreduce", "Mapreduce", true, MapReduceSpecification.class),

  @SerializedName("Workflow")
  WORKFLOW(4, "workflows", "Workflow", true, WorkflowSpecification.class),

  @SerializedName("Webapp")
  WEBAPP(5, "webapp", "Webapp", false, WebappSpecification.class),

  @SerializedName("Service")
  SERVICE(6, "services", "Service", true, ServiceSpecification.class);

  private final int programType;
  private final String prettyName;
  private final boolean listable;
  private final Class<? extends ProgramSpecification> specClass;
  private final String categoryName;

  private ProgramType(int type, String categoryName, String prettyName, boolean listable,
                      Class<? extends ProgramSpecification> specClass) {
    this.programType = type;
    this.categoryName = categoryName;
    this.prettyName = prettyName;
    this.listable = listable;
    this.specClass = specClass;
  }

  public static ProgramType typeOfSpecification(ProgramSpecification spec) {
    Class<? extends ProgramSpecification> specClass = spec.getClass();
    for (ProgramType type : ProgramType.values()) {
      if (type.specClass.isAssignableFrom(specClass)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown specification type: " + specClass);
  }

  public boolean isListable() {
    return listable;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public String getPrettyName() {
    return prettyName;
  }

  public static ProgramType valueOfPrettyName(String pretty) {
    return valueOf(pretty.toUpperCase());
  }

  @Override
  public String toString() {
    return prettyName;
  }

}
