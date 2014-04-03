package pages;

import drivers.Global;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.List;

import static drivers.Global.ROOT_URL;
import static drivers.Global.globalDriver;
import static org.junit.Assert.fail;
/** BasePage is a root pageobject with common elements that present at every page.
 * 
 * 1 part Left Panel
 * 2 part Header
 * 3 part Footer
 * 
 * @author Elmira P.
 */
public class BasePage {
  public static final String BASE_URL = ROOT_URL + Global.properties.getProperty("overviewPage");

  private By logoBy = By.id("logo");
  private By productNameBy = By.id("product-name");
  private By overviewBy = By.id("nav-overview");
  private By collectBy = By.id("nav-collect");
  private By processBy = By.id("nav-process");
  private By storeBy = By.id("nav-store");
  private By queryBy = By.id("nav-queries");
  private By metricsBy = By.id("header-metrics");
  private By accountBy = By.id("header-account");
  private By termsBy = By.id("footer-terms");
  private By contactBy = By.id("footer-contact");
  private By supportFooterBy = By.id("footer-support");
  private By resetLinkBy = By.id("footer-reset");
  private By footerBy = By.id("footer");
  private By resetDivBy = By.id("modal-from-dom");
  private By closeResetBy = By.className("close");
  private By copyrightBy = By.id("copyright");
  private By cancelBtnBy = By.className("cancel");
  private By okayBtnBy = By.className("okay");
  private By dropHoverBy = By.id("drop-hover");
  private By h1By = By.cssSelector("h1");
  private By tableBy = By.tagName("table");
  private By statusAppBy = By.className("app-list-status");
  private By panelBy = By.className("panel");
  private By noContentDivBy = By.className("object-list-empty-content");
  //
  protected By containerValueBy = By.className("sparkline-box-value");
  private By containerTitleBy = By.className("sparkline-box-title");
  private By rowValueBy = By.className("sparkline-list-value");
  private By startStopBy = By.className("start-stop");
  //
  private By breadCrumbBy = By.id("breadcrumb");
  private By statusBy = By.className("flow-state");
  private By injectFieldBy = By.id("flow-injector-input");
  private By logViewBy = By.id("logView");
  private By navPillsBy = By.className("nav-pills");
  
  public boolean isLogViewPresent() {
    return isElementPresent(logViewBy);
  }
  
  public WebElement getLogLink() {
    return getNavPills().findElements(By.tagName("a")).get(1);
  }
  
  public WebElement getNavPills() {
    return waitAndReturn(navPillsBy);
  }
  
  
  public WebElement getInjectField() {
    return waitAndReturn(injectFieldBy);
  }
  
  public WebElement getStartStop() {
    return waitAndReturn(startStopBy);
  }
  
  public WebElement getBreadCrumb() {
    return waitAndReturn(breadCrumbBy);
  }
  public boolean isBreadCrumbExist() {
    return isElementPresent(breadCrumbBy);
  }
  public WebElement getStatus() {
    return waitAndReturn(statusBy);
  }
 
  public boolean isStatusPresent() {
    return isElementPresent(statusBy);
  }
  public WebElement getPanel() {
    return waitAndReturn(panelBy);
  }
  
  public WebElement getTableFromDriver() {
    return waitAndReturn(tableBy);
  }
  
  public WebElement getNameApp(WebElement elem) {
    return elem.findElement(By.tagName("a"));
  }
  
  public WebElement getTableFromWebElem(WebElement elem) {
    return elem.findElement(tableBy);
  }
  public WebElement getTableFromWebElem(WebElement elem, int index) {
    return elem.findElements(tableBy).get(index);
  }
  
  public WebElement getStatusApp(WebElement td) {
    return td.findElement(statusAppBy);
  }
  public List<WebElement> getRows(WebElement table) {
    WebElement tbody = table.findElement(By.tagName("tbody"));
    return tbody.findElements(By.tagName("tr"));
  }
  
  public WebElement getTd(WebElement table, int rowInd, int tdInd) {
    WebElement row = this.getRows(table).get(rowInd);
    //System.out.println(row.getText());
    List<WebElement> tds = row.findElements(By.tagName("td"));
    return tds.get(tdInd);
  }

