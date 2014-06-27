package com.continuuity.explore.service;

import com.continuuity.common.conf.CConfiguration;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Tests whether txns get closed on stopping explore service.
 */
public class HiveExploreServiceStopTest extends BaseHiveExploreServiceTest {
  @BeforeClass
  public static void start() throws Exception {
    startServices(CConfiguration.create());
  }

  @Test
  public void testServiceStop() throws Exception {
    ExploreService exploreService = injector.getInstance(ExploreService.class);
    Set<Long> beforeTxns = transactionManager.getCurrentState().getInProgress().keySet();

    exploreService.execute("show tables");

    Set<Long> queryTxns = Sets.difference(transactionManager.getCurrentState().getInProgress().keySet(), beforeTxns);
    Assert.assertFalse(queryTxns.isEmpty());

    // Give time to the query to execute
    TimeUnit.MILLISECONDS.sleep(1000);

    // Stop explore service
    exploreService.stopAndWait();

    // Make sure that the transaction got closed
    Assert.assertEquals(ImmutableSet.<Long>of(),
                        Sets.intersection(
                          queryTxns,
                          transactionManager.getCurrentState().getInProgress().keySet()).immutableCopy()
    );
  }

}
