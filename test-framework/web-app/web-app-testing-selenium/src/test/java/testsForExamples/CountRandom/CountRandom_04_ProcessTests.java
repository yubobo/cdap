package testsForExamples.CountRandom;

import drivers.Global;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import pageTests.GenericTest;
import pages.AppPage;
import pages.FlowPage;

import java.util.Scanner;

import static drivers.Global.globalDriver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/** CountRandomProcessTests is class for checking flow page then example is uploaded.
 * using CountRandom example
 * In the beginning reset everything,
 * load app, go to flows page, 
 * provide sanity tests, running, stopping, history, logs
 * 
 * @author elmira
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CountRandom_04_ProcessTests extends GenericTest{
  static AppPage app;
  static FlowPage page;
  static CountRandom example;
  
  @BeforeClass
  public static void setUp(){
    app = new AppPage();
    page = new FlowPage();
    example = new CountRandom();
    Global.getDriver();
    page.runReset();
    page.uploadApp();
    globalDriver.get(example.getAppUrl());
    page.waitForProcessNumbersPresent();
  }
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
  @Test
  public void test_01_GotoFlowsPage() {
    assumeTrue(app.isTablePresentInWebElement(page.getProcessPanel()));
    WebElement table = app.getTableFromWebElem(app.getProcessPanel());
    WebElement td1 = app.getTd(table, 0, 0);
    WebElement flowUrl = td1.findElement(By.tagName("a"));
    flowUrl.click();
    String url = Global.properties.getProperty("countRandomDSUrl");
    assertEquals(Global.ROOT_URL + url, globalDriver.getCurrentUrl());
    globalDriver.get(Global.ROOT_URL + url);
  }
  
  @Test
  public void test_02_BreadCrumb() {
    page.waitForTitlePresent();
    assertTrue("Can't find breadcrumb", page.isBreadCrumbExist());
  }
  
  @Test
  public void test_03_BreadCrumbText() {
    assumeTrue(page.isBreadCrumbExist());
    assertEquals(example.getNameOfJar(), page.getBreadCrumb().getText());
  }
  
  @Test
  public void test_04_H1() {
    assertTrue("Can't find H1", page.isH1Present());
  }
  @Test
  public void test_05_H1text() {
    page.waitForH1match(example.getNameOfJar() + " Flow");
    
    System.out.println(globalDriver.findElement(By.tagName("h1")).getText());
    assertEquals(example.getNameOfJar() + " Flow", page.getH1().getText());
  }
  
  @Test
  public void test_06_ProcessedContainerPresent() {
    assertTrue("Can't find process container", page.isProcessContainer());
  }
  @Test
  public void test_07_ProcessedTitlePresent() {
    assumeTrue(page.isProcessContainer());
    assertTrue("Can't find title of process container", page.isContainerTitle(page.getProcessedContainer()));
  }
  
  @Test
  public void test_08_ProcessedTitleText() {
    assumeTrue(page.isContainerTitle(page.getProcessedContainer()));
    String strToMatch = Global.properties.getProperty("processingRate");
    assertEquals(strToMatch, page.getContainerTitle(page.getProcessedContainer()).getText());
  }
  @Test
  public void test_09_ProcessedValuePresent() {
    page.waitForProcessNumbersPresent();
    Scanner scanner = new Scanner(page.getContainerValue(page.getProcessedContainer()).getText());
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("eventsPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
  }
  
  @Test
  public void test_10_ProcessedSVGPresent() {
    assertTrue("Can't find SVG in Processed", page.isSVGPresent(page.getProcessedContainer()));
  }
  @Test
  public void test_11_BusynessContainerPresent() {
    assertTrue("Can't find busyness container", page.isBusynessContainer());
  }
  @Test
  public void test_12_BusynessTitlePresent() {
    assumeTrue(page.isBusynessContainer());
    assertTrue("Can't find title of busyness container", page.isContainerTitle(page.getBusynessContainer()));
  }
  @Test
  public void test_13_BusynessTitleText() {
    assumeTrue(page.isContainerTitle(page.getBusynessContainer()));
    String strToMatch = Global.properties.getProperty("busyness");
    assertEquals(strToMatch, page.getContainerTitle(page.getBusynessContainer()).getText());
  }
  @Test
  public void test_14_BusynessValuePresent() {
    String text = page.getContainerValue(page.getBusynessContainer()).getText();
    assertEquals("0%", text);
  }
  @Test
  public void test_15_BusynessSVGPresent() {
    assertTrue("Can't find SVG in Busyness", page.isSVGPresent(page.getBusynessContainer()));
  }
  
  @Test
  public void test_16_StatusPresent() {
    assertTrue("Can't find status", page.isStatusPresent());
  }
  @Test
  public void test_17_StatusText() {
    assumeTrue(page.isStatusPresent());
    String statusToMatch = Global.properties.getProperty("statusStopped").toLowerCase();
    assertEquals(statusToMatch, page.getStatus().getText().toLowerCase());
  }
  // Source
  @Test
  public void test_18_FlowletSourcePresent() {
    assertTrue("Can't find source picture", page.isSourcePresent());
  }
  @Test
  public void test_19_EyeDropSourcePresent() {
    assertTrue("Can't find eyedrop icon in source picture", page.isEyeDropPresent(page.getSource()));
  }
  @Test
  public void test_20_SourceValue() {
    page.waitForSourceValueNumbersPresent();
    assumeTrue(page.isSourcePresent());
    assertEquals("0", page.getPictValue(page.getSource()).getText());
  }
  
  @Test
  public void test_21_SourceInstance() {
    assertTrue("Can't find source instance", page.isInstancePresent(page.getSource()));
  }
  @Test
  public void test_22_SourceInstanceText() {
    assumeTrue(page.isInstancePresent(page.getSource()));
    assertEquals("1", page.getInstance(page.getSource()).getText());
  }
  @Test
  public void test_23_SourceTitlePresent() {
    assertTrue("Can't find title of source picture", page.isPictTitlePresent(page.getSource()));
  }
  @Test
  public void test_24_SourceTitleText() {
    assumeTrue(page.isPictTitlePresent(page.getSource()));
    String strToMatch = Global.properties.getProperty("source");
    assertEquals(strToMatch, page.getPictTitle(page.getSource()).getText());
  }
  @Test
  public void test_25_SourceIconPresent() {
    assertTrue("Can't find circle icon of source picture", page.isPictIconPresent(page.getSource()));
  }
  
  //Splitter
  @Test
  public void test_26_SplitterPresent() {
    assertTrue("Can't find splitter picture", page.isSplitterPresent());
  }
  @Test
  public void test_27_EyeDropSplitterPresent() {
    assertTrue("Can't find eyedrop icon of splitter picture", page.isEyeDropPresent(page.getSplitter()));
  }
  @Test
  public void test_28_SplitterValue() {
    assumeTrue(page.isSplitterPresent());
    assertEquals("0", page.getPictValue(page.getSplitter()).getText());
  }
  @Test
  public void test_29_SplitterInstance() {
    assertTrue("Can't find splitter instance", page.isInstancePresent(page.getSplitter()));
  }
  @Test
  public void test_30_SplitterInstanceText() {
    assumeTrue(page.isInstancePresent(page.getSplitter()));
    assertEquals("1", page.getInstance(page.getSplitter()).getText());
  }
  @Test
  public void test_31_SplitterTitlePresent() {
    assertTrue("Can't find title of splitter picture", page.isPictTitlePresent(page.getSplitter()));
  }
  @Test
  public void test_32_SplitterTitleText() {
    assumeTrue(page.isPictTitlePresent(page.getSplitter()));
    String strToMatch = Global.properties.getProperty("splitter");
    assertEquals(strToMatch, page.getPictTitle(page.getSplitter()).getText());
  }
  @Test
  public void test_33_SplitterIconPresent() {
    assertTrue("Can't find splitter icon", page.isPictIconPresent(page.getSplitter()));
  }
  //Counter
  @Test
  public void test_34_FlowletCounterPresent() {
    assertTrue("Can't find counter picture", page.isCounterPresent());
  }
  @Test
  public void test_35_EyeDropCounterPresent() {
    assertTrue("Can't find eyedrop icon of counter picture", page.isEyeDropPresent(page.getCounter()));
  }
  @Test
  public void test_36_CounterValue() {
    assumeTrue(page.isCounterPresent());
    assertEquals("0", page.getPictValue(page.getCounter()).getText());
  }
  @Test
  public void test_37_CounterInstance() {
    assertTrue("Can't find Counter instance", page.isInstancePresent(page.getCounter()));
  }
  @Test
  public void test_38_CounterInstanceText() {
    assumeTrue(page.isInstancePresent(page.getCounter()));
    assertEquals("1", page.getInstance(page.getCounter()).getText());
  }
  @Test
  public void test_39_CounterTitlePresent() {
    assertTrue("Can't find title of counter picture", page.isPictTitlePresent(page.getCounter()));
  }
  @Test
  public void test_40_CounterTitleText() {
    assumeTrue(page.isPictTitlePresent(page.getCounter()));
    String strToMatch = Global.properties.getProperty("counter");
    assertEquals(strToMatch, page.getPictTitle(page.getCounter()).getText());
  }
  @Test
  public void test_41_CounterIconPresent() {
    assertTrue("Can't find counter's icon", page.isPictIconPresent(page.getCounter()));
  }
  
  @Test
  public void test_42_StartStopPresent() {
    assertTrue("Can't find Start/Stop button", page.isStartStopPresent());
  }
  
  @Test
  public void test_43_StartClick() {
    assumeTrue(page.isStartStopPresent());
    page.getStartStop().click();
    
  }
  
  @Test
  public void test_44_StatusTextRun() {
    assumeTrue(page.isStatusPresent());
    String strToMatch = Global.properties.getProperty("statusStarting");
    assertEquals(strToMatch, page.getStatus().getText());
  }
  @Test
  public void test_45_ForRunning() {
    String strToMatch = Global.properties.getProperty("statusRunning");
    page.waitForRunningSign(strToMatch);
    assertEquals(strToMatch, page.getStatus().getText());
  }
  @Test
  public void test_46_ProcessNumbers() {
    page.waitForRunningProcess();
    WebElement process = page.getProcessedContainer();
    WebElement value = page.getContainerValue(process);
    String text = value.getText();
    assertTrue("Numbers of process should be changing", text.length() > 6);
  }
  @Test
  public void test_47_BusynessNumbers() {
    WebElement process = page.getBusynessContainer();
    WebElement value = page.getContainerValue(process);
    String text = value.getText();
    assertTrue("Numbers of busyness should be changing", text.length() > 2);
  }
  @Test
  public void test_49_SplitterValueRunning() {
    assumeTrue(page.isSplitterPresent());
    String text = page.getPictValue(page.getSplitter()).getText();
    assertTrue("Numbers of splitter should be changing", text.length() > 2);
  }
  @Test
  public void test_50_CounterValueRunning() {
    assumeTrue(page.isCounterPresent());
    String text = page.getPictValue(page.getCounter()).getText();
    assertTrue("Numbers of counter should be changing", text.length() > 2);
  }
  @Test
  public void test_51_StartStopClick() {
    assumeTrue(page.isStartStopPresent());
    page.getStartStop().click();
    String strToMatch = Global.properties.getProperty("statusStopping");
    assertEquals(strToMatch, page.getStatus().getText());
    
  }
  @Test
  public void test_52_forStopped() {
    String statusToMatch = Global.properties.getProperty("statusStopped");
    page.waitForRunningSign(statusToMatch);
    assertEquals(statusToMatch.toLowerCase(), page.getStatus().getText().toLowerCase());
  }
  @Test
  public void test_53_ProcessValueZero() {
    page.waitForProcessNumbersIsZero();
    Scanner scanner = new Scanner(page.getContainerValue(page.getProcessedContainer()).getText());
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("eventsPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_54_BusynessValueZero() {
    String text = page.getContainerValue(page.getBusynessContainer()).getText();
    assertEquals("0%", text);
  }
  @Test
  public void test_55_NavPills() {
   WebElement elem = page.getNavPills();
   assertEquals(3, elem.findElements(By.tagName("a")).size());
  }
  @Test
  public void test_56_LogClick() {
    page.getLogLink().click();
    assertTrue("Can't find log div", page.isLogViewPresent());
  }
  @Test
  public void test_57_HistoryClick() {
    page.getHistoryLink(2).click();
    assertTrue("Flowlets are not faded", page.isFlowFadePresent());
    assertEquals(1, page.countRowHistoryTable());
  }
  
}
  
