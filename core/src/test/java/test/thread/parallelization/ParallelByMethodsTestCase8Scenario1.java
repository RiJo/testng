package test.thread.parallelization;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.thread.parallelization.sample.TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample;
import test.thread.parallelization.sample.TestClassBFourMethodsWithFactoryUsingDataProviderAndNoDepsSample;
import test.thread.parallelization.sample.TestClassCSixMethodsWithFactoryUsingDataProviderAndNoDepsSample;
import test.thread.parallelization.sample.TestClassDThreeMethodsWithFactoryUsingDataProviderAndNoDepsSample;
import test.thread.parallelization.sample.TestClassEFiveMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassFSixMethodsWithFactoryUsingDataProviderAndNoDepsSample;
import test.thread.parallelization.sample.TestClassGThreeMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassHFourMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassIFiveMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassJFourMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassKFiveMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassLThreeMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassMFourMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassNFiveMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassOSixMethodsWithNoDepsSample;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static test.thread.parallelization.TestNgRunStateTracker.getAllSuiteLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllSuiteListenerStartEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllTestLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllTestMethodLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteAndTestLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerFinishEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerStartEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getTestLevelEventLogsForTest;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerFinishEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerStartEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodLevelEventLogsForTest;
import static test.thread.parallelization.TestNgRunStateTracker.reset;

/** This class covers PTP_TC_8, Scenario 1 in the Parallelization Test Plan.
 *
 * Test Case Summary: Parallel by methods mode with parallel test suites using factories with data providers but no
 *                    dependencies.
 *
 * Scenario Description: Three suites with 1, 2 and 3 tests respectively. One suite with two tests has a test
 *                       consisting of a single test class without a factory while the other shall consist of test
 *                       classes with factories using data providers with varying numbers of data sets. One suite shall
 *                       consist of a single test with multiple test classes with factories using data providers with
 *                       varying numbers of data sets. One suite shall have multiple tests with multiple classes, none
 *                       of which use a factory.
 *
 * 1) The suite thread pool is 2, so one suite will have to wait for one of the others to complete execution before it
 *    can begin execution
 * 2) For one of the suites, the thread count and parallel mode are specified at the suite level
 * 3) For one of the suites, the thread count and parallel mode are specified at the test level
 * 4) For one of the suites, the parallel mode is specified at the suite level, and the thread counts are specified at
 *    the test level (thread counts for each test differ)
 * 5) The thread count is less than the number of test methods for the tests in two of the suites, so some methods will
 *    have to wait for the active thread count to drop below the maximum thread count before they can begin execution.
 * 6) The thread count is more than the number of test methods for the tests in one of the suites, ensuring that none
 *    of the methods in that suite should have to wait for any other method to complete execution
 * 7) The expectation is that threads will be spawned for each test method that executes just as they would if there
 *    were no factories and test suites simply used the default mechanism for instantiating single instances of the
 *    test classes.
 * 8) There are NO configuration methods
 * 9) All test methods pass
 * 10) NO ordering is specified
 * 11) `group-by-instances is NOT set
 * 12) here are no method exclusions
 */
public class ParallelByMethodsTestCase8Scenario1 extends BaseParallelizationTest {
    private static final Logger log = Logger.getLogger(ParallelByMethodsTestCase8Scenario1.class);

    private static final String SUITE_A = "TestSuiteA";
    private static final String SUITE_B = "TestSuiteB";
    private static final String SUITE_C = "TestSuiteC";

    private static final String SUITE_A_TEST_A = "TestSuiteA-TwoTestClassTest";

    private static final String SUITE_B_TEST_A = "TestSuiteB-SingleTestClassTest";
    private static final String SUITE_B_TEST_B = "TestSuiteB-ThreeTestClassTest";

    private static final String SUITE_C_TEST_A = "TestSuiteC-ThreeTestClassTest";
    private static final String SUITE_C_TEST_B = "TestSuiteC-TwoTestClassTest";
    private static final String SUITE_C_TEST_C = "TestSuiteC-FourTestClassTest";

    private static final int THREAD_POOL_SIZE = 2;

    private Map<String, Integer> expectedInvocationCounts = new HashMap<>();

    private Map<String, List<TestNgRunStateTracker.EventLog>> testEventLogsMap = new HashMap<>();

    private List<TestNgRunStateTracker.EventLog> suiteLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> testLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> testMethodLevelEventLogs;

    private List<TestNgRunStateTracker.EventLog> suiteOneSuiteAndTestLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> suiteOneSuiteLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> suiteOneTestLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> suiteOneTestMethodLevelEventLogs;

