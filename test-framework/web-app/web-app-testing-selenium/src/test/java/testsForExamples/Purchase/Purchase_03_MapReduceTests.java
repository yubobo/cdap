package testsForExamples.Purchase;

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

/** 
 * CountRandomActionsTests for actions with CountRandom.
 * jar file should be uploaded, all quantity and fields checked
 * @author elmira
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Purchase_03_MapReduceTests extends GenericTest {
  static AppPage app;
  static FlowPage page;
  static Purchase example;
  
  @BeforeClass
  public static void setUp(){
    app = new AppPage();
    page = new FlowPage();
    example = new Purchase();
    Global.getDriver();
    globalDriver.get(example.getAppUrl());
    app.waitForProcessNumbersPresent();
    app.waitForStorage();
  }
 
  @Test
  public void test_01_GoToMapReducePage() {
    WebElement table = app.getTableFromWebElem(app.getProcessPanel(), 1);
    WebElement td1 = app.getTd(table, 0, 0);
    WebElement name = app.getNameApp(td1);
    name.click();
    String url = globalDriver.getCurrentUrl();
    String strToMatch = Global.ROOT_URL + Global.properties.getProperty("mapReduceUrl");
    assertEquals(strToMatch, url);
  }
  @Test
  public void test_02_CheckBreadCrumbs() {
    assertTrue("Can't find bread crumbs on the page", page.isBreadCrumbExist());
    assertEquals(example.getNameOfJar(), page.getBreadCrumb().getText());
    //System.out.println(page.getBreadCrumb().getText());
  }
  @Test
  public void test_03_CheckH1() {
    String strToMatch = Global.properties.getProperty("mapReduceH1");
    assertEquals(strToMatch, page.getH1().getText());
  }
  @Test
  public void test_04_checkMappingContainerPresent() {
    assertTrue("Can't find Mapping container", page.isMappingPresent());
  }
  @Test
  public void test_05_checkMappingTitle() {
    WebElement title = page.getContainerTitle(page.getMapping());
    String strToMatch = Global.properties.getProperty("mapping");
    assertEquals(strToMatch, title.getText());
  }
  @Test
  public void test_06_checkMappingValue() {
    WebElement value = page.getContainerValue(page.getMapping());
    assertEquals("0%", value.getText());
    assertTrue("Can't find svg in mapping container", page.isSVGPresent(page.getMapping()));
  }

  @Test
  public void test_07_checkReducingContainerPresent() {
    assertTrue("Can't find Reducing container", page.isReducingPresent());
  }
  @Test
  public void test_08_checkReducingTitle() {
    WebElement title = page.getContainerTitle(page.getReducing());
    String strToMatch = Global.properties.getProperty("reducing");
    assertEquals(strToMatch, title.getText());
  }
  @Test
  public void test_09_checkReducingValue() {
    WebElement value = page.getContainerValue(page.getReducing());
    assertEquals("0%", value.getText());
    assertTrue("Can't find svg in reducing container", page.isSVGPresent(page.getReducing()));
  }
  @Test
  public void test_10_CheckZeroesinMap() {
    WebElement elem = page.getBatchMap();
    Scanner scanner = new Scanner(elem.getText());
    String map = scanner.nextLine();
    String strToMatch = Global.properties.getProperty("map");
    assertEquals(strToMatch, map);
    String in = scanner.nextLine();
    strToMatch = Global.properties.getProperty("zeroIn");
    assertEquals(strToMatch, in);
    String out = scanner.nextLine();
    strToMatch = Global.properties.getProperty("zeroOut");
    assertEquals(strToMatch, out);
  }
  @Test
  public void test_11_CheckZeroesinReduce() {
    WebElement elem = page.getBatchReduce();
    Scanner scanner = new Scanner(elem.getText());
    String reduce = scanner.nextLine();
    String strToMatch = Global.properties.getProperty("reduce");
    assertEquals(strToMatch, reduce);
    String in = scanner.nextLine();
    strToMatch = Global.properties.getProperty("zeroIn");
    assertEquals(strToMatch, in);
    String out = scanner.nextLine();
    strToMatch = Global.properties.getProperty("zeroOut");
    assertEquals(strToMatch, out);
  }
  @Test
  public void test_12_CheckStopped() {
    String status = page.getStatus().getText();
    String strToMatch = Global.properties.getProperty("statusStopped");
    assertEquals(strToMatch, status);
  }
  @Test
  public void test_13_CheckStartClick() {
    assertTrue("Can't find Start/Stop button", page.isStartStopPresent());
    page.getStartStop().click();
  }
  @Test
  public void test_14_CheckStatusStarting() {
    String status = page.getStatus().getText();
    String strToMatch = Global.properties.getProperty("statusStarting");
    assertEquals(strToMatch, status);
    
  }
  @Test
  public void test_15_CheckStatusRunning() {
    String strToMatch = Global.properties.getProperty("statusRunning");
    page.waitForRunningSign(strToMatch);
    String status = page.getStatus().getText();
    assertEquals(strToMatch, status);
  }
  @Test
  public void test_16_CheckStopClick() {
    page.getStartStop().click();
    String strToMatch = Global.properties.getProperty("statusStopped");
    page.waitForRunningSign(strToMatch);
    String status = page.getStatus().getText();
    assertEquals(strToMatch, status);
  }
 
  @Test
  public void test_17_NavPills() {
   WebElement elem = page.getNavPills();
   assertEquals(3, elem.findElements(By.tagName("a")).size());
  }
  
  @Test
  public void test_18_LogClick() {
    page.getLogLink().click();
    assertTrue("Can't find log div", page.isLogViewPresent());
  }
  @Test
  public void test_19_HistoryClick() {
    page.getHistoryLink(2).click();
    assertTrue("History should have at least one row", page.countRowHistoryTable() > 0);
  }
  
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
}
