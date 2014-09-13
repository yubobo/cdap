/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.internal.app.runtime.spark;

import co.cask.cdap.api.spark.Spark;
import co.cask.cdap.api.spark.SparkSpecification;
import co.cask.cdap.app.ApplicationSpecification;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.app.runtime.Arguments;
import co.cask.cdap.app.runtime.ProgramController;
import co.cask.cdap.app.runtime.ProgramOptions;
import co.cask.cdap.app.runtime.ProgramRunner;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.lang.CombineClassLoader;
import co.cask.cdap.common.lang.InstantiatorFactory;
import co.cask.cdap.common.logging.LoggingContextAccessor;
import co.cask.cdap.common.metrics.MetricsCollectionService;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.internal.app.runtime.ProgramOptionConstants;
import co.cask.cdap.internal.app.runtime.ProgramServiceDiscovery;
import co.cask.cdap.proto.ProgramType;
import co.cask.tephra.Transaction;
import co.cask.tephra.TransactionExecutorFactory;
import co.cask.tephra.TransactionSystemClient;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Service;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.deploy.SparkSubmit;
import org.apache.twill.api.RunId;
import org.apache.twill.discovery.DiscoveryServiceClient;
import org.apache.twill.filesystem.Location;
import org.apache.twill.filesystem.LocationFactory;
import org.apache.twill.internal.RunIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Runs {@link Spark} programs
 */
public class SparkProgramRunner implements ProgramRunner {

  public static final String SPARK_HCONF_FILENAME = "spark_hconf.xml";
  private static final Logger LOG = LoggerFactory.getLogger(SparkProgramRunner.class);

  private final DatasetFramework datasetFramework;
  private final Configuration hConf;
  private final CConfiguration cConf;
  private SparkProgramController controller;
  private final MetricsCollectionService metricsCollectionService;
  private final ProgramServiceDiscovery serviceDiscovery;
  private final TransactionExecutorFactory txExecutorFactory;
  private final TransactionSystemClient txSystemClient;
  private final LocationFactory locationFactory;
  private final DiscoveryServiceClient discoveryServiceClient;

  @Inject
  public SparkProgramRunner(DatasetFramework datasetFramework, CConfiguration cConf,
                            MetricsCollectionService metricsCollectionService,
                            ProgramServiceDiscovery serviceDiscovery, Configuration hConf,
                            TransactionExecutorFactory txExecutorFactory,
                            TransactionSystemClient txSystemClient, LocationFactory locationFactory,
                            DiscoveryServiceClient discoveryServiceClient) {
    this.hConf = hConf;
    this.datasetFramework = datasetFramework;
    this.cConf = cConf;
    this.metricsCollectionService = metricsCollectionService;
    this.serviceDiscovery = serviceDiscovery;
    this.txExecutorFactory = txExecutorFactory;
    this.locationFactory = locationFactory;
    this.txSystemClient = txSystemClient;
    this.discoveryServiceClient = discoveryServiceClient;

  }

