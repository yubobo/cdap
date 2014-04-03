package testsForExamples.CountRandom;

import static drivers.Global.globalDriver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.Scanner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import pageTests.GenericTest;
import pages.AppPage;
import pages.BasePage;
import drivers.Global;

/** 
 * CountRandomActionsTests for actions with CountRandom.
 * jar file should be uploaded, all quantity and fields checked
 * @author elmira
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CountRandom_03_ActionsTests extends GenericTest {
  static AppPage page;
  static CountRandom example;
  
  @BeforeClass
  public static void setUp(){
    page = new AppPage();
    example = new CountRandom();
    Global.getDriver();
    //page.runReset();
    //page.uploadApp();
    globalDriver.get(example.getAppUrl());
    page.waitForProcessNumbersPresent();
    page.waitForStorage();
  }
  
  @Test
  public void test_01_StartClick() {
    page.getStart().click();
    if (page.isHoverPresent()) {
      BasePage.checkingHover("block");
      BasePage.checkingHover("none");
    }
  }
  
  @Test
  public void test_03_Running() {
    assumeTrue(page.isTablePresentInWebElement(page.getProcessPanel()));
    WebElement table = page.getTableFromWebElem(page.getProcessPanel());
    WebElement td1 = page.getTd(table, 0, 0);
    String status = page.getStatusApp(td1).getText();
    Global.driverWait(15);
    String strToMatch = Global.properties.getProperty("statusRunning").toLowerCase();
    assertEquals(strToMatch, status.toLowerCase());
    page.waitForProcessingRateIsNotZero();
  }
  @Test
  public void test_04_ProcessingRateIsNotZero() {
    assumeTrue(page.isTablePresentInWebElement(page.getProcessPanel()));
    WebElement table = page.getTableFromWebElem(page.getProcessPanel());
    WebElement td1 = page.getTd(table, 0, 1);
    
    assertFalse("Processing rate is not changing", td1.getText().equals("0"));
  }
  @Test
  public void test_05_BusynessRateIsNotZero() {
    assumeTrue(page.isTablePresentInWebElement(page.getProcessPanel()));
    WebElement table = page.getTableFromWebElem(page.getProcessPanel());
    WebElement td1 = page.getTd(table, 0, 2);
    assertFalse("Busyness rate is not changing", td1.getText().equals("0%"));
  }
  @Test
  public void test_06_WriteRateIsNotZero() {
    assumeTrue(page.isTablePresentInWebElement(page.getStorePanel()));
    WebElement table = page.getTableFromWebElem(page.getStorePanel());
    WebElement td1 = page.getTd(table, 0, 2);
    assertFalse("Write rate is not changing", td1.getText().equals("0"));
  }
  
  @Test
  public void test_07_StopClick() {
    page.getStop().click();
    page.waitForStopping();
  }
  @Test
  public void test_08_IsSignStopped() {
    assumeTrue(page.isTablePresentInWebElement(page.getProcessPanel()));
    WebElement table = page.getTableFromWebElem(page.getProcessPanel());
    WebElement td1 = page.getTd(table, 0, 0);
    String status = page.getStatusApp(td1).getText().toLowerCase();
    String statusToMatch = Global.properties.getProperty("statusStopped").toLowerCase();
    assertEquals(statusToMatch, status);
  }
  @Test
  public void test_09_ProcessingRateIsZero() {
    page.waitForProcessingRateIsZero();
    assumeTrue(page.isTablePresentInWebElement(page.getProcessPanel()));
    WebElement table = page.getTableFromWebElem(page.getProcessPanel());
    WebElement td1 = page.getTd(table, 0, 1);
    assertEquals("0", td1.getText());
    assertTrue("SVG is not present in processing rate cell", page.isSVGPresent(td1));
  }
  @Test
  public void test_10_BusynessRateIsZero() {
    assumeTrue(page.isTablePresentInWebElement(page.getProcessPanel()));
    WebElement table = page.getTableFromWebElem(page.getProcessPanel());
    WebElement td1 = page.getTd(table, 0, 2);
    assertEquals("0%", td1.getText());
    assertTrue("SVG is not present in busyness rate cell", page.isSVGPresent(td1));
  }
  @Test
  public void test_11_WriteRateIsZero() {
    assumeTrue(page.isTablePresentInWebElement(page.getStorePanel()));
    WebElement table = page.getTableFromWebElem(page.getStorePanel());
    WebElement td1 = page.getTd(table, 0, 2);
    assertEquals("0", td1.getText());
    assertTrue("SVG is not present in write rate cell", page.isSVGPresent(td1));
  }
  @Test
  public void test_12_StorageNotZero() {
    WebElement table = page.getTableFromWebElem(page.getStorePanel());
    WebElement td1 = page.getTd(table, 0, 1);
    Scanner scanner = new Scanner(td1.getText());
    String sum1 = scanner.nextLine();
    assertFalse("Storage rate is zero", sum1.equals("0"));
  }
  @Test
  public void test_13_SumsOfStorageValue() {
    assumeTrue(page.isTablePresentInWebElement(page.getStorePanel()));
    WebElement table = page.getTableFromWebElem(page.getStorePanel());
    WebElement td1 = page.getTd(table, 0, 1);
    Scanner scanner = new Scanner(td1.getText());
    String sum1 = scanner.nextLine();
    String text = page.getContainerValue(page.getStorageContainer()).getText();
    scanner = new Scanner(text);
    String sum2 = scanner.nextLine();
    assertEquals(sum1, sum2);
    
  }
  
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
}