    private List<TestNgRunStateTracker.EventLog> suiteTwoSuiteAndTestLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> suiteTwoSuiteLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> suiteTwoTestLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> suiteTwoTestMethodLevelEventLogs;

    private List<TestNgRunStateTracker.EventLog> suiteThreeSuiteAndTestLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> suiteThreeSuiteLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> suiteThreeTestLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> suiteThreeTestMethodLevelEventLogs;

    private List<TestNgRunStateTracker.EventLog> suiteOneTestOneTestMethodLevelEventLogs;

    private List<TestNgRunStateTracker.EventLog> suiteTwoTestOneTestMethodLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> suiteTwoTestTwoTestMethodLevelEventLogs;

    private List<TestNgRunStateTracker.EventLog> suiteThreeTestOneTestMethodLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> suiteThreeTestTwoTestMethodLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> suiteThreeTestThreeTestMethodLevelEventLogs;

    private TestNgRunStateTracker.EventLog suiteOneSuiteListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteOneSuiteListenerOnFinishEventLog;

    private TestNgRunStateTracker.EventLog suiteTwoSuiteListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteTwoSuiteListenerOnFinishEventLog;

    private TestNgRunStateTracker.EventLog suiteThreeSuiteListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteThreeSuiteListenerOnFinishEventLog;

    private TestNgRunStateTracker.EventLog suiteOneTestOneListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteOneTestOneListenerOnFinishEventLog;

    private TestNgRunStateTracker.EventLog suiteTwoTestOneListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteTwoTestOneListenerOnFinishEventLog;
    private TestNgRunStateTracker.EventLog suiteTwoTestTwoListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteTwoTestTwoListenerOnFinishEventLog;

    private TestNgRunStateTracker.EventLog suiteThreeTestOneListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteThreeTestOneListenerOnFinishEventLog;
    private TestNgRunStateTracker.EventLog suiteThreeTestTwoListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteThreeTestTwoListenerOnFinishEventLog;
    private TestNgRunStateTracker.EventLog suiteThreeTestThreeListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteThreeTestThreeListenerOnFinishEventLog;

