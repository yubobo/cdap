package drivers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

/** Driver is a class for firefox driver.
 * doesn't need binary file, built in firefox browser
 * @author elmira
 *
 */
public class FireFox extends Driver {
  public FireFox() {

  }
  public WebDriver getDriver() {
    if (driver == null) {
      driver = new FirefoxDriver();
    }
    return driver;
  }
}
