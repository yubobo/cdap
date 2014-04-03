package pages;

import static drivers.Global.globalDriver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import drivers.Global;

/** FlowPage is class for concrete app flows. 
 * 
 * @author elmira
 *
 */
public class FlowPage extends AppPage {
  
  private By sourceBy = By.id("flowletsource");
  private By splitterBy = By.id("flowletsplitter");
  private By counterBy = By.id("flowletcounter");
  private By eyedropBy = By.className("window-input");
  private By eyeDropValueBy = By.className("window-input-label");
  private By instanceBy = By.className("window-instances");
  private By pictTitleBy = By.className("window-title");
  private By pictIconBy = By.className("window-icon");
  private By arrowBy = By.className("dropdown-toggle");
  private By actionButtonsBy = By.id("action-buttons");
  private By flowizBy = By.className("flowviz-fade");
  private By historyTable = By.className("table-condensed");
  private By popupBy = By.className("popup-container");
  private By popupH1By = By.className("popup-title");
  private By inputLinkBy = By.id("flowlet-popup-inputs-tab");
  private By processedLinkBy = By.id("flowlet-popup-processed-tab");
  private By outputsLinkBy = By.id("flowlet-popup-outputs-tab");
  private By outputContainerBy = By.id("flowlet-popup-outputs");
  private By proceedContainerBy = By.id("flowlet-popup-processed");
  private By inputContainerBy = By.id("flowlet-popup-inputs");
  private By noContentBy = By.className("flowlet-popup-empty");
  private By tableInTabsBy = By.tagName("table");
  private By tdTitleBy = By.className("sparkline-flowlet-title");
  private By tdValueBy = By.className("sparkline-flowlet-value");
  private By closeFlowletBy = By.className("popup-close");
  private By batchMapBy = By.id("batch-map");
  private By batchReduceBy = By.id("batch-reduce");
  private By mappingBy = By.className("mapping");
  private By reducingBy = By.className("reducing");
  private By flowletwhoBy = By.id("flowletwho");
  private By flowletsaverBy = By.id("flowletsaver");
  private By flowRunBy = By.id("workflow-next-run");
  private By injectBtnBy = By.className("btn");
  private By tickTokBy = By.className("flow-schedule");
  private By builderBy = By.id("PurchaseHistoryBuilder");
  
  public WebElement getFlowRun() {
    return waitAndReturn(flowRunBy);
  }
  
  public WebElement getBuilder() {
    return waitAndReturn(builderBy);
  }
  
  public WebElement getTickTok() {
    return waitAndReturn(tickTokBy);
  }
  
  public WebElement getInjectBtn() {
    WebElement popup = this.getPopup();
    return popup.findElement(injectBtnBy);
  }
  
  public WebElement getCloseFlowlet() {
    return waitAndReturn(closeFlowletBy);
  }
  public WebElement getSaver() {
    return waitAndReturn(flowletsaverBy);
  }
  public WebElement getFlowletwho() {
    return waitAndReturn(flowletwhoBy);
  }
  public WebElement getBatchMap() {
    return waitAndReturn(batchMapBy);
  }
  public WebElement getBatchReduce() {
    return waitAndReturn(batchReduceBy);
  }
  public WebElement getProceedContainer() {
    return waitAndReturn(proceedContainerBy);
  }
  public WebElement getMapping() {
    return waitAndReturn(mappingBy);
  }
  public WebElement getReducing() {
    return waitAndReturn(reducingBy);
  }
  public WebElement getTableProcessed() {
    WebElement outsideTable = this.getProceedContainer().findElement(tableInTabsBy);
    //WebElement insideTable = outsideTable.findElement(tableInTabsBy);
    return outsideTable;
  }
  public WebElement getOutputContainer() {
    return waitAndReturn(outputContainerBy);
  }
  public WebElement getInputContainer() {
    return waitAndReturn(inputContainerBy);
  }
  public WebElement getOutputTable() {
    WebElement outsideTable = this.getOutputContainer().findElement(tableInTabsBy);
    WebElement insideTable = outsideTable.findElement(tableInTabsBy);
    return insideTable;
  }
  public WebElement getOutputTd(int index) {
    return this.getOutputTable().findElements(By.tagName("td")).get(index);
  }
  public WebElement getInputTable() {
    WebElement outsideTable = this.getInputContainer().findElement(tableInTabsBy);
    WebElement insideTable = outsideTable.findElement(tableInTabsBy);
    return insideTable;
  }
  public WebElement getInputTd(int index) {
    return this.getInputTable().findElements(By.tagName("td")).get(index);
  }
  public WebElement getInputTitle(int index) {
    return getInputTd(index).findElement(tdTitleBy);
  }
  public WebElement getInputValue(int index) {
    return getInputTd(index).findElement(tdValueBy);
  }
  
