/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.internal.app.runtime.distributed;

import com.continuuity.api.flow.FlowSpecification;
import com.continuuity.api.flow.FlowletDefinition;
import com.continuuity.api.flow.flowlet.FlowletSpecification;
import com.continuuity.app.program.Program;
import com.continuuity.app.program.Type;
import com.google.common.base.Preconditions;
import org.apache.twill.api.EventHandler;
import org.apache.twill.api.ResourceSpecification;
import org.apache.twill.api.TwillApplication;
import org.apache.twill.api.TwillSpecification;
import org.apache.twill.filesystem.Location;

import java.io.File;
import java.util.Map;

/**
 *
 */
public final class FlowTwillApplication implements TwillApplication {

  private final FlowSpecification spec;
  private final Program program;
  private final File hConfig;
  private final File cConfig;
  private final boolean disableTransaction;
  private final EventHandler eventHandler;

  public FlowTwillApplication(Program program, FlowSpecification spec,
                              File hConfig, File cConfig, boolean disableTransaction,
                              EventHandler eventHandler) {
    this.spec = spec;
    this.program = program;
    this.hConfig = hConfig;
    this.cConfig = cConfig;
    this.disableTransaction = disableTransaction;
    this.eventHandler = eventHandler;
  }

  @Override
  public TwillSpecification configure() {
    TwillSpecification.Builder.MoreRunnable moreRunnable = TwillSpecification.Builder.with()
      .setName(String.format("%s.%s.%s.%s",
                             Type.FLOW.name().toLowerCase(),
                             program.getAccountId(), program.getApplicationId(), spec.getName()))
      .withRunnable();

    Location programLocation = program.getJarLocation();
    String programName = programLocation.getName();
    TwillSpecification.Builder.RunnableSetter runnableSetter = null;
    for (Map.Entry<String, FlowletDefinition> entry  : spec.getFlowlets().entrySet()) {
      FlowletDefinition flowletDefinition = entry.getValue();
      FlowletSpecification flowletSpec = flowletDefinition.getFlowletSpec();
      ResourceSpecification resourceSpec = ResourceSpecification.Builder.with()
        .setVirtualCores(flowletSpec.getResources().getVirtualCores())
        .setMemory(flowletSpec.getResources().getMemoryMB(), ResourceSpecification.SizeUnit.MEGA)
        .setInstances(flowletDefinition.getInstances())
        .build();

      String flowletName = entry.getKey();
      runnableSetter = moreRunnable
        .add(flowletName,
             new FlowletTwillRunnable(flowletName, "hConf.xml", "cConf.xml", disableTransaction), resourceSpec)
        .withLocalFiles()
          .add(programName, programLocation.toURI())
          .add("hConf.xml", hConfig.toURI())
          .add("cConf.xml", cConfig.toURI())
          .add("hive-beeline-0.12.0.2.0.11.0-1.jar", new File("/usr/lib/hive/lib/hive-beeline-0.12.0.2.0.11.0-1.jar").toURI())
          .add("hive-exec-0.12.0.2.0.11.0-1.jar", new File("/usr/lib/hive/lib/hive-exec-0.12.0.2.0.11.0-1.jar").toURI())
          .add("hive-jdbc-0.12.0.2.0.11.0-1.jar", new File("/usr/lib/hive/lib/hive-jdbc-0.12.0.2.0.11.0-1.jar").toURI())
          .add("hive-shims-0.12.0.2.0.11.0-1.jar", new File("/usr/lib/hive/lib/hive-shims-0.12.0.2.0.11.0-1.jar").toURI())
          .add("hive-cli-0.12.0.2.0.11.0-1.jar", new File("/usr/lib/hive/lib/hive-cli-0.12.0.2.0.11.0-1.jar").toURI())
          .add("hive-hbase-handler-0.12.0.2.0.11.0-1.jar", new File("/usr/lib/hive/lib/hive-hbase-handler-0.12.0.2.0.11.0-1.jar").toURI())
          .add("hive-metastore-0.12.0.2.0.11.0-1.jar", new File("/usr/lib/hive/lib/hive-metastore-0.12.0.2.0.11.0-1.jar").toURI())
          .add("hive-common-0.12.0.2.0.11.0-1.jar", new File("/usr/lib/hive/lib/hive-common-0.12.0.2.0.11.0-1.jar").toURI())
          .add("hive-hwi-0.12.0.2.0.11.0-1.jar", new File("/usr/lib/hive/lib/hive-hwi-0.12.0.2.0.11.0-1.jar").toURI())
          .add("hive-serde-0.12.0.2.0.11.0-1.jar", new File("/usr/lib/hive/lib/hive-serde-0.12.0.2.0.11.0-1.jar").toURI())
          .add("hive-contrib-0.12.0.2.0.11.0-1.jar", new File("/usr/lib/hive/lib/hive-contrib-0.12.0.2.0.11.0-1.jar").toURI())
          .add("hive-service-0.12.0.2.0.11.0-1.jar", new File("/usr/lib/hive/lib/hive-service-0.12.0.2.0.11.0-1.jar").toURI())
          .add("explore-client-2.3.0-SNAPSHOT.jar", new File("/opt/continuuity/reactor-master/lib/explore-client-2.3.0-SNAPSHOT.jar").toURI())
          .add("activation-1.1.jar", new File("/usr/lib/hive/lib/activation-1.1.jar").toURI())
          .add("antlr-runtime-3.4.jar", new File("/usr/lib/hive/lib/antlr-runtime-3.4.jar").toURI())
          .add("avro-1.7.1.jar", new File("/usr/lib/hive/lib/avro-1.7.1.jar").toURI())
          .add("avro-mapred-1.7.1.jar", new File("/usr/lib/hive/lib/avro-mapred-1.7.1.jar").toURI())
          .add("bonecp-0.7.1.RELEASE.jar", new File("/usr/lib/hive/lib/bonecp-0.7.1.RELEASE.jar").toURI())
          .add("commons-cli-1.2.jar", new File("/usr/lib/hive/lib/commons-cli-1.2.jar").toURI())
          .add("commons-codec-1.7.jar", new File("/usr/lib/hive/lib/commons-codec-1.7.jar").toURI())
          .add("commons-collections-3.2.1.jar", new File("/usr/lib/hive/lib/commons-collections-3.2.1.jar").toURI())
          .add("commons-compress-1.4.1.jar", new File("/usr/lib/hive/lib/commons-compress-1.4.1.jar").toURI())
          .add("commons-configuration-1.6.jar", new File("/usr/lib/hive/lib/commons-configuration-1.6.jar").toURI())
          .add("commons-el-1.0.jar", new File("/usr/lib/hive/lib/commons-el-1.0.jar").toURI())
          .add("commons-httpclient-3.1.jar", new File("/usr/lib/hive/lib/commons-httpclient-3.1.jar").toURI())
          .add("commons-io-2.4.jar", new File("/usr/lib/hive/lib/commons-io-2.4.jar").toURI())
          .add("commons-lang-2.6.jar", new File("/usr/lib/hive/lib/commons-lang-2.6.jar").toURI())
          .add("commons-logging-1.1.1.jar", new File("/usr/lib/hive/lib/commons-logging-1.1.1.jar").toURI())
          .add("commons-logging-api-1.1.jar", new File("/usr/lib/hive/lib/commons-logging-api-1.1.jar").toURI())
          .add("commons-math-2.2.jar", new File("/usr/lib/hive/lib/commons-math-2.2.jar").toURI())
          .add("commons-pool-1.5.4.jar", new File("/usr/lib/hive/lib/commons-pool-1.5.4.jar").toURI())
          .add("core-3.1.1.jar", new File("/usr/lib/hive/lib/core-3.1.1.jar").toURI())
          .add("datanucleus-api-jdo-3.2.1.jar", new File("/usr/lib/hive/lib/datanucleus-api-jdo-3.2.1.jar").toURI())
          .add("datanucleus-core-3.2.2.jar", new File("/usr/lib/hive/lib/datanucleus-core-3.2.2.jar").toURI())
          .add("datanucleus-rdbms-3.2.1.jar", new File("/usr/lib/hive/lib/datanucleus-rdbms-3.2.1.jar").toURI())
          .add("derby-10.4.2.0.jar", new File("/usr/lib/hive/lib/derby-10.4.2.0.jar").toURI())
          .add("findbugs-annotations-1.3.9-1.jar", new File("/usr/lib/hive/lib/findbugs-annotations-1.3.9-1.jar").toURI())
          .add("guava-12.0.1.jar", new File("/usr/lib/hive/lib/guava-12.0.1.jar").toURI())
          .add("htrace-core-2.01.jar", new File("/usr/lib/hive/lib/htrace-core-2.01.jar").toURI())
          .add("httpclient-4.1.3.jar", new File("/usr/lib/hive/lib/httpclient-4.1.3.jar").toURI())
          .add("httpcore-4.1.4.jar", new File("/usr/lib/hive/lib/httpcore-4.1.4.jar").toURI())
          .add("jackson-core-asl-1.8.8.jar", new File("/usr/lib/hive/lib/jackson-core-asl-1.8.8.jar").toURI())
          .add("jackson-jaxrs-1.8.8.jar", new File("/usr/lib/hive/lib/jackson-jaxrs-1.8.8.jar").toURI())
          .add("jackson-mapper-asl-1.8.8.jar", new File("/usr/lib/hive/lib/jackson-mapper-asl-1.8.8.jar").toURI())
          .add("jackson-xc-1.8.8.jar", new File("/usr/lib/hive/lib/jackson-xc-1.8.8.jar").toURI())
          .add("jamon-runtime-2.3.1.jar", new File("/usr/lib/hive/lib/jamon-runtime-2.3.1.jar").toURI())
          .add("jasper-compiler-5.5.23.jar", new File("/usr/lib/hive/lib/jasper-compiler-5.5.23.jar").toURI())
          .add("jasper-runtime-5.5.23.jar", new File("/usr/lib/hive/lib/jasper-runtime-5.5.23.jar").toURI())
          .add("JavaEWAH-0.3.2.jar", new File("/usr/lib/hive/lib/JavaEWAH-0.3.2.jar").toURI())
          .add("javolution-5.5.1.jar", new File("/usr/lib/hive/lib/javolution-5.5.1.jar").toURI())
          .add("jaxb-api-2.2.2.jar", new File("/usr/lib/hive/lib/jaxb-api-2.2.2.jar").toURI())
          .add("jaxb-impl-2.2.3-1.jar", new File("/usr/lib/hive/lib/jaxb-impl-2.2.3-1.jar").toURI())
          .add("jdo-api-3.0.1.jar", new File("/usr/lib/hive/lib/jdo-api-3.0.1.jar").toURI())
          .add("jersey-core-1.8.jar", new File("/usr/lib/hive/lib/jersey-core-1.8.jar").toURI())
          .add("jersey-json-1.8.jar", new File("/usr/lib/hive/lib/jersey-json-1.8.jar").toURI())
          .add("jersey-server-1.8.jar", new File("/usr/lib/hive/lib/jersey-server-1.8.jar").toURI())
          .add("jettison-1.3.1.jar", new File("/usr/lib/hive/lib/jettison-1.3.1.jar").toURI())
          .add("jetty-6.1.26.jar", new File("/usr/lib/hive/lib/jetty-6.1.26.jar").toURI())
          .add("jetty-sslengine-6.1.26.jar", new File("/usr/lib/hive/lib/jetty-sslengine-6.1.26.jar").toURI())
          .add("jetty-util-6.1.26.jar", new File("/usr/lib/hive/lib/jetty-util-6.1.26.jar").toURI())
          .add("jline-0.9.94.jar", new File("/usr/lib/hive/lib/jline-0.9.94.jar").toURI())
          .add("json-20090211.jar", new File("/usr/lib/hive/lib/json-20090211.jar").toURI())
          .add("jsp-2.1-6.1.14.jar", new File("/usr/lib/hive/lib/jsp-2.1-6.1.14.jar").toURI())
          .add("jsp-api-2.1-6.1.14.jar", new File("/usr/lib/hive/lib/jsp-api-2.1-6.1.14.jar").toURI())
          .add("jsr305-1.3.9.jar", new File("/usr/lib/hive/lib/jsr305-1.3.9.jar").toURI())
          .add("kryo-2.22.jar", new File("/usr/lib/hive/lib/kryo-2.22.jar").toURI())
          .add("libfb303-0.9.0.jar", new File("/usr/lib/hive/lib/libfb303-0.9.0.jar").toURI())
          .add("libthrift-0.9.0.jar", new File("/usr/lib/hive/lib/libthrift-0.9.0.jar").toURI())
          .add("log4j-1.2.17.jar", new File("/usr/lib/hive/lib/log4j-1.2.17.jar").toURI())
          .add("maven-ant-tasks-2.1.3.jar", new File("/usr/lib/hive/lib/maven-ant-tasks-2.1.3.jar").toURI())
          .add("metrics-core-2.1.2.jar", new File("/usr/lib/hive/lib/metrics-core-2.1.2.jar").toURI())
          .add("mysql-connector-java.jar", new File("/usr/lib/hive/lib/mysql-connector-java.jar").toURI())
          .add("netty-3.6.6.Final.jar", new File("/usr/lib/hive/lib/netty-3.6.6.Final.jar").toURI())
          .add("postgresql-jdbc4.jar", new File("/usr/lib/hive/lib/postgresql-jdbc4.jar").toURI())
          .add("protobuf-java-2.5.0.jar", new File("/usr/lib/hive/lib/protobuf-java-2.5.0.jar").toURI())
          .add("servlet-api-2.5-6.1.14.jar", new File("/usr/lib/hive/lib/servlet-api-2.5-6.1.14.jar").toURI())
          .add("slf4j-api-1.7.5.jar", new File("/usr/lib/hive/lib/slf4j-api-1.7.5.jar").toURI())
          .add("snappy-0.2.jar", new File("/usr/lib/hive/lib/snappy-0.2.jar").toURI())
          .add("ST4-4.0.4.jar", new File("/usr/lib/hive/lib/ST4-4.0.4.jar").toURI())
          .add("stax-api-1.0.1.jar", new File("/usr/lib/hive/lib/stax-api-1.0.1.jar").toURI())
          .add("tempus-fugit-1.1.jar", new File("/usr/lib/hive/lib/tempus-fugit-1.1.jar").toURI())
          .add("xz-1.0.jar", new File("/usr/lib/hive/lib/xz-1.0.jar").toURI())
          .apply();
    }

    Preconditions.checkState(runnableSetter != null, "No flowlet for the flow.");
    return runnableSetter.anyOrder().withEventHandler(eventHandler).build();
  }
}
