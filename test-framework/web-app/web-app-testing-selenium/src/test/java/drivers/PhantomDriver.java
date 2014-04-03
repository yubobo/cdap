package drivers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

/** Driver is a class for phantom browser (headless without GUI).
 * executable binary file for driver in resources directory
 * if it throw mistake that is not executable check for mode of the file
 * and chmod it to be executable 
 * chmod a+x file
 * @author elmira
 *
 */
public class PhantomDriver extends Driver {
  public PhantomDriver () {

  }
  public WebDriver getDriver() {
    String path = Global.properties.getProperty("phantomMac");
    System.setProperty("phantomjs.binary.path", "resources/phantomjs/bin/phantomjs");
    driver = new CustomPhantomJSDriver();
    return this.driver;

  }
}
