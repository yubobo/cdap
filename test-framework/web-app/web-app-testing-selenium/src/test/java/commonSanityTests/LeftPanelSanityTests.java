
package commonSanityTests;

import static org.junit.Assert.*;

import org.junit.AfterClass;

import static org.junit.Assume.*;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import pageTests.GenericTest;
import pages.BasePage;
import pages.CollectPage;
import pages.HomePage;
import pages.ProcessPage;
import pages.QueryPage;
import pages.StorePage;
import drivers.Global;
import static drivers.Global.*;

/**  Left Panel Sanity Checks provides sanity checks for the left panel of the every page...
 * The same for all pages
 * Checking existence of elements and texts
 * Also checking for broken links
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LeftPanelSanityTests extends GenericTest {
  static BasePage page;

  @BeforeClass
  public static void setUp(){
    page = new BasePage();
    Global.getDriver();
    globalDriver.get(page.getBaseUrl());
    Global.driverWait(5);
  }
  
  @Test
  public void test_01_Logo() {
    assertTrue("Can't find logo", page.isLogoPresent());
  }
  
  @Test
  public void test_02_logoClick() {
    assumeTrue(page.isLogoPresent());
    page.getLogo().click();
    waitForLoading();
    boolean rightUrl = ((globalDriver.getCurrentUrl().equals(HomePage.BASE_URL)) || 
                        (globalDriver.getCurrentUrl().equals(Global.ROOT_URL + "#")));
    assertTrue("Wrong Url", rightUrl);
    waitForLoading(page.getBaseUrl());
  
  }
  
  @Test
  public void test_03_ProductName() {
    assertTrue("Can't find product name", page.isProductNamePresent());
  }
  
  @Test
  public void test_04_Overview() {
    assertTrue("Can't find Overview link", page.isOverviewPresent());
  }
  
  @Test
  public void test_05_OverviewText() {
    assumeTrue(page.isOverviewPresent());
    assertEquals("Overview", page.getOverview().getText());
  }

  @Test
  public void test_06_OverviewClick() {
    assumeTrue(page.isOverviewPresent());
    page.getOverview().click();
    waitForLoading();
    assertEquals(HomePage.BASE_URL, globalDriver.getCurrentUrl());
    //assertEquals(Global.ROOT_URL + "#", globalDriver.getCurrentUrl());
    waitForLoading(page.getBaseUrl());
  }

  @Test
  public void test_07_Collect() {
    assertTrue("Can't find Collect link", page.isCollectPresent());
  }

  @Test
  public void test_08_CollectText() {
    assumeTrue(page.isCollectPresent());
    assertEquals("Collect", page.getCollect().getText());
  }
  @Test
  public void test_09_CollectClick() {
    assumeTrue(page.isCollectPresent());
    page.getCollect().click();
    assertEquals(CollectPage.BASE_URL, globalDriver.getCurrentUrl());
    waitForLoading(page.getBaseUrl());
  }

  @Test
  public void test_10_Process() {
    assertTrue("Can't find Process link", page.isProcessPresent());
  }

  @Test
  public void test_11_ProcessText() {
    assumeTrue(page.isProcessPresent());
    assertEquals("Process", page.getProcess().getText());
  }
  @Test
  public void test_12_ProcessClick() {
    assumeTrue(page.isProcessPresent());
    page.getProcess().click();
    assertEquals(ProcessPage.BASE_URL, globalDriver.getCurrentUrl());
    waitForLoading(page.getBaseUrl());
  }
  @Test
  public void test_13_Store() {
    assertTrue("Can't find Store link", page.isStorePresent());
  }

  @Test
  public void test_14_StoreText() {
    assumeTrue(page.isStorePresent());
    assertEquals("Store", page.getStore().getText());
  }

  @Test
  public void test_15_StoreClick() {
    assumeTrue(page.isStorePresent());
    page.getStore().click();
    assertEquals(StorePage.BASE_URL, globalDriver.getCurrentUrl());
    waitForLoading(page.getBaseUrl());
  }

  @Test
  public void test_16_Query() {
    assertTrue("Can't find Query link", page.isQueryPresent());
  }

  @Test
  public void test_17_QueryText() {
    assumeTrue(page.isQueryPresent());
    assertEquals("Query", page.getQuery().getText());
  }
  @Test
  public void test_18_QueryClick() {
    assumeTrue(page.isQueryPresent());
    page.getQuery().click();
    assertEquals(QueryPage.BASE_URL, globalDriver.getCurrentUrl());
    waitForLoading(page.getBaseUrl());
  }
  
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }

}

