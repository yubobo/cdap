package co.cask.cdap.internal.app.runtime.spark;

import co.cask.cdap.api.spark.Spark;
import co.cask.cdap.api.spark.SparkSpecification;
import co.cask.cdap.app.program.ManifestFields;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.lang.CombineClassLoader;
import co.cask.cdap.common.logging.LoggingContextAccessor;
import co.cask.cdap.data2.util.hbase.HBaseTableUtilFactory;
import co.cask.cdap.internal.app.runtime.batch.MapReduceRuntimeService;
import co.cask.cdap.internal.app.runtime.spark.dataset.SparkDatasetInputFormat;
import co.cask.cdap.internal.app.runtime.spark.dataset.SparkDatasetOutputFormat;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.ProgramType;
import co.cask.tephra.DefaultTransactionExecutor;
import co.cask.tephra.Transaction;
import co.cask.tephra.TransactionExecutor;
import co.cask.tephra.TransactionFailureException;
import co.cask.tephra.TransactionSystemClient;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.ProvisionException;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.MRConfig;
import org.apache.spark.deploy.SparkSubmit;
import org.apache.twill.filesystem.Location;
import org.apache.twill.filesystem.LocationFactory;
import org.apache.twill.internal.ApplicationBundler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 *
 */
final class SparkRuntimeService extends AbstractExecutionThreadService {
  private static final Logger LOG = LoggerFactory.getLogger(MapReduceRuntimeService.class);
  public static final String SPARK_HCONF_FILENAME = "spark_hconf.xml";

  private final CConfiguration cConf;
  private final Configuration hConf;
  private final Spark sparkJob;
  private final SparkSpecification sparkSpec;
  private final Location programJarLocation;
  private final BasicSparkContext context;
  private final LocationFactory locationFactory;
  private final TransactionSystemClient txClient;
  private Transaction transaction;
  private Runnable cleanupTask;
  private volatile boolean stopRequested;

  public SparkRuntimeService(CConfiguration cConf, Configuration hConf, Spark job, SparkSpecification spec,
                             BasicSparkContext context, Location programJarLocation, LocationFactory locationFactory,
                             TransactionSystemClient txClient) {
    this.cConf = cConf;
    this.hConf = hConf;
    this.sparkJob = job;
    this.sparkSpec = spec;
    this.programJarLocation = programJarLocation;
    this.context = context;
    this.locationFactory = locationFactory;
    this.txClient = txClient;
  }


  @Override
  protected String getServiceName() {
    return "MapReduceRunner-" + sparkSpec.getName();
  }

