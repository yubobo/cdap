/*
 * Copyright © 2014 Cask Data, Inc.
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

package co.cask.cdap.client;

import co.cask.cdap.api.dataset.table.Table;
import co.cask.cdap.client.app.StandaloneDataset;
import co.cask.cdap.client.app.StandaloneDatasetModule;
import co.cask.cdap.client.common.ClientTestBase;
import co.cask.cdap.common.exception.AlreadyExistsException;
import co.cask.cdap.common.exception.DatasetModuleNotFoundException;
import co.cask.cdap.common.exception.DatasetTypeNotFoundException;
import co.cask.cdap.proto.DatasetMeta;
import co.cask.cdap.proto.DatasetModuleMeta;
import co.cask.cdap.proto.DatasetTypeMeta;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.NamespaceMeta;
import co.cask.cdap.test.XSlowTests;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Test for {@link DatasetClient}, {@link DatasetModuleClient}, and {@link DatasetTypeClient}.
 */
@Category(XSlowTests.class)
public class DatasetClientTestRun extends ClientTestBase {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetClientTestRun.class);
  private static final Id.Namespace TEST_NAMESPACE = Id.Namespace.from("testNamespace");
  private static final Id.Namespace OTHER_NAMESPACE = Id.Namespace.from("otherNamespace");

  private DatasetClient datasetClient;
  private DatasetModuleClient moduleClient;
  private DatasetTypeClient typeClient;

  @Before
  public void setUp() throws Throwable {
    super.setUp();
    datasetClient = new DatasetClient(clientConfig);
    moduleClient = new DatasetModuleClient(clientConfig);
    typeClient = new DatasetTypeClient(clientConfig);
    NamespaceClient namespaceClient = new NamespaceClient(clientConfig);
    try {
      namespaceClient.create(new NamespaceMeta.Builder().setName(TEST_NAMESPACE).build());
    } catch (AlreadyExistsException e) {
    }
    try {
      namespaceClient.create(new NamespaceMeta.Builder().setName(OTHER_NAMESPACE).build());
    } catch (AlreadyExistsException e) {
    }
  }

  @After
  public void tearDown() throws Exception {
    NamespaceClient namespaceClient = new NamespaceClient(clientConfig);
    namespaceClient.delete(TEST_NAMESPACE.getId());
    namespaceClient.delete(OTHER_NAMESPACE.getId());
  }

  @Test
  public void testAll() throws Exception {
    Id.Namespace namespace = TEST_NAMESPACE;
    Id.DatasetModule module = Id.DatasetModule.from(namespace, StandaloneDatasetModule.NAME);
    Id.DatasetType type = Id.DatasetType.from(namespace, StandaloneDataset.class.getName());
    Id.DatasetModule moduleInOtherNamespace = Id.DatasetModule.from(OTHER_NAMESPACE, StandaloneDatasetModule.NAME);
    Id.DatasetType typeInOtherNamespace = Id.DatasetType.from(OTHER_NAMESPACE, StandaloneDataset.class.getName());

    int numBaseModules = moduleClient.list(namespace).size();
    int numBaseTypes = typeClient.list(namespace).size();

    LOG.info("Adding Dataset module");
    File moduleJarFile = createAppJarFile(StandaloneDatasetModule.class);
    moduleClient.add(namespace, StandaloneDatasetModule.NAME,
                     StandaloneDatasetModule.class.getName(), moduleJarFile);
    moduleClient.waitForExists(module, 30, TimeUnit.SECONDS);
    Assert.assertEquals(numBaseModules + 1, moduleClient.list(namespace).size());
    Assert.assertEquals(numBaseTypes + 2, typeClient.list(namespace).size());

    LOG.info("Checking that the new Dataset module exists");
    DatasetModuleMeta datasetModuleMeta = moduleClient.get(module);
    Assert.assertNotNull(datasetModuleMeta);
    Assert.assertEquals(StandaloneDatasetModule.NAME, datasetModuleMeta.getName());

    LOG.info("Checking that the new Dataset module does not exist in a different namespace");
    try {
      moduleClient.get(moduleInOtherNamespace);
      Assert.fail("datasetModule found in namespace other than one in which it was expected");
    } catch (DatasetModuleNotFoundException expected) {
    }

    LOG.info("Checking that the new Dataset type exists");
    typeClient.waitForExists(type, 5, TimeUnit.SECONDS);
    DatasetTypeMeta datasetTypeMeta = typeClient.get(type);
    Assert.assertNotNull(datasetTypeMeta);
    Assert.assertEquals(StandaloneDataset.TYPE_NAME, datasetTypeMeta.getName());

    datasetTypeMeta = typeClient.get(type);
    Assert.assertNotNull(datasetTypeMeta);
    Assert.assertEquals(StandaloneDataset.class.getName(), datasetTypeMeta.getName());

    LOG.info("Checking that the new Dataset type does not exist in a different namespace");
    try {
      typeClient.get(typeInOtherNamespace);
      Assert.fail("datasetType found in namespace other than one in which it was expected");
    } catch (DatasetTypeNotFoundException expected) {
    }

    LOG.info("Creating, truncating, and deleting dataset of new Dataset type");
    // Before creating dataset, there are some system datasets already exist
    int numBaseDataset = datasetClient.list(namespace).size();

    Id.DatasetInstance instance = Id.DatasetInstance.from(namespace, "testDataset");

    datasetClient.create(instance.getNamespace(), instance.getId(), StandaloneDataset.TYPE_NAME);
    Assert.assertEquals(numBaseDataset + 1, datasetClient.list(namespace).size());
    datasetClient.truncate(instance);

    DatasetMeta metaBefore = datasetClient.get(instance);
    Assert.assertEquals(0, metaBefore.getSpec().getProperties().size());

    datasetClient.update(instance, ImmutableMap.of("sdf", "foo", "abc", "123"));
    DatasetMeta metaAfter = datasetClient.get(instance);
    Assert.assertEquals(2, metaAfter.getSpec().getProperties().size());
    Assert.assertTrue(metaAfter.getSpec().getProperties().containsKey("sdf"));
    Assert.assertTrue(metaAfter.getSpec().getProperties().containsKey("abc"));
    Assert.assertEquals("foo", metaAfter.getSpec().getProperties().get("sdf"));
    Assert.assertEquals("123", metaAfter.getSpec().getProperties().get("abc"));

    datasetClient.updateExisting(instance, ImmutableMap.of("sdf", "fzz"));
    metaAfter = datasetClient.get(instance);
    Assert.assertEquals(2, metaAfter.getSpec().getProperties().size());
    Assert.assertTrue(metaAfter.getSpec().getProperties().containsKey("sdf"));
    Assert.assertTrue(metaAfter.getSpec().getProperties().containsKey("abc"));
    Assert.assertEquals("fzz", metaAfter.getSpec().getProperties().get("sdf"));
    Assert.assertEquals("123", metaAfter.getSpec().getProperties().get("abc"));

    datasetClient.delete(instance);
    datasetClient.waitForDeleted(instance, 10, TimeUnit.SECONDS);
    Assert.assertEquals(numBaseDataset, datasetClient.list(namespace).size());

    LOG.info("Creating and deleting multiple Datasets");
    for (int i = 1; i <= 3; i++) {
      datasetClient.create(namespace, "testDataset" + i, StandaloneDataset.TYPE_NAME);
    }
    Assert.assertEquals(numBaseDataset + 3, datasetClient.list(namespace).size());
    for (int i = 1; i <= 3; i++) {
      datasetClient.delete(Id.DatasetInstance.from(namespace, "testDataset" + i));
    }
    Assert.assertEquals(numBaseDataset, datasetClient.list(namespace).size());

    LOG.info("Deleting Dataset module");
    moduleClient.delete(module);
    Assert.assertEquals(numBaseModules, moduleClient.list(namespace).size());
    Assert.assertEquals(numBaseTypes, typeClient.list(namespace).size());

    LOG.info("Adding Dataset module and then deleting all Dataset modules");
    moduleClient.add(namespace, "testModule1", StandaloneDatasetModule.class.getName(), moduleJarFile);
    Assert.assertEquals(numBaseModules + 1, moduleClient.list(namespace).size());
    Assert.assertEquals(numBaseTypes + 2, typeClient.list(namespace).size());

    moduleClient.deleteAll(namespace);
    Assert.assertEquals(numBaseModules, moduleClient.list(namespace).size());
    Assert.assertEquals(numBaseTypes, typeClient.list(namespace).size());
  }

  @Test
  public void testSystemTypes() throws Exception {
    // Tests that a dataset can be created in a namespace, even if the type does not exist in that namespace.
    // The dataset type is being resolved from the system namespace.
    Id.Namespace namespace = TEST_NAMESPACE;
    Id.DatasetType type = Id.DatasetType.from(namespace, Table.class.getName());
    Id.DatasetInstance instance = Id.DatasetInstance.from(namespace, "tableTypeDataset");

    Assert.assertFalse(typeClient.exists(type));
    Assert.assertFalse(datasetClient.exists(instance));
    datasetClient.create(namespace, instance.getId(), Table.class.getName());
    Assert.assertTrue(datasetClient.exists(instance));
  }
}
