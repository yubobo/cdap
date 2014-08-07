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

package co.cask.cdap.internal.app.runtime;

import co.cask.cdap.api.data.DataSetContext;
import co.cask.cdap.api.dataset.DatasetDefinition;
import co.cask.cdap.api.metrics.Metrics;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.common.metrics.MetricsCollectionService;
import co.cask.cdap.common.metrics.MetricsCollector;
import co.cask.cdap.common.metrics.MetricsScope;
import co.cask.cdap.data.dataset.DataSetInstantiator;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.twill.api.RunId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for program runtime context
 * TODO: ENG-2702 opened to fix the deprecated AbstractContext and cleanup related to context overall.
 */
@Deprecated
public abstract class AbstractContext implements DataSetContext {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractContext.class);

  protected final DataSetInstantiator instantiator;
  private final Program program;
  private final RunId runId;
  private final Map<DatasetKey, Closeable> datasets;

  public AbstractContext(Program program, RunId runId, DataSetInstantiator instantiator) {
    this.program = program;
    this.runId = runId;
    this.datasets = Maps.newConcurrentMap();
    this.instantiator = instantiator;
  }

  public abstract Metrics getMetrics();

  @Override
  public String toString() {
    return String.format("accountId=%s, applicationId=%s, program=%s, runid=%s",
                         getAccountId(), getApplicationId(), getProgramName(), runId);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Closeable> T getDataSet(String name) {
    return getDataSet(name, DatasetDefinition.NO_ARGUMENTS);
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized
  <T extends Closeable> T getDataSet(String name, Map<String, String> arguments) {
    DatasetKey key = new DatasetKey(name, arguments);
    T dataset = (T) datasets.get(key);
    if (dataset != null) {
      return dataset;
    }
    dataset = instantiator.instantiateDataSet(name, arguments);
    Preconditions.checkArgument(dataset != null, "%s is not a known DataSet.", name);
    onInstantiate(name, arguments, dataset);
    datasets.put(key, dataset);
    return dataset;
  }

  /**
   * Hook to call whenever a dataset is newly instantiated. To be overridden by subclasses.
   * @param dataset the newly instantiated dataset
   */
  protected void onInstantiate(String name, Map<String, String> arguments, Object dataset) {
    // do nothing by default
  }

  public String getAccountId() {
    return program.getAccountId();
  }

  public String getApplicationId() {
    return program.getApplicationId();
  }

  public String getProgramName() {
    return program.getName();
  }

  public Program getProgram() {
    return program;
  }

  public RunId getRunId() {
    return runId;
  }

  protected static MetricsCollector getMetricsCollector(MetricsScope scope,
                                                        MetricsCollectionService collectionService, String context) {
    // NOTE: RunId metric is not supported now. Need UI refactoring to enable it.
    return collectionService.getCollector(scope, context, "0");
  }

  /**
   * Flush all writes made by datasets created through this context.
   */
  public void flushOperations() throws Exception {
    instantiator.flushOperations();
  }

  /**
   * Release all resources held by this context, for example, datasets. Subclasses should override this
   * method to release additional resources.
   */
  public void close() {
    for (Closeable ds : datasets.values()) {
      closeDataSet(ds);
    }
  }

  /**
   * Closes one dataset; logs but otherwise ignores exceptions.
   */
  protected void closeDataSet(Closeable ds) {
    try {
      ds.close();
    } catch (Throwable t) {
      LOG.error("Dataset throws exceptions during close:" + ds.toString() + ", in context: " + this);
    }
  }

  private static class DatasetKey {
    private final String name;
    private final Map<String, String> args;

    public DatasetKey(String name, Map<String, String> args) {
      Preconditions.checkNotNull(name);
      this.name = name;
      this.args = (args == null || args.isEmpty()) ? null : ImmutableMap.copyOf(args);
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      if (other == null || !(other instanceof DatasetKey)) {
        return false;
      }
      DatasetKey key = (DatasetKey) other;
      return name.equals(key.name) && Objects.equals(args, key.args);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, args);
    }
  }
}

