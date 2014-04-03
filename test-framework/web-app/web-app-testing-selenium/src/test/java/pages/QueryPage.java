package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import drivers.Global;
import static drivers.Global.*;
import static org.junit.Assert.fail;

/** QueryPage query page of sdk.
 * 
 * BASE_URL - URL of this page
 * @author Elmira P.
 ***************************/
public class QueryPage extends BasePage {
  public static final String BASE_URL = ROOT_URL + Global.properties.getProperty("queryPage");
  private By requestsBy = By.className("requests");
  private By failuresBy = By.className("failures");
  private By queryBtnBy = By.className("query-run");
  private By queryExBtnBy = By.className("query-execute");
  private By responseBy = By.className("query-response");
  
  
  public WebElement getRequests() {
    return waitAndReturn(requestsBy);
  }
  public WebElement getResponse() {
    return waitAndReturn(responseBy);
  }
  public WebElement getInputField(WebElement elem) {
    return elem.findElement(By.tagName("input"));
  }
  public WebElement getFailed() {
    return waitAndReturn(failuresBy);
  }
  public WebElement getQueryBtn() {
    return waitAndReturn(queryBtnBy);
  }
  public WebElement getQueryExBtn() {
    return waitAndReturn(queryExBtnBy);
  }
  public boolean isRequestsPresent() {
    return isElementPresent(requestsBy);
  }
  public boolean isFailedPresent() {
    return isElementPresent(failuresBy);
  }
  public boolean isQueryBtnPresent() {
    return isElementPresent(queryBtnBy);
  }
  public boolean isQueryExBtnPresent() {
    return isElementPresent(queryExBtnBy);
  }
  public void waitForResponce() {
    try {
      Boolean message = (new WebDriverWait(globalDriver, 100))
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              QueryPage page = new QueryPage();
              return  (page.getResponse().getAttribute("value").length() > 0);
            }});
    } catch (NoSuchElementException e) {
      fail("Not visible");
      e.printStackTrace();
    } 
  }

}
