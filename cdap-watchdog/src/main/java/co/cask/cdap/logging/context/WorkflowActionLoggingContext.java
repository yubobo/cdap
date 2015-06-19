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

package co.cask.cdap.logging.context;

import co.cask.cdap.api.workflow.WorkflowAction;
import co.cask.cdap.common.logging.ApplicationLoggingContext;

import javax.annotation.Nullable;

/**
 * Logging Context for {@link WorkflowAction}
 */
public class WorkflowActionLoggingContext extends ApplicationLoggingContext {

  public static final String TAG_WORKFLOWACTION_ID = ".workflowActionId";

  /**
   * Constructs ApplicationLoggingContext.
   *
   * @param namespaceId   namespace id
   * @param applicationId application id
   * @param runId         run id of the application
   */
  public WorkflowActionLoggingContext(String namespaceId, String applicationId, String actionId, String runId,
                                      String instanceId, @Nullable String adapterId) {
    super(namespaceId, applicationId, runId);
    setSystemTag(TAG_WORKFLOWACTION_ID, actionId);
    setInstanceId(instanceId);
    if (adapterId != null) {
      setAdapterId(adapterId);
    }
  }

  @Override
  public String getLogPartition() {
    return String.format("%s:%s", super.getLogPartition(), getSystemTag(TAG_WORKFLOWACTION_ID));
  }

  @Override
  public String getLogPathFragment(String logBaseDir) {
    return String.format("%s/workflowaction-%s", super.getLogPathFragment(logBaseDir),
                         getSystemTag(TAG_WORKFLOWACTION_ID));
  }
}
