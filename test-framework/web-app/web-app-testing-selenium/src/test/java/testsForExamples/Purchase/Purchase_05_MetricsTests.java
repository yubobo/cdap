package testsForExamples.Purchase;

import drivers.Global;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebElement;
import pageTests.GenericTest;
import pages.AppPage;
import pages.MetricsPage;

import java.util.Scanner;

import static drivers.Global.globalDriver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/** 
 * CountRandomActionsTests for actions with CountRandom.
 * jar file should be uploaded, all quantity and fields checked
 * @author elmira
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Purchase_05_MetricsTests extends GenericTest {
  static AppPage app;
  static MetricsPage page;
  static Purchase example;
  
  @BeforeClass
  public static void setUp(){
    app = new AppPage();
    page = new MetricsPage();
    example = new Purchase();
    Global.getDriver();
    globalDriver.get(page.getBaseUrl());
  }
  @Test
  public void test_01_AddBtnClick() {
    assumeTrue(page.isAddBtnPresent());
    page.getAddBtn().click();
    page.confDiv("block");
    assertTrue(page.getConfDiv().getCssValue("display").equals("block"));
  }
  @Test
  public void test_02_ClickSelectElem() {
    page.getSelectElem().click();
  }
  @Test
  public void test_03_AppPart() {
    String strToMatch = Global.properties.getProperty("apps");
    assertEquals(strToMatch, page.getPartTitle(0).getText());
    strToMatch = Global.properties.getProperty("purchaseApp");
    assertEquals(strToMatch, page.getSubListItem(page.getList(), 0).getText());
  }
  @Test
  public void test_04_DatasetPart() {
    String strToMatch = Global.properties.getProperty("datasets");
    assertEquals(strToMatch, page.getPartTitle(1).getText());
    Scanner scanner = new Scanner(page.getSubListItem(page.getList(), 1).getText());
    strToMatch = Global.properties.getProperty("customers");
    assertEquals(strToMatch, scanner.nextLine());
    strToMatch = Global.properties.getProperty("history");
    assertEquals(strToMatch, scanner.nextLine());
    strToMatch = Global.properties.getProperty("purchases");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_04_FlowPart() {
    String strToMatch = Global.properties.getProperty("flows");
    assertEquals(strToMatch, page.getPartTitle(2).getText());
    strToMatch = Global.properties.getProperty("purchaseFlow1");
    assertEquals(strToMatch, page.getSubListItem(page.getList(), 2).getText());
  }
  @Test
  public void test_05_MRPart() {
    String strToMatch = Global.properties.getProperty("mapreduses");
    assertEquals(strToMatch, page.getPartTitle(3).getText());
    strToMatch = Global.properties.getProperty("purchaseFlow2");
    assertEquals(strToMatch, page.getSubListItem(page.getList(), 3).getText());
  }
  @Test
  public void test_06_ProcPart() {
    String strToMatch = Global.properties.getProperty("procedures");
    assertEquals(strToMatch, page.getPartTitle(4).getText());
    strToMatch = Global.properties.getProperty("purchaseQuery");
    assertEquals(strToMatch, page.getSubListItem(page.getList(), 4).getText());
  }
  @Test
  public void test_07_StreamsPart() {
    String strToMatch = Global.properties.getProperty("streams");
    assertEquals(strToMatch, page.getPartTitle(5).getText());
    strToMatch = Global.properties.getProperty("purchaseStream");
    assertEquals(strToMatch, page.getSubListItem(page.getList(), 5).getText());
  }
 @Test
 public void test_08_PurchaseHistoryClick() {
   page.getSubListItem(page.getList(), 0).click();
   WebElement chosen = page.getChosenElem(0);
   String strToMatch = Global.properties.getProperty("chosen");
   assertEquals(strToMatch, chosen.getText());
 }
 @Test
 public void test_09_SelectMetricClick() {
   page.getSelectMetric().click();
   Global.driverWait(1);
   page.getListM().get(0).click();
   WebElement chosen = page.getChosenElem(1);
   String strToMatch = Global.properties.getProperty("metricChosen");
   assertEquals(strToMatch, chosen.getText());
 }
 @Test
 public void test_10_AddClick() {
   page.getConfAdd().click();
   assertTrue("Can't find square", page.isRectPresent());
   String strToMatch = Global.properties.getProperty("purchaseApp");
   assertEquals(strToMatch, page.getMetricTitle().getText());
   strToMatch = Global.properties.getProperty("metricChosen");
   assertEquals(strToMatch, page.getMetricSubTitle().getText());
 }
 @Test
 public void test_11_CheckSVGPresent() {
   assertTrue("Cant'find svg", page.isSVGPresent(page.getWidget()));
 }
 @Test
 public void test_12_RemoveMetrics() {
   page.getRemoveMetrics().click();
 }
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
}
