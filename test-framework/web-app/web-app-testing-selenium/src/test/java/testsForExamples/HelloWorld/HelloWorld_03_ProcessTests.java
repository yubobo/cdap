package testsForExamples.HelloWorld;

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
import pages.FlowPage;
import drivers.Global;

/** 
 * CountRandomActionsTests for actions with CountRandom.
 * jar file should be uploaded, all quantity and fields checked
 * @author elmira
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HelloWorld_03_ProcessTests extends GenericTest {
  static AppPage app;
  static FlowPage page;
  static HelloWorld example;
  static WebElement table;
  
  @BeforeClass
  public static void setUp(){
    app = new AppPage();
    page = new FlowPage();
    example = new HelloWorld();
    Global.getDriver();
    globalDriver.get(example.getAppUrl());
    app.waitForProcessNumbersPresent();
    app.waitForStorage();
  }
 
  @Test
  public void test_01_GoToProcessPage() {
    WebElement table = app.getTableFromWebElem(app.getProcessPanel(), 0);
    WebElement td1 = app.getTd(table, 0, 0);
    WebElement name = app.getNameApp(td1);
    name.click();
    String url = globalDriver.getCurrentUrl();
    String strToMatch = Global.ROOT_URL + Global.properties.getProperty("whoFlow");
    assertEquals(strToMatch, url);
    globalDriver.get(strToMatch);
  }
  
  @Test
  public void test_02_CheckBreadCrumbs() {
    assertTrue("Can't find bread crumbs on the page", page.isBreadCrumbExist());
    assertEquals(example.getNameOfJar(), page.getBreadCrumb().getText());
  }
 
  @Test
  public void test_03_CheckH1() {
    String strToMatch = Global.properties.getProperty("whoFlowH1");
    assertEquals(strToMatch, page.getH1().getText());
  }
  @Test
  public void test_06_ProcessedContainerPresent() {
    assertTrue("Can't find process container", page.isProcessContainer());
  }
  @Test
  public void test_07_ProcessedTitlePresent() {
    assumeTrue(page.isProcessContainer());
    assertTrue("Can't find title of process container", page.isContainerTitle(page.getProcessedContainer()));
  }
  
  @Test
  public void test_08_ProcessedTitleText() {
    assumeTrue(page.isContainerTitle(page.getProcessedContainer()));
    String strToMatch = Global.properties.getProperty("processingRate");
    assertEquals(strToMatch, page.getContainerTitle(page.getProcessedContainer()).getText());
  }
  @Test
  public void test_09_ProcessedValuePresent() {
    page.waitForProcessNumbersPresent();
    Scanner scanner = new Scanner(page.getContainerValue(page.getProcessedContainer()).getText());
    assertEquals("0", scanner.nextLine());
    String strToMatch = Global.properties.getProperty("eventsPerSecond");
    assertEquals(strToMatch, scanner.nextLine());
  }
  
  @Test
  public void test_10_ProcessedSVGPresent() {
    assertTrue("Can't find SVG in Processed", page.isSVGPresent(page.getProcessedContainer()));
  }
  @Test
  public void test_11_BusynessContainerPresent() {
    assertTrue("Can't find busyness container", page.isBusynessContainer());
  }
  @Test
  public void test_12_BusynessTitlePresent() {
    assumeTrue(page.isBusynessContainer());
    assertTrue("Can't find title of busyness container", page.isContainerTitle(page.getBusynessContainer()));
  }
  @Test
  public void test_13_BusynessTitleText() {
    assumeTrue(page.isContainerTitle(page.getBusynessContainer()));
    String strToMatch = Global.properties.getProperty("busyness");
    assertEquals(strToMatch, page.getContainerTitle(page.getBusynessContainer()).getText());
  }
  @Test
  public void test_14_BusynessValuePresent() {
    String text = page.getContainerValue(page.getBusynessContainer()).getText();
    assertEquals("0%", text);
  }
  @Test
  public void test_15_BusynessSVGPresent() {
    assertTrue("Can't find SVG in Busyness", page.isSVGPresent(page.getBusynessContainer()));
  }
  @Test
  public void test_16_StatusPresent() {
    assertTrue("Can't find status", page.isStatusPresent());
  }
  @Test
  public void test_17_StatusText() {
    assumeTrue(page.isStatusPresent());
    String statusToMatch = Global.properties.getProperty("statusStopped").toLowerCase();
    assertEquals(statusToMatch, page.getStatus().getText().toLowerCase());
  }
  //who
   @Test
   public void test_18_FlowletWhoPresent() {
     assertTrue("Can't find who picture", page.isFlowletwhoPresent());
   }
   @Test
   public void test_19_EyeDropWhoPresent() {
     assertTrue("Can't find eyedrop icon in who picture", page.isEyeDropPresent(page.getFlowletwho()));
   }
   @Test
   public void test_20_WhoValue() {
     //page.waitForWhoValueNumbersPresent();
     assumeTrue(page.isFlowletwhoPresent());
     assertEquals("0", page.getPictValue(page.getFlowletwho()).getText());
   }
   @Test
   public void test_21_WhoTitlePresent() {
     assertTrue("Can't find title of Who picture", page.isPictTitlePresent(page.getFlowletwho()));
   }
   @Test
   public void test_22_WhoTitleText() {
     assumeTrue(page.isPictTitlePresent(page.getFlowletwho()));
     String strToMatch = Global.properties.getProperty("who");
     assertEquals(strToMatch, page.getPictTitle(page.getFlowletwho()).getText());
   }
   @Test
   public void test_23_whoIconPresent() {
     assertTrue("Can't find circle icon of who picture", page.isPictIconPresent(page.getFlowletwho()));
   }
 //Saver
   @Test
   public void test_24_SaverPresent() {
     assertTrue("Can't find saver picture", page.isSaverPresent());
   }
   @Test
   public void test_25_EyeDropSaverPresent() {
     assertTrue("Can't find eyedrop icon of saver picture", page.isEyeDropPresent(page.getSaver()));
   }
   @Test
   public void test_26_SaverValue() {
     assumeTrue(page.isSaverPresent());
     assertEquals("0", page.getPictValue(page.getSaver()).getText());
   }
   @Test
   public void test_27_SaverInstance() {
     assertTrue("Can't find saver instance", page.isInstancePresent(page.getSaver()));
   }
   @Test
   public void test_28_SaverInstanceText() {
     assumeTrue(page.isInstancePresent(page.getSaver()));
     assertEquals("1", page.getInstance(page.getSaver()).getText());
   }
   @Test
   public void test_29_SaverTitlePresent() {
     assertTrue("Can't find title of saver picture", page.isPictTitlePresent(page.getSaver()));
   }
   @Test
   public void test_30_SaverTitleText() {
     assumeTrue(page.isPictTitlePresent(page.getSaver()));
     String strToMatch = Global.properties.getProperty("saver");
     assertEquals(strToMatch, page.getPictTitle(page.getSaver()).getText());
   }
   @Test
   public void test_31_SaverIconPresent() {
     assertTrue("Can't find saver icon", page.isPictIconPresent(page.getSaver()));
   }
   @Test
   public void test_32_StartStopPresent() {
     assertTrue("Can't find Start/Stop button", page.isStartStopPresent());
   }
   
   @Test
   public void test_33_StartClick() {
     assumeTrue(page.isStartStopPresent());
     page.getStartStop().click();
     
   }
   @Test
   public void test_34_StatusTextRun() {
     assumeTrue(page.isStatusPresent());
     String strToMatch = Global.properties.getProperty("statusStarting");
     assertEquals(strToMatch, page.getStatus().getText());
   }
   @Test
   public void test_35_ForRunning() {
     String strToMatch = Global.properties.getProperty("statusRunning");
     page.waitForRunningSign(strToMatch);
     assertEquals(strToMatch, page.getStatus().getText());
     
   }
   @Test
   public void test_36_WhoClick() {
     page.getFlowletwho().click();
     assertTrue("Can't find popup div", page.isPopupPresent());
   }
   @Test
   public void test_37_WhoTitle() {
     WebElement elem = page.getPopupH1();
     String strToMatch = Global.properties.getProperty("whoFlowletH1");
     assertEquals(strToMatch, elem.getText());
   }
   @Test
   public void test_38_TestStorageInPopup() {
     table = page.getTableFromWebElem(page.getPopup());
     WebElement storage = page.getTd(table, 0, 0);
     Scanner scanner = new Scanner(storage.getText());
     assertEquals("0", scanner.nextLine());
     String strToMatch = Global.properties.getProperty("b");
     assertEquals(strToMatch, scanner.nextLine());
   }
   @Test
   public void test_39_TestEventsInPopup() {
     WebElement events = page.getTd(table, 0, 1);
     Scanner scanner = new Scanner(events.getText());
     assertEquals("0", scanner.nextLine());
     String strToMatch = Global.properties.getProperty("events");
     assertEquals(strToMatch, scanner.nextLine());
   }
   @Test
   public void test_40_EPSinPopup() {
     WebElement eps = page.getTd(table, 0, 2);
     assertEquals("0", eps.getText());
     assertTrue("svg is not present in popup", page.isSVGPresent(eps));
   }
   @Test
   public void test_41_sendText() {
     WebElement textInput = page.getInjectField();
     String str = Global.properties.getProperty("textToInject");
     textInput.sendKeys(str);
     page.getInjectBtn().click();
     page.waitFoEventsIsNotZero();
     WebElement events = page.getTd(table, 0, 1);
     Scanner scanner = new Scanner(events.getText());
     assertTrue("Number should be bigger than 0", Integer.parseInt(scanner.nextLine()) > 0);
     page.getCloseFlowlet().click();
   }
   
   @Test
   public void test_42_StartStopClick() {
     assumeTrue(page.isStartStopPresent());
     page.getStartStop().click();
     String strToMatch = Global.properties.getProperty("statusStopping");
     assertEquals(strToMatch, page.getStatus().getText());
     
   }
   @Test
   public void test_43_forStopped() {
     String statusToMatch = Global.properties.getProperty("statusStopped");
     page.waitForRunningSign(statusToMatch);
     assertEquals(statusToMatch.toLowerCase(), page.getStatus().getText().toLowerCase());
   }
   
  
  @Test
  public void test_44_NavPills() {
   WebElement elem = page.getNavPills();
   assertEquals(3, elem.findElements(By.tagName("a")).size());
  }
  
  @Test
  public void test_45_LogClick() {
    page.getLogLink().click();
    assertTrue("Can't find log div", page.isLogViewPresent());
  }
  @Test
  public void test_46_HistoryClick() {
    page.getHistoryLink(2).click();
    assertTrue("History should have at least one row", page.countRowHistoryTable() > 0);
  }
  
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
  
  
  
}
