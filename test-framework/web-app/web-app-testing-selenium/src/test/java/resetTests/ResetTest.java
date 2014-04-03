package resetTests;

import static drivers.Global.globalDriver;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebElement;

import pageTests.GenericTest;
import pages.BasePage;
import pages.HomePage;
import drivers.Global;

/** ResetTest is class for testing functionality of Reset link.
 * should run BEFORE ANY testing
 * to perform removing any preexisting condition
 * 
 *  @author Elmira P.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ResetTest extends GenericTest{
  static HomePage page;
  @BeforeClass
  public static void setUp(){
    page = new HomePage();
    Global.getDriver();
    globalDriver.get(page.BASE_URL);
    page.waitForZeroPresent();

  }
  @Test
  public void test_01_Reset() {
    assertTrue(page.isResetPresent());
  }

  @Test
  public void test_02_ResetText() {
    assumeTrue(page.isResetPresent());
    assertEquals("RESET", page.getReset().getText());
  }
  @Test
  public void test_03_ResetClick() {
    assumeTrue(page.isResetPresent());
    page.getReset().click();
    page.waitForVisibility("block");
    WebElement resetDiv = page.getResetDiv();
    assertEquals("block", resetDiv.getCssValue("display"));
  }
  @Test
  public void test_04_Close() {
    assertTrue(page.isResetClosePresent());
  }
  @Test
  public void test_05_CloseClick() {
    assumeTrue(page.isResetClosePresent());
    page.getCloseReset().click();
    page.waitForVisibility("none");
    WebElement resetDiv = page.getResetDiv();
    assertEquals("none", resetDiv.getCssValue("display"));
    goToDivAgain(page);
  }
  @Test
  public void test_06_Cancel() {
    assertTrue(page.isResetCancelPresent());
  }
  @Test
  public void test_07_CancelClick() {
    assumeTrue(page.isResetCancelPresent());
    page.getCancelBtn(page.getResetDiv()).click();
    page.waitForVisibility("none");
    WebElement resetDiv = page.getResetDiv();
    assertEquals("none", resetDiv.getCssValue("display"));
    goToDivAgain(page);
  }
  @Test
  public void test_08_Okay() {
    assertTrue(page.isResetOkayPresent());
  }
  @Test
  public void test_09_OkayClick() {
    assumeTrue(page.isResetOkayPresent());
    page.getOkayBtn(page.getResetDiv()).click();
    Global.driverWait(5);
    BasePage.checkingHover("block");
    BasePage.checkingHover("none");
    //assertEquals("block", page.getDropHover().getCssValue("display"));
  }
  /*
  @Test
  public void waitDeletionProcess() {
    BasePage.checkingHover("none");
    assertEquals("none", page.getDropHover().getCssValue("display"));
  }
  */
  @Test
  public void test_10_checkIfDeleted() {
    assertTrue(page.isNoContentDivPresent());
  }
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
  public static void goToDivAgain(HomePage page) {
    page.getReset().click();
    page.waitForVisibility("block");
  }
}
