package testsForExamples.CountRandom;

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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import pageTests.GenericTest;
import pages.AppPage;
import pages.FlowPage;
import drivers.Global;

/** CountRandomFlowletsSplitterTests tests for Splitter flowlet.
 * using CountRandom example
 * 
 * go to flowlets, provide tests
 * 
 * @author elmira
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CountRandom_08_FlowletsCounterTests extends GenericTest {
  static AppPage app;
  static FlowPage page;
  static CountRandom example;
  
  @BeforeClass
  public static void setUp(){
    app = new AppPage();
    page = new FlowPage();
    example = new CountRandom();
    Global.getDriver();
    globalDriver.get(example.getAppUrl());
    WebElement table = app.getTableFromWebElem(app.getProcessPanel());
    WebElement td1 = app.getTd(table, 0, 0);
    WebElement flowUrl = page.getNameApp(td1);
    flowUrl.click();
  }
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
  @Test
  public void test_01_PopupPresent() {
    assumeTrue(page.isCounterPresent());
    page.getCounter().click();
    assertTrue("Can't find popup div", page.isPopupPresent());
  }
 
  @Test
  public void test_02_H1Present() {
    assertTrue("Can't find title of popup div", page.isPopupH1Present());
  }
  @Test
  public void test_03_H1Text() {
    assumeTrue(page.isPopupH1Present());
    String strToMatch = Global.properties.getProperty("counterH1");
    assertEquals(strToMatch, page.getPopupH1().getText());
  }
  
  @Test
  public void test_04_InputPresent() {
    assertTrue("Can't find Input link", page.isInputLinkPresent());
  }
  @Test
  public void test_05_SourcePresent() {
    String strToMatch = Global.properties.getProperty("splitter");
    assertEquals(strToMatch, page.getInputTd(0).getText());
  }
  @Test
  public void test_06_InboundTitle() {
    String title = page.getInputTitle(2).getText();
    String strToMatch = Global.properties.getProperty("inbound");
    assertEquals(strToMatch, title); 
  }
  
  @Test
  public void test_07_InboundValue() {
    String value = page.getInputValue(2).getText();
    Scanner scanner = new Scanner(value);
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("eventsPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_08_InboundSVGPresent() {
    assertTrue("Can't find SVG in Inbound cell", page.isSVGPresent(page.getInputTd(2)));
  }
  @Test
  public void test_09_InputText() {
    String strToMatch = Global.properties.getProperty("inputs");
    assertEquals(strToMatch, page.getInputLink().getText());
  }
  
  @Test
  public void test_10_ProcessedPresent() {
    assertTrue("Can't find Processed link", page.isProcessedLinkPresent());
  }
  @Test
  public void test_11_ProcessedText() {
    String strToMatch = Global.properties.getProperty("processedFlowlets");
    assertEquals(strToMatch, page.getProcessedLink().getText());
  }
  @Test
  public void test_12_ProcessedLinkClick() {
    page.getProcessedLink().click();
    assertTrue("Can't find table on the processed tab", page.isTableProcessedPresent());
  }
 
  @Test
  public void test_13_ProcessedTitle() {
    assumeTrue(page.isTableProcessedPresent());
    String title = page.getTdTitle(0).getText();
    String strToMatch = Global.properties.getProperty("processingRate");
    assertEquals(strToMatch, title); 
  }
  
  @Test
  public void test_14_ProcessedValue() {
    assumeTrue(page.isTableProcessedPresent());
    String value = page.getTdValue(0).getText();
    Scanner scanner = new Scanner(value);
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("eventsPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_15_ProcessedSVGPresent() {
    assertTrue("Can't find SVG on Processed cell", page.isSVGPresent(page.getProcessedTd(0)));
  }
  @Test
  public void test_16_OperationsTitle() {
    assumeTrue(page.isTableProcessedPresent());
    String title = page.getTdTitle(1).getText();
    String strToMatch = Global.properties.getProperty("operations");
    assertEquals(strToMatch, title); 
  }
  @Test
  public void test_17_OperationsValue() {
    assumeTrue(page.isTableProcessedPresent());
    String value = page.getTdValue(1).getText();
    Scanner scanner = new Scanner(value);
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("eventsPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_18_OperationsSVGPresent() {
    assertTrue("Can't find SVG on Operations cell", page.isSVGPresent(page.getProcessedTd(1)));
  }
  @Test
  public void test_19_BusynessTitle() {
    assumeTrue(page.isTableProcessedPresent());
    String title = page.getTdTitle(2).getText();
    String strToMatch = Global.properties.getProperty("busyness");
    assertEquals(strToMatch, title); 
  }
  @Test
  public void test_20_BusynessValue() {
    assumeTrue(page.isTableProcessedPresent());
    String value = page.getTdValue(2).getText();
    assertEquals("0%", value);
  }
  @Test
  public void test_21_BusynessSVGPresent() {
    assertTrue("Can't find SVG on Busyness cell", page.isSVGPresent(page.getProcessedTd(2)));
  }
  @Test
  public void test_22_ErrorsTitle() {
    assumeTrue(page.isTableProcessedPresent());
    String title = page.getTdTitle(3).getText();
    String strToMatch = Global.properties.getProperty("errors");
    assertEquals(strToMatch, title); 
  }
  @Test
  public void test_23_ErrorsValue() {
    assumeTrue(page.isTableProcessedPresent());
    String value = page.getTdValue(3).getText();
    Scanner scanner = new Scanner(value);
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("eventsPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_24_ErrorsSVGPresent() {
    assertTrue("Can't find SVG on Errors cell", page.isSVGPresent(page.getProcessedTd(3)));
  }
  @Test
  public void test_25_OutputsPresent() {
    assertTrue("Can't find Outputs link", page.isOutputsLinkPresent());
  }
  @Test
  public void test_26_OutputsText() {
    String strToMatch = Global.properties.getProperty("outputs");
    assertEquals(strToMatch, page.getOutputsLink().getText());
  }
  @Test
  public void test_27_OutputsClick() {
    page.getOutputsLink().click();
    
  }@Test
  public void test_28_NoOutputsPresent() {
    assertTrue("Can't find 'No Outputs div'", page.isNoInputsPresent());
  }
  @Test
  public void test_29_NoOutputsText() {
    String strToMatch = Global.properties.getProperty("noOutputs");
    assertEquals(strToMatch, page.getNoContent().getText());
  }
  @Test
  public void test_32_CloseFlowlet() {
    page.getCloseFlowlet().click();
  }
}
