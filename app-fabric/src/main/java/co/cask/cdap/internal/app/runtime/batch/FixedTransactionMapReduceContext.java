/*
 * Copyright 2014 Cask, Inc.
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

package co.cask.cdap.internal.app.runtime.batch;

import co.cask.cdap.api.mapreduce.MapReduceSpecification;
import co.cask.cdap.app.metrics.MapReduceMetrics;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.app.runtime.Arguments;
import co.cask.cdap.common.metrics.MetricsCollectionService;
import co.cask.cdap.data.dataset.DataSetInstantiator;
import co.cask.cdap.internal.app.runtime.ProgramServiceDiscovery;
import com.continuuity.tephra.Transaction;
import com.continuuity.tephra.TransactionAware;
import com.continuuity.tephra.TransactionContext;
import org.apache.twill.api.RunId;

import java.util.Map;

/**
 * Extends the m/r context by allowing to dynamically instantiate datasets. This context will
 * be used inside mappers and reducers, where the transaction is fixed for the entire life time
 * of this context. Every time a dataset is instantiated through the context, it will make sure
 * to inform the dataset of the transaction by calling startTx().
 */
public class FixedTransactionMapReduceContext extends BasicMapReduceContext {

  private TransactionContext txContext;
  private Transaction tx;

  public FixedTransactionMapReduceContext(Program program, MapReduceMetrics.TaskType type,
                                          RunId runId, Arguments runtimeArguments,
                                          DataSetInstantiator dataSetInstantiator,
                                          MapReduceSpecification spec,
                                          long logicalStartTime, String workflowBatch,
                                          ProgramServiceDiscovery serviceDiscovery,
                                          MetricsCollectionService metricsCollectionService,
                                          Transaction tx) {
    super(program, type, runId, runtimeArguments, dataSetInstantiator, spec,
          logicalStartTime, workflowBatch, serviceDiscovery, metricsCollectionService);
    this.tx = tx;
  }

  @Override
  protected void onInstantiate(String name, Map<String, String> arguments, Object dataset) {
    if (dataset instanceof TransactionAware) {
      ((TransactionAware) dataset).startTx(tx);
    }
  }
}