  public WebElement getContainerTitle(WebElement cont) {
    isElementPresent(cont, containerTitleBy);
    return cont.findElement(containerTitleBy);
  }
  public WebElement getContainerValue(WebElement cont) {
    this.isElementPresent(cont, containerValueBy);
    return cont.findElement(containerValueBy);
  }
  
  public WebElement getRowValue(WebElement td) {
    return td.findElement(rowValueBy);
  }
  
  public WebElement getNoContentDiv() {
    return waitAndReturn(noContentDivBy);
  }
  
  public WebElement getNoContentInElement(WebElement elem) {
    return elem.findElement(noContentDivBy);
  }
  public boolean isContainerTitle(WebElement cont) {
    return isElementPresent(cont, this.containerTitleBy);
  }
  
  public boolean isSVGPresent(WebElement cont) {
    return isElementPresent(cont, By.tagName("svg"));
  }
  
  public WebElement getAccount() {
    return waitAndReturn(accountBy);
  }

  public String getBaseUrl(){
    try {
      return getClass().getField("BASE_URL").get(null).toString();
    } catch (Exception e) {
      // never ever should be thrown as every page class has this field
      e.printStackTrace();
    }
    return BASE_URL;
  }
  public WebElement waitAndReturn(By by) {
    By param = by;
    isElementPresent(param);
    return globalDriver.findElement(param);
  }
  public WebElement getCancelBtn(WebElement div) {
    return div.findElement(cancelBtnBy);
  }

  public WebElement getCloseReset() {
    return waitAndReturn(closeResetBy);
  }

  public WebElement getCollect() {
    return waitAndReturn(collectBy);
  }

  public WebElement getContact() {
    return waitAndReturn(contactBy);
  }

  public WebElement getCopyright() {
    return waitAndReturn(copyrightBy);
  }
  
  public WebElement getH1() {
    return waitAndReturn(h1By);
  }
  
  public WebElement getDropHover() {
    try {
      return globalDriver.findElement(dropHoverBy); 
    } catch (StaleElementReferenceException ex) {
      System.out.println("removed from dom");
      WebElement elem = globalDriver.findElement(dropHoverBy);
      return elem;
    }
    
    //return globalDriver.findElement(dropHoverBy);
  }
  public WebElement getLogo() {
    return waitAndReturn(logoBy);
  }
  public WebElement getMetrics() {
    return waitAndReturn(metricsBy);
  }
  public WebElement getOkayBtn(WebElement div) {
    return div.findElement(okayBtnBy);
  }
  public WebElement getOverview() {
    return waitAndReturn(overviewBy);
  }

  public WebElement getProcess() {
    return waitAndReturn(processBy);
  }
  public WebElement getProductName() {
    return waitAndReturn(productNameBy);
  }
  public WebElement getQuery() {
    return waitAndReturn(queryBy);
  }
  public WebElement getReset() {
    return waitAndReturn(resetLinkBy);
  }

  public WebElement getResetDiv() {
    return waitAndReturn(resetDivBy);
  }

  public WebElement getStore() {
    return waitAndReturn(storeBy);
  }

  public WebElement getSupportFooter() {
    return waitAndReturn(supportFooterBy);
  }

  public WebElement getTerms() {
    return waitAndReturn(termsBy);
  }
  
  public boolean isAccountPresent() {
    return isElementPresent(accountBy);
  }

  public boolean isCollectPresent() {
    return isElementPresent(collectBy);
  }
 
  public boolean isContactPresent() {
    return isElementPresent(contactBy);
  }
  public boolean isPanelPresent() {
    return isElementPresent(panelBy);
  }
  
  public boolean isCopyrightPresent() {
    return isElementPresent(copyrightBy);
  }
  public boolean isH1Present() {
    return isElementPresent(h1By);
  }
  public boolean isNoContentDivPresent() {
    return isElementPresent(noContentDivBy);
  }
  
  public boolean isNoContentPresent(WebElement panel) {
    return isElementPresent(panel, noContentDivBy);
  }
  
