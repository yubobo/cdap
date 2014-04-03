package testsForExamples.CountRandom;

import drivers.Global;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import pageTests.GenericTest;
import pages.AppPage;
import pages.DatasetsPage;

import java.util.Scanner;

import static drivers.Global.globalDriver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/** CountRandomDatasetsTests class for datasets page using CountRandom example.
 * 
 * @author elmira
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CountRandom_05_DatasetsTests extends GenericTest{
  static AppPage app;
  static DatasetsPage page;
  static CountRandom example;
  @BeforeClass
  public static void setUp(){
    app = new AppPage();
    page = new DatasetsPage();
    example = new CountRandom();
    Global.getDriver();
    page.runReset();
    page.uploadApp();
    globalDriver.get(example.getAppUrl());
    page.waitForProcessNumbersPresent();
  }
  @Test
  public void test_01_GotoDatasetsPage() {
    assumeTrue(app.isTablePresentInWebElement(page.getStorePanel()));
    WebElement table = app.getTableFromWebElem(app.getStorePanel());
    WebElement td1 = app.getTd(table, 0, 0);
    WebElement datasetUrl = td1.findElement(By.tagName("a"));
    datasetUrl.click();
    String url = Global.properties.getProperty("crDatasetUrl");
    assertEquals(Global.ROOT_URL + url, globalDriver.getCurrentUrl());
    globalDriver.get(Global.ROOT_URL + url);
  }
  @Test
  public void test_02_BreadCrumb() {
    assertTrue("Can't find breadcrums", page.isBreadCrumbExist());
  }
  @Test
  public void test_03_BreadCrumbText() {
    assumeTrue(page.isBreadCrumbExist());
    assertEquals("Store", page.getBreadCrumb().getText());
  }
  @Test
  public void test_04_H1() {
    assertTrue("Can't find H1", page.isH1Present());
  }
  @Test
  public void test_05_H1text() {
    assertEquals("randomTable Dataset", page.getH1().getText());
  }
 
  @Test
  public void test_06_WriteContainerPresent() {
    assertTrue("Can't find write container", page.isWritePresent());
  }
  // write rate 
  @Test
  public void test_07_WritePresent() {
    assumeTrue(page.isWritePresent());
    assertTrue("Can't find write container title", page.isContainerTitle(page.getWriteContainer()));
  }
  
  @Test
  public void test_08_WriteTitleText() {
    assumeTrue(page.isContainerTitle(page.getWriteContainer()));
    String strToMatch = Global.properties.getProperty("writeRate");
    assertEquals(strToMatch, page.getContainerTitle(page.getWriteContainer()).getText());
  }
  
  @Test
  public void test_09_WriteValuePresent() {
    page.waitForWriteNumbersPresent();
    Scanner scanner = new Scanner(page.getContainerValue(page.getWriteContainer()).getText());
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("bytesPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_10_WriteSVGPresent() {
    assertTrue("svg is not present in the write container", page.isSVGPresent(page.getWriteContainer()));
  }
  // read rate
  @Test
  public void test_11_ReadPresent() {
    assumeTrue(page.isReadPresent());
    assertTrue("Can't find read container title", page.isContainerTitle(page.getReadContainer()));
  }
  
  @Test
  public void test_12_ReadTitleText() {
    assumeTrue(page.isContainerTitle(page.getReadContainer()));
    String strToMatch = Global.properties.getProperty("readRate");
    assertEquals(strToMatch, page.getContainerTitle(page.getReadContainer()).getText());
  }
  
  @Test
  public void test_13_ReadValuePresent() {
    Scanner scanner = new Scanner(page.getContainerValue(page.getReadContainer()).getText());
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("opss");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_14_ReadSVGPresent() {
    assertTrue("svg is not present in read container", page.isSVGPresent(page.getReadContainer()));
  }
  @Test
  public void test_15_StorageContainerPresent() {
    assertTrue("Can't find Storage container", page.isStorageContainer());
  }
  @Test
  public void test_16_StorageTitlePresent() {
    assumeTrue(page.isStorageContainer());
    assertTrue("Can't find title of storage container", page.isContainerTitle(page.getStorageContainer()));
  }
  @Test
  public void test_17_StorageTitleText() {
    assumeTrue(page.isContainerTitle(page.getStorageContainer()));
    String strToMatch = Global.properties.getProperty("stored");
    assertEquals(strToMatch, page.getContainerTitle(page.getStorageContainer()).getText());
  }
  @Test
  public void test_18_StorageValue() {
    //page.waitForStorage();
    String text = page.getContainerValue(page.getStorageContainer()).getText();
    Scanner scanner = new Scanner(text);
    assertTrue("Storage number is not present", text.length() > 0);
  }
  
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }

}
