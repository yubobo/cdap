package testsForExamples.HelloWorld;

import drivers.Global;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import pageTests.GenericTest;
import pages.AppPage;
import pages.QueryPage;

import java.util.Scanner;

import static drivers.Global.globalDriver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
/**
 *  class for Queries in HelloWorld Example.
 * @author elmira
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HelloWorld_04_QueryTests extends GenericTest {
  static AppPage app;
  static QueryPage page;
  static HelloWorld example;
  static WebElement table;
  
  @BeforeClass
  public static void setUp(){
    app = new AppPage();
    page = new QueryPage();
    example = new HelloWorld();
    Global.getDriver();
    globalDriver.get(example.getAppUrl());
    app.waitForProcessNumbersPresent();
    app.waitForStorage();
  }
  @Test
  public void test_01_GoToQueryPage() {
    WebElement table = app.getTableFromWebElem(app.getQueryPanel(), 0);
    WebElement td1 = app.getTd(table, 0, 0);
    WebElement name = app.getNameApp(td1);
    name.sendKeys(Keys.ENTER);
    String url = globalDriver.getCurrentUrl();
    String strToMatch = Global.ROOT_URL + Global.properties.getProperty("greetingUrl");
    assertEquals(strToMatch, url);
  }
  @Test
  public void test_02_CheckBreadCrumbs() {
    assertTrue("Can't find bread crumbs on the page", page.isBreadCrumbExist());
    assertEquals(example.getNameOfJar(), page.getBreadCrumb().getText());
  }
  @Test
  public void test_03_CheckH1() {
    String strToMatch = Global.properties.getProperty("greetingH1");
    assertEquals(strToMatch, page.getH1().getText());
  }
  @Test
  public void test_04_CheckRequestsPresent() {
    assertTrue("Can't find request container", page.isRequestsPresent());
  }
  @Test
  public void test_05_CheckRequestsTitle() {
    assumeTrue(page.isRequestsPresent());
    String title = page.getContainerTitle(page.getRequests()).getText();
    String strToMatch = Global.properties.getProperty("requests");
    assertEquals(strToMatch, title);
  }
  @Test
  public void test_06_CheckRequestsValue() {
    assumeTrue(page.isRequestsPresent());
    WebElement elem = page.getContainerValue(page.getRequests());
    Scanner scanner = new Scanner(elem.getText());
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("eventsPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
    assertTrue("Can't find svg in request container", page.isSVGPresent(page.getRequests()));
  }
  @Test
  public void test_07_CheckFailedPresent() {
    assertTrue("Can't find failed container", page.isFailedPresent());
  }
  @Test
  public void test_08_CheckFailedTitle() {
    assumeTrue(page.isFailedPresent());
    String title = page.getContainerTitle(page.getFailed()).getText();
    String strToMatch = Global.properties.getProperty("failed");
    assertEquals(strToMatch, title);
  }
  @Test
  public void test_10_CheckFailedValue() {
    assumeTrue(page.isFailedPresent());
    WebElement elem = page.getContainerValue(page.getFailed());
    Scanner scanner = new Scanner(elem.getText());
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("eventsPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
    assertTrue("Can't find svg in failed container", page.isSVGPresent(page.getFailed()));
  }
  @Test
  public void test_11_CheckQueryBtnPresent() {
    assertTrue("Can't find run btn", page.isQueryBtnPresent());
  }
  @Test
  public void test_12_CheckQueryBtnText() {
    assumeTrue(page.isQueryBtnPresent());
    String strToMatch = Global.properties.getProperty("queryRunBtn");
    assertEquals(strToMatch, page.getQueryBtn().getText());
  }
  @Test
  public void test_13_StartStopPresent() {
    assertTrue("Can't find Start/Stop button", page.isStartStopPresent());
  }
  
  @Test
  public void test_14_StartClick() {
    assumeTrue(page.isStartStopPresent());
    page.getStartStop().click();
  }

  @Test
  public void test_15_StatusTextRun() {
    assumeTrue(page.isStatusPresent());
    String strToMatch = Global.properties.getProperty("statusStarting");
    assertEquals(strToMatch, page.getStatus().getText());
  }
  @Test
  public void test_16_ForRunning() {
    String strToMatch = Global.properties.getProperty("statusRunning");
    page.waitForRunningSign(strToMatch);
    assertEquals(strToMatch, page.getStatus().getText());
    
  }
  @Test
  public void test_17_CheckQueryExBtnText() {
    assumeTrue(page.isQueryExBtnPresent());
    String strToMatch = Global.properties.getProperty("queryRunBtnExecute");
    assertEquals(strToMatch, page.getQueryExBtn().getText());
  }
  @Test
  public void test_18_CheckTableInThePanel() {
    WebElement panel = page.getPanel();
    WebElement table = page.getTableFromWebElem(panel);
    WebElement method = page.getTd(table, 0, 0);
    String str1 = Global.properties.getProperty("method");
    WebElement input = page.getInputField(method);
    input.sendKeys(str1);
    
    WebElement param = page.getTd(table, 0, 1);
    String str2 = Global.properties.getProperty("parameters");
    WebElement input1 = page.getInjectField();
    input1.sendKeys(str2);
    page.getQueryExBtn().click();
    String strToMatch = Global.properties.getProperty("response");
    page.waitForResponce();
    assertEquals(strToMatch, page.getResponse().getAttribute("value"));
  }
  
  @Test
  public void test_19_StartStopClick() {
    assumeTrue(page.isStartStopPresent());
    page.getStartStop().click();
    String strToMatch = Global.properties.getProperty("statusStopping");
    assertEquals(strToMatch, page.getStatus().getText());
    
  }
  @Test
  public void test_20_forStopped() {
    String statusToMatch = Global.properties.getProperty("statusStopped");
    page.waitForRunningSign(statusToMatch);
    assertEquals(statusToMatch.toLowerCase(), page.getStatus().getText().toLowerCase());
  }
  @Test
  public void test_21_LogClick() {
    page.getLogLink().click();
    assertTrue("Can't find log div", page.isLogViewPresent());
  }
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
  
}
