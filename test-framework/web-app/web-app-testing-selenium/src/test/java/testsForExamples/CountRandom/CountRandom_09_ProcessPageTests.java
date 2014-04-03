package testsForExamples.CountRandom;

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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import pageTests.GenericTest;
import pages.ProcessPage;
import drivers.Global;

/** Class providing tests for Process Page.
 * app CountRandom uploaded, Click on the Process link
 * on the left panel, run the tests
 * 
 * @author elmira
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CountRandom_09_ProcessPageTests extends GenericTest {

  static ProcessPage page;
  static CountRandom example;
  static WebElement table;
  @BeforeClass
  public static void setUp(){
    page = new ProcessPage();
    example = new CountRandom();
    Global.getDriver();
    globalDriver.get(page.getBaseUrl());

  }
  @Test
  public void test_01_PanelPresent() {
    assertTrue("Can't find panel", page.isPanelPresent());
  }
  @Test
  public void test_02_TablePresent() {
    assertTrue("Can't find table in the panel", page.isTablePresentInWebElement(page.getPanel()));
  }
  @Test
  public void test_03_NameOfJar() {
    table = page.getTableFromWebElem(page.getPanel());
    WebElement td = page.getTd(table, 0, 0);
    WebElement nameApp = page.getNameApp(td);
    assertEquals(example.getNameOfJar(), nameApp.getText());
    String status = page.getStatusApp(td).getText();
    String strToMatch = Global.properties.getProperty("statusStopped");
    assertEquals(strToMatch, status);
  }
  @Test
  public void test_04_ProcessingRate() {
    assumeTrue(page.isTablePresentInWebElement(page.getPanel()));
    WebElement td1 = page.getTd(table, 0, 1);
    assertEquals("0", td1.getText());
    assertTrue(page.isSVGPresent(td1));
  }
  @Test
  public void test_05_BusynessRate() {
    assumeTrue(page.isTablePresentInWebElement(page.getPanel()));
    WebElement td1 = page.getTd(table, 0, 2);
    assertEquals("0%", td1.getText());
    assertTrue(page.isSVGPresent(td1));
  }
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }


}
