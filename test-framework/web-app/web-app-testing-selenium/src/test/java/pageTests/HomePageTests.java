package pageTests;

import drivers.Global;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import pages.HomePage;

import static drivers.Global.globalDriver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**  HomePageTest provides sanity checks for the homepage.
 * Checking existence of elements and texts
 * Also checking for broken links
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HomePageTests extends GenericTest {
  static HomePage page;
  @BeforeClass
  public static void setUp(){
    page = new HomePage();
    Global.getDriver();
    globalDriver.get(HomePage.BASE_URL);
    Global.driverWait(5);
    //page.waitForZeroPresent();

  }
  
  @Test
  public void test_02_H1() {
    page.waitForZeroPresent();
    assertTrue("Can't find H1 title", page.isH1Present());
  }
  @Test
  public void test_03_H1Text() {
    assumeTrue(page.isH1Present());
    String strToMatch = Global.properties.getProperty("overviewH1");
    assertEquals(strToMatch, page.getH1().getText());
  }
  @Test 
  public void test_04_LoadAnApp() {
    assertTrue("Can't find 'Load an app' btn", page.isLoadAppPresent());
  }
  
  @Test
  public void test_05_Quarters() {
    page.quarters();
    assertEquals(4, page.countQuarters());
  }
  @Test
  public void test_06_CollectTitle() {
    assertEquals("Collect", page.getQuarter1().findElement(page.getQuarterTitleBy()).getText());
    
   }
  @Test
  public void test_07_ProcessTitle() {
    assertEquals("Process", page.getQuarter2().findElement(page.getQuarterTitleBy()).getText());
    
   }
  @Test
  public void test_08_StoreTitle() {
    assertEquals("Store", page.getQuarter3().findElement(page.getQuarterTitleBy()).getText());
    
   }
  @Test
  public void test_09_QueryTitle() {
    assertEquals("Query", page.getQuarter4().findElement(page.getQuarterTitleBy()).getText());
    
   }
  @Test
  public void test_10_CollectValue() {
    String text = page.quarterValue(page.getQuarter1()).getText();
    assertEquals("0", text);
  }
  @Test
  public void test_11_ProcessValue() {
    String text = page.quarterValue(page.getQuarter2()).getText();
    assertEquals("0", text);
  }
  @Test
  public void test_12_StoreValue() {
    String text = page.quarterValue(page.getQuarter3()).getText();
    assertEquals("0", text);
  }
  @Test
  public void test_13_QueryValue() {
    String text = page.quarterValue(page.getQuarter4()).getText();
    assertEquals("0", text);
  }
  @Test
  public void test_14_Eps() {
    String text = page.quarterValueAttr(page.getQuarter1()).getText();
    assertEquals("EPS", text);
  }
  @Test
  public void test_15_Percent() {
    String text = page.quarterValueAttr(page.getQuarter2()).getText();
    assertEquals("%", text);
  }
  @Test
  public void test_16_Bs() {
    String text = page.quarterValueAttr(page.getQuarter3()).getText();
    assertEquals("B/s", text);
  }
  @Test
  public void test_17_Qps() {
    String text = page.quarterValueAttr(page.getQuarter4()).getText();
    assertEquals("QPS", text);
  }
  @Test
  public void test_18_isQuarter1HasSVG() {
    assertTrue("Can't find SVG on Collect panel", page.isQuarterHasSVG(page.getQuarter1()));
    
  }
  @Test
  public void test_19_isQuarter2HasSVG() {
    assertTrue("Can't find SVG on Process panel", page.isQuarterHasSVG(page.getQuarter2()));
    
  }
  @Test
  public void test_20_isQuarter3HasSVG() {
    assertTrue("Can't find SVG on Store panel", page.isQuarterHasSVG(page.getQuarter3()));
    
  }
  @Test
  public void test_21_isQuarter4HasSVG() {
    assertTrue("Can't find SVG on Query panel", page.isQuarterHasSVG(page.getQuarter4()));
    
  }
   //working only after reset function!!
  @Test
  public void test_22_NoApp() {
    assertTrue("Can't find 'No application' div", page.isNoContentDivPresent());
  }
  @Test
  public void test_23_NoAppText() {
    assumeTrue(page.isNoContentDivPresent());
    String str1 = page.getNoContentDiv().getText();
    String str2 = "No Applications.";
    assertTrue(str1.contains(str2));
  }
  
  @Test
  public void test_24_TitleApp() {
    assertTrue("Can't find apps panel title ", page.isTitleAppPresent());
  }
  @Test
  public void test_25_TitleAppText() {
    assumeTrue(page.isTitleAppPresent());
    assertEquals("Applications", page.getTitleApp().getText());
   
  }
  
  @Test
  public void test_26_DropBoxPresent() {
    assertTrue("Can't find dropdown menu", page.isDropDownPresent());
  }
  
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
  

}
