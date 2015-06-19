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

package co.cask.cdap.internal.app.runtime.workflow;

import co.cask.cdap.api.metrics.MetricsCollectionService;
import co.cask.cdap.api.templates.AdapterSpecification;
import co.cask.cdap.api.workflow.WorkflowActionContext;
import co.cask.cdap.api.workflow.WorkflowActionSpecification;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.app.runtime.Arguments;
import co.cask.cdap.app.stream.StreamWriterFactory;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.logging.LoggingContext;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.internal.app.runtime.BasicRunnableContext;
import co.cask.cdap.internal.app.runtime.adapter.PluginInstantiator;
import co.cask.cdap.logging.context.WorkflowActionLoggingContext;
import co.cask.cdap.proto.Id;
import co.cask.cdap.templates.AdapterDefinition;
import co.cask.tephra.TransactionSystemClient;
import org.apache.twill.api.RunId;
import org.apache.twill.discovery.DiscoveryServiceClient;

import javax.annotation.Nullable;

/**
 * Default implementation of a {@link WorkflowActionContext}
 */
public class BasicWorkflowActionContext extends BasicRunnableContext<WorkflowActionSpecification>
  implements WorkflowActionContext {

  private final WorkflowActionSpecification specification;


  public BasicWorkflowActionContext(WorkflowActionSpecification spec, Program program, RunId runId, int instanceId,
                                    Arguments runtimeArgs, CConfiguration cConf,
                                    MetricsCollectionService metricsCollectionService,
                                    DatasetFramework datasetFramework, TransactionSystemClient transactionSystemClient,
                                    DiscoveryServiceClient discoveryServiceClient,
                                    StreamWriterFactory streamWriterFactory,
                                    @Nullable AdapterDefinition adapterSpec,
                                    @Nullable PluginInstantiator pluginInstantiator) {
    super(spec, spec.getDatasets(), program, runId, instanceId, runtimeArgs, cConf, metricsCollectionService,
          datasetFramework, transactionSystemClient, discoveryServiceClient, streamWriterFactory,
          adapterSpec, pluginInstantiator);
    this.specification = spec;
  }

  @Override
  public LoggingContext createLoggingContext(Id.Program programId, RunId runId,
                                             @Nullable AdapterSpecification adapterSpec) {
    String adapterName = adapterSpec == null ? null : adapterSpec.getName();
    return new WorkflowActionLoggingContext(programId.getNamespaceId(), programId.getApplicationId(), programId.getId(),
                                            runId.getId(), String.valueOf(getInstanceId()), adapterName);
  }

  @Override
  public WorkflowActionSpecification getSpecification() {
    return specification;
  }
}
