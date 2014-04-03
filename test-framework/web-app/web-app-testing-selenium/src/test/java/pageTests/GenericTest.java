package pageTests;


import drivers.Global;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;

import static drivers.Global.driverWait;
import static drivers.Global.globalDriver;
import static drivers.Global.waitForLoading;

/** Generic Test is parent object for all tests classes
 * Created for common helpers methods
 * @author elmira
 *
 */
public class GenericTest {
  
  public String switchToNewTab(WebElement elemForClick, String url) {
    String oldTab = globalDriver.getWindowHandle();
    elemForClick.click();
    driverWait(5);
    ArrayList<String> newTab = new ArrayList<String>(globalDriver.getWindowHandles());
    newTab.remove(oldTab);
    // change focus to new tab
    globalDriver.switchTo().window(newTab.get(0));
    String newUrl = globalDriver.getCurrentUrl();
    globalDriver.close();
    // change focus back to old tab
    globalDriver.switchTo().window(oldTab);
    waitForLoading(url);
    
    return newUrl;
  }
  public static void closeDriver() {
    if (!Global.RUN_SUITE) {
      globalDriver.close();
      globalDriver.quit();
      globalDriver = null;
    }
  }

}
