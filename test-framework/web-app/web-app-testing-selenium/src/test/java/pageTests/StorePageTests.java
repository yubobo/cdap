package pageTests;

import static drivers.Global.globalDriver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.Scanner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import pages.StorePage;
import drivers.Global;

/**  StorePageTests provides sanity checks for the store page.
 * Checking existence of elements and texts
 * 
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StorePageTests extends GenericTest {
  static StorePage page;
  @BeforeClass
  public static void setUp(){
    page = new StorePage();
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
    assertEquals("Store Data", page.getH1().getText());
  }
  
  @Test 
  public void test_04_NoFlow() {
    assertTrue("Can't find 'No Datasets' div", page.isNoContentDivPresent());
  }
  @Test
  public void test_05_TitleNoFlows() {
    Scanner scanner = new Scanner(page.getNoContentDiv().getText());
    String strToMatch = Global.properties.getProperty("noDatasets");
    assertEquals(strToMatch, scanner.nextLine());
    strToMatch = Global.properties.getProperty("addDatasets");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
  

}
