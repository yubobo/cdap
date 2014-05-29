package com.continuuity.data2.transaction.queue.inmemory;

import com.continuuity.data2.transaction.stream.StreamAdmin;
import com.continuuity.data2.transaction.stream.StreamConfig;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collection;
import java.util.Map;

/**
 * admin for queues in memory.
 */
@Singleton
public class InMemoryStreamAdmin extends InMemoryQueueAdmin implements StreamAdmin {

  private Map<String, StreamConfig> streams = Maps.newHashMap();

  @Inject
  public InMemoryStreamAdmin(InMemoryQueueService queueService) {
    super(queueService);
  }

  @Override
  public void dropAll() throws Exception {
    queueService.resetStreams();
    streams.clear();
  }

  @Override
  public StreamConfig getConfig(String streamName) {
    return streams.get(streamName);
  }

  @Override
  public void create(String name) throws Exception {
    super.create(name);

    streams.put(name, new StreamConfig(name, Long.MAX_VALUE, Long.MAX_VALUE, null));
  }

  public Collection<StreamConfig> getAll(String accountId) {
    return streams.values();
  }

}
