
package commonSanityTests;

import drivers.Global;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import pageTests.GenericTest;
import pages.BasePage;
import pages.HomePage;

import static drivers.Global.CONTACT_URL;
import static drivers.Global.TERMS_URL;
import static drivers.Global.globalDriver;
import static drivers.Global.waitForLoading;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
/** Footer Sanity Checks provide sanity checks for footer of the every page.
 * The same for all pages
 * Checking existence of elements and texts
 * Also checking for  broken links
 * Except Reset link, it's in ResetTests class 
 * @author elmira
 * 
*/
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FooterSanityTests extends GenericTest {
  static BasePage page;
  @BeforeClass
  public static void setUp(){
    Global.getDriver();
    page = new HomePage();
    globalDriver.get(page.getBaseUrl());
    waitForLoading(page.getBaseUrl());

  }
  
  @Test
  public void test_01_Copyright() {
    assertTrue("Can't find Copyright", page.isCopyrightPresent());
  }

  @Test
  public void test_02_SupportFooter() {
    assertTrue("Can't find Support link in footer", page.isSupportFooterPresent());
  }
  
  @Test
  public void test_05_Terms() {
    assertTrue("Can't find Terms link", page.isTermsPresent());
  }

  @Test
  public void test_06_TermsText() {
    assumeTrue(page.isTermsPresent());
    assertEquals("TERMS", page.getTerms().getText());
  }
  @Test
  public void test_07_TermsClick() {
    assumeTrue(page.isTermsPresent());
    String newUrl = switchToNewTab(page.getTerms(), page.getBaseUrl());
    assertEquals(TERMS_URL, newUrl);
  }
  @Test
  public void test_08_Contact() {
    assertTrue("Can't find Contact link", page.isContactPresent());
  }

  @Test
  public void test_09_ContactText() {
    assumeTrue(page.isContactPresent());
    assertEquals("CONTACT", page.getContact().getText());
  }
  @Test
  public void test_10_ContactClick() {
    assumeTrue(page.isContactPresent());
    String newUrl = switchToNewTab(page.getContact(), page.getBaseUrl());
    assertEquals(CONTACT_URL, newUrl);
  }
 
  
  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
  
}
