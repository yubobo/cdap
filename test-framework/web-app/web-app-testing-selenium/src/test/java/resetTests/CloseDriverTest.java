package resetTests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static drivers.Global.globalDriver;

/**
 * Dummy test for closing the driver
 * @author elmira
 *
 */
public class CloseDriverTest {
  @BeforeClass
  public static void setUp() {
  }
  @Test
  public void testClosing() {
    System.out.println("Closing the driver, buy-buy");
  }
  @AfterClass
  public static void tearDown() {
    if (globalDriver != null) {
      globalDriver.quit();
      globalDriver = null;
    }
  }
  

}
