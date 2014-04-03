package pages;

import drivers.Global;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import static drivers.Global.globalDriver;
import static org.junit.Assert.fail;

/** AppPage is class for pages that appear after link of the name of application.
 * 
 * @author elmira
 *
 */
public class AppPage extends BasePage {
  private By panelTitleBy = By.className("panel-title");
  private By processedContainerBy = By.className("app-processed");
  private By busynessContainerBy = By.className("app-busyness");
  private By storageContainerBy = By.className("app-storage");
  private By collectPanelBy = By.id("collect-panel");
  private By processPanelBy = By.id("process-panel");
  private By storePanelBy = By.id("store-panel");
  private By queryPanelBy = By.id("query-panel");
  private By startBy = By.className("start");
  private By stopBy = By.className("stop");
  
  public WebElement getStart() {
    return waitAndReturn(startBy);
  }
  public WebElement getStop() {
    return waitAndReturn(stopBy);
  }
  private int appCollect, appProcess, appStore, appQuery;
  public int getAppCollect() {
    return appCollect;
  }
  public int getAppProcess() {
    return appProcess;
  }

  public int getAppQuery() {
    return appQuery;
  }
  public int getAppStore() {
    return appStore;
  }
      
  public WebElement getBusynessContainer() {
    return waitAndReturn(busynessContainerBy);
  }
  public WebElement getCollectPanel() {
    return waitAndReturn(collectPanelBy);
  }
  
  public WebElement getPanelTitle(WebElement panel) {
    this.isElementPresent(panel, panelTitleBy);
    return panel.findElement(panelTitleBy);
  }
  
  public WebElement getProcessedContainer() {
    return waitAndReturn(processedContainerBy);
  }

  public WebElement getProcessPanel() {
    return waitAndReturn(processPanelBy);
  }
  public WebElement getQueryPanel() {
    return waitAndReturn(queryPanelBy);
  }
  
  public WebElement getStorageContainer() {
    return waitAndReturn(this.storageContainerBy);
  }
  
  public WebElement getStorePanel() {
    return waitAndReturn(storePanelBy);
  }
  
  public boolean isBusynessContainer() {
    return isElementPresent(this.busynessContainerBy);
  }
  public boolean isCollectPanelPresent() {
    return isElementPresent(collectPanelBy);
  }
  
  public boolean isProcessContainer() {
    return isElementPresent(this.processedContainerBy);
  }
  public boolean isProcessPanelPresent() {
    return isElementPresent(processPanelBy);
  }
  public boolean isQueryPanelPresent() {
    return isElementPresent(queryPanelBy);
  }
  public boolean isStorageContainer() {
    return isElementPresent(storageContainerBy);
  }
  public boolean isStorePanelPresent() {
    return isElementPresent(storePanelBy);
  }
  public boolean isStartPresent(WebElement elem) {
    return isElementPresent(elem, startBy);
  }
  public boolean isStopPresent(WebElement elem) {
    return isElementPresent(elem, stopBy);
  }
  
  public void waitForProcessNumbersPresent() {
    AppPage page = new AppPage();
    System.out.println("Waiting for Process numbers to appear");
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              AppPage page = new AppPage();
              WebElement process = page.getProcessedContainer();
              WebElement value = page.getContainerValue(process);
              String text = value.getText();
             // System.out.println(text);
              return  (text.length() > 3);
            }});
    } catch (TimeoutException e) {
      fail("TimeOut, process number is not 0");
    } 
  }  
  public void waitForStorage() {
    System.out.println("Waiting for numbers to appear in storage");
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              AppPage page = new AppPage();
              WebElement store = page.getStorageContainer();
              WebElement value = page.getContainerValue(store);
              String text = value.getText();
              return  (text.length() > 2);
            }
          });
    } catch (TimeoutException e) {
      fail("TimeOut, storage's numbers are not appearing");
    } 
  }
  public void waitForStorageNumbersPresent() {
    System.out.println("Waiting for numbers to appear in storage");
    AppPage page = new AppPage();
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              AppPage page = new AppPage();
              WebElement store = page.getStorageContainer();
              WebElement value = page.getContainerValue(store);
              String text = value.getText();
              return  (text.equals("0"));
            }});
    } catch (TimeoutException e) {
      fail("TimeOut, storage's number is not 0");
    } 
  }  
  public void waitForTitlePresent() {
    try {
      System.out.println("Waiting for process title to appear");
      WebElement message = (new WebDriverWait(globalDriver, 100))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<WebElement>(){
            public WebElement apply(WebDriver d) {
              AppPage page = new AppPage();
              return  (page.getContainerTitle(page.getProcessedContainer()));
            }});
    } catch (TimeoutException e) {
      fail("TimeOut, process title dind't appear");
      e.printStackTrace();
    } 
  }  
  public void waitForStopping() {
    System.out.println("Waiting for stopping");
    final String statusToMatch = Global.properties.getProperty("statusStopped");
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              AppPage page = new AppPage();
              WebElement table = page.getTableFromWebElem(page.getProcessPanel());
              WebElement td1 = page.getTd(table, 0, 0);
              String status = page.getStatusApp(td1).getText();
              return  (status.equals(statusToMatch));
            }});
    } catch (TimeoutException e) {
      fail("Time out, Status is not becoming '" + statusToMatch + "'");
    }
  }
  public void waitForBusynessRateIsZero() {
    System.out.println("Waiting for busyness rate is zero");
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              AppPage page = new AppPage();
              WebElement table = page.getTableFromWebElem(page.getProcessPanel());
              WebElement td1 = page.getTd(table, 0, 2);
              String text = td1.getText();
              return  (text.equals("0"));
            }});
    } catch (TimeoutException e) {
      fail("Time out, Processing rate is not zero!");
    }
  }
  public void waitForWriteRateIsZero() {
    System.out.println("Waiting for write rate is zero");
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
        .ignoring(StaleElementReferenceException.class)
        .until(new ExpectedCondition<Boolean>(){
          public Boolean apply(WebDriver d) {
            AppPage page = new AppPage();
            WebElement table = page.getTableFromWebElem(page.getStorePanel());
            WebElement td1 = page.getTd(table, 0, 1);
            String text = td1.getText();
            return  (text.equals("0"));
          }});
    } catch (TimeoutException e) {
      fail("Time out, Processing rate is not zero!");
    }
  }
  public void waitForProcessingRateIsZero() {
    System.out.println("Waiting for processing rate is zero");
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
        .ignoring(StaleElementReferenceException.class)
        .until(new ExpectedCondition<Boolean>(){
          public Boolean apply(WebDriver d) {
            AppPage page = new AppPage();
            WebElement table = page.getTableFromWebElem(page.getProcessPanel());
            WebElement td1 = page.getTd(table, 0, 1);
            String text = td1.getText();
            return  (text.equals("0"));
          }});
    } catch (TimeoutException e) {
      fail("Time out, Processing rate is not zero!");
    }
  }
  public void waitForProcessingRateIsNotZero() {
    System.out.println("Waiting for processing rate is not zero");
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              AppPage page = new AppPage();
              WebElement table = page.getTableFromWebElem(page.getProcessPanel());
              WebElement td1 = page.getTd(table, 0, 1);
              String text = td1.getText();
              return  (!text.equals("0"));
            }});
    } catch (TimeoutException e) {
      fail("Time out, Processing rates are not changing after we pressed 'Start'");
    }
  }
  
}
