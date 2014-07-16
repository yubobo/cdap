package com.continuuity.explore.service;

import com.continuuity.api.metadata.ColumnDesc;
import com.continuuity.api.metadata.QueryHandle;
import com.continuuity.api.metadata.QueryResult;
import com.continuuity.api.metadata.QueryStatus;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for exploring datasets.
 */
public interface Explore {

  /**
   * Execute a Hive SQL statement asynchronously. The returned {@link QueryHandle} can be used to get the status/result
   * of the operation.

   * @param statement SQL statement.
   * @return {@link com.continuuity.api.metadata.QueryHandle} representing the operation.
   * @throws ExploreException on any error executing statement.
   * @throws SQLException if there are errors in the SQL statement.
   */
  QueryHandle execute(String statement) throws ExploreException, SQLException;

  /**
   * Fetch the status of a running Hive operation.

   * @param handle handle returned by {@link #execute(String)}.
   * @return status of the operation.
   * @throws ExploreException on any error fetching status.
   * @throws HandleNotFoundException when handle is not found.
   * @throws SQLException if there are errors in the SQL statement.
   */
  QueryStatus getStatus(QueryHandle handle) throws ExploreException, HandleNotFoundException, SQLException;

  /**
   * Fetch the schema of the result of a Hive operation. This can be called only after the state of the operation is
   *               {@link QueryStatus.OpStatus#FINISHED}.

   * @param handle handle returned by {@link #execute(String)}.
   * @return list of {@link ColumnDesc} representing the schema of the results. Empty list if there are no results.
   * @throws ExploreException on any error fetching schema.
   * @throws HandleNotFoundException when handle is not found.
   * @throws SQLException if there are errors in the SQL statement.
   */
  List<ColumnDesc> getResultSchema(QueryHandle handle) throws ExploreException, HandleNotFoundException, SQLException;

  /**
   * Fetch the results of a Hive operation. This can be called only after the state of the operation is
   *               {@link QueryStatus.OpStatus#FINISHED}. Can be called multiple times, until it returns an empty list
   *               indicating the end of results.

   * @param handle handle returned by {@link #execute(String)}.
   * @param size max rows to fetch in the call.
   * @return list of {@link com.continuuity.api.metadata.QueryResult}s.
   * @throws ExploreException on any error fetching results.
   * @throws HandleNotFoundException when handle is not found.
   * @throws SQLException if there are errors in the SQL statement.
   */
  List<QueryResult> nextResults(QueryHandle handle, int size)
    throws ExploreException, HandleNotFoundException, SQLException;

  /**
   * Cancel a running Hive operation. After the operation moves into a {@link QueryStatus.OpStatus#CANCELED},
   * {@link #close(QueryHandle)} needs to be called to release resources.

   * @param handle handle returned by {@link #execute(String)}.
   * @throws ExploreException on any error cancelling operation.
   * @throws HandleNotFoundException when handle is not found.
   * @throws SQLException if there are errors in the SQL statement.
   */
  void cancel(QueryHandle handle) throws ExploreException, HandleNotFoundException, SQLException;

  /**
   * Release resources associated with a Hive operation. After this call, handle of the operation becomes invalid.

   * @param handle handle returned by {@link #execute(String)}.
   * @throws ExploreException on any error closing operation.
   * @throws HandleNotFoundException when handle is not found.
   */
  void close(QueryHandle handle) throws ExploreException, HandleNotFoundException;
}
