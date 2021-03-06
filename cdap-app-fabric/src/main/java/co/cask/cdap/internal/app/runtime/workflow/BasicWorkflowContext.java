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
package co.cask.cdap.internal.app.runtime.workflow;

import co.cask.cdap.api.workflow.WorkflowActionSpecification;
import co.cask.cdap.api.workflow.WorkflowContext;
import co.cask.cdap.api.workflow.WorkflowSpecification;
import co.cask.cdap.api.workflow.WorkflowToken;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import javax.annotation.Nullable;

/**
 *
 */
final class BasicWorkflowContext implements WorkflowContext {

  private final WorkflowSpecification workflowSpec;
  private final WorkflowActionSpecification specification;
  private final long logicalStartTime;
  private final ProgramWorkflowRunner programWorkflowRunner;
  private final Map<String, String> runtimeArgs;
  private final WorkflowToken token;

  BasicWorkflowContext(WorkflowSpecification workflowSpec, @Nullable WorkflowActionSpecification specification,
                       long logicalStartTime, @Nullable ProgramWorkflowRunner programWorkflowRunner,
                       Map<String, String> runtimeArgs, WorkflowToken token) {
    this.workflowSpec = workflowSpec;
    this.specification = specification;
    this.logicalStartTime = logicalStartTime;
    this.programWorkflowRunner = programWorkflowRunner;
    this.runtimeArgs = ImmutableMap.copyOf(runtimeArgs);
    this.token = token;
  }

  @Override
  public WorkflowSpecification getWorkflowSpecification() {
    return workflowSpec;
  }

  @Override
  public WorkflowActionSpecification getSpecification() {
    if (specification == null) {
      throw new UnsupportedOperationException("Operation not allowed.");
    }
    return specification;
  }

  @Override
  public long getLogicalStartTime() {
    return logicalStartTime;
  }

  @Override
  public Runnable getProgramRunner(String name) {
    if (programWorkflowRunner == null) {
      throw new UnsupportedOperationException("Operation not allowed.");
    }
    return programWorkflowRunner.create(name);
  }

  @Override
  public Map<String, String> getRuntimeArguments() {
    return runtimeArgs;
  }

  @Override
  public WorkflowToken getToken() {
    return token;
  }
}