  @Override
  protected void startUp() throws Exception {

    final Configuration conf = new Configuration(hConf);

    // Create a classloader that have the context/system classloader as parent and the program classloader as child
    final ClassLoader classLoader = new CombineClassLoader(
      Objects.firstNonNull(Thread.currentThread().getContextClassLoader(), ClassLoader.getSystemClassLoader()),
      ImmutableList.of(context.getProgram().getClassLoader())
    );

    conf.setClassLoader(classLoader);

    // additional spark job initialization at run-time
    beforeSubmit(sparkJob);

    final Location jobJarCopy = copyProgramJar(programJarLocation, context);
    LOG.info("Copied Program Jar to {}, source: {}", jobJarCopy.toURI().getPath(),
             programJarLocation.toURI().toString());


    final Transaction tx = txClient.startLong();
    // We remember tx, so that we can re-use it in Spark tasks
    SparkContextConfig.set(conf, context, cConf, tx, jobJarCopy);

    // packaging Spark job dependency jar which includes required classes with dependencies
    final Location dependencyJar = buildDependencyJar(context, SparkContextConfig.getHConf());
    LOG.info("Built Dependency Jar at {}", dependencyJar.toURI().getPath());

    final String[] sparkSubmitArgs = prepareSparkSubmitArgs(sparkSpec, conf, jobJarCopy, dependencyJar);
    LOG.info("Submitting Spark program: {}", context.toString());

    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(conf.getClassLoader());
    boolean success = false;
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


    this.cleanupTask = new Runnable() {
      @Override
      public void run() {
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
    };
  }


  @Override
  protected void run() {

  }

  /**
   * Called before submitting spark job
   *
   * @param job     the {@link Spark} job
   * @throws co.cask.tephra.TransactionFailureException
   * @throws InterruptedException
   */

  private void beforeSubmit(final Spark job) throws TransactionFailureException,
    InterruptedException {
    createTransactionExecutor().execute(new TransactionExecutor.Subroutine() {
      @Override
      public void apply() throws Exception {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(job.getClass().getClassLoader());
        try {
          job.beforeSubmit(context);
        } finally {
          Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
      }
    });
  }
  /**
   * Copies the user submitted program jar
   *
   * @param jobJarLocation {link Location} of the user's job
   * @param context        {@link BasicSparkContext} context of this job
   * @return {@link Location} where the program jar was copied
   * @throws IOException if failed to get the {@link Location#getInputStream()} or {@link Location#getOutputStream()}
   */
  private Location copyProgramJar(Location jobJarLocation, BasicSparkContext context) throws IOException {

    Id.Program programId = context.getProgram().getId();
    Location programJarCopy = locationFactory.create(String.format("%s.%s.%s.%s.%s.program.jar",
                                                                   ProgramType.SPARK.name().toLowerCase(),
                                                                   programId.getAccountId(),
                                                                   programId.getApplicationId(), programId.getId(),
                                                                   context.getRunId().getId()));
    InputStream src = jobJarLocation.getInputStream();
    try {
      OutputStream dest = programJarCopy.getOutputStream();
      try {
        ByteStreams.copy(src, dest);
      } finally {
        dest.close();
      }
    } finally {
      src.close();
    }
    return programJarCopy;
  }


  @Override
  protected void triggerShutdown() {
    try {
      stopRequested = true;
      if (SparkProgramWrapper.isSparkProgramRunning()) {
        SparkProgramWrapper.stopSparkProgram();
      }
    } catch (Exception e) {
      LOG.error("Failed to stop Spark job {}", sparkSpec.getName(), e);
      throw Throwables.propagate(e);
    }
  }

  @Override
  protected Executor executor() {
    // Always execute in new daemon thread.
    return new Executor() {
      @Override
      public void execute(final Runnable runnable) {
        final Thread t = new Thread(new Runnable() {

          @Override
          public void run() {
            // note: this sets logging context on the thread level
            LoggingContextAccessor.setLoggingContext(context.getLoggingContext());
            runnable.run();
          }
        });
        t.setDaemon(true);
        t.setName(getServiceName());
        t.start();
      }
    };
  }

  /**
   * Creates a {@link TransactionExecutor} with all the {@link co.cask.tephra.TransactionAware} in the context.
   */
  private TransactionExecutor createTransactionExecutor() {
    return new DefaultTransactionExecutor(txClient, context.getDatasetInstantiator().getTransactionAware());
  }


  /**
   * Packages all the dependencies of the Spark job
   *
   * @param context {@link BasicSparkContext} created for this job
   * @param conf    {@link Configuration} prepared for this job by {@link SparkContextConfig}
   * @return {@link Location} of the dependency jar
   * @throws IOException if failed to package the jar through
   *                     {@link org.apache.twill.internal.ApplicationBundler#createBundle(Location, Iterable, Iterable)}
   */
  private Location buildDependencyJar(BasicSparkContext context, Configuration conf)
    throws IOException {
    ApplicationBundler appBundler = new ApplicationBundler(Lists.newArrayList("org.apache.hadoop"),
                                                           Lists.newArrayList("org.apache.hadoop.hbase",
                                                                              "org.apache.hadoop.hive"));
    Id.Program programId = context.getProgram().getId();

    Location appFabricDependenciesJarLocation =
      locationFactory.create(String.format("%s.%s.%s.%s.%s_temp.jar",
                                           ProgramType.SPARK.name().toLowerCase(), programId.getAccountId(),
                                           programId.getApplicationId(), programId.getId(),
                                           context.getRunId().getId()));

    LOG.debug("Creating Spark Job Dependency jar: {}", appFabricDependenciesJarLocation.toURI());

    URI hConfLocation = writeHConfLocally(context, conf);

    Set<Class<?>> classes = Sets.newHashSet();
    Set<URI> resources = Sets.newHashSet();

    classes.add(Spark.class);
    classes.add(SparkDatasetInputFormat.class);
    classes.add(SparkDatasetOutputFormat.class);
    classes.add(SparkProgramWrapper.class);
    classes.add(JavaSparkContext.class);
    classes.add(ScalaSparkContext.class);

    // We have to add this Hadoop Configuration to the dependency jar so that when the Spark job runs outside
    // CDAP it can create the BasicMapReduceContext to have access to our datasets, transactions etc.
    resources.add(hConfLocation);

    try {
      Class<?> hbaseTableUtilClass = new HBaseTableUtilFactory().get().getClass();
      classes.add(hbaseTableUtilClass);
    } catch (ProvisionException e) {
      LOG.warn("Not including HBaseTableUtil classes in submitted Job Jar since they are not available");
    }

    try {
      ClassLoader oldCLassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(conf.getClassLoader());
      appBundler.createBundle(appFabricDependenciesJarLocation, classes, resources);
      Thread.currentThread().setContextClassLoader(oldCLassLoader);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    } finally {
      deleteLocalHConf(hConfLocation);
    }


    // ApplicationBundler currently packages classes, jars and resources under classes, lib,
    // resources directory. Spark expects everything to exists on top level and doesn't look for things recursively
    // under folders. So we need move everything one level up in the dependency jar.
    return updateDependencyJar(appFabricDependenciesJarLocation, context);
  }


  /**
   * Stores the Hadoop {@link Configuration} locally which is then packaged with the dependency jar so that this
   * {@link Configuration} is available to Spark jobs.
   *
   * @param context {@link BasicSparkContext} created for this job
   * @param conf    {@link Configuration} of this job which has to be written to a file
   * @return {@link URI} the URI of the file to which {@link Configuration} is written
   * @throws {@link RuntimeException} if failed to get an output stream through {@link Location#getOutputStream()}
   */
  private URI writeHConfLocally(BasicSparkContext context, Configuration conf) {
    Id.Program programId = context.getProgram().getId();
    // There can be more than one Spark job running simultaneously so store their Hadoop Configuration file under
    // different directories uniquely identified by their run id. We cannot add the run id to filename itself to
    // uniquely identify them as there is no way to access the run id in the Spark job without first loading the
    // Hadoop configuration in which the run id is stored.
    Location hConfLocation =
      locationFactory.create(String.format("%s%s/%s.%s/%s", ProgramType.SPARK.name().toLowerCase(),
                                           Location.TEMP_FILE_SUFFIX, programId.getId(), context.getRunId().getId(),
                                           SPARK_HCONF_FILENAME));

    OutputStream hConfOS = null;
    try {
      hConfOS = new BufferedOutputStream(hConfLocation.getOutputStream());
      conf.writeXml(hConfOS);
      hConfOS.flush();
    } catch (IOException ioe) {
      LOG.error("Failed to write Hadoop Configuration file locally at {}", hConfLocation.toURI(), ioe);
      throw Throwables.propagate(ioe);
    } finally {
      Closeables.closeQuietly(hConfOS);
    }

    LOG.info("Hadoop Configuration stored at {} ", hConfLocation.toURI());
    return hConfLocation.toURI();
  }


  /**
   * Updates the dependency jar packaged by the {@link ApplicationBundler#createBundle(Location, Iterable,
   * Iterable)} by moving the things inside classes, lib, resources a level up as expected by spark.
   *
   * @param dependencyJar {@link Location} of the job jar to be updated
   * @param context       {@link BasicSparkContext} of this job
   */
  private Location updateDependencyJar(Location dependencyJar, BasicSparkContext context) throws IOException {

    final String[] prefixToStrip = {ApplicationBundler.SUBDIR_CLASSES, ApplicationBundler.SUBDIR_LIB,
      ApplicationBundler.SUBDIR_RESOURCES};

    Id.Program programId = context.getProgram().getId();

    Location updatedJar = locationFactory.create(String.format("%s.%s.%s.%s.%s.jar",
                                                               ProgramType.SPARK.name().toLowerCase(),
                                                               programId.getAccountId(),
                                                               programId.getApplicationId(), programId.getId(),
                                                               context.getRunId().getId()));

    // Creates Manifest
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(ManifestFields.MANIFEST_VERSION, "1.0");
    JarOutputStream jarOutput = new JarOutputStream(updatedJar.getOutputStream(), manifest);

    try {
      JarInputStream jarInput = new JarInputStream(dependencyJar.getInputStream());

      try {
        JarEntry jarEntry = jarInput.getNextJarEntry();

        while (jarEntry != null) {
          boolean isDir = jarEntry.isDirectory();
          String entryName = jarEntry.getName();
          String newEntryName = entryName;

          for (String prefix : prefixToStrip) {
            if (entryName.startsWith(prefix) && !entryName.equals(prefix)) {
              newEntryName = entryName.substring(prefix.length());
            }
          }

          jarEntry = new JarEntry(newEntryName);
          jarOutput.putNextEntry(jarEntry);
          if (!isDir) {
            ByteStreams.copy(jarInput, jarOutput);
          }
          jarEntry = jarInput.getNextJarEntry();
        }
      } finally {
        jarInput.close();
        dependencyJar.delete();
      }
    } finally {
      jarOutput.close();
    }
    return updatedJar;
  }

  /**
   * Deletes the local copy of Hadoop Configuration file created earlier.
   *
   * @param hConfLocation the {@link URI} to the Hadoop Configuration file to be deleted
   */
  private void deleteLocalHConf(URI hConfLocation) {
    // get the path to the folder containing this file
    String hConfLocationFolder = hConfLocation.toString().substring(0, hConfLocation.toString().lastIndexOf("/"));
    try {
      File hConfFile = new File(new URI(hConfLocationFolder));
      FileUtils.deleteDirectory(hConfFile);
    } catch (Exception e) {
      LOG.warn("Failed to delete the local hadoop configuration");
    }
  }


  /**
   * Called after the spark job finishes
   *
   * @param succeeded boolean of job status
   * @throws TransactionFailureException
   * @throws InterruptedException
   */
  private void onFinish(final boolean succeeded) throws TransactionFailureException, InterruptedException {
    createTransactionExecutor().execute(new TransactionExecutor.Subroutine() {
      @Override
      public void apply() throws Exception {
        sparkJob.onFinish(succeeded, context);
      }
    });
  }


  @Override
  protected void shutDown() throws Exception {

    // job completed so update running status and get the success status
    boolean success = SparkProgramWrapper.isSparkProgramSuccessful();
    //todo: where to do this
    //SparkProgramWrapper.setSparkProgramRunning(false);
    //Thread.currentThread().setContextClassLoader(oldClassLoader);



    try {
      if (success) {
        LOG.info("Committing MapReduce Job transaction: {}", context);
        // committing long running tx: no need to commit datasets, as they were committed in external processes
        // also no need to rollback changes if commit fails, as these changes where performed by mapreduce tasks
        // NOTE: can't call afterCommit on datasets in this case: the changes were made by external processes.
        if (!txClient.commit(transaction)) {
          LOG.warn("MapReduce Job transaction failed to commit");
          throw new TransactionFailureException("Failed to commit transaction for MapReduce " + context.toString());
        }
      } else {
        // aborting long running tx: no need to do rollbacks, etc.
        txClient.abort(transaction);
      }
    } finally {
      // whatever happens we want to call this
      try {
        onFinish(success);
      } finally {
        context.close();
        cleanupTask.run();
      }
    }
  }

  /**
   * Prepares arguments which {@link SparkProgramWrapper} is submitted to {@link SparkSubmit} to run.
   *
   * @param sparkSpec     {@link SparkSpecification} of this job
   * @param conf          {@link Configuration} of the job whose {@link org.apache.hadoop.mapreduce.MRConfig#FRAMEWORK_NAME} specifies the mode in
   *                      which spark runs
   * @param jobJarCopy    {@link Location} copy of user program
   * @param dependencyJar {@link Location} jar containing the dependencies of this job
   * @return String[] of arguments with which {@link SparkProgramWrapper} will be submitted
   */
  private String[] prepareSparkSubmitArgs(SparkSpecification sparkSpec, Configuration conf, Location jobJarCopy,
                                          Location dependencyJar) {
    return new String[]{"--class", SparkProgramWrapper.class.getCanonicalName(), "--master",
      conf.get(MRConfig.FRAMEWORK_NAME), jobJarCopy.toURI().getPath(), "--jars", dependencyJar.toURI().getPath(),
      sparkSpec.getMainClassName()};
  }
}
