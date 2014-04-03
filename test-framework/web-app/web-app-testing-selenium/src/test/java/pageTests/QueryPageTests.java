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

import pages.QueryPage;
import drivers.Global;

/**  QueryPageTests provides sanity checks for the query page.
 * Checking existence of elements and texts
 * 
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryPageTests extends GenericTest {
  static QueryPage page;
  @BeforeClass
  public static void setUp(){
    page = new QueryPage();
    Global.getDriver();
    globalDriver.get(page.getBaseUrl());

  }
  
  @Test
  public void test_01_H1() {
    Global.driverWait(5);
    assertTrue("Can't find H1 title", page.isH1Present());
  }
  @Test
  public void test_02_H1Text() {
    assumeTrue(page.isH1Present());
    String strToMatch = Global.properties.getProperty("queryH1");
    assertEquals(strToMatch, page.getH1().getText());
  }
  
  @Test 
  public void test_03_NoQuery() {
    assertTrue("Can't find 'No Query' div", page.isNoContentDivPresent());
  }
  @Test
  public void test_04_TitleNoQuery() {
    Scanner scanner = new Scanner(page.getNoContentDiv().getText());
    String strToMatch = Global.properties.getProperty("noProcedures");
    assertEquals(strToMatch, scanner.nextLine());
    strToMatch = Global.properties.getProperty("addProcedures");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
  

}
