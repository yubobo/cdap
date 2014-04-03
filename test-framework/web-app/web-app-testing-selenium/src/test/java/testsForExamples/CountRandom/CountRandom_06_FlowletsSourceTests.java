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

/** CountRandomFlowletsSourceTests tests for Source flowlet.
 * using CountRandom example
 * In the beginning reset everything,
 * load app, go to flows page, run app
 * go to flowlets, provide tests
 * 
 * @author elmira
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CountRandom_06_FlowletsSourceTests extends GenericTest{
  static AppPage app;
  static FlowPage page;
  static CountRandom example;
  
  @BeforeClass
  public static void setUp(){
    app = new AppPage();
    page = new FlowPage();
    example = new CountRandom();
    Global.getDriver();
    //page.runReset();
    //page.uploadApp();
    globalDriver.get(example.getAppUrl());
    //page.waitForProcessNumbersPresent();
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
    assumeTrue(page.isSourcePresent());
    page.getSource().click();
    assertTrue("Can't find popup div", page.isPopupPresent());
  }
  @Test
  public void test_02_H1Present() {
    assertTrue("Can't find title of popup div", page.isPopupH1Present());
  }
  @Test
  public void test_03_H1Text() {
    assumeTrue(page.isPopupH1Present());
    String strToMatch = Global.properties.getProperty("sourceH1");
    assertEquals(strToMatch, page.getPopupH1().getText());
  }
  @Test
  public void test_04_InputPresent() {
    assertTrue("Can't find Input link", page.isInputLinkPresent());
  }
  @Test
  public void test_05_NoInputsPresent() {
    assertTrue("Can't find 'No Inputs div'", page.isNoInputsPresent());
  }
  @Test
  public void test_06_NoInputsText() {
    String strToMatch = Global.properties.getProperty("noInputs");
    assertEquals("There are no inputs on this flowlet.", page.getNoContent().getText());
  }
  @Test
  public void test_07_InputText() {
    String strToMatch = Global.properties.getProperty("inputs");
    assertEquals(strToMatch, page.getInputLink().getText());
  }
  @Test
  public void test_08_ProcessedPresent() {
    assertTrue("Can't find Processed link", page.isProcessedLinkPresent());
  }
  @Test
  public void test_09_ProcessedText() {
    String strToMatch = Global.properties.getProperty("processedFlowlets");
    assertEquals(strToMatch, page.getProcessedLink().getText());
  }
  @Test
  public void test_10_ProcessedLinkClick() {
    page.getProcessedLink().click();
    assertTrue("Can't find table on the processed tab", page.isTableProcessedPresent());
  }
  @Test
  public void test_11_ProcessedTitle() {
    assumeTrue(page.isTableProcessedPresent());
    String title = page.getTdTitle(0).getText();
    String strToMatch = Global.properties.getProperty("processingRate");
    assertEquals(strToMatch, title); 
  }
  @Test
  public void test_12_ProcessedValue() {
    assumeTrue(page.isTableProcessedPresent());
    String value = page.getTdValue(0).getText();
    Scanner scanner = new Scanner(value);
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("eventsPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_13_ProcessedSVGPresent() {
    assertTrue("Can't find SVG on Processed cell", page.isSVGPresent(page.getProcessedTd(0)));
  }
  @Test
  public void test_14_OperationsTitle() {
    assumeTrue(page.isTableProcessedPresent());
    String title = page.getTdTitle(1).getText();
    String strToMatch = Global.properties.getProperty("operations");
    assertEquals(strToMatch, title); 
  }
  @Test
  public void test_15_OperationsValue() {
    assumeTrue(page.isTableProcessedPresent());
    String value = page.getTdValue(1).getText();
    Scanner scanner = new Scanner(value);
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("eventsPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_16_OperationsSVGPresent() {
    assertTrue("Can't find SVG on Operations cell", page.isSVGPresent(page.getProcessedTd(1)));
  }
  @Test
  public void test_17_BusynessTitle() {
    assumeTrue(page.isTableProcessedPresent());
    String title = page.getTdTitle(2).getText();
    String strToMatch = Global.properties.getProperty("busyness");
    assertEquals(strToMatch, title); 
  }
  @Test
  public void test_18_BusynessValue() {
    assumeTrue(page.isTableProcessedPresent());
    String value = page.getTdValue(2).getText();
    assertEquals("0%", value);
  }
  @Test
  public void test_19_BusynessSVGPresent() {
    assertTrue("Can't find SVG on Busyness cell", page.isSVGPresent(page.getProcessedTd(2)));
  }
  @Test
  public void test_20_ErrorsTitle() {
    assumeTrue(page.isTableProcessedPresent());
    String title = page.getTdTitle(3).getText();
    String strToMatch = Global.properties.getProperty("errors");
    assertEquals("ERRORS", title); 
  }
  @Test
  public void test_21_ErrorsValue() {
    assumeTrue(page.isTableProcessedPresent());
    String value = page.getTdValue(3).getText();
    Scanner scanner = new Scanner(value);
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("eventsPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_22_ErrorsSVGPresent() {
    assertTrue("Can't find SVG on Errors cell", page.isSVGPresent(page.getProcessedTd(3)));
  }
  @Test
  public void test_23_OutputsPresent() {
    assertTrue("Can't find Outputs link", page.isOutputsLinkPresent());
  }
  @Test
  public void test_24_OutputsText() {
    String strToMatch = Global.properties.getProperty("outputs");
    assertEquals(strToMatch, page.getOutputsLink().getText());
  }
  @Test
  public void test_25_OutputsClick() {
    page.getOutputsLink().click();
    
  }
  @Test
  public void test_26_OutboundTitle() {
    assumeTrue(page.isTableProcessedPresent());
    String title = page.getOutputTitle(0).getText();
    String strToMatch = Global.properties.getProperty("outbound");
    assertEquals(strToMatch, title); 
  }
  @Test
  public void test_27_OutboundValue() {
    assumeTrue(page.isTableProcessedPresent());
    String value = page.getOutputValue(0).getText();
    Scanner scanner = new Scanner(value);
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("eventsPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
  }
  @Test
  public void test_28_OutboundSVGPresent() {
    assertTrue("Can't find SVG in Outbound cell", page.isSVGPresent(page.getOutputTd(0)));
  }
  @Test
  public void test_29_SplitterPresent() {
    String strToMatch = Global.properties.getProperty("splitter");
    assertEquals(strToMatch, page.getOutputTd(2).getText());
  }
  @Test
  public void test_30_CloseFlowlet() {
    page.getCloseFlowlet().click();
  }
}