  public WebElement getProcessedTd(int index) {
    return this.getTableProcessed().findElements(By.tagName("td")).get(index);
  }
  public WebElement getTdTitle(int index) {
    return getProcessedTd(index).findElement(tdTitleBy);
  }
  public WebElement getTdValue(int index) {
    return getProcessedTd(index).findElement(tdValueBy);
  }
  
  public WebElement getOutputTitle(int index) {
    return getOutputTd(index).findElement(tdTitleBy);
  }
  public WebElement getOutputValue(int index) {
    return getOutputTd(index).findElement(tdValueBy);
  }
  
  public WebElement getNoContent() {
    return waitAndReturn(noContentBy);
  }

  public WebElement getInputLink() {
    return waitAndReturn(inputLinkBy);
  }
  
  public WebElement getProcessedLink() {
    return waitAndReturn(processedLinkBy);
  }

  public WebElement getOutputsLink() {
    return waitAndReturn(outputsLinkBy);
  }
  
  public WebElement getPopupH1() {
    return waitAndReturn(popupH1By);
  }

  public WebElement getPopup() {
    return waitAndReturn(popupBy);
  }

  public WebElement getHistoryTable() {
    return waitAndReturn(historyTable);
  }
  public int countRowHistoryTable() {
    WebElement tb = getHistoryTable().findElement(By.tagName("tbody"));
     return tb.findElements(By.tagName("tr")).size();
  }
  public WebElement getStatusLink() {
    return getNavPills().findElements(By.tagName("a")).get(0);
  }
  
  public WebElement getHistoryLink(int index) {
    return getNavPills().findElements(By.tagName("a")).get(index);
  }
  
  public WebElement getActionButtons() {
    return waitAndReturn(actionButtonsBy);
  }
  private By dropDownBy = By.className("dropdown-menu");

  public WebElement getDropDown() {
    System.out.println(this.getActionButtons().getText());
    return this.getActionButtons().findElement(dropDownBy);
  }
  public WebElement getArrow() {
    return waitAndReturn(arrowBy);
  }
  public WebElement getPictIcon(WebElement elem) {
    return elem.findElement(pictIconBy);
  }
  public WebElement getPictTitle(WebElement elem) {
    return elem.findElement(pictTitleBy);
  }
  public WebElement getInstance(WebElement elem) {
    return elem.findElement(instanceBy);
  }
  public WebElement getEyedropBy(WebElement elem) {
    return elem.findElement(eyedropBy);
  }
  public WebElement getPictValue(WebElement elem) {
    return elem.findElement(eyeDropValueBy);
  }
  public WebElement getSource() {
    return waitAndReturn(sourceBy);
  }
  public WebElement getSplitter() {
    return waitAndReturn(splitterBy);
  }
  public WebElement getCounter() {
    return waitAndReturn(counterBy);
  }
  public boolean isSourcePresent() {
    return isElementPresent(sourceBy);
  }
  public boolean isSplitterPresent() {
    return isElementPresent(splitterBy);
  }
  public boolean isCounterPresent() {
    return isElementPresent(counterBy);
  }
  public boolean isEyeDropPresent(WebElement parent) {
    return isElementPresent(parent, eyedropBy);
  }
  public boolean isInstancePresent(WebElement parent) {
    return isElementPresent(parent, instanceBy);
  }
  public boolean isPictTitlePresent(WebElement parent) {
    return isElementPresent(parent, pictTitleBy);
  }
  public boolean isPictIconPresent(WebElement parent) {
    return isElementPresent(parent, pictIconBy);
  }
  public boolean isArrowPresent() {
    return isElementPresent(arrowBy);
  }
  public boolean isDropDownPresent() {
    return isElementPresent(getActionButtons(), dropDownBy);
  }
  
