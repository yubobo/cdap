package drivers;

import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

/**
 * Custom driver for avoiding exception.
 * @author elmira
 *
 */
  public class CustomPhantomJSDriver extends PhantomJSDriver {

    @Override
    public void get(String url) {
        int count = 0;
        int maxTries = 10;
        while (count < maxTries) {
            try {
                super.get(url);
                break;
            } catch (UnreachableBrowserException e) {
                count++;
            }
        }
        if (count == maxTries) {
            throw new UnreachableBrowserException(url);
        }
    }
}
