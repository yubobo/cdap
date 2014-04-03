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
public class Purchase_02_AppPageTests extends GenericTest{
  static AppPage page;
  static Purchase example;
  static WebElement table;
  
  
  @BeforeClass
  public static void setUp(){
    page = new AppPage();
    example = new Purchase();
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
    assertTrue("Can't find H1", page.isH1Present());
  }
  @Test
  public void test_03_H1Text() {
    assumeTrue(page.isH1Present());
    assertEquals(example.getNameOfJar() + " Application", page.getH1().getText());
    }
  
  @Test
  public void test_04_ProcessedContainerPresent() {
    assertTrue("Can't find Process container", page.isProcessContainer());
  }
  @Test
  public void test_05_ProcessedTitlePresent() {
    page.waitForTitlePresent();
    assumeTrue(page.isProcessContainer());
    assertTrue("Can't find Processed container title", page.isContainerTitle(page.getProcessedContainer()));
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
    assertTrue("Can't find SVG in processed container", page.isSVGPresent(page.getProcessedContainer()));
  }
  @Test
  public void test_09_BusynessContainerPresent() {
    assertTrue("Can't find Busyness container", page.isBusynessContainer());
  }
  @Test
  public void test_10_BusynessTitlePresent() {
    assumeTrue(page.isBusynessContainer());
    assertTrue("Can't find Busyness container title", page.isContainerTitle(page.getBusynessContainer()));
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
    assertTrue("Can't find svg in Busyness container", page.isSVGPresent(page.getBusynessContainer()));
  }
  @Test
  public void test_14_StorageContainerPresent() {
    assertTrue(page.isStorageContainer());
  }
  @Test
  public void test_15_StorageTitlePresent() {
    assumeTrue(page.isStorageContainer());
    assertTrue("Can't find Storage container", page.isContainerTitle(page.getStorageContainer()));
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
    assertTrue(text.length() > 0);
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
  public void test_20_CollectTable() {
    assertTrue("Cant find table in Collect panel", page.isTablePresentInWebElement(page.getCollectPanel()));
  }
  @Test
  public void test_21_nameOfStream() {
    table = page.getTableFromWebElem(page.getCollectPanel());
    WebElement td = page.getTd(table, 0, 0);
    WebElement name = page.getNameApp(td);
    assertEquals("purchaseStream", name.getText());
    WebElement desc = page.getStatusApp(td);
    String strToMatch = Global.properties.getProperty("stream");
    assertEquals(strToMatch, desc.getText());
  }
  @Test
  public void test_22_CollectStorage() {
    WebElement td1 = page.getTd(table, 0, 1);
    Scanner scanner = new Scanner(td1.getText());
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("b");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_23_CollectEvents() {
    WebElement td1 = page.getTd(table, 0, 2);
    Scanner scanner = new Scanner(td1.getText());
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("events");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_24_CollectArrivalRate() {
    WebElement td1 = page.getTd(table, 0, 3);
    assertEquals("0", td1.getText());
    assertTrue("Can't find SVG on arrival rate cell", page.isSVGPresent(td1));
  }
  
  // Process panel
  @Test
  public void test_25_ProcessPanelPresent() {
    assertTrue("Can't find Process panel", page.isProcessPanelPresent());
  }
  
  @Test
  public void test_26_ProcessTitle() {
    assumeTrue(page.isProcessPanelPresent());
    String strToMatch = Global.properties.getProperty("process");
    assertEquals(strToMatch, page.getPanelTitle(page.getProcessPanel()).getText());
  }
  @Test
  public void test_27_StartPresent() {
    assertTrue("Can't find Start button on process panel", page.isStartPresent(page.getProcessPanel()));
  }
  @Test
  public void test_28_StopPresent() {
    assertTrue("Can't find Stop button on process panel", page.isStopPresent(page.getProcessPanel()));
  }
  
  @Test
  public void test_26_ProcessTableExist() {
    assertTrue("Can't find table on process panel", page.isTablePresentInWebElement(page.getProcessPanel()));
  }
  @Test
  public void test_27_FlowsNamesAndStatus() {
    table = page.getTableFromWebElem(page.getProcessPanel(), 0);
    WebElement td1 = page.getTd(table, 0, 0);
    WebElement name = page.getNameApp(td1);
    String strToMatch = Global.properties.getProperty("purchaseFlow1");
    assertEquals(strToMatch, name.getText());
    
    WebElement status = page.getStatusApp(td1);
    strToMatch = Global.properties.getProperty("statusStopped");
    assertEquals(strToMatch, status.getText());
  }
  @Test
  public void test_28_FlowsProcessingRate() {
    WebElement td1 = page.getTd(table, 0, 1);
    assertEquals("0", td1.getText());
    assertTrue("Can't find SVG in processing rate cell", page.isSVGPresent(td1));
  }
  
  @Test
  public void test_29_FlowsBusyness() {
    WebElement td1 = page.getTd(table, 0, 2);
    assertEquals("0%", td1.getText());
    assertTrue("Can't find SVG in busyness cell", page.isSVGPresent(td1));
  }
  @Test
  public void test_30_MapReduceNamesAndStatus() {
    table = page.getTableFromWebElem(page.getProcessPanel(), 1);
    WebElement td1 = page.getTd(table, 0, 0);
    WebElement name = page.getNameApp(td1);
    String strToMatch = Global.properties.getProperty("purchaseFlow2");
    assertEquals(strToMatch, name.getText());
    
    WebElement status = page.getStatusApp(td1);
    strToMatch = Global.properties.getProperty("statusStopped");
    assertEquals(strToMatch, status.getText());
  }
  @Test
  public void test_31_FlowsMappingStatus() {
    WebElement td1 = page.getTd(table, 0, 1);
    assertEquals("0%", td1.getText());
    assertTrue("Can't find SVG in maping status cell", page.isSVGPresent(td1));
  }
  
  @Test
  public void test_32_FlowsReducingStatus() {
    WebElement td1 = page.getTd(table, 0, 2);
    assertEquals("0%", td1.getText());
    assertTrue("Can't find SVG in reducing status cell", page.isSVGPresent(td1));
  }
  @Test
  public void test_33_WorkflowNamesAndStatus() {
    table = page.getTableFromWebElem(page.getProcessPanel(), 2);
    WebElement td1 = page.getTd(table, 0, 0);
    WebElement name = page.getNameApp(td1);
    String strToMatch = Global.properties.getProperty("purchaseFlow3");
    assertEquals(strToMatch, name.getText());
    
    WebElement status = page.getStatusApp(td1);
    strToMatch = Global.properties.getProperty("statusStopped");
    assertEquals(strToMatch, status.getText());
  }
  
  // Store
  @Test
  public void test_34_StorePanelPresent() {
    assertTrue("Can't find store panel", page.isStorePanelPresent());
  }
  @Test
  public void test_35_StoreTitle() {
    assumeTrue(page.isStorePanelPresent());
    String strToMatch = Global.properties.getProperty("store");
    assertEquals(strToMatch, page.getPanelTitle(page.getStorePanel()).getText());
  }
  @Test
  public void test_36_TablePresentInStorePanel() {
    assertTrue("Can't find table on store panel", page.isTablePresentInWebElement(page.getStorePanel()));
  }
  @Test
  public void test_37_NameOfFirstDatasets() {
    table = page.getTableFromWebElem(page.getStorePanel(), 0);
    WebElement td1 = page.getTd(table, 0, 0);
    String name = page.getNameApp(td1).getText();
    String strToMatch = Global.properties.getProperty("history");
    assertEquals(strToMatch, name);
    String status = page.getStatusApp(td1).getText();
    strToMatch = Global.properties.getProperty("hstDesc");
    assertEquals(strToMatch, status);
  }
  @Test
  public void test_38_IsStorageValueIsZeroFirstDataset() {
    WebElement td1 = page.getTd(table, 0, 1);
    Scanner scanner = new Scanner(td1.getText());
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("b");
    assertEquals(strToMatch, scanner.nextLine());
  }
  
  @Test
  public void test_39_WriteRateFirstDataset() {
    WebElement td1 = page.getTd(table, 0, 2);
    assertEquals("0", td1.getText());
    assertTrue("Can't find svg in write rate, first dataset", page.isSVGPresent(td1));
  }
  @Test
  public void test_40_NameOfSecondDatasets() {
    WebElement td1 = page.getTd(table, 1, 0);
    String name = page.getNameApp(td1).getText();
    String strToMatch = Global.properties.getProperty("purchases");
    assertEquals(strToMatch, name);
    String status = page.getStatusApp(td1).getText();
    strToMatch = Global.properties.getProperty("prcDesc");
    assertEquals(strToMatch, status);
  }
  @Test
  public void test_41_IsStorageValueIsZeroSecondDataset() {
    WebElement td1 = page.getTd(table, 1, 1);
    Scanner scanner = new Scanner(td1.getText());
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("b");
    assertEquals(strToMatch, scanner.nextLine());
  }
  
  @Test
  public void test_42_WriteRateSecondDataset() {
    WebElement td1 = page.getTd(table, 1, 2);
    assertEquals("0", td1.getText());
    assertTrue("Can't find svg in write rate, first dataset", page.isSVGPresent(td1));
  }
  
  @Test
  public void test_43_NameOfThirdDatasets() {
    WebElement td1 = page.getTd(table, 2, 0);
    String name = page.getNameApp(td1).getText();
    String strToMatch = Global.properties.getProperty("customers");
    assertEquals(strToMatch, name);
    String status = page.getStatusApp(td1).getText();
    strToMatch = Global.properties.getProperty("rtDesc");
    assertEquals(strToMatch, status);
  }
  @Test
  public void test_44_IsStorageValueIsZeroThirdDataset() {
    WebElement td1 = page.getTd(table, 2, 1);
    Scanner scanner = new Scanner(td1.getText());
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("b");
    assertEquals(strToMatch, scanner.nextLine());
  }
  
  @Test
  public void test_45_WriteRateThirdDataset() {
    WebElement td1 = page.getTd(table, 2, 2);
    assertEquals("0", td1.getText());
    assertTrue("Can't find svg in write rate, first dataset", page.isSVGPresent(td1));
  }
  // Query
  @Test
  public void test_46_QueryPanelPresent() {
    assertTrue("Can't find query panel", page.isQueryPanelPresent());
  }
  @Test
  public void test_47_QueryTitle() {
    assumeTrue(page.isQueryPanelPresent());
    String strToMatch = Global.properties.getProperty("query");
    assertEquals(strToMatch, page.getPanelTitle(page.getQueryPanel()).getText());
  }
  @Test
  public void test_48_StartPresent() {
    assertTrue("Can't find Start button on Query panel", page.isStartPresent(page.getQueryPanel()));
  }
  @Test
  public void test_49_StopPresent() {
    assertTrue("Can't find Stop button on Query panel", page.isStopPresent(page.getQueryPanel()));
  }
  @Test
  public void test_50_NameOfProcedure() {
    table = page.getTableFromWebElem(page.getQueryPanel(), 0);
    WebElement td1 = page.getTd(table, 0, 0);
    String name = page.getNameApp(td1).getText();
    String strToMatch = Global.properties.getProperty("purchaseQuery");
    assertEquals(strToMatch, name);
    String status = page.getStatusApp(td1).getText().toLowerCase();
    strToMatch = Global.properties.getProperty("statusStopped").toLowerCase();
    assertEquals(strToMatch, status);
  }
  @Test
  public void test_51_IsStorageValueIsZeroRequestRate() {
    WebElement td1 = page.getTd(table, 0, 1);
    assertEquals("0", td1.getText());
  }
  @Test
  public void test_52_IsStorageValueIsZeroErrorRate() {
    WebElement td1 = page.getTd(table, 0, 1);
    assertEquals("0", td1.getText());
  }
  

}
