package pages;

import static drivers.Global.*;
import static org.junit.Assert.fail;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import drivers.Global;

/** Store Page is store page of sdk.
 * 
 * BASE_URL - URL of this page
 * @author Elmira P. 
 */
public class StorePage extends BasePage {
  public static final String BASE_URL = ROOT_URL + Global.properties.getProperty("storePage");
  
  public void waitForProcessNumbersPresent() {
    System.out.println("Waiting for Process numbers to appear");
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              StorePage page = new StorePage();
              WebElement table = page.getTableFromWebElem(page.getPanel());
              WebElement td1 = page.getTd(table, 0, 1);
              String text = td1.getText();
              return  (text.length() > 2);
            }});
    } catch (TimeoutException e) {
      fail("TimeOut, process number is not 0");
    } 
  }  
  
}
