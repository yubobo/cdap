package pages;

import static drivers.Global.globalDriver;
import static org.junit.Assert.fail;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** FlowPage is class for concrete app flows. 
 * 
 * @author elmira
 *
 */
public class DatasetsPage extends AppPage {
  private By breadCrumbBy = By.id("breadcrumb");
  private By writeContainerBy = By.className("write-rate");
  private By readContainerBy = By.className("read-rate");
  
  public WebElement getBreadCrumb() {
    return waitAndReturn(breadCrumbBy);
  }
  public boolean isBreadCrumbExist() {
    return isElementPresent(breadCrumbBy);
  }
  public WebElement getWriteContainer() {
    return waitAndReturn(writeContainerBy);
  }
  public WebElement getReadContainer() {
    return waitAndReturn(readContainerBy);
  }
  public boolean isWritePresent() {
    return isElementPresent(writeContainerBy);
  }
  public boolean isReadPresent() {
    return isElementPresent(readContainerBy);
  }
  public void waitForWriteNumbersPresent() {
    //try {
    System.out.println("Waiting for numbers to appear in write rate");
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              DatasetsPage page = new DatasetsPage();
              WebElement write = page.getWriteContainer();
              WebElement value = page.getContainerValue(write);
              String text = value.getText();
              return  (text.length() > 3);
            }});
    } catch (TimeoutException e) {
      fail("TimeOut, element not find");
    } 
  }  
  
  
}

