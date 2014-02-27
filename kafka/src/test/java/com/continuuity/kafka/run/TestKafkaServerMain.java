/*
 * Copyright 2014 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.kafka.run;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.conf.KafkaConstants;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.util.concurrent.AbstractIdleService;
import org.apache.twill.common.Cancellable;
import org.apache.twill.internal.kafka.client.ZKKafkaClientService;
import org.apache.twill.internal.zookeeper.InMemoryZKServer;
import org.apache.twill.kafka.client.Compression;
import org.apache.twill.kafka.client.FetchedMessage;
import org.apache.twill.kafka.client.KafkaClientService;
import org.apache.twill.kafka.client.KafkaConsumer;
import org.apache.twill.kafka.client.KafkaPublisher;
import org.apache.twill.zookeeper.RetryStrategies;
import org.apache.twill.zookeeper.ZKClientService;
import org.apache.twill.zookeeper.ZKClientServices;
import org.apache.twill.zookeeper.ZKClients;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class TestKafkaServerMain {

  private static final Logger LOG = LoggerFactory.getLogger(TestKafkaServerMain.class);

  @ClassRule
  public static TemporaryFolder tmpFolder = new TemporaryFolder();

  @BeforeClass
  public static void init() throws Exception {
    // Start ZK server
    InMemoryZKServer zkServer = InMemoryZKServer.builder().setDataDir(tmpFolder.newFolder()).build();
    zkServer.startAndWait();

    LOG.info("ZK server runs on {}", zkServer.getConnectionStr());

    // Get the test classpath
    ClassLoader classLoader = TestKafkaServerMain.class.getClassLoader();
    String classFileName = TestKafkaServerMain.class.getName().replace('.', '/') + ".class";
    String classFilePath = new File(classLoader.getResource(classFileName).toURI()).getAbsolutePath();
    File basePath = new File(classFilePath.substring(0, classFilePath.length() - classFileName.length()));

    // Generate the continuuity-site.xml file in the test classpath
    File siteFile = new File(basePath, "continuuity-site.xml");
    CConfiguration cConf = CConfiguration.create();
    cConf.clear();
    cConf.set(Constants.CFG_ZOOKEEPER_ENSEMBLE, zkServer.getConnectionStr() + "/${reactor.namespace}");
    cConf.setInt(KafkaConstants.ConfigKeys.NUM_PARTITIONS_CONFIG, 1);
    cConf.set(KafkaConstants.ConfigKeys.LOG_DIR_CONFIG, tmpFolder.newFolder().getAbsolutePath());

    Writer writer = Files.newWriter(siteFile, Charsets.UTF_8);
    try {
      cConf.writeXml(writer);
    } finally {
      writer.close();
    }
  }

  @AfterClass
  public static void finish() {

  }

  @Test
  public void testKafkaServer() throws IOException, InterruptedException {
    KafkaServerMain kafkaServer = new KafkaServerMain();
    kafkaServer.init(new String[0]);
    kafkaServer.start();

    try {

      KafkaClientService kafkaClient = createKafkaClient();
      kafkaClient.startAndWait();

      // Publish some messages and consume it.
      try {
        KafkaPublisher publisher = kafkaClient.getPublisher(KafkaPublisher.Ack.FIRE_AND_FORGET, Compression.NONE);
        String topic = "testKafka";
        publisher.prepare(topic)
          .add(Charsets.UTF_8.encode("Testing 1"), "1")
          .add(Charsets.UTF_8.encode("Testing 2"), "2")
          .add(Charsets.UTF_8.encode("Testing 3"), "3")
          .add(Charsets.UTF_8.encode("Testing 4"), "4")
          .add(Charsets.UTF_8.encode("Testing 5"), "5")
          .send();

        final CountDownLatch latch = new CountDownLatch(5);
        final CountDownLatch complete = new CountDownLatch(1);
        final Queue<String> results = new ConcurrentLinkedQueue<String>();
        Cancellable cancel = kafkaClient.getConsumer().prepare()
          .addFromBeginning(topic, 0)
          .consume(new KafkaConsumer.MessageCallback() {
            @Override
            public void onReceived(Iterator<FetchedMessage> messages) {
              while (messages.hasNext()) {
                results.add(Charsets.UTF_8.decode(messages.next().getPayload()).toString());
                latch.countDown();
              }
            }

            @Override
            public void finished() {
              complete.countDown();
            }
          });

        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));

        int idx = 0;
        for (String str : results) {
          idx++;
          Assert.assertEquals("Testing " + idx, str);
        }

        cancel.cancel();

        Assert.assertTrue(complete.await(5, TimeUnit.SECONDS));

      } finally {
        kafkaClient.stopAndWait();
      }
    } finally {
      try {
        kafkaServer.stop();
      } finally {
        kafkaServer.destroy();
      }
    }
  }

  private KafkaClientService createKafkaClient() {
    CConfiguration cConf = CConfiguration.create();
    String zkConnectStr = cConf.get(Constants.CFG_ZOOKEEPER_ENSEMBLE);

    final ZKClientService zkClientService =
      ZKClientServices.delegate(
        ZKClients.reWatchOnExpire(
          ZKClients.retryOnFailure(
            ZKClientService.Builder.of(zkConnectStr).build(),
            RetryStrategies.exponentialDelay(500, 2000, TimeUnit.MILLISECONDS)
          )
        ));

    // Initialize Kafka client
    String kafkaZKNamespace = cConf.get(KafkaConstants.ConfigKeys.ZOOKEEPER_NAMESPACE_CONFIG);
    final KafkaClientService kafkaClientService = new ZKKafkaClientService(
      kafkaZKNamespace == null
        ? zkClientService
        : ZKClients.namespace(zkClientService, "/" + kafkaZKNamespace)
    );

    return new TestKafkaClientService(zkClientService, kafkaClientService);
  }

  // A KafkaClientService that starts/stops the zkclient as well.
  private static final class TestKafkaClientService extends AbstractIdleService implements KafkaClientService {
    private final ZKClientService zkClientDelegate;
    private final KafkaClientService kafkaDelegate;

    private TestKafkaClientService(ZKClientService zkClientDelegate, KafkaClientService kafkaDelegate) {
      this.zkClientDelegate = zkClientDelegate;
      this.kafkaDelegate = kafkaDelegate;
    }

    @Override
    public KafkaPublisher getPublisher(KafkaPublisher.Ack ack, Compression compression) {
      return kafkaDelegate.getPublisher(ack, compression);
    }

    @Override
    public KafkaConsumer getConsumer() {
      return kafkaDelegate.getConsumer();
    }

    @Override
    protected void startUp() throws Exception {
      zkClientDelegate.startAndWait();
      try {
        kafkaDelegate.startAndWait();
      } catch (Exception e) {
        try {
          zkClientDelegate.stopAndWait();
        } catch (Exception ex) {
          LOG.error("Failed to stop ZK client", ex);
        }
        throw e;
      }
    }

    @Override
    protected void shutDown() throws Exception {
      try {
        kafkaDelegate.stopAndWait();
      } finally {
        try {
          zkClientDelegate.stopAndWait();
        } catch (Exception e) {
          LOG.error("Failed to stop ZK client", e);
        }
      }
    }
  }
}
