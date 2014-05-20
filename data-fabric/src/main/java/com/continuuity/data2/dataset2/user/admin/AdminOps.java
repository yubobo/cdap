package com.continuuity.data2.dataset2.user.admin;

import com.continuuity.data2.datafabric.dataset.DataFabricDatasetManager;
import com.continuuity.internal.data.dataset.DatasetAdmin;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Provides AdminOp instances.
 */
public class AdminOps {

  public static final Map<String, AdminOp> getDefaultAdminOps() {
    return ImmutableMap.<String, AdminOp>builder()
      .put("exists", new Exists())
      .put("create", new Create())
      .put("drop", new Drop())
      .put("truncate", new Truncate())
      .put("upgrade", new Upgrade())
      .build();
  }

  /**
   * Wrapper for {@link DatasetAdmin#exists()}.
   */
  public static final class Exists implements AdminOp {
    @Override
    public Object execute(DataFabricDatasetManager client, String instanceName,
                          ClassLoader classLoader) throws Exception {
      DatasetAdmin admin = client.getAdmin(instanceName, classLoader);
      return admin != null && admin.exists();
    }
  }

  /**
   * Wrapper for {@link DatasetAdmin#create()}.
   */
  public static final class Create implements AdminOp {
    @Override
    public Object execute(DataFabricDatasetManager client, String instanceName,
                          ClassLoader classLoader) throws Exception {
      DatasetAdmin admin = client.getAdmin(instanceName, classLoader);
      Preconditions.checkNotNull(admin, "Dataset instance " + instanceName + " does not exist");
      admin.create();
      return null;
    }
  }

  /**
   * Wrapper for {@link DatasetAdmin#drop()}.
   */
  public static final class Drop implements AdminOp {
    @Override
    public Object execute(DataFabricDatasetManager client, String instanceName,
                          ClassLoader classLoader) throws Exception {
      DatasetAdmin admin = client.getAdmin(instanceName, classLoader);
      Preconditions.checkNotNull(admin, "Dataset instance " + instanceName + " does not exist");
      admin.drop();
      return null;
    }
  }

  /**
   * Wrapper for {@link DatasetAdmin#truncate()}.
   */
  public static final class Truncate implements AdminOp {
    @Override
    public Object execute(DataFabricDatasetManager client, String instanceName,
                          ClassLoader classLoader) throws Exception {
      DatasetAdmin admin = client.getAdmin(instanceName, classLoader);
      Preconditions.checkNotNull(admin, "Dataset instance " + instanceName + " does not exist");
      admin.truncate();
      return null;
    }
  }

  /**
   * Wrapper for {@link DatasetAdmin#upgrade()}.
   */
  public static final class Upgrade implements AdminOp {
    @Override
    public Object execute(DataFabricDatasetManager client, String instanceName,
                          ClassLoader classLoader) throws Exception {
      DatasetAdmin admin = client.getAdmin(instanceName, classLoader);
      Preconditions.checkNotNull(admin, "Dataset instance " + instanceName + " does not exist");
      admin.upgrade();
      return null;
    }
  }
}
