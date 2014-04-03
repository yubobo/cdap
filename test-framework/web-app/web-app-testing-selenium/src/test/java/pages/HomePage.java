package pages;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import drivers.Global;
import static drivers.Global.*;
import static org.junit.Assert.fail;

/** HomePage is homepage of sdk.
 * 
 * BASE_URL - URL of this page
 * By inputFieldBy is hidden ("app-upload-input") hardcoded to be visible to input name of file
 * @author Elmira P.
 */
public class HomePage extends BasePage {
  public static final String BASE_URL = ROOT_URL + Global.properties.getProperty("overviewPage");
  private By loadAnAppBy = By.id("load-app-trigger");
  
  private By titleAppBy = By.className("panel-title");
  private String inputId = "app-upload-input";
  private By inputFieldBy = By.id(inputId);
  private By quarter1By = By.id("quarter1");
  private By quarter2By = By.id("quarter2");
  private By quarter3By = By.id("quarter3");
  private By quarter4By = By.id("quarter4");
  private By quarterTitleBy = By.className("quarter-title");
  private By quarterValueBy = By.className("value-number");
  private By quarterMeasureBy = By.className("value-measure");
  private By dropDownBy = By.className("dropdown-toggle");
  private By busynessBy = By.className("sparkline-list-value");
  private By appSvgContainerBy = By.className("sparkline-list-container");
  
  public WebElement getBusyness() {
    return waitAndReturn(busynessBy);
  }
  private WebElement cellAppLink;
  List<WebElement> quarters;
  public int countQuarters() {
    return quarters.size();
  }
  public WebElement getCellAppLink() {
    return cellAppLink;
  }
 
  public WebElement getDropDown() {
    return globalDriver.findElement(dropDownBy);
  }
  
  public WebElement getInputField() {
    return globalDriver.findElement(inputFieldBy);
  }
  
  public String getInputId() {
    return inputId;
  }
  public WebElement getLoadAnApp() {
    return waitAndReturn(loadAnAppBy);
  }
  
  public WebElement getQuarter1() {
    return waitAndReturn(quarter1By);
  }
  public WebElement getQuarter2() {
    return waitAndReturn(quarter2By);
  }
  public WebElement getQuarter3() {
    return waitAndReturn(quarter3By);
  }
  
  public WebElement getQuarter4() {
    return waitAndReturn(quarter4By);
  }
  
  public By getQuarterTitleBy() {
    return quarterTitleBy;
  }
  public WebElement getTitleApp() {
    return waitAndReturn(titleAppBy);
  }
  
  public boolean isDropDownPresent() {
    return isElementPresent(dropDownBy);
  }
  public boolean isLoadAppPresent() {
    return isElementPresent(loadAnAppBy);
  }
  
  public boolean isQuarterHasSVG(WebElement quarter) {
    return isElementPresent(quarter, By.tagName("svg"));
  }
  
  public boolean isTitleAppPresent() {
    return isElementPresent(titleAppBy);
  }
  public boolean isBusinessPresent() {
    return isElementPresent(busynessBy);
  }
  public WebElement getAppSvgContainerBy() {
    return waitAndReturn(appSvgContainerBy);
  }
  public boolean isAppSVGpresent() {
    return isElementPresent(this.getAppSvgContainerBy(), By.tagName("svg"));
  }
  public void quarters() {
    quarters = globalDriver.findElements(By.className("quarter"));
  }
  public WebElement quarterValue(WebElement quarter) {
    return quarter.findElement(quarterValueBy);
  }
  public WebElement quarterValueAttr(WebElement quarter) {
    return quarter.findElement(quarterMeasureBy);
  }
  public void setCellAppLink(WebElement cellAppLink) {
    this.cellAppLink = cellAppLink;
  }
  public void waitForVisibility(final String condition) {
    try {
      Boolean message = (new WebDriverWait(globalDriver, 5))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              HomePage page = new HomePage();
              WebElement resetDiv = page.getResetDiv();
              return  (resetDiv.getCssValue("display").equals(condition));
            }});
    } catch (NoSuchElementException e) {
      fail("Not visible");
      e.printStackTrace();
    } 
  }    
  public void waitForZeroPresent() {
    try {
      Boolean message = (new WebDriverWait(globalDriver, 25))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              HomePage page = new HomePage();
              WebElement quarter1 = page.getQuarter1();
              WebElement value = page.quarterValue(quarter1);
              String text = value.getText();
              return  (text.equals("0"));
            }});
    } catch (NoSuchElementException e) {
      fail("Element not found!!");
      e.printStackTrace();
    } 
  }

  public void waitForBuzynessZeroPresent() {
    System.out.println("Waiting for numbers to appear in buzyness");
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              HomePage page = new HomePage();
              String text = page.getBusyness().getText();
              return  (text.length() > 1);
            }});
    } catch (TimeoutException e) {
      fail("TimeOut, element not find");
    } 
  }
  public void uploadAnApp(String nameApp) {
    WebElement elem = this.getInputField();
    JavascriptExecutor js = (JavascriptExecutor) globalDriver;
    String script = "document.getElementById('" + this.getInputId() + "').style.display = 'block';";
    js.executeScript(script);
    String path = Global.properties.getProperty(nameApp);
    File f = new File(path);
    path = f.getAbsolutePath();
    elem.sendKeys(path);
  }
  
}
