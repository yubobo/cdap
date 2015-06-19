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

package co.cask.cdap.internal.app.runtime.worker;

import co.cask.cdap.api.metrics.MetricsCollectionService;
import co.cask.cdap.api.templates.AdapterSpecification;
import co.cask.cdap.api.worker.WorkerContext;
import co.cask.cdap.api.worker.WorkerSpecification;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.app.runtime.Arguments;
import co.cask.cdap.app.stream.StreamWriterFactory;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.logging.LoggingContext;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.internal.app.runtime.BasicRunnableContext;
import co.cask.cdap.internal.app.runtime.adapter.PluginInstantiator;
import co.cask.cdap.logging.context.WorkerLoggingContext;
import co.cask.cdap.proto.Id;
import co.cask.cdap.templates.AdapterDefinition;
import co.cask.tephra.TransactionSystemClient;
import org.apache.twill.api.RunId;
import org.apache.twill.discovery.DiscoveryServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * Default implementation of {@link WorkerContext}
 */
public class BasicWorkerContext extends BasicRunnableContext<WorkerSpecification> implements WorkerContext {
  private static final Logger LOG = LoggerFactory.getLogger(BasicWorkerContext.class);

  private final WorkerSpecification specification;
  private volatile int instanceCount;

  public BasicWorkerContext(WorkerSpecification spec, Program program, RunId runId, int instanceId,
                            int instanceCount, Arguments runtimeArgs, CConfiguration cConf,
                            MetricsCollectionService metricsCollectionService,
                            DatasetFramework datasetFramework,
                            TransactionSystemClient transactionSystemClient,
                            DiscoveryServiceClient discoveryServiceClient,
                            StreamWriterFactory streamWriterFactory,
                            @Nullable AdapterDefinition adapterSpec,
                            @Nullable PluginInstantiator pluginInstantiator) {
    super(spec, spec.getDatasets(), program, runId, instanceId, runtimeArgs, cConf,
          metricsCollectionService, datasetFramework, transactionSystemClient, discoveryServiceClient,
          streamWriterFactory, adapterSpec, pluginInstantiator);
    this.specification = spec;
    this.instanceCount = instanceCount;
  }

  @Override
  public LoggingContext createLoggingContext(Id.Program programId, RunId runId,
                                             @Nullable AdapterSpecification adapterSpec) {
    String adapterName = adapterSpec == null ? null : adapterSpec.getName();
    return new WorkerLoggingContext(programId.getNamespaceId(), programId.getApplicationId(), programId.getId(),
                                    runId.getId(), String.valueOf(getInstanceId()), adapterName);
  }

  @Override
  public WorkerSpecification getSpecification() {
    return specification;
  }

  @Override
  public int getInstanceCount() {
    return instanceCount;
  }

  public void setInstanceCount(int instanceCount) {
    this.instanceCount = instanceCount;
  }

}
