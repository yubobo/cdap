package com.continuuity.examples.dataset;

import com.continuuity.api.dataset.Dataset;
import com.continuuity.api.dataset.DatasetAdmin;
import com.continuuity.api.dataset.DatasetProperties;
import com.continuuity.api.dataset.module.DatasetModule;
import com.continuuity.data2.dataset2.DatasetFramework;
import com.continuuity.data2.dataset2.DatasetManagementException;
import com.continuuity.data2.dataset2.InMemoryDatasetFramework;
import com.continuuity.data2.dataset2.lib.table.CoreDatasetsModule;
import com.continuuity.data2.dataset2.module.lib.inmemory.InMemoryOrderedTableModule;
import com.continuuity.data2.transaction.DefaultTransactionExecutor;
import com.continuuity.data2.transaction.TransactionAware;
import com.continuuity.data2.transaction.TransactionExecutor;
import com.continuuity.data2.transaction.inmemory.MinimalTxSystemClient;
import com.google.common.base.Preconditions;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import javax.annotation.Nullable;

/**
 *
 */
public class AbstractDatasetTest {

  private DatasetFramework framework;

  @Before
  public void setUp() throws Exception {
    framework = new InMemoryDatasetFramework();
    framework.addModule("inMemory", new InMemoryOrderedTableModule());
    framework.addModule("core", new CoreDatasetsModule());
  }

  @After
  public void tearDown() throws Exception {
    framework.deleteModule("core");
    framework.deleteModule("inMemory");
  }

  protected void addModule(String name, DatasetModule module) throws DatasetManagementException {
    framework.addModule(name, module);
  }

  protected void deleteModule(String name) throws DatasetManagementException {
    framework.deleteModule(name);
  }

  protected void createInstance(String type, String instanceName, DatasetProperties properties)
    throws IOException, DatasetManagementException {

    framework.addInstance(type, instanceName, properties);
  }

  protected void deleteInstance(String instanceName) throws IOException, DatasetManagementException {
    framework.deleteInstance(instanceName);
  }

  protected <T extends Dataset> T getInstance(String datasetName) throws DatasetManagementException, IOException {
    return framework.getDataset(datasetName, null);
  }

  protected <T extends DatasetAdmin> T getAdmin(String datasetName, @Nullable ClassLoader classLoader)
    throws IOException, DatasetManagementException {

    return framework.getAdmin(datasetName, classLoader);
  }

  protected <T extends DatasetAdmin> T getAdmin(String datasetName) throws IOException, DatasetManagementException {
    return framework.getAdmin(datasetName, null);
  }

  protected TransactionExecutor newTransactionExecutor(TransactionAware...tables) {
    Preconditions.checkArgument(tables != null);
    return new DefaultTransactionExecutor(new MinimalTxSystemClient(), tables);
  }
}