  @Override
  public ProgramController run(Program program, ProgramOptions options) {
    // Extract and verify parameters
    final ApplicationSpecification appSpec = program.getSpecification();
    Preconditions.checkNotNull(appSpec, "Missing application specification.");

    ProgramType processorType = program.getType();
    Preconditions.checkNotNull(processorType, "Missing processor type.");
    Preconditions.checkArgument(processorType == ProgramType.SPARK, "Only Spark process type is supported.");

    final SparkSpecification spec = appSpec.getSpark().get(program.getName());
    Preconditions.checkNotNull(spec, "Missing SparkSpecification for %s", program.getName());

    // Optionally get runId. If the spark started by other program (e.g. Workflow), it inherit the runId.
    Arguments arguments = options.getArguments();
    RunId runId = arguments.hasOption(ProgramOptionConstants.RUN_ID) ? RunIds.fromString(arguments.getOption
      (ProgramOptionConstants.RUN_ID)) : RunIds.generate();

    long logicalStartTime = arguments.hasOption(ProgramOptionConstants.LOGICAL_START_TIME)
      ? Long.parseLong(arguments.getOption(ProgramOptionConstants.LOGICAL_START_TIME)) : System.currentTimeMillis();

    String workflowBatch = arguments.getOption(ProgramOptionConstants.WORKFLOW_BATCH);

    Spark job;
    try {
      job = new InstantiatorFactory(false).get(TypeToken.of(program.<Spark>getMainClass())).create();
    } catch (Exception e) {
      LOG.error("Failed to instantiate MapReduce class for {}", spec.getClassName(), e);
      throw Throwables.propagate(e);
    }


    final BasicSparkContext context = new BasicSparkContext(program, runId, options.getUserArguments(),
                                                            program.getSpecification().getDatasets().keySet(), spec,
                                                            logicalStartTime, workflowBatch, serviceDiscovery,
                                                            metricsCollectionService, datasetFramework, cConf,
                                                            discoveryServiceClient);


    LoggingContextAccessor.setLoggingContext(context.getLoggingContext());

    Service sparkService = new SparkRuntimeService(cConf, hConf, job, spec, context,
                                                   program.getJarLocation(), locationFactory,
                                                   txSystemClient);

    controller = new SparkProgramController(sparkService, context);

    LOG.info("Starting Spark Job: {}", context.toString());
    sparkService.start();
    return controller;
//
//    submit(job, spec, program.getJarLocation(), context);
//
//
//
//    // adding listener which stops spark job when controller stops.
//    controller.addListener(new AbstractListener() {
//      @Override
//      public void stopping() {
//        // TODO: This does not work as Spark goes into deadlock while closing the context in local mode
//        // Jira: REACTOR-951
//        LOG.info("Stopping Spark Job: {}", context);
//        try {
//          if (SparkProgramWrapper.isSparkProgramRunning()) {
//            SparkProgramWrapper.stopSparkProgram();
//          }
//        } catch (Exception e) {
//          LOG.error("Failed to stop Spark job {}", spec.getName(), e);
//          throw Throwables.propagate(e);
//        }
//      }
//    }, MoreExecutors.sameThreadExecutor());
    return controller;
  }

  private void submit(final Spark job, SparkSpecification sparkSpec, Location programJarLocation,
                      final BasicSparkContext context) throws Exception {

    final Configuration conf = new Configuration(hConf);

    // Create a classloader that have the context/system classloader as parent and the program classloader as child
    final ClassLoader classLoader = new CombineClassLoader(
      Objects.firstNonNull(Thread.currentThread().getContextClassLoader(), ClassLoader.getSystemClassLoader()),
      ImmutableList.of(context.getProgram().getClassLoader())
    );

    conf.setClassLoader(classLoader);

    // additional spark job initialization at run-time
    beforeSubmit(job, context);

    final Location jobJarCopy = copyProgramJar(programJarLocation, context);
    LOG.info("Copied Program Jar to {}, source: {}", jobJarCopy.toURI().getPath(),
             programJarLocation.toURI().toString());


    final Transaction tx = txSystemClient.startLong();
    // We remember tx, so that we can re-use it in Spark tasks
    SparkContextConfig.set(conf, context, cConf, tx, jobJarCopy);

    // packaging Spark job dependency jar which includes required classes with dependencies
    final Location dependencyJar = buildDependencyJar(context, SparkContextConfig.getHConf());
    LOG.info("Built Dependency Jar at {}", dependencyJar.toURI().getPath());

    final String[] sparkSubmitArgs = prepareSparkSubmitArgs(sparkSpec, conf, jobJarCopy, dependencyJar);

    new Thread() {
      @Override
      public void run() {
        boolean success = false;
        try {
          LoggingContextAccessor.setLoggingContext(context.getLoggingContext());

          LOG.info("Submitting Spark program: {}", context.toString());

          ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
          Thread.currentThread().setContextClassLoader(conf.getClassLoader());
          try {
            SparkProgramWrapper.setSparkProgramRunning(true);
            SparkSubmit.main(sparkSubmitArgs);
          } catch (Exception e) {
            LOG.error("Failed to submit Spark program {}", context.toString(), e);
          } finally {
            // job completed so update running status and get the success status
            success = SparkProgramWrapper.isSparkProgramSuccessful();
            SparkProgramWrapper.setSparkProgramRunning(false);
            Thread.currentThread().setContextClassLoader(oldClassLoader);
          }
        } catch (Exception e) {
          LOG.warn("Exception while setting classloader for the current thread", e);
          throw Throwables.propagate(e);
        } finally {
          stopController(success, context, job, tx);
          try {
            dependencyJar.delete();
          } catch (IOException e) {
            LOG.warn("Failed to delete Spark Job Dependency Jar: {}", dependencyJar.toURI());
          }
          try {
            jobJarCopy.delete();
          } catch (IOException e) {
            LOG.warn("Failed to delete Spark Job Jar: {}", jobJarCopy.toURI());
          }
        }
      }
    }.start();
  }


}