  public boolean isFlowFadePresent() {
    return isElementPresent(flowizBy);
  }
  public boolean isPopupPresent() {
    return isElementPresent(popupBy);
  }
  public boolean isPopupH1Present() {
    return isElementPresent(popupH1By);
  }
  public boolean isInputLinkPresent() {
    return isElementPresent(inputLinkBy);
  }
  public boolean isProcessedLinkPresent() {
    return isElementPresent(processedLinkBy);
  }
  public boolean isOutputsLinkPresent() {
    return isElementPresent(outputsLinkBy);
  }
  public boolean isNoInputsPresent() {
    return isElementPresent(noContentBy);
  }
  public boolean isTableProcessedPresent() {
    return isElementPresent(tableInTabsBy);
  }
  public boolean isMappingPresent() {
    return isElementPresent(mappingBy);
  }
  public boolean isReducingPresent() {
    return isElementPresent(reducingBy);
  }
  public boolean isFlowletwhoPresent() {
    return isElementPresent(flowletwhoBy);
  }
  public boolean isSaverPresent() {
    return isElementPresent(flowletsaverBy);
  }
  public boolean isFlowRunPresent() {
    return isElementPresent(flowRunBy);
  }
  public boolean isBuilderPresent() {
    return isElementPresent(builderBy);
  }
 
  public void waitForH1match(final String sign) {
    //try {
    System.out.println("Waiting for " + sign);
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              FlowPage page = new FlowPage();
              String text = page.getH1().getText();
              return  (text.equals(sign));
            }});
    } catch (TimeoutException e) {
      fail("TimeOut, H1 is incorrect");
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
      fail("TimeOut, processing rate numbers are not changing");
    }
  }
  public void waitForRunningProcess() {
    System.out.println("Waiting for running numbers in process");
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              FlowPage page = new FlowPage();
              WebElement process = page.getProcessedContainer();
              WebElement value = page.getContainerValue(process);
              String text = value.getText();
              return  (text.length() > 6);
            }});
    } catch (TimeoutException e) {
      fail("TimeOut, processed numbers are not changing");
    } 
  }
  public void waitForSourceValueNumbersPresent() {
    System.out.println("Waiting for numbers to appear in source");
    FlowPage page = new FlowPage();
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              FlowPage page = new FlowPage();
              String text = page.getPictValue(page.getSource()).getText();
              return  (text.equals("0"));
            }});
    } catch (TimeoutException e) {
      fail("TimeOut, numbers are not apper in the source");
    } 
  }  
  public void waitForProcessNumbersIsZero() {
    System.out.println("Waiting for Process numbers is zero");
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              FlowPage page = new FlowPage();
              WebElement process = page.getProcessedContainer();
              WebElement value = page.getContainerValue(process);
              String text = value.getText();
              Scanner scanner = new Scanner(text);
              String firstLine = scanner.nextLine();
              //System.out.println(text);
              return  (firstLine.equals("0"));
            }});
    } catch (TimeoutException e) {
      fail("TimeOut, processed numbers are not zero");
    } 
  } 
  public void waitFoEventsIsNotZero() {
    System.out.println("Waiting for events number is not a  zero");
    try {
      Boolean message = (new WebDriverWait(globalDriver, 15))
          .ignoring(StaleElementReferenceException.class)
          .until(new ExpectedCondition<Boolean>(){
            public Boolean apply(WebDriver d) {
              FlowPage page = new FlowPage();
              WebElement table = page.getTableFromWebElem(page.getPopup());
              WebElement events = page.getTd(table, 0, 1);
              Scanner scanner = new Scanner(events.getText());
              int number = Integer.parseInt(scanner.nextLine());
              return  (number > 0);
            }});
    } catch (TimeoutException e) {
      fail("TimeOut, events numbers are zero");
    } 
  } 
  
  
 }

