package com.continuuity.data2.transaction;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class TimingTxSystemClient implements TransactionSystemClient {
  private final TransactionSystemClient delegate;
  private Timing startShortTime;
  private Timing startLongTime;
  private Timing canCommitTime;
  private Timing commitTime;
  private Timing abortTime;
  private Timing invalidateTime;

  @Inject
  public TimingTxSystemClient(@Named("txSystemClient") TransactionSystemClient delegate) {
    this.delegate = delegate;
    this.startShortTime = new Timing("startShortTime", TimeUnit.SECONDS.toMillis(1));
    this.startLongTime = new Timing("startLongTime", TimeUnit.SECONDS.toMillis(1));
    this.canCommitTime = new Timing("canCommitTime", TimeUnit.SECONDS.toMillis(1));
    this.commitTime = new Timing("commitTime", TimeUnit.SECONDS.toMillis(1));
    this.abortTime = new Timing("abortTime", TimeUnit.SECONDS.toMillis(1));
    this.invalidateTime = new Timing("invalidateTime", TimeUnit.SECONDS.toMillis(1));
  }

  @Override
  public Transaction startShort() {
    startShortTime.start();
    try {
      return delegate.startShort();
    } finally {
      startShortTime.end();
    }
  }

  @Override
  public Transaction startShort(int timeout) {
    startShortTime.start();
    try {
      return delegate.startShort();
    } finally {
      startShortTime.end();
    }
  }

  @Override
  public Transaction startLong() {
    startLongTime.start();
    try {
      return delegate.startLong();
    } finally {
      startLongTime.end();
    }
  }

  @Override
  public boolean canCommit(Transaction tx, Collection<byte[]> changeIds) {
    canCommitTime.start();
    try {
      return delegate.canCommit(tx, changeIds);
    } finally {
      canCommitTime.end();
    }
  }

  @Override
  public boolean commit(Transaction tx) {
    commitTime.start();
    try {
      return delegate.commit(tx);
    } finally {
      commitTime.end();
    }
  }

  @Override
  public void abort(Transaction tx) {
    abortTime.start();
    try {
      delegate.abort(tx);
    } finally {
      abortTime.end();
    }
  }

  @Override
  public void invalidate(Transaction tx) {
    invalidateTime.start();
    try {
      delegate.invalidate(tx);
    } finally {
      invalidateTime.end();
    }
  }

  private static class Timing {
    private static final Logger LOG = LoggerFactory.getLogger(Timing.class);

    private final String name;
    private final long reportInterval;

    private long startTs;
    private long lastReportedTs;
    private long currentIntervalCount;
    private long currentIntervalLatency;
    private long totalCount;
    private long totalLatency;

    private long start;

    public Timing(String name, long reportInterval) {
      this.name = name;
      this.reportInterval = reportInterval;
      this.startTs = System.currentTimeMillis();
    }

    public void start() {
      start = System.currentTimeMillis();
    }

    public void end() {
      long now = System.currentTimeMillis();
      long latency = now - start;

      totalCount += 1;
      totalLatency += latency;
      currentIntervalCount += 1;
      currentIntervalLatency += latency;

      // report if needed
      if (now > lastReportedTs + reportInterval) {
        LOG.info(name + " stats. " +
                   " total: " +
                   " {count: " + totalCount +
                   ", time: " + (now - startTs) +
                   ", avg latency: " + (totalLatency / totalCount) + "}" +
                   " last interval: " +
                   " {count: " + currentIntervalCount +
                   ", time: " + (now - lastReportedTs) +
                   ", avg latency: " + (currentIntervalLatency / currentIntervalCount) + "}");
        currentIntervalCount = 0;
        currentIntervalLatency = 0;
        lastReportedTs = now;
      }
    }
  }

}
