package utilities;
/**
 * SuiteOrder is class managing order of execution of tests.
 * @author elmira
 *
 */

import commonSanityTests.FooterSanityTests;
import commonSanityTests.HeaderSanityTests;
import commonSanityTests.LeftPanelSanityTests;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import pageTests.CollectPageTests;
import pageTests.HomePageTests;
import pageTests.MetricsPageTests;
import pageTests.ProcessPageTests;
import pageTests.QueryPageTests;
import pageTests.StorePageTests;
import resetTests.CloseDriverTest;
import resetTests.ResetTest;
import testsForExamples.CountRandom.CountRandom_01_UploadTests;
import testsForExamples.CountRandom.CountRandom_02_AppPageTests;
import testsForExamples.CountRandom.CountRandom_03_ActionsTests;
import testsForExamples.CountRandom.CountRandom_04_ProcessTests;
import testsForExamples.CountRandom.CountRandom_05_DatasetsTests;
import testsForExamples.CountRandom.CountRandom_06_FlowletsSourceTests;
import testsForExamples.CountRandom.CountRandom_07_FlowletsSplitterTests;
import testsForExamples.CountRandom.CountRandom_08_FlowletsCounterTests;
import testsForExamples.CountRandom.CountRandom_09_ProcessPageTests;
import testsForExamples.CountRandom.CountRandom_10_StorePageTests;
import testsForExamples.HelloWorld.HelloWorld_01_UploadTests;
import testsForExamples.HelloWorld.HelloWorld_02_AppPageTests;
import testsForExamples.HelloWorld.HelloWorld_03_ProcessTests;
import testsForExamples.HelloWorld.HelloWorld_04_QueryTests;
import testsForExamples.Purchase.Purchase_01_UploadTests;
import testsForExamples.Purchase.Purchase_02_AppPageTests;
import testsForExamples.Purchase.Purchase_03_MapReduceTests;
import testsForExamples.Purchase.Purchase_04_WorkFlowTests;
import testsForExamples.Purchase.Purchase_05_MetricsTests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.Map;

/**
 * JUnit Suite Test.
 * @author elmira
 *
 */
 
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ResetTest.class,
        LeftPanelSanityTests.class,
        HeaderSanityTests.class,
        FooterSanityTests.class,
        HomePageTests.class,
        MetricsPageTests.class,
        ProcessPageTests.class,
        CollectPageTests.class,
        QueryPageTests.class,
        StorePageTests.class,
        CountRandom_01_UploadTests.class,
        CountRandom_02_AppPageTests.class,
        CountRandom_03_ActionsTests.class,
        CountRandom_04_ProcessTests.class,
        CountRandom_05_DatasetsTests.class,
        CountRandom_06_FlowletsSourceTests.class,
        CountRandom_07_FlowletsSplitterTests.class,
        CountRandom_08_FlowletsCounterTests.class,
        CountRandom_09_ProcessPageTests.class,
        CountRandom_10_StorePageTests.class,
        Purchase_01_UploadTests.class,
        Purchase_02_AppPageTests.class,
        Purchase_03_MapReduceTests.class,
        Purchase_04_WorkFlowTests.class,
        Purchase_05_MetricsTests.class,
        HelloWorld_01_UploadTests.class,
        HelloWorld_02_AppPageTests.class,
        HelloWorld_03_ProcessTests.class,
        HelloWorld_04_QueryTests.class,
        CloseDriverTest.class
        
})
public class SuiteOrder {

  static Process process;

  @BeforeClass
  public static void setUpClass() throws Exception {

    String currentDir = System.getProperty("user.dir");
    String reactorDir = new File(currentDir).getParentFile().getParentFile().getParentFile().getPath();

    String version = FileUtils.readFileToString(new File(reactorDir + "/version.txt")).replace("\n", "");

    ProcessBuilder builder = new ProcessBuilder();
    Map<String, String> env = builder.environment();
    env.put("PATH", env.get("PATH") + "/bin:"
      + "/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin");
    builder.directory(new File(reactorDir));
    builder.command("/bin/sh","-c","./gradlew clean build -x test");

    builder.redirectErrorStream(true);

    process = builder.start();
    final BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    Thread loggingThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          String line;
          StringBuffer buffer = new StringBuffer(2048);
          while ((line = bufferedreader.readLine()) != null) {
            buffer.append(line);
            System.out.println(line);
          }
        } catch (final IOException ioe) {
          ioe.printStackTrace();
        }
      }
    });
    loggingThread.setDaemon(true);
    loggingThread.start();
    process.waitFor();


    builder = new ProcessBuilder();
    builder.directory(new File(reactorDir + "/distributions/build/distributions/"));
    builder.command("/bin/sh","-c","unzip continuuity-sdk-" + version + "-SNAPSHOT.zip");

    builder.redirectErrorStream(true);

    process = builder.start();
    final BufferedReader unzipReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    loggingThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          String line;
          StringBuffer buffer = new StringBuffer(2048);
          while ((line = unzipReader.readLine()) != null) {
            buffer.append(line);
            System.out.println(line);
          }
        } catch (final IOException ioe) {
          ioe.printStackTrace();
        }
      }
    });
    loggingThread.setDaemon(true);
    loggingThread.start();
    process.waitFor();

    builder = new ProcessBuilder();
    builder.directory(
      new File(reactorDir + "/distributions/build/distributions/continuuity-sdk-" + version + "-SNAPSHOT"));
    builder.command("/bin/sh","-c","./bin/reactor.sh start");

    builder.redirectErrorStream(true);

    process = builder.start();
    final BufferedReader startReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    loggingThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          String line;
          StringBuffer buffer = new StringBuffer(2048);
          while ((line = startReader.readLine()) != null) {
            buffer.append(line);
            System.out.println(line);
          }
        } catch (final IOException ioe) {
          ioe.printStackTrace();
        }
      }
    });
    loggingThread.setDaemon(true);
    loggingThread.start();
    process.waitFor();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  private static int getPort() {
    int port = -1;
    try {
      ServerSocket s = new ServerSocket(0);
      port = s.getLocalPort();
      s.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Could not find port");
    }
    return port;
  }
  

}
