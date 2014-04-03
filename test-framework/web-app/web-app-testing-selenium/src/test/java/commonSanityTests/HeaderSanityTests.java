
package commonSanityTests;

import drivers.Global;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import pageTests.GenericTest;
import pages.BasePage;
import pages.MetricsPage;

import static drivers.Global.globalDriver;
import static drivers.Global.waitForLoading;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/** Header Sanity Checks provides sanity checks for footer of the every page..
 * The same for all pages
 * Checking existence of elements and texts
 * Also checking for broken links
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HeaderSanityTests extends GenericTest {

  static BasePage page;
  
  @BeforeClass
  public static void setUp(){
    page = new BasePage();
    Global.getDriver();
    globalDriver.get(page.getBaseUrl());
    Global.driverWait(15);
  }
  
  @Test
  public void test_01_Metrics() {
    assertTrue("Can't find Metrics link", page.isMetricsPresent());
  }
  @Test
  public void test_02_MetricsText() {
    assumeTrue(page.isMetricsPresent());
    assertEquals("Metrics", page.getMetrics().getText());
  }
  @Test
  public void test_03_MetricsClick() {
    assumeTrue(page.isMetricsPresent());
    page.getMetrics().click();
    assertEquals(MetricsPage.BASE_URL, globalDriver.getCurrentUrl());
    waitForLoading(page.getBaseUrl());
  }
  @Test
  public void test_04_Account() {
    assertTrue("Can't find Account link", page.isAccountPresent());
  }
  @Test
  public void test_05_AccountText() {
    assumeTrue(page.isAccountPresent());
    assertEquals("My Account", page.getAccount().getText());
  }
  @Test
  public void test_06_AccountClick() {
    assumeTrue(page.isAccountPresent());
    String newUrl = switchToNewTab(page.getAccount(), page.getBaseUrl());
    assertEquals(Global.ACCOUNT_URL, newUrl);
  }
  
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
}
