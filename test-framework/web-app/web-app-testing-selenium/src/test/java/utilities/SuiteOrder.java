package utilities;
/**
 * SuiteOrder is class managing order of execution of tests.
 * @author elmira
 *
 */
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import pageTests.CollectPageTests;
import pageTests.HomePageTests;
import pageTests.MetricsPageTests;
import pageTests.ProcessPageTests;
import pageTests.QueryPageTests;
import pageTests.StorePageTests;
import commonSanityTests.FooterSanityTests;
import commonSanityTests.HeaderSanityTests;
import commonSanityTests.LeftPanelSanityTests;
import drivers.Global;
import resetTests.CloseDriverTest;
import resetTests.ResetTest;
import testsForExamples.CountRandom.CountRandom_03_ActionsTests;
import testsForExamples.CountRandom.CountRandom_02_AppPageTests;
import testsForExamples.CountRandom.CountRandom_05_DatasetsTests;
import testsForExamples.CountRandom.CountRandom_04_ProcessTests;
import testsForExamples.CountRandom.CountRandom_01_UploadTests;
import testsForExamples.CountRandom.CountRandom_06_FlowletsSourceTests;
import testsForExamples.CountRandom.CountRandom_07_FlowletsSplitterTests;
import testsForExamples.CountRandom.CountRandom_08_FlowletsCounterTests;
import testsForExamples.CountRandom.CountRandom_09_ProcessPageTests;
import testsForExamples.CountRandom.CountRandom_10_StorePageTests;
import testsForExamples.HelloWorld.HelloWorld_01_UploadTests;
import testsForExamples.HelloWorld.HelloWorld_02_AppPageTests;
import testsForExamples.HelloWorld.HelloWorld_03_ProcessTests;
import testsForExamples.HelloWorld.HelloWorld_04_QueryTests;
import testsForExamples.Purchase.Purchase_01_UploadTests;
import testsForExamples.Purchase.Purchase_02_AppPageTests;
import testsForExamples.Purchase.Purchase_03_MapReduceTests;
import testsForExamples.Purchase.Purchase_04_WorkFlowTests;
import testsForExamples.Purchase.Purchase_05_MetricsTests;
 
/**
 * JUnit Suite Test.
 * @author elmira
 *
 */
 
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ResetTest.class,
        LeftPanelSanityTests.class,
        HeaderSanityTests.class,
        FooterSanityTests.class,
        HomePageTests.class,
        MetricsPageTests.class,
        ProcessPageTests.class,
        CollectPageTests.class,
        QueryPageTests.class,
        StorePageTests.class,
        CountRandom_01_UploadTests.class,
        CountRandom_02_AppPageTests.class,
        CountRandom_03_ActionsTests.class,
        CountRandom_04_ProcessTests.class,
        CountRandom_05_DatasetsTests.class,
        CountRandom_06_FlowletsSourceTests.class,
        CountRandom_07_FlowletsSplitterTests.class,
        CountRandom_08_FlowletsCounterTests.class,
        CountRandom_09_ProcessPageTests.class,
        CountRandom_10_StorePageTests.class,
        Purchase_01_UploadTests.class,
        Purchase_02_AppPageTests.class,
        Purchase_03_MapReduceTests.class,
        Purchase_04_WorkFlowTests.class,
        Purchase_05_MetricsTests.class,
        HelloWorld_01_UploadTests.class,
        HelloWorld_02_AppPageTests.class,
        HelloWorld_03_ProcessTests.class,
        HelloWorld_04_QueryTests.class,
        CloseDriverTest.class
        
})
public class SuiteOrder {
  

}
