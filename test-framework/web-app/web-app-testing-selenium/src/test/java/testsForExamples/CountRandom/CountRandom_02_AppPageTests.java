package testsForExamples.CountRandom;

import drivers.Global;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebElement;
import pageTests.GenericTest;
import pages.AppPage;

import java.util.Scanner;

import static drivers.Global.globalDriver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/** CountRandom already uploaded and now we go to the app page.
 * 
 * @author elmira
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CountRandom_02_AppPageTests extends GenericTest {
  static AppPage page;
  static CountRandom example;
  static WebElement table;
  
  
  @BeforeClass
  public static void setUp(){
    page = new AppPage();
    example = new CountRandom();
    Global.getDriver();
    globalDriver.get(example.getAppUrl());
    page.waitForProcessNumbersPresent();
  }
  
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
  @Test
  public void test_01_Url() {
    assertEquals(example.getAppUrl(), globalDriver.getCurrentUrl());
  }
  @Test
  public void test_02_H1Present() {
    assertTrue("H1 is not present on the page", page.isH1Present());
  }
  @Test
  public void test_03_H1Text() {
    assumeTrue(page.isH1Present());
    assertEquals(example.getNameOfJar() + " Application", page.getH1().getText());
    
  }
  @Test
  public void test_04_ProcessedContainerPresent() {
    assertTrue("Can't find Processed container on the page", page.isProcessContainer());
  }
  @Test
  public void test_05_ProcessedTitlePresent() {
    page.waitForTitlePresent();
    assumeTrue(page.isProcessContainer());
    assertTrue("Can't find processed title", page.isContainerTitle(page.getProcessedContainer()));
  }
 
  @Test
  public void test_06_ProcessedTitleText() {
    assumeTrue(page.isContainerTitle(page.getProcessedContainer()));
    String strToMatch = Global.properties.getProperty("processed");
    assertEquals(strToMatch, page.getContainerTitle(page.getProcessedContainer()).getText());
  }
  @Test
  public void test_07_ProcessedValuePresent() {
    Scanner scanner = new Scanner(page.getContainerValue(page.getProcessedContainer()).getText());
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("eventsPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_08_ProcessedSVGPresent() {
    assertTrue("Can't find svg in the processed container", page.isSVGPresent(page.getProcessedContainer()));
  }
  @Test
  public void test_09_BusynessContainerPresent() {
    assertTrue("Can't find Busyness container", page.isBusynessContainer());
  }
  @Test
  public void test_10_BusynessTitlePresent() {
    assumeTrue(page.isBusynessContainer());
    assertTrue("Can't find Busyness title", page.isContainerTitle(page.getBusynessContainer()));
  }
  @Test
  public void test_11_BusynessTitleText() {
    assumeTrue(page.isContainerTitle(page.getBusynessContainer()));
    String strToMatch = Global.properties.getProperty("busyness");
    assertEquals(strToMatch, page.getContainerTitle(page.getBusynessContainer()).getText());
  }
  @Test
  public void test_12_BusynessValuePresent() {
    String text = page.getContainerValue(page.getBusynessContainer()).getText();
    assertEquals("0%", text);
  }
  @Test
  public void test_13_BusynessSVGPresent() {
    assertTrue("svg is not present in Busyness container", page.isSVGPresent(page.getBusynessContainer()));
  }
  @Test
  public void test_14_StorageContainerPresent() {
    assertTrue("Can't find storage container", page.isStorageContainer());
  }
  @Test
  public void test_15_StorageTitlePresent() {
    assumeTrue(page.isStorageContainer());
    assertTrue("Can't find storage title", page.isContainerTitle(page.getStorageContainer()));
  }
  @Test
  public void test_16_StorageTitleText() {
    assumeTrue(page.isContainerTitle(page.getStorageContainer()));
    String strToMatch = Global.properties.getProperty("storage");
    assertEquals(strToMatch, page.getContainerTitle(page.getStorageContainer()).getText());
  }
  @Test
  public void test_17_StorageValue() {
    //page.waitForStorageNumbersPresent();
    page.waitForStorage();
    String text = page.getContainerValue(page.getStorageContainer()).getText();
    Scanner scanner = new Scanner(text);
    assertTrue("storage numbers are not present", text.length() > 0);
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("b");
    assertEquals(strToMatch, scanner.nextLine());
  }
  // Collect panel
  @Test
  public void test_18_CollectPanelPresent() {
    assertTrue("Can't find Collect panel", page.isCollectPanelPresent());
  }
 
  @Test
  public void test_19_CollectTitle() {
    assumeTrue(page.isCollectPanelPresent());
    String strToMatch = Global.properties.getProperty("collect");
    assertEquals(strToMatch, page.getPanelTitle(page.getCollectPanel()).getText());
  }

  @Test
  public void test_20_NoCollect() {
    // in CountRandom example collect should zero
    assertTrue("Can't find 'No collect' div", page.isNoContentPresent(page.getCollectPanel()));
  }
  @Test
  public void test_21_NoCollectText() {
    assumeTrue(page.isNoContentPresent(page.getCollectPanel()));
    Scanner scanner = new Scanner(page.getNoContentInElement(page.getCollectPanel()).getText());
    String strToMatch = Global.properties.getProperty("noStreams");
    assertEquals(strToMatch, scanner.nextLine());
    strToMatch = Global.properties.getProperty("addStreams");
    assertEquals(strToMatch, scanner.nextLine());
  }
  // Process panel
  @Test
  public void test_22_ProcessPanelPresent() {
    assertTrue("Can't find process panel", page.isProcessPanelPresent());
  }
  
  @Test
  public void test_23_ProcessTitle() {
    assumeTrue(page.isProcessPanelPresent());
    String strToMatch = Global.properties.getProperty("process");
    assertEquals(strToMatch, page.getPanelTitle(page.getProcessPanel()).getText());
  }
  @Test
  public void test_24_StartPresent() {
    assertTrue("Can't find 'Start' button", page.isStartPresent(page.getProcessPanel()));
  }
  @Test
  public void test_25_StopPresent() {
    assertTrue("Can't find 'Stop' button", page.isStopPresent(page.getProcessPanel()));
  }
  @Test
  public void test_26_ProcessTableExist() {
    assertTrue("Can't find process table", page.isTablePresentInWebElement(page.getProcessPanel()));
  }
  
  @Test
  public void test_27_AppRowInProcess() {
    assumeTrue(page.isTablePresentInWebElement(page.getProcessPanel()));
    table = page.getTableFromWebElem(page.getProcessPanel());
    assertEquals(example.getProcess(), page.getRows(table).size());
  }
  
  @Test
  public void test_28_NameOfApp() {
    assumeTrue(page.isTablePresentInWebElement(page.getProcessPanel()));
    WebElement td1 = page.getTd(table, 0, 0);
    String name = page.getNameApp(td1).getText();
    assertEquals(example.getNameOfJar(), name);
    String status = page.getStatusApp(td1).getText();
    String strToMatch = Global.properties.getProperty("statusStopped");
    assertEquals(strToMatch, status);
  }
  
  @Test
  public void test_29_ProcessingRate() {
    assumeTrue(page.isTablePresentInWebElement(page.getProcessPanel()));
    WebElement td1 = page.getTd(table, 0, 1);
    assertEquals("0", td1.getText());
    assertTrue("svg is not present in processing rate cell", page.isSVGPresent(td1));
  }
  @Test
  public void test_30_BusynessRate() {
    assumeTrue(page.isTablePresentInWebElement(page.getProcessPanel()));
    WebElement td1 = page.getTd(table, 0, 2);
    assertEquals("0%", td1.getText());
    assertTrue("svg is not present in busyness rate cell", page.isSVGPresent(td1));
  }
  // Store
  @Test
  public void test_31_StorePanelPresent() {
    assertTrue("Can't find Store panel", page.isStorePanelPresent());
  }
  @Test
  public void test_32_StoreTitle() {
    assumeTrue(page.isStorePanelPresent());
    String strToMatch = Global.properties.getProperty("store");
    assertEquals(strToMatch, page.getPanelTitle(page.getStorePanel()).getText());
  }
  @Test
  public void test_33_TablePresentInStorePanel() {
    assertTrue("Can't find table in Store panel", page.isTablePresentInWebElement(page.getStorePanel()));
  }
  @Test
  public void test_34_NameOfDatasets() {
    assumeTrue(page.isTablePresentInWebElement(page.getStorePanel()));
    table = page.getTableFromWebElem(page.getStorePanel());
    WebElement td1 = page.getTd(table, 0, 0);
    String name = page.getNameApp(td1).getText();
    String strToMatch = Global.properties.getProperty("randomTable");
    assertEquals(strToMatch, name);
    String status = page.getStatusApp(td1).getText();
    strToMatch = Global.properties.getProperty("rtDesc");
    assertEquals(strToMatch, status);
  }
  @Test
  public void test_35_IsStorageValueIsZero() {
    assumeTrue(page.isTablePresentInWebElement(page.getStorePanel()));
    WebElement td1 = page.getTd(table, 0, 1);
    Scanner scanner = new Scanner(td1.getText());
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("b");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_36_WriteRate() {
    assumeTrue(page.isTablePresentInWebElement(page.getStorePanel()));
    WebElement td1 = page.getTd(table, 0, 2);
    assertEquals("0", td1.getText());
    assertTrue("svg is not present in write rate cell", page.isSVGPresent(td1));
  }
  // Query
  @Test
  public void test_37_QueryPanelPresent() {
    assertTrue("Can't find Query panel", page.isQueryPanelPresent());
  }
  @Test
  public void test_38_QueryTitle() {
    assumeTrue(page.isQueryPanelPresent());
    String strToMatch = Global.properties.getProperty("query");
    assertEquals(strToMatch, page.getPanelTitle(page.getQueryPanel()).getText());
  }
  @Test
  public void test_39_NoQueryText() {
    assumeTrue(page.isNoContentPresent(page.getQueryPanel()));
    Scanner scanner = new Scanner(page.getNoContentInElement(page.getQueryPanel()).getText());
    String strToMatch = Global.properties.getProperty("noProcedures");
    assertEquals(strToMatch, scanner.nextLine());
    strToMatch = Global.properties.getProperty("addProcedures");
    assertEquals(strToMatch, scanner.nextLine());
  }

  

}
