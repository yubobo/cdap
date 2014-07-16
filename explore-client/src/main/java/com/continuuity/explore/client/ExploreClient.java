package com.continuuity.explore.client;

import com.continuuity.api.metadata.QueryHandle;
import com.continuuity.explore.service.Explore;
import com.continuuity.explore.service.ExploreException;

/**
 * Explore client discovers explore service, and executes explore commands using the service.
 */
public interface ExploreClient extends Explore {

  /**
   * Returns true if the explore service is up and running.
   */
  boolean isAvailable();

  /**
   * Enables ad-hoc exploration of the given {@link com.continuuity.api.data.batch.RecordScannable}.
   * @param datasetInstance dataset instance name.
   */
  QueryHandle enableExplore(String datasetInstance) throws ExploreException;

  /**
   * Disable ad-hoc exploration of the given {@link com.continuuity.api.data.batch.RecordScannable}.
   * @param datasetInstance dataset instance name.
   */
  QueryHandle disableExplore(String datasetInstance) throws ExploreException;
}
