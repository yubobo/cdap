package com.continuuity.internal.data.dataset.schema;

/**
 * Interface that must be implemented by all data sets that expose a schema.
 */
public interface SchemaAware {

  /**
   * @return the schema of the dataset.
   */
  Schema getSchema();

}
