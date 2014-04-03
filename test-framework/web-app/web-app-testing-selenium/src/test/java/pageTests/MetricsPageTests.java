package pageTests;

import static drivers.Global.globalDriver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import pages.MetricsPage;
import drivers.Global;

/**  QueryPageTests provides sanity checks for the query page.
 * Checking existence of elements and texts
 * 
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MetricsPageTests extends GenericTest {
  static MetricsPage page;
  @BeforeClass
  public static void setUp(){
    page = new MetricsPage();
    Global.getDriver();
    globalDriver.get(page.getBaseUrl());

  }
  
  @Test
  public void test_02_H1() {
    Global.driverWait(5);
    assertTrue("Can't find H1 title", page.isH1Present());
  }
  @Test
  public void test_03_H1Text() {
    assumeTrue(page.isH1Present());
    String strToMatch = Global.properties.getProperty("metricsH1");
    assertEquals(strToMatch, page.getH1().getText());
  }
  
  @Test 
  public void test_04_NoMetrics() {
    assertTrue("Can't find 'No metrics' div", page.isNoMetricsPresent());
  }
  @Test
  public void test_05_TitleNoMetrics() {
    assertEquals("No metrics selected.", page.getNoMetricsTitle().getText());
  }
  @Test
  public void test_06_AddDivPresent() {
    assertTrue("Can't find '+ Add' div", page.isAddDivPresent());
  }
  @Test
  public void test_07_AddBtnPresent() {
    assertTrue("Can't find '+ Add' btn", page.isAddBtnPresent());
  }
  @Test
  public void test_08_AddBtnClick() {
    assumeTrue(page.isAddBtnPresent());
    page.getAddBtn().click();
    page.confDiv("block");
    assertTrue(page.getConfDiv().getCssValue("display").equals("block"));
    
  }
  @Test
  public void test_09_ConfCancelPresent() {
    assertTrue("Can't find Cancel btn on Add div", page.isConfCancelPresent());
  }
  @Test
  public void test_10_ConfAddPresent() {
    assertTrue("Can't find Add btn on Add div", page.isConfAddPresent());
  }
  @Test
  public void test_11_ConfCancelClick() {
    assumeTrue(page.isConfCancelPresent());
    page.getConfCancel().click();
    page.confDiv("none");
    assertTrue(page.getConfDiv().getCssValue("display").equals("none"));
  }
  @Test
  public void test_12_PauseBtnPresent() {
    assertTrue("Can't find Pause btn", page.isPausePresent());
  }
  
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
  

}