  public boolean isElementPresent(By by){
    int count = 0; 
    while (count < 4){
      try {
        WebElement myDynamicElement = (new WebDriverWait(globalDriver, 15))
            //.ignoring(StaleElementReferenceException.class)
            .until(ExpectedConditions.presenceOfElementLocated(by));
        return true;
      } catch (TimeoutException e) {
        return false;
      } catch (StaleElementReferenceException e) {
        e.toString();
        System.out.println("Trying to recover from a stale element :" + e.getMessage());
        count = count + 1;
      }
      count = count + 4;
    }
    return true;
  }
  public boolean isElementPresent(WebElement elem, By by) {
    try {
      List<WebElement> myDynamicElement = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
      
    } catch (TimeoutException e) {
      return false;
    }
    try {
      elem.findElement(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }
  
  public boolean isFooterPresent() {
    return isElementPresent(footerBy);
  }

  //logo
  public boolean isLogoPresent() {
    return isElementPresent(logoBy);
  }

  //Metrics
  public boolean isMetricsPresent() {
    return isElementPresent(metricsBy);
  }

  //Overview
  public boolean isOverviewPresent() {
    return isElementPresent(overviewBy);
  }

  //Process
  public boolean isProcessPresent() {
    return isElementPresent(processBy);
  }
  //product-name
  public boolean isProductNamePresent() {
    return isElementPresent(productNameBy);
  }

  //Query
  public boolean isQueryPresent() {
    return isElementPresent(queryBy);
  }
  public boolean isResetCancelPresent() {
    return isElementPresent(this.getResetDiv(), this.cancelBtnBy);
  }
  public boolean isResetClosePresent() {
    return isElementPresent(closeResetBy);
  }
  public boolean isResetOkayPresent() {
    return isElementPresent(this.getResetDiv(), this.okayBtnBy);
  }
  public boolean isStartStopPresent() {
    return isElementPresent(startStopBy);
  }

  public boolean isResetPresent() {
    return isElementPresent(resetLinkBy);
  }
  public boolean isStorePresent() {
    return isElementPresent(storeBy);
  }
  public boolean isSupportFooterPresent() {
    return isElementPresent(supportFooterBy);
  }

  public boolean isTermsPresent() {
    return isElementPresent(termsBy);
  }
  public boolean isTablePresentInWebElement(WebElement elem) {
    return isElementPresent(elem, tableBy);
  }
  public static void checkingHover(final String condition) {
    try {
      Boolean message = (new WebDriverWait(globalDriver, 5))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              BasePage page = new BasePage();
              WebElement hover = page.getDropHover();
              return  (hover.getCssValue("display").equals(condition));
            }});
    } catch (TimeoutException e) {
      return;
      //e.printStackTrace();
    } 
  }  
  public boolean isHoverPresent() {
    return isElementPresent(dropHoverBy);
  }
  public boolean runReset() {
    HomePage homepage = new HomePage();
    globalDriver.get(homepage.BASE_URL);
    homepage.waitForZeroPresent();
    homepage.getReset().click();
    homepage.waitForVisibility("block");
    homepage.getOkayBtn(homepage.getResetDiv()).click();
    Global.driverWait(5);
    //BasePage.checkingHover("block");
    //BasePage.checkingHover("none");
    return homepage.isNoContentDivPresent();
  }
  public boolean uploadApp() {
 // make invisible input field to be visible
    HomePage page = new HomePage();
    WebElement elem = page.getInputField();
    JavascriptExecutor js = (JavascriptExecutor) globalDriver;
    String script = "document.getElementById('" + page.getInputId() + "').style.display = 'block';";
    js.executeScript(script);
    String path = Global.properties.getProperty("countRandomPath");
    File f = new File(path);
    path = f.getAbsolutePath();
    elem.sendKeys(path);
    BasePage.checkingHover("block");
    BasePage.checkingHover("none");
    return true;
  }
  public void waitForRunningSign(final String sign) {
    //try {
    System.out.println("Waiting for " + sign);
    try {
      Boolean message = (new WebDriverWait(globalDriver, 75))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              BasePage page = new BasePage();
              String text = page.getStatus().getText();
              return  (text.equals(sign));
            }});
    } catch (TimeoutException e) {
      fail("TimeOut, status" + sign + "is not present");
    }

  }
}
