package com.continuuity.data2.datafabric.dataset.type;

import com.continuuity.api.metadata.DatasetModuleMeta;

import java.io.IOException;

/**
 * Creates a {@link ClassLoader} for a {@link com.continuuity.api.metadata.DatasetModuleMeta}.
 */
public interface DatasetTypeClassLoaderFactory {

  ClassLoader create(DatasetModuleMeta moduleMeta, ClassLoader parentClassLoader) throws IOException;

}
