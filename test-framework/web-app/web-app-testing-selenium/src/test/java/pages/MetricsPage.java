package pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import drivers.Global;
import static drivers.Global.*;
import static org.junit.Assert.fail;

/** MetricsPage is a page of Metrics.
 * BASE_URL - URL of this page
 * 
 * @author Elmira P.
 */
public class MetricsPage extends BasePage {
  public static final String BASE_URL = ROOT_URL + Global.properties.getProperty("metricsPage");
  private By noMetricsBy = By.className("object-list-empty");
  private By noMetricsTitleBy = By.className("object-list-empty-content");
  private By addDivBy = By.id("analyze-selected");
  private By addBtnBy = By.className("analyze-selected-metric-color");
  private By confDivBy = By.id("analyze-configurator");
  private By confCancelBy = By.className("analyze-gray-btn");
  private By confAddBy = By.className("analyze-blue-btn");
  private By pauseBtnBy = By.className("pause");
  private By selectElemBy = By.id("s2id_elementSelector");
  private By ul = By.className("select2-results");
  private By listBy = By.className("select2-result-unselectable");
  private By listMBy = By.className("select2-result-selectable");
  private By partTitleBy = By.className("select2-result-label");
  private By sublistBy = By.className("select2-result-sub");
  private By chosenBy = By.className("select2-chosen");
  private By selectMetricBy = By.id("s2id_metricSelector");
  private By rectBy = By.className("analyze-selected-metric-color");
  private By metricsTitleBy = By.className("analyze-selected-metric-element");
  private By metricsSubTitleBy = By.className("analyze-selected-metric-name");
  private By widgetBy = By.id("metrics-explorer-widget");
  private By closeMetricsBy = By.className("analyze-selected-metric-remove");
  
  public WebElement getMetricTitle() {
    return waitAndReturn(metricsTitleBy);
  }
  public WebElement getRemoveMetrics() {
    return waitAndReturn(closeMetricsBy);
  }
  public WebElement getWidget() {
    return waitAndReturn(widgetBy);
  }
  public WebElement getMetricSubTitle() {
    return waitAndReturn(metricsSubTitleBy);
  }
  
  public boolean isRectPresent() {
    return isElementPresent(rectBy);
  }
  
  public List<WebElement> getList() {
    WebElement ulElem = globalDriver.findElements(ul).get(1);
    return ulElem.findElements(listBy);
  }
  public List<WebElement> getListM() {
    WebElement ulElem = globalDriver.findElements(ul).get(1);
    return ulElem.findElements(listMBy);
  }
  public WebElement getPartTitle(int index) {
    return getList().get(index).findElement(partTitleBy);
  }
  public WebElement getSubListItem(List <WebElement> elem, int index) {
    
    return elem.get(index).findElement(sublistBy);
  }
  public WebElement getSelectElem() {
    return waitAndReturn(selectElemBy);
  }
  public WebElement getSelectMetric() {
    return waitAndReturn(selectMetricBy);
  }
  public WebElement getChosenElem(int index) {
    return globalDriver.findElements(chosenBy).get(index);
  }
  public WebElement getPauseBtn() {
    return waitAndReturn(pauseBtnBy);
  }
  public WebElement getConfAdd() {
    return waitAndReturn(confAddBy);
  }
  public WebElement getConfCancel() {
    return waitAndReturn(confCancelBy);
  }
  public WebElement getConfDiv() {
    return waitAndReturn(confDivBy);
  }
  public By getConfDivBy() {
    return confDivBy;
  }
  public WebElement getAddBtn() {
    return waitAndReturn(addBtnBy);
  }
  public WebElement getAddDiv() {
    return waitAndReturn(addDivBy);
  }
  public WebElement getNoMetrics() {
    return waitAndReturn(noMetricsBy);
  }
  public boolean isAddDivPresent() {
    return isElementPresent(addDivBy);
  }
  public boolean isNoMetricsPresent() {
    return isElementPresent(noMetricsBy);
  }
  public boolean isConfCancelPresent() {
    return isElementPresent(confCancelBy);
  }
  public boolean isAddBtnPresent() {
    return isElementPresent(addBtnBy);
  }
  public boolean isConfAddPresent() {
    return isElementPresent(confAddBy);
  }
  public WebElement getNoMetricsTitle() {
    WebElement elem = waitAndReturn(noMetricsTitleBy);
    
    return elem.findElement(By.tagName("div"));
  }
  public boolean isPausePresent() {
    return isElementPresent(pauseBtnBy);
  }
  public void confDiv(final String condition) {
    try {
      Boolean message = (new WebDriverWait(globalDriver, 100))
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              MetricsPage page = new MetricsPage();
              WebElement conf = page.getConfDiv();
              return  (conf.getCssValue("display").equals(condition));
            }});
    } catch (NoSuchElementException e) {
      fail("Not visible");
      e.printStackTrace();
    } 
  }
}
