/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.internal.app.runtime.distributed;

import com.continuuity.app.program.Program;
import com.continuuity.app.runtime.ProgramController;
import com.continuuity.app.runtime.ProgramOptions;
import com.continuuity.app.runtime.ProgramRunner;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.twill.AbortOnTimeoutEventHandler;
import com.continuuity.data.security.HBaseTokenUtils;
import com.continuuity.data2.util.hbase.HBaseTableUtilFactory;
import com.continuuity.internal.app.program.ForwardingProgram;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.Credentials;
import org.apache.twill.api.EventHandler;
import org.apache.twill.api.TwillApplication;
import org.apache.twill.api.TwillController;
import org.apache.twill.api.TwillPreparer;
import org.apache.twill.api.TwillRunner;
import org.apache.twill.api.logging.PrinterLogHandler;
import org.apache.twill.common.ServiceListenerAdapter;
import org.apache.twill.common.Threads;
import org.apache.twill.filesystem.LocalLocationFactory;
import org.apache.twill.filesystem.Location;
import org.apache.twill.yarn.YarnSecureStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Defines the base framework for starting {@link Program} in the cluster.
 */
public abstract class AbstractDistributedProgramRunner implements ProgramRunner {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractDistributedProgramRunner.class);

  private final TwillRunner twillRunner;
  private final Configuration hConf;
  private final CConfiguration cConf;
  protected final EventHandler eventHandler;

  /**
   * An interface for launching TwillApplication. Used by sub-classes only.
   */
  protected interface ApplicationLauncher {
    TwillController launch(TwillApplication twillApplication);
  }

  protected AbstractDistributedProgramRunner(TwillRunner twillRunner, Configuration hConf, CConfiguration cConf) {
    this.twillRunner = twillRunner;
    this.hConf = hConf;
    this.cConf = cConf;
    this.eventHandler = createEventHandler(cConf);
  }

  protected EventHandler createEventHandler(CConfiguration cConf) {
    return new AbortOnTimeoutEventHandler(cConf.getLong(Constants.CFG_TWILL_NO_CONTAINER_TIMEOUT, Long.MAX_VALUE));
  }

  @Override
  public final ProgramController run(final Program program, final ProgramOptions options) {
    final File hConfFile;
    final File cConfFile;
    final Program copiedProgram;
    try {
      // Copy config files and program jar to local temp, and ask Twill to localize it to container.
      // What Twill does is to save those files in HDFS and keep using them during the lifetime of application.
      // Twill will manage the cleanup of those files in HDFS.
      hConfFile = saveHConf(hConf, File.createTempFile("hConf", ".xml"));
      cConfFile = saveCConf(cConf, File.createTempFile("cConf", ".xml"));
      copiedProgram = copyProgramJar(program);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }

    final String runtimeArgs = new Gson().toJson(options.getUserArguments());

    // Obtains and add the HBase delegation token as well (if in non-secure mode, it's a no-op)
    // Twill would also ignore it if it is not running in secure mode.
    // The HDFS token should already obtained by Twill.
    return launch(copiedProgram, options, hConfFile, cConfFile, new ApplicationLauncher() {
      @Override
      public TwillController launch(TwillApplication twillApplication) {
        TwillPreparer twillPreparer = twillRunner
            .prepare(twillApplication);
        if (options.isDebug()) {
          LOG.info("Starting {} with debugging enabled.", program.getId());
          twillPreparer.enableDebugging();
        }
        TwillController twillController = twillPreparer
            .withDependencies(new HBaseTableUtilFactory().get().getClass())
            .withClassPaths("hive-beeline-0.12.0.2.0.11.0-1.jar")
            .withClassPaths("hive-exec-0.12.0.2.0.11.0-1.jar")
            .withClassPaths("hive-jdbc-0.12.0.2.0.11.0-1.jar")
            .withClassPaths("hive-shims-0.12.0.2.0.11.0-1.jar")
            .withClassPaths("hive-cli-0.12.0.2.0.11.0-1.jar")
            .withClassPaths("hive-hbase-handler-0.12.0.2.0.11.0-1.jar")
            .withClassPaths("hive-metastore-0.12.0.2.0.11.0-1.jar")
            .withClassPaths("hive-common-0.12.0.2.0.11.0-1.jar")
            .withClassPaths("hive-hwi-0.12.0.2.0.11.0-1.jar")
            .withClassPaths("hive-serde-0.12.0.2.0.11.0-1.jar")
            .withClassPaths("hive-contrib-0.12.0.2.0.11.0-1.jar")
            .withClassPaths("hive-service-0.12.0.2.0.11.0-1.jar")
            .withClassPaths("explore-client-2.3.0-SNAPSHOT.jar")
            .withClassPaths("activation-1.1.jar")
            .withClassPaths("antlr-runtime-3.4.jar")
            .withClassPaths("avro-1.7.1.jar")
            .withClassPaths("avro-mapred-1.7.1.jar")
            .withClassPaths("bonecp-0.7.1.RELEASE.jar")
            .withClassPaths("commons-cli-1.2.jar")
            .withClassPaths("commons-codec-1.7.jar")
            .withClassPaths("commons-collections-3.2.1.jar")
            .withClassPaths("commons-compress-1.4.1.jar")
            .withClassPaths("commons-configuration-1.6.jar")
            .withClassPaths("commons-el-1.0.jar")
            .withClassPaths("commons-httpclient-3.1.jar")
            .withClassPaths("commons-io-2.4.jar")
            .withClassPaths("commons-lang-2.6.jar")
            .withClassPaths("commons-logging-1.1.1.jar")
            .withClassPaths("commons-logging-api-1.1.jar")
            .withClassPaths("commons-math-2.2.jar")
            .withClassPaths("commons-pool-1.5.4.jar")
            .withClassPaths("core-3.1.1.jar")
            .withClassPaths("datanucleus-api-jdo-3.2.1.jar")
            .withClassPaths("datanucleus-core-3.2.2.jar")
            .withClassPaths("datanucleus-rdbms-3.2.1.jar")
            .withClassPaths("derby-10.4.2.0.jar")
            .withClassPaths("findbugs-annotations-1.3.9-1.jar")
            .withClassPaths("guava-12.0.1.jar")
            .withClassPaths("htrace-core-2.01.jar")
            .withClassPaths("httpclient-4.1.3.jar")
            .withClassPaths("httpcore-4.1.4.jar")
            .withClassPaths("jackson-core-asl-1.8.8.jar")
            .withClassPaths("jackson-jaxrs-1.8.8.jar")
            .withClassPaths("jackson-mapper-asl-1.8.8.jar")
            .withClassPaths("jackson-xc-1.8.8.jar")
            .withClassPaths("jamon-runtime-2.3.1.jar")
            .withClassPaths("jasper-compiler-5.5.23.jar")
            .withClassPaths("jasper-runtime-5.5.23.jar")
            .withClassPaths("JavaEWAH-0.3.2.jar")
            .withClassPaths("javolution-5.5.1.jar")
            .withClassPaths("jaxb-api-2.2.2.jar")
            .withClassPaths("jaxb-impl-2.2.3-1.jar")
            .withClassPaths("jdo-api-3.0.1.jar")
            .withClassPaths("jersey-core-1.8.jar")
            .withClassPaths("jersey-json-1.8.jar")
            .withClassPaths("jersey-server-1.8.jar")
            .withClassPaths("jettison-1.3.1.jar")
            .withClassPaths("jetty-6.1.26.jar")
            .withClassPaths("jetty-sslengine-6.1.26.jar")
            .withClassPaths("jetty-util-6.1.26.jar")
            .withClassPaths("jline-0.9.94.jar")
            .withClassPaths("json-20090211.jar")
            .withClassPaths("jsp-2.1-6.1.14.jar")
            .withClassPaths("jsp-api-2.1-6.1.14.jar")
            .withClassPaths("jsr305-1.3.9.jar")
            .withClassPaths("kryo-2.22.jar")
            .withClassPaths("libfb303-0.9.0.jar")
            .withClassPaths("libthrift-0.9.0.jar")
            .withClassPaths("log4j-1.2.17.jar")
            .withClassPaths("maven-ant-tasks-2.1.3.jar")
            .withClassPaths("metrics-core-2.1.2.jar")
            .withClassPaths("mysql-connector-java.jar")
            .withClassPaths("netty-3.6.6.Final.jar")
            .withClassPaths("postgresql-jdbc4.jar")
            .withClassPaths("protobuf-java-2.5.0.jar")
            .withClassPaths("servlet-api-2.5-6.1.14.jar")
            .withClassPaths("slf4j-api-1.7.5.jar")
            .withClassPaths("snappy-0.2.jar")
            .withClassPaths("ST4-4.0.4.jar")
            .withClassPaths("stax-api-1.0.1.jar")
            .withClassPaths("tempus-fugit-1.1.jar")
            .withClassPaths("xz-1.0.jar")
            .addLogHandler(new PrinterLogHandler(new PrintWriter(System.out)))
            .addSecureStore(YarnSecureStore.create(HBaseTokenUtils.obtainToken(hConf, new Credentials())))
            .withApplicationArguments(
                String.format("--%s", RunnableOptions.JAR), copiedProgram.getJarLocation().getName(),
                String.format("--%s", RunnableOptions.RUNTIME_ARGS), runtimeArgs
            ).start();
        return addCleanupListener(twillController, hConfFile, cConfFile, copiedProgram);
      }
    });
  }

  /**
   * Sub-class overrides this method to launch the twill application.
   */
  protected abstract ProgramController launch(Program program, ProgramOptions options,
                                              File hConfFile, File cConfFile, ApplicationLauncher launcher);


  private File saveHConf(Configuration conf, File file) throws IOException {
    Writer writer = Files.newWriter(file, Charsets.UTF_8);
    try {
      conf.writeXml(writer);
    } finally {
      writer.close();
    }
    return file;
  }

  private File saveCConf(CConfiguration conf, File file) throws IOException {
    Writer writer = Files.newWriter(file, Charsets.UTF_8);
    try {
      conf.writeXml(writer);
    } finally {
      writer.close();
    }
    return file;
  }

  /**
   * Copies the program jar to a local temp file and return a {@link Program} instance
   * with {@link Program#getJarLocation()} points to the local temp file.
   */
  private Program copyProgramJar(final Program program) throws IOException {
    File tempJar = File.createTempFile(program.getName(), ".jar");
    Files.copy(new InputSupplier<InputStream>() {
      @Override
      public InputStream getInput() throws IOException {
        return program.getJarLocation().getInputStream();
      }
    }, tempJar);

    final Location jarLocation = new LocalLocationFactory().create(tempJar.toURI());

    return new ForwardingProgram(program) {
      @Override
      public Location getJarLocation() {
        return jarLocation;
      }
    };
  }

  /**
   * Adds a listener to the given TwillController to delete local temp files when the program has started/terminated.
   * The local temp files could be removed once the program is started, since Twill would keep the files in
   * HDFS and no long needs the local temp files once program is started.
   *
   * @return The same TwillController instance.
   */
  private TwillController addCleanupListener(TwillController controller, final File hConfFile,
                                             final File cConfFile, final Program program) {

    final AtomicBoolean deleted = new AtomicBoolean(false);
    controller.addListener(new ServiceListenerAdapter() {
      @Override
      public void running() {
        cleanup();
      }

      @Override
      public void terminated(Service.State from) {
        cleanup();
      }

      @Override
      public void failed(Service.State from, Throwable failure) {
        cleanup();
      }

      private void cleanup() {
        if (deleted.compareAndSet(false, true)) {
          LOG.debug("Cleanup tmp files for {}: {} {} {}",
              program.getName(), hConfFile, cConfFile, program.getJarLocation().toURI());
          hConfFile.delete();
          cConfFile.delete();
          try {
            program.getJarLocation().delete();
          } catch (IOException e) {
            LOG.warn("Failed to delete program jar {}", program.getJarLocation().toURI(), e);
          }
        }
      }
    }, Threads.SAME_THREAD_EXECUTOR);
    return controller;
  }
}
