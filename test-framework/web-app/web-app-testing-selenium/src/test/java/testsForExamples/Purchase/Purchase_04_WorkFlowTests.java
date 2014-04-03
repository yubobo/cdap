package testsForExamples.Purchase;

import static drivers.Global.globalDriver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.Scanner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import pageTests.GenericTest;
import pages.AppPage;
import pages.BasePage;
import pages.FlowPage;
import drivers.Global;

/** 
 * CountRandomActionsTests for actions with CountRandom.
 * jar file should be uploaded, all quantity and fields checked
 * @author elmira
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Purchase_04_WorkFlowTests extends GenericTest {
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
    WebElement table = app.getTableFromWebElem(app.getProcessPanel(), 2);
    WebElement td1 = app.getTd(table, 0, 0);
    WebElement name = app.getNameApp(td1);
    name.click();
    String url = globalDriver.getCurrentUrl();
    String strToMatch = Global.ROOT_URL + Global.properties.getProperty("workFlowUrl");
    Global.driverWait(5);
    assertEquals(strToMatch, url);
  }
  
  @Test
  public void test_02_CheckBreadCrumbs() {
    assertTrue("Can't find bread crumbs on the page", page.isBreadCrumbExist());
    assertEquals(example.getNameOfJar(), page.getBreadCrumb().getText());
  }
  
  @Test
  public void test_03_CheckH1() {
    String strToMatch = Global.properties.getProperty("workFlowH1");
    assertEquals(strToMatch, page.getH1().getText());
  }
  @Test
  public void test_04_isTimeContainerPresent() {
    assertTrue("Can't find container for run", page.isFlowRunPresent());
  }
  @Test
  public void test_05_TimeContainerTitle() {
    assumeTrue(page.isFlowRunPresent());
    String strToMatch = Global.properties.getProperty("schedule");
    WebElement title = page.getContainerTitle(page.getFlowRun());
    assertEquals(strToMatch, title.getText());
  }
  @Test
  public void test_06_getClock() {
    assumeTrue(page.isFlowRunPresent());
    WebElement clock = page.getTickTok();
    assertEquals(8, clock.getText().length());
  }
  @Test
  public void test_07_PicturePresent() {
    assertTrue("Can't find builder picture ", page.isBuilderPresent());
  }
  @Test
  public void test_08_PictureTitle() {
    assumeTrue(page.isBuilderPresent());
    String strToMatch = Global.properties.getProperty("builder");
    assertEquals(strToMatch, page.getBuilder().getText());
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
   assertEquals(2, elem.findElements(By.tagName("a")).size());
  }
  
  @Test
  public void test_19_HistoryClick() {
    page.getHistoryLink(1).click();
    assertTrue("History should have at least one row", page.countRowHistoryTable() > 0);
  }
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
}
