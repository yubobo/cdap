package com.continuuity.data2.datafabric.dataset.service;

import com.continuuity.data2.datafabric.dataset.type.DatasetModuleMeta;
import com.continuuity.internal.data.dataset.DatasetInstanceProperties;
import com.continuuity.internal.data.dataset.DatasetInstanceSpec;
import com.google.common.collect.ImmutableList;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class DatasetAdminHandlerTest extends DatasetInstanceHandlerTest {

  @Test
  public void testList() throws Exception {
    // nothing has been created, modules and types list is empty
    List<DatasetInstanceSpec> instances = getInstances().value;

    // nothing in the beginning
    Assert.assertEquals(0, instances.size());

    // create dataset instance with type that is not yet known to the system should fail
    DatasetInstanceProperties props = new DatasetInstanceProperties.Builder().property("prop1", "val1").build();
    Assert.assertEquals(HttpStatus.SC_NOT_FOUND, createInstance("dataset1", "datasetType2", props));

    // deploy modules
    DatasetTypeHandlerTest.deployModule("module1", TestModule1.class);
    DatasetTypeHandlerTest.deployModule("module2", TestModule2.class);

    // create dataset instance
    Assert.assertEquals(HttpStatus.SC_OK, createInstance("dataset1", "datasetType2", props));

    // verify instance was created
    instances = getInstances().value;
    Assert.assertEquals(1, instances.size());
    // verifying spec is same as expected
    DatasetInstanceSpec dataset1Spec = createSpec("dataset1", "datasetType2", props);
    Assert.assertEquals(dataset1Spec, instances.get(0));

    // verify created instance info can be retrieved
    DatasetInstanceMeta datasetInfo = getInstance("dataset1").value;
    Assert.assertEquals(dataset1Spec, datasetInfo.getSpec());
    Assert.assertEquals(dataset1Spec.getType(), datasetInfo.getType().getName());
    // type meta should have 2 modules that has to be loaded to create type's class and in the order they must be loaded
    List<DatasetModuleMeta> modules = datasetInfo.getType().getModules();
    Assert.assertEquals(2, modules.size());
    DatasetTypeHandlerTest.verify(modules.get(0), "module1", TestModule1.class, ImmutableList.of("datasetType1"),
                                  Collections.<String>emptyList(), ImmutableList.of("module2"));
    DatasetTypeHandlerTest.verify(modules.get(1), "module2", TestModule2.class, ImmutableList.of("datasetType2"),
                                  ImmutableList.of("module1"), Collections.<String>emptyList());

    // try to retrieve non-existed instance
    Assert.assertEquals(HttpStatus.SC_NOT_FOUND, getInstance("non-existing-dataset").status);

    // cannot create instance with same name again
    Assert.assertEquals(HttpStatus.SC_CONFLICT, createInstance("dataset1", "datasetType2", props));
    Assert.assertEquals(1, getInstances().value.size());

    // cannot delete non-existing dataset instance
    Assert.assertEquals(HttpStatus.SC_NOT_FOUND, deleteInstance("non-existing-dataset"));
    Assert.assertEquals(1, getInstances().value.size());

    // delete dataset instance
    Assert.assertEquals(HttpStatus.SC_OK, deleteInstance("dataset1"));
    Assert.assertEquals(0, getInstances().value.size());

    // delete dataset modules
    Assert.assertEquals(HttpStatus.SC_OK, DatasetTypeHandlerTest.deleteModule("module2"));
    Assert.assertEquals(HttpStatus.SC_OK, DatasetTypeHandlerTest.deleteModule("module1"));
  }
}