    @BeforeClass
    public void setUp() {
        reset();

        XmlSuite suiteOne = createXmlSuite(SUITE_A);
        XmlSuite suiteTwo = createXmlSuite(SUITE_B);
        XmlSuite suiteThree = createXmlSuite(SUITE_C);

        suiteOne.setParallel(XmlSuite.ParallelMode.METHODS);
        suiteOne.setThreadCount(10);

        createXmlTest(suiteOne, SUITE_A_TEST_A, TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class,
                TestClassCSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class);
        createXmlTest(suiteTwo, SUITE_B_TEST_A, TestClassEFiveMethodsWithNoDepsSample.class);
        createXmlTest(suiteTwo, SUITE_B_TEST_B, TestClassDThreeMethodsWithFactoryUsingDataProviderAndNoDepsSample.class,
                TestClassBFourMethodsWithFactoryUsingDataProviderAndNoDepsSample.class,
                TestClassFSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class);

        suiteTwo.setParallel(XmlSuite.ParallelMode.METHODS);

        for(XmlTest test : suiteTwo.getTests()) {
            if(test.getName().equals(SUITE_B_TEST_A)) {
                test.setThreadCount(3);
            } else {
                test.setThreadCount(20);
            }
        }

        createXmlTest(suiteThree, SUITE_C_TEST_A, TestClassGThreeMethodsWithNoDepsSample.class,
                TestClassHFourMethodsWithNoDepsSample.class, TestClassIFiveMethodsWithNoDepsSample.class);
        createXmlTest(suiteThree, SUITE_C_TEST_B, TestClassJFourMethodsWithNoDepsSample.class,
                TestClassKFiveMethodsWithNoDepsSample.class);
        createXmlTest(suiteThree, SUITE_C_TEST_C, TestClassLThreeMethodsWithNoDepsSample.class,
                TestClassMFourMethodsWithNoDepsSample.class, TestClassNFiveMethodsWithNoDepsSample.class,
                TestClassOSixMethodsWithNoDepsSample.class);

        for(XmlTest test : suiteThree.getTests()) {
            test.setParallel(XmlSuite.ParallelMode.METHODS);

            switch(test.getName()) {
                case SUITE_C_TEST_A:
                    test.setThreadCount(25);
                    break;
                case SUITE_C_TEST_B:
                    test.setThreadCount(5);
                    break;
                default:
                    test.setThreadCount(12);
                    break;
            }
        }

        addParams(suiteOne, SUITE_A, SUITE_A_TEST_A, "100",
                TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        "(paramOne,paramTwo,paramThree)" +
                TestClassCSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        "(paramOne,paramTwo,paramThree,paramFour)"
        );

        addParams(suiteTwo, SUITE_B, SUITE_B_TEST_A, "100");
        addParams(suiteTwo, SUITE_B, SUITE_B_TEST_B, "100", "paramOne,paramTwo,paramThree,paramFour");

        addParams(suiteThree, SUITE_C, SUITE_C_TEST_A, "100");
        addParams(suiteThree, SUITE_C, SUITE_C_TEST_B, "100");
        addParams(suiteThree, SUITE_C, SUITE_C_TEST_C, "100");

        TestNG tng = create(suiteOne, suiteTwo, suiteThree);
        tng.setSuiteThreadPoolSize(2);
        tng.addListener((ITestNGListener) new TestNgRunStateListener());

        log.debug("Beginning ParallelByMethodsTestCase8Scenario1. This test scenario consists of three " +
                "suites with 1, 2 and 3 tests respectively. One suite with two tests has a test consisting of a " +
                "single test class without a factory while the other shall consist of test classes with factories " +
                "using data providers with varying numbers of data sets. One suite shall consist of a single test " +
                "with multiple test classes with factories using data providers with varying numbers of data sets. " +
                "One suite shall have multiple tests with multiple classes, none of which use a factory.");

        log.debug("Suite: " + SUITE_A + ", Test: " + SUITE_A_TEST_A + ", Test classes: " +
                TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() + ", " +
                TestClassCSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                ". Thread count: 10");

        log.debug("Suite: " + SUITE_B + ", Test: " + SUITE_B_TEST_A + ", Test class: " +
                TestClassEFiveMethodsWithNoDepsSample.class.getCanonicalName() + ". Thread count: 3");

        log.debug("Suite " + SUITE_B + ", Test: " + SUITE_B_TEST_B + ", Test classes: " +
                TestClassDThreeMethodsWithFactoryUsingDataProviderAndNoDepsSample.class + ", " +
                TestClassBFourMethodsWithFactoryUsingDataProviderAndNoDepsSample.class + ", " +
                TestClassFSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class + ". Thread count: 20");

        log.debug("Suite: " + SUITE_C + ", Test: " + SUITE_C_TEST_A + ", Test classes: " +
                TestClassGThreeMethodsWithNoDepsSample.class.getCanonicalName() + ", " +
                TestClassHFourMethodsWithNoDepsSample.class.getCanonicalName() + ", " +
                TestClassIFiveMethodsWithNoDepsSample.class + ". Thread count: 10");

        log.debug("Suite: " + SUITE_C + ", Test: " + SUITE_C_TEST_B + ", Test classes: " +
                TestClassJFourMethodsWithNoDepsSample.class.getCanonicalName() + ", " +
                TestClassKFiveMethodsWithNoDepsSample.class + ". Thread count: 5");

        log.debug("Suite: " + SUITE_C + ", Test: " + SUITE_C_TEST_C + ", Test classes: " +
                TestClassLThreeMethodsWithNoDepsSample.class.getCanonicalName() + ", " +
                TestClassMFourMethodsWithNoDepsSample.class.getCanonicalName() + ", " +
                TestClassNFiveMethodsWithNoDepsSample.class.getCanonicalName() + ", " +
                TestClassOSixMethodsWithNoDepsSample.class.getCanonicalName() + ". Thread count: 12.");

        tng.run();

        expectedInvocationCounts.put(
                TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodA", 3);
        expectedInvocationCounts.put(
                TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodB", 3);
        expectedInvocationCounts.put(
                TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodC", 3);
        expectedInvocationCounts.put(
                TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodD", 3);
        expectedInvocationCounts.put(
                TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodE", 3);

        expectedInvocationCounts.put(
                TestClassCSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodA", 4);
        expectedInvocationCounts.put(
                TestClassCSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodB", 4);
        expectedInvocationCounts.put(
                TestClassCSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodC", 4);
        expectedInvocationCounts.put(
                TestClassCSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodD", 4);
        expectedInvocationCounts.put(
                TestClassCSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodE", 4);
        expectedInvocationCounts.put(
                TestClassCSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodF", 4);

        expectedInvocationCounts.put(
                TestClassEFiveMethodsWithNoDepsSample.class.getCanonicalName() + ".testMethodA", 1);
        expectedInvocationCounts.put(
                TestClassEFiveMethodsWithNoDepsSample.class.getCanonicalName() + ".testMethodB", 1);
        expectedInvocationCounts.put(
                TestClassEFiveMethodsWithNoDepsSample.class.getCanonicalName() + ".testMethodC", 1);
        expectedInvocationCounts.put(
                TestClassEFiveMethodsWithNoDepsSample.class.getCanonicalName() + ".testMethodD", 1);
        expectedInvocationCounts.put(
                TestClassEFiveMethodsWithNoDepsSample.class.getCanonicalName() + ".testMethodE", 1);


        expectedInvocationCounts.put(
                TestClassDThreeMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodA", 4);
        expectedInvocationCounts.put(
                TestClassDThreeMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodB", 4);
        expectedInvocationCounts.put(
                TestClassDThreeMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodC", 4);


        expectedInvocationCounts.put(
                TestClassBFourMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodA", 4);
        expectedInvocationCounts.put(
                TestClassBFourMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodB", 4);
        expectedInvocationCounts.put(
                TestClassBFourMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodC", 4);
        expectedInvocationCounts.put(
                TestClassBFourMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodD", 4);


        expectedInvocationCounts.put(
                TestClassFSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodA", 4);
        expectedInvocationCounts.put(
                TestClassFSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodB", 4);
        expectedInvocationCounts.put(
                TestClassFSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodC", 4);
        expectedInvocationCounts.put(
                TestClassFSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodD", 4);
        expectedInvocationCounts.put(
                TestClassFSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodE", 4);
        expectedInvocationCounts.put(
                TestClassFSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                        ".testMethodF", 4);

        expectedInvocationCounts.put(TestClassGThreeMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodA", 1);
        expectedInvocationCounts.put(TestClassGThreeMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodB", 1);
        expectedInvocationCounts.put(TestClassGThreeMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodC", 1);

        expectedInvocationCounts.put(TestClassHFourMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodA", 1);
        expectedInvocationCounts.put(TestClassHFourMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodB", 1);
        expectedInvocationCounts.put(TestClassHFourMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodC", 1);
        expectedInvocationCounts.put(TestClassHFourMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodD", 1);

        expectedInvocationCounts.put(TestClassIFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodA", 1);
        expectedInvocationCounts.put(TestClassIFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodB", 1);
        expectedInvocationCounts.put(TestClassIFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodC", 1);
        expectedInvocationCounts.put(TestClassIFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodD", 1);
        expectedInvocationCounts.put(TestClassIFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodE", 1);

        expectedInvocationCounts.put(TestClassJFourMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodA", 1);
        expectedInvocationCounts.put(TestClassJFourMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodB", 1);
        expectedInvocationCounts.put(TestClassJFourMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodC", 1);
        expectedInvocationCounts.put(TestClassJFourMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodD", 1);

        expectedInvocationCounts.put(TestClassKFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodA", 1);
        expectedInvocationCounts.put(TestClassKFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodB", 1);
        expectedInvocationCounts.put(TestClassKFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodC", 1);
        expectedInvocationCounts.put(TestClassKFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodD", 1);
        expectedInvocationCounts.put(TestClassKFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodE", 1);

        expectedInvocationCounts.put(TestClassLThreeMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodA", 1);
        expectedInvocationCounts.put(TestClassLThreeMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodB", 1);
        expectedInvocationCounts.put(TestClassLThreeMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodC", 1);

        expectedInvocationCounts.put(TestClassMFourMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodA", 1);
        expectedInvocationCounts.put(TestClassMFourMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodB", 1);
        expectedInvocationCounts.put(TestClassMFourMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodC", 1);
        expectedInvocationCounts.put(TestClassMFourMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodD", 1);

        expectedInvocationCounts.put(TestClassNFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodA", 1);
        expectedInvocationCounts.put(TestClassNFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodB", 1);
        expectedInvocationCounts.put(TestClassNFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodC", 1);
        expectedInvocationCounts.put(TestClassNFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodD", 1);
        expectedInvocationCounts.put(TestClassNFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodE", 1);

        expectedInvocationCounts.put(TestClassOSixMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodA", 1);
        expectedInvocationCounts.put(TestClassOSixMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodB", 1);
        expectedInvocationCounts.put(TestClassOSixMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodC", 1);
        expectedInvocationCounts.put(TestClassOSixMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodD", 1);
        expectedInvocationCounts.put(TestClassOSixMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodE", 1);
        expectedInvocationCounts.put(TestClassOSixMethodsWithNoDepsSample.class.getCanonicalName() +
                ".testMethodF", 1);

        suiteLevelEventLogs = getAllSuiteLevelEventLogs();
        testLevelEventLogs = getAllTestLevelEventLogs();
        testMethodLevelEventLogs = getAllTestMethodLevelEventLogs();

        suiteOneSuiteAndTestLevelEventLogs = getSuiteAndTestLevelEventLogsForSuite(SUITE_A);
        suiteOneSuiteLevelEventLogs = getSuiteLevelEventLogsForSuite(SUITE_A);
        suiteOneTestLevelEventLogs = getTestLevelEventLogsForSuite(SUITE_A);

        suiteTwoSuiteAndTestLevelEventLogs = getSuiteAndTestLevelEventLogsForSuite(SUITE_B);
        suiteTwoSuiteLevelEventLogs = getSuiteLevelEventLogsForSuite(SUITE_B);
        suiteTwoTestLevelEventLogs = getTestLevelEventLogsForSuite(SUITE_B);

        suiteThreeSuiteAndTestLevelEventLogs = getSuiteAndTestLevelEventLogsForSuite(SUITE_C);
        suiteThreeSuiteLevelEventLogs = getSuiteLevelEventLogsForSuite(SUITE_C);
        suiteThreeTestLevelEventLogs = getTestLevelEventLogsForSuite(SUITE_C);

        suiteOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForSuite(SUITE_A);
        suiteTwoTestMethodLevelEventLogs = getTestMethodLevelEventLogsForSuite(SUITE_B);
        suiteThreeTestMethodLevelEventLogs = getTestMethodLevelEventLogsForSuite(SUITE_C);

        suiteOneTestOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_A);

        suiteTwoTestOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A);
        suiteTwoTestTwoTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B);

        suiteThreeTestOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_A);
        suiteThreeTestTwoTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_B);
        suiteThreeTestThreeTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_C);

        testEventLogsMap.put(SUITE_B_TEST_A, getTestLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A));
        testEventLogsMap.put(SUITE_B_TEST_B, getTestLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B));

        testEventLogsMap.put(SUITE_C_TEST_A, getTestLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_A));
        testEventLogsMap.put(SUITE_C_TEST_B, getTestLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_B));
        testEventLogsMap.put(SUITE_C_TEST_C, getTestLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_C));

        suiteOneSuiteListenerOnStartEventLog = getSuiteListenerStartEventLog(SUITE_A);
        suiteOneSuiteListenerOnFinishEventLog = getSuiteListenerFinishEventLog(SUITE_A);

        suiteTwoSuiteListenerOnStartEventLog = getSuiteListenerStartEventLog(SUITE_B);
        suiteTwoSuiteListenerOnFinishEventLog = getSuiteListenerFinishEventLog(SUITE_B);

        suiteThreeSuiteListenerOnStartEventLog = getSuiteListenerStartEventLog(SUITE_C);
        suiteThreeSuiteListenerOnFinishEventLog = getSuiteListenerFinishEventLog(SUITE_C);

        suiteOneTestOneListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_A, SUITE_A_TEST_A);
        suiteOneTestOneListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_A, SUITE_A_TEST_A);

        suiteTwoTestOneListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_B, SUITE_B_TEST_A);
        suiteTwoTestOneListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_B, SUITE_B_TEST_A);

        suiteTwoTestTwoListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_B, SUITE_B_TEST_B);
        suiteTwoTestTwoListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_B, SUITE_B_TEST_B);

        suiteThreeTestOneListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_C, SUITE_C_TEST_A);
        suiteThreeTestOneListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_C, SUITE_C_TEST_A);
        suiteThreeTestTwoListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_C, SUITE_C_TEST_B);
        suiteThreeTestTwoListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_C, SUITE_C_TEST_B);
        suiteThreeTestThreeListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_C, SUITE_C_TEST_C);
        suiteThreeTestThreeListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_C, SUITE_C_TEST_C);
    }

    //Verifies that the expected number of suite, test and test method level events were logged for each of the three
    //suites.
    @Test
    public void sanityCheck() {
        assertEquals(suiteLevelEventLogs.size(), 6, "There should be 6 suite level events logged for " + SUITE_A +
                ", " + SUITE_B + " and " + SUITE_C + ": " + suiteLevelEventLogs);
        assertEquals(testLevelEventLogs.size(), 12, "There should be 12 test level events logged for " + SUITE_A +
                ", " + SUITE_B + " and " + SUITE_C + ": " + testLevelEventLogs);

        assertEquals(testMethodLevelEventLogs.size(), 405, "There should 405 test method level events logged for " +
                SUITE_A + ", " + SUITE_B + " and " + SUITE_C + ": " + testMethodLevelEventLogs);

        assertEquals(suiteOneSuiteLevelEventLogs.size(), 2, "There should be 2 suite level events logged for " +
                SUITE_A + ": " + suiteOneSuiteLevelEventLogs);
        assertEquals(suiteOneTestLevelEventLogs.size(), 2, "There should be 2 test level events logged for " + SUITE_A +
                ": " + suiteOneTestLevelEventLogs);
        assertEquals(suiteOneTestMethodLevelEventLogs.size(), 117, "There should be 117 test method level events " +
                "logged for " + SUITE_A + ": " + suiteOneTestMethodLevelEventLogs);

        assertEquals(suiteTwoSuiteLevelEventLogs.size(), 2, "There should be 2 suite level events logged for " +
                SUITE_B + ": " + suiteTwoSuiteLevelEventLogs);
        assertEquals(suiteTwoTestLevelEventLogs.size(), 4, "There should be 4 test level events logged for " + SUITE_B +
                ": " + suiteTwoTestLevelEventLogs);
        assertEquals(suiteTwoTestMethodLevelEventLogs.size(), 171, "There should be 171 test method level events " +
                "logged for " + SUITE_B + ": " + suiteTwoTestMethodLevelEventLogs);

        assertEquals(suiteThreeSuiteLevelEventLogs.size(), 2, "There should be 2 suite level events logged for " +
                SUITE_C + ": " + suiteThreeSuiteLevelEventLogs);
        assertEquals(suiteThreeTestLevelEventLogs.size(), 6, "There should be 6 test level events logged for " +
                SUITE_C + ": " + suiteThreeTestLevelEventLogs);
        assertEquals(suiteThreeTestMethodLevelEventLogs.size(), 117, "There should be 117 test method level events " +
                "logged for " + SUITE_C + ": " + suiteThreeTestMethodLevelEventLogs);

    }

    //Verify that the suites run in parallel by checking that the suite and test level events for both suites have
    //overlapping timestamps. Verify that there are two separate threads executing the suite-level and test-level
    //events for each suite.
    @Test
    public void verifyThatSuitesRunInParallelThreads() {
        verifyParallelSuitesWithUnequalExecutionTimes(suiteLevelEventLogs, THREAD_POOL_SIZE);
    }

    @Test
    public void verifyTestLevelEventsRunInSequentialOrderForIndividualSuites() {
        verifySequentialTests(suiteOneSuiteAndTestLevelEventLogs, suiteOneTestLevelEventLogs,
                suiteOneSuiteListenerOnStartEventLog, suiteOneSuiteListenerOnFinishEventLog);

        verifySequentialTests(suiteTwoSuiteAndTestLevelEventLogs, suiteTwoTestLevelEventLogs,
                suiteTwoSuiteListenerOnStartEventLog, suiteTwoSuiteListenerOnFinishEventLog);

        verifySequentialTests(suiteThreeSuiteAndTestLevelEventLogs, suiteThreeTestLevelEventLogs,
                suiteThreeSuiteListenerOnStartEventLog, suiteThreeSuiteListenerOnFinishEventLog);
    }

    //Verify the expected number of test class instances for the test method events.
    //Verify that the same test class instances are associated with each of the test methods from the sample test class
    @Test
    public void verifyNumberOfInstanceOfTestClassForAllTestMethodsForAllSuites() {

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_A,
                SUITE_A_TEST_A,
                Arrays.asList(
                        TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class,
                        TestClassCSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class
                ),
                3, 4);

        verifySameInstancesOfTestClassesAssociatedWithMethods(
                SUITE_A,
                SUITE_A_TEST_A,
                Arrays.asList(
                        TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class,
                        TestClassCSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class
                )
        );

        verifyNumberOfInstancesOfTestClassForMethods(
                SUITE_B,
                SUITE_B_TEST_A,
                TestClassEFiveMethodsWithNoDepsSample.class,
                1);

        verifySameInstancesOfTestClassAssociatedWithMethods(
                SUITE_B,
                SUITE_B_TEST_A,
                TestClassEFiveMethodsWithNoDepsSample.class
        );

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_B,
                SUITE_B_TEST_B,
                Arrays.asList(
                        TestClassDThreeMethodsWithFactoryUsingDataProviderAndNoDepsSample.class,
                        TestClassBFourMethodsWithFactoryUsingDataProviderAndNoDepsSample.class,
                        TestClassFSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class
                ),
                4, 4, 4
        );

        verifySameInstancesOfTestClassesAssociatedWithMethods(
                SUITE_B,
                SUITE_B_TEST_B,
                Arrays.asList(
                        TestClassDThreeMethodsWithFactoryUsingDataProviderAndNoDepsSample.class,
                        TestClassBFourMethodsWithFactoryUsingDataProviderAndNoDepsSample.class,
                        TestClassFSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class
                )
        );

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_C,
                SUITE_C_TEST_A,
                Arrays.asList(
                        TestClassGThreeMethodsWithNoDepsSample.class,
                        TestClassHFourMethodsWithNoDepsSample.class,
                        TestClassIFiveMethodsWithNoDepsSample.class
                ),
                1
        );

        verifySameInstancesOfTestClassesAssociatedWithMethods(
                SUITE_C,
                SUITE_C_TEST_A,
                Arrays.asList(
                        TestClassGThreeMethodsWithNoDepsSample.class,
                        TestClassHFourMethodsWithNoDepsSample.class,
                        TestClassIFiveMethodsWithNoDepsSample.class
                )
        );

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_C,
                SUITE_C_TEST_B,
                Arrays.asList(TestClassJFourMethodsWithNoDepsSample.class, TestClassKFiveMethodsWithNoDepsSample.class),
                1);

        verifySameInstancesOfTestClassesAssociatedWithMethods(
                SUITE_C,
                SUITE_C_TEST_B,
                Arrays.asList(TestClassJFourMethodsWithNoDepsSample.class, TestClassKFiveMethodsWithNoDepsSample.class)
        );

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_C,
                SUITE_C_TEST_C,
                Arrays.asList(
                        TestClassLThreeMethodsWithNoDepsSample.class,
                        TestClassMFourMethodsWithNoDepsSample.class,
                        TestClassNFiveMethodsWithNoDepsSample.class,
                        TestClassOSixMethodsWithNoDepsSample.class
                ),
                1
        );

        verifySameInstancesOfTestClassesAssociatedWithMethods(
                SUITE_C,
                SUITE_C_TEST_C,
                Arrays.asList(
                        TestClassLThreeMethodsWithNoDepsSample.class,
                        TestClassMFourMethodsWithNoDepsSample.class,
                        TestClassNFiveMethodsWithNoDepsSample.class,
                        TestClassOSixMethodsWithNoDepsSample.class
                )
        );
    }

    //Verify that the test method listener's onTestStart method runs after the test listener's onStart method for
    //all the test methods in all tests and suites.
    @Test
    public void verifyTestLevelMethodLevelEventLogsOccurBetweenAfterTestListenerStartAndFinishEventLogs() {
        verifyEventsOccurBetween(suiteOneTestOneListenerOnStartEventLog, suiteOneTestOneTestMethodLevelEventLogs,
                suiteOneTestOneListenerOnFinishEventLog,  "All of the test method level event logs for " +
                        SUITE_A_TEST_A + " should have timestamps between the test listener's onStart and onFinish " +
                        "event logs for " + SUITE_A_TEST_A + ". Test listener onStart event log: " +
                        suiteOneTestOneListenerOnStartEventLog + ". Test listener onFinish event log: " +
                        suiteOneTestOneListenerOnFinishEventLog + ". Test method level event logs: " +
                        suiteOneTestOneTestMethodLevelEventLogs);

        verifyEventsOccurBetween(suiteTwoTestOneListenerOnStartEventLog, suiteTwoTestOneTestMethodLevelEventLogs,
                suiteTwoTestOneListenerOnFinishEventLog,  "All of the test method level event logs for " +
                        SUITE_B_TEST_A + " should have timestamps between the test listener's onStart and onFinish " +
                        "event logs for " + SUITE_B_TEST_A + ". Test listener onStart event log: " +
                        suiteTwoTestOneListenerOnStartEventLog + ". Test listener onFinish event log: " +
                        suiteTwoTestOneListenerOnFinishEventLog + ". Test method level event logs: " +
                        suiteTwoTestOneTestMethodLevelEventLogs);

        verifyEventsOccurBetween(suiteTwoTestTwoListenerOnStartEventLog, suiteTwoTestTwoTestMethodLevelEventLogs,
                suiteTwoTestTwoListenerOnFinishEventLog,  "All of the test method level event logs for " +
                        SUITE_B_TEST_B + " should have timestamps between the test listener's onStart and onFinish " +
                        "event logs for " + SUITE_B_TEST_B + ". Test listener onStart event log: " +
                        suiteTwoTestTwoListenerOnStartEventLog + ". Test listener onFinish event log: " +
                        suiteTwoTestTwoListenerOnFinishEventLog + ". Test method level event logs: " +
                        suiteTwoTestTwoTestMethodLevelEventLogs);

        verifyEventsOccurBetween(suiteThreeTestOneListenerOnStartEventLog, suiteThreeTestOneTestMethodLevelEventLogs,
                suiteThreeTestOneListenerOnFinishEventLog,  "All of the test method level event logs for " +
                        SUITE_C_TEST_A + " should have timestamps between the test listener's onStart and onFinish " +
                        "event logs for " + SUITE_C_TEST_A + ". Test listener onStart event log: " +
                        suiteThreeTestOneListenerOnStartEventLog + ". Test listener onFinish event log: " +
                        suiteThreeTestOneListenerOnFinishEventLog + ". Test method level event logs: " +
                        suiteThreeTestOneTestMethodLevelEventLogs);

        verifyEventsOccurBetween(suiteThreeTestTwoListenerOnStartEventLog, suiteThreeTestTwoTestMethodLevelEventLogs,
                suiteThreeTestTwoListenerOnFinishEventLog,  "All of the test method level event logs for " +
                        SUITE_C_TEST_B + " should have timestamps between the test listener's onStart and onFinish " +
                        "event logs for " + SUITE_C_TEST_B + ". Test listener onStart event log: " +
                        suiteThreeTestTwoListenerOnStartEventLog + ". Test listener onFinish event log: " +
                        suiteThreeTestTwoListenerOnFinishEventLog + ". Test method level event logs: " +
                        suiteThreeTestTwoTestMethodLevelEventLogs);

        verifyEventsOccurBetween(suiteThreeTestThreeListenerOnStartEventLog, suiteThreeTestThreeTestMethodLevelEventLogs,
                suiteThreeTestThreeListenerOnFinishEventLog,  "All of the test method level event logs for " +
                        SUITE_C_TEST_C + " should have timestamps between the test listener's onStart and onFinish " +
                        "event logs for " + SUITE_C_TEST_C + ". Test listener onStart event log: " +
                        suiteThreeTestThreeListenerOnStartEventLog + ". Test listener onFinish event log: " +
                        suiteThreeTestThreeListenerOnFinishEventLog + ". Test method level event logs: " +
                        suiteThreeTestThreeTestMethodLevelEventLogs);

    }

    //Verifies that the method level events all run in different threads from the test and suite level events.
    //Verifies that the test method listener and execution events for a given test method all run in the same thread.
    @Test
    public void verifyThatMethodLevelEventsRunInDifferentThreadsFromSuiteAndTestLevelEvents() {

        verifyEventThreadsSpawnedAfter(getAllSuiteListenerStartEventLogs().get(0).getThreadId(), testMethodLevelEventLogs,
                "All the thread IDs for the test method level events should be greater than the thread ID for the " +
                        "suite and test level events. The expectation is that since the suite and test level events " +
                        "are running sequentially, and all the test methods are running in parallel, new threads "  +
                        "will be spawned after the thread executing the suite and test level events when new methods " +
                        "begin executing. Suite and test level events thread ID: " +
                        getAllSuiteListenerStartEventLogs().get(0).getThreadId() + ". Test method level event logs: " +
                        testMethodLevelEventLogs);

        verifyEventsForTestMethodsRunInTheSameThread(
                TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class, SUITE_A, SUITE_A_TEST_A);

        verifyEventsForTestMethodsRunInTheSameThread(
                TestClassCSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class, SUITE_A, SUITE_A_TEST_A);

        verifyEventsForTestMethodsRunInTheSameThread(TestClassEFiveMethodsWithNoDepsSample.class, SUITE_B,
                SUITE_B_TEST_A);

        verifyEventsForTestMethodsRunInTheSameThread(
                TestClassDThreeMethodsWithFactoryUsingDataProviderAndNoDepsSample.class, SUITE_B, SUITE_B_TEST_B);

        verifyEventsForTestMethodsRunInTheSameThread(
                TestClassBFourMethodsWithFactoryUsingDataProviderAndNoDepsSample.class, SUITE_B, SUITE_B_TEST_B);

        verifyEventsForTestMethodsRunInTheSameThread(
                TestClassFSixMethodsWithFactoryUsingDataProviderAndNoDepsSample.class, SUITE_B, SUITE_B_TEST_B);

        verifyEventsForTestMethodsRunInTheSameThread(TestClassGThreeMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_A);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassHFourMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_A);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassIFiveMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_A);

        verifyEventsForTestMethodsRunInTheSameThread(TestClassJFourMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_B);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassKFiveMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_B);

        verifyEventsForTestMethodsRunInTheSameThread(TestClassLThreeMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_C);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassMFourMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_C);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassNFiveMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_C);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassOSixMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_C);
    }

    //Verifies that the test methods execute in different threads in parallel fashion.
    @Test
    public void verifyThatTestMethodsRunInParallelThreads() {
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_A), SUITE_A_TEST_A, 10);
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A), SUITE_B_TEST_A, 3);
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B), SUITE_B_TEST_B, 20);
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_A), SUITE_C_TEST_A, 25);
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_B), SUITE_C_TEST_B, 5);
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_C), SUITE_C_TEST_C, 12);
    }
}
