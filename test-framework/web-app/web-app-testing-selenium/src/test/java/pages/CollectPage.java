package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import drivers.Global;
import static drivers.Global.*;

/** Collect page is subclass of BasePage.
 * BASE_URL - URL of this page
 * 
 * @author Elmira P.
 */
public class CollectPage extends BasePage {
  public static final String BASE_URL = ROOT_URL + Global.properties.getProperty("collectPage");
  private By dropDownBy = By.className("dropdown-toggle");
 
  public WebElement getDropDown() {
    return waitAndReturn(dropDownBy);
  }
  public boolean isDropDownPresent() {
    return isElementPresent(dropDownBy);
  }

}
