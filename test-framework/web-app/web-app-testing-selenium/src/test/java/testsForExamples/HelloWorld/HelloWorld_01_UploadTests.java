package testsForExamples.HelloWorld;

import static drivers.Global.globalDriver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.junit.runners.MethodSorters;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import pages.BasePage;
import pages.HomePage;
import drivers.Global;
import pageTests.GenericTest;


/** CountRandomTest class for test uploading CountRandom.java.
 * Right now CountRandom in resources folder
 * 
 * @author elmira
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HelloWorld_01_UploadTests extends GenericTest{
  static HomePage page;
  static WebElement table;
  static HelloWorld example;
  static int numOfRow;
  
  @BeforeClass
  public static void setUp(){
    page = new HomePage();
    example = new HelloWorld();
    Global.getDriver();
    globalDriver.get(page.getBaseUrl());
  }
  
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
  @Test
  public void runReset() {
    assumeTrue(page.runReset());
    page.waitForZeroPresent();
    
  }
  @Test
  // first step is uploading app
  public void test_01_LoadAnApp() {
    page.uploadAnApp("helloWorldPath");
    if (page.isHoverPresent()) {
      System.out.println("checking Hover");
      BasePage.checkingHover("block");
      BasePage.checkingHover("none");
    }
  }
  @Test
  public void test_03_Table() {
    Global.driverWait(5);
    table = page.getTableFromDriver();
    
  }
  
  @Test
  public void test_04_CollectHomePage() {
    assertEquals(String.valueOf(example.getCollect()), page.getTd(table, 0, 1).getText());
  }
  
  @Test
  public void test_05_ProcessHomePage() {
    assertEquals(String.valueOf(example.getProcess()), page.getTd(table, 0, 2).getText());
  }
 
  @Test
  public void test_06_StoreHomePage() {
    assertEquals(String.valueOf(example.getStore()), page.getTd(table, 0, 3).getText());
  }
  
  @Test
  public void test_07_QueryHomePage() {
    assertEquals(String.valueOf(example.getQuery()), page.getTd(table, 0, 4).getText());
  }
  
  @Test 
  public void test_08_DescriptionOfApp() {
    WebElement name = page.getStatusApp(page.getTd(table, 0, 0));
    assertEquals(example.getDescription(), name.getText());
  }
  
  @Test
  public void test_09_BusinessPresent() {
    page.waitForBuzynessZeroPresent();
    assertEquals("0%", page.getBusyness().getText());
  }
  
  @Test
  public void test_10_AppSVGPresent() {
    assertTrue(page.isAppSVGpresent());
  }
  @Test
  public void test_11_goToAppPage() {
    WebElement name = page.getNameApp(page.getTd(table, 0, 0));
    assertEquals(example.getNameOfJar(), name.getText());
    name.click();
    assertEquals(example.getAppUrl(), globalDriver.getCurrentUrl());
  }
  
}
