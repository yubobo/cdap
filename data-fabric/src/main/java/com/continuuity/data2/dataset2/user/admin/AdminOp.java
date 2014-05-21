package com.continuuity.data2.dataset2.user.admin;

import com.continuuity.data2.datafabric.dataset.DataFabricDatasetManager;

/**
 * Dataset admin operation.
 */
public interface AdminOp {
  AdminOpResponse execute(DataFabricDatasetManager client, String instanceName, ClassLoader classLoader) throws Exception;
}
