package com.continuuity.api.data;

import java.util.List;

/**
 * DataSets which allow integration with external map reduce processing need to implement this interface in order
 * to allow Hive / HCatalog to obtain the correct schema information for reading records.
 */
public interface DataSetSchema {
  public List<DataSetTable> listTables();


}
