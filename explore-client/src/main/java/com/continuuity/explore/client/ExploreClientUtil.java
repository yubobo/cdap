package com.continuuity.explore.client;

import com.continuuity.api.metadata.QueryHandle;
import com.continuuity.api.metadata.QueryStatus;
import com.continuuity.explore.service.Explore;
import com.continuuity.explore.service.ExploreException;
import com.continuuity.explore.service.HandleNotFoundException;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * Helper methods for explore client.
 */
public class ExploreClientUtil {

  /**
   * Polls for state of the operation represented by the {@link QueryHandle}, and returns when operation has completed
   * execution.
   * @param exploreClient explore client used to poll status.
   * @param handle handle representing the operation.
   * @param sleepTime time to sleep between pooling.
   * @param timeUnit unit of sleepTime.
   * @param maxTries max number of times to poll.
   * @return completion status of the operation, null on reaching maxTries.
   * @throws ExploreException
   * @throws HandleNotFoundException
   * @throws InterruptedException
   */
  public static QueryStatus waitForCompletionStatus(Explore exploreClient, QueryHandle handle,
                                               long sleepTime, TimeUnit timeUnit, int maxTries)
    throws ExploreException, HandleNotFoundException, InterruptedException, SQLException {
    QueryStatus status;
    int tries = 0;
    do {
      timeUnit.sleep(sleepTime);
      status = exploreClient.getStatus(handle);

      if (++tries > maxTries) {
        break;
      }
    } while (status.getStatus() == QueryStatus.OpStatus.RUNNING ||
             status.getStatus() == QueryStatus.OpStatus.PENDING ||
             status.getStatus() == QueryStatus.OpStatus.INITIALIZED ||
             status.getStatus() == QueryStatus.OpStatus.UNKNOWN);
    return status;
  }
}
