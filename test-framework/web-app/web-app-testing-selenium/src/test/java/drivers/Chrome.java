package drivers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/** Driver is a class for chrome browser.
 * executable binary file for driver in resources directory
 * if it throw mistake that is not executable check for mode of the file
 * and chmod it to be executable 
 * chmod a+x file
 * @author elmira
 *
 */
public class Chrome extends Driver {
  public Chrome() {

  }
  public WebDriver getDriver() {
    if (driver == null) {
      if (Global.OS_VERSION == Global.OS.MAC_OS) {
        String path = Global.properties.getProperty("chromeMac");
        System.setProperty("webdriver.chrome.driver", path);
      } else {
        String path = Global.properties.getProperty("chromeLinux");
        System.setProperty("webdriver.chrome.driver", path);
      }
      driver = new ChromeDriver();
    }
    return driver;
  }

}
