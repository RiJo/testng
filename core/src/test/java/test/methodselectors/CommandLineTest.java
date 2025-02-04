package test.methodselectors;

import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import test.SimpleBaseTest;
import testhelper.OutputDirectoryPatch;

import java.util.ArrayList;
import java.util.List;

public class CommandLineTest extends SimpleBaseTest {

  private String[] ARG_WITHOUT_CLASSES =
    new String[]{
      "-log", "0",
      "-d", OutputDirectoryPatch.getOutputDirectory(),
      "-methodselectors", "",
      ""
  };

  private String[] ARG_WITH_GROUPS =
    new String[]{
      "-log", "0",
      "-d", OutputDirectoryPatch.getOutputDirectory(),
      "-testclass", "test.methodselectors.SampleTest",
      "-methodselectors", "",
      "-groups", ""
  };

  private String[] ARG_WITHOUT_GROUPS =
    new String[]{
      "-log", "0",
      "-d", OutputDirectoryPatch.getOutputDirectory(),
      "-testclass", "test.methodselectors.SampleTest",
      "-methodselectors", "",
  };

  private TestListenerAdapter tla;

  @BeforeMethod
  public void setup() {
    ppp("setup()");
    tla = new TestListenerAdapter();
  }

  @Test
  public void commandLineNegativePriorityAllGroups() {
    ppp("commandLineNegativePriorityAllGroups()");
    ARG_WITHOUT_GROUPS[7] = "test.methodselectors.AllTestsMethodSelector:-1";
    TestNG.privateMain(ARG_WITHOUT_GROUPS, tla);
    String[] passed = {
        "test1", "test2", "test3"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineNegativePriorityGroup2() {
    ppp("commandLineNegativePriorityGroup2()");
    ARG_WITHOUT_GROUPS[7] = "test.methodselectors.Test2MethodSelector:-1";
    TestNG.privateMain(ARG_WITHOUT_GROUPS, tla);
    String[] passed = {
        "test2"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineLessThanPriorityTest1Test() {
    ppp("commandLineLessThanPriorityTest1Test()");
    ARG_WITH_GROUPS[7] = "test.methodselectors.Test2MethodSelector:5";
    ARG_WITH_GROUPS[9] = "test1";
    TestNG.privateMain(ARG_WITH_GROUPS, tla);
    String[] passed = {
        "test1", "test2"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineGreaterThanPriorityTest1Test2() {
    ppp("commandLineGreaterThanPriorityTest1Test2()");
    ARG_WITH_GROUPS[7] = "test.methodselectors.Test2MethodSelector:15";
    ARG_WITH_GROUPS[9] = "test1";
    TestNG.privateMain(ARG_WITH_GROUPS, tla);
    String[] passed = {
        "test2"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }
  @Test
  public void commandLineLessThanPriorityAllTests() {
    ppp("commandLineLessThanPriorityAllTests()");
    ARG_WITH_GROUPS[7] = "test.methodselectors.AllTestsMethodSelector:5";
    ARG_WITH_GROUPS[9] = "test1";
    TestNG.privateMain(ARG_WITH_GROUPS, tla);
    String[] passed = {
        "test1", "test2", "test3"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineMultipleSelectors() {
    ppp("commandLineMultipleSelectors()");
    ARG_WITH_GROUPS[7] = "test.methodselectors.NoTestSelector:7,test.methodselectors.Test2MethodSelector:5";
    ARG_WITH_GROUPS[9] = "test1";
    TestNG.privateMain(ARG_WITH_GROUPS, tla);
    String[] passed = {
        "test1", "test2"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineNoTest1Selector() {
    ppp("commandLineNoTest1Selector()");
    ARG_WITHOUT_GROUPS[7] = "test.methodselectors.NoTest1MethodSelector:5";
    TestNG.privateMain(ARG_WITHOUT_GROUPS, tla);
    String[] passed = {
        "test2", "test3"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineTestWithXmlFile() {
    ppp("commandLineTestWithXmlFile()");
    ARG_WITHOUT_CLASSES[5] = "test.methodselectors.NoTest1MethodSelector:5";
    ARG_WITHOUT_CLASSES[6] = getPathToResource("testng-methodselectors.xml");
    TestNG.privateMain(ARG_WITHOUT_CLASSES, tla);
    String[] passed = {
        "test2", "test3"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test(description = "GITHUB-2407")
  public void testOverrideExcludedMethodsCommandLineExclusions() {
    ppp("testOverrideExcludedMethodsCommandLineExclusions");
    String[] args = new String[]{
            "src/test/resources/test/methodselectors/sampleTest.xml",
            "-log", "0",
            "-d", OutputDirectoryPatch.getOutputDirectory(),
            "-excludegroups", "test1",
            "-overrideincludedmethods"
    };

    TestNG.privateMain(args, tla);

    // test1 is excluded, so only test2 is left in the passed list
    String[] passed = {"test2"};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test(description = "GITHUB-2407")
  public void testOverrideExcludedMethodsSuiteExclusions() {
    ppp("testOverrideExcludedMethodsSuiteExclusions");
    String[] args = new String[]{
            "src/test/resources/test/methodselectors/sampleTestExclusions.xml",
            "-log", "0",
            "-d", OutputDirectoryPatch.getOutputDirectory(),
            "-overrideincludedmethods"
    };

    TestNG.privateMain(args, tla);

    String[] passed = {};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  private void verifyTests(String title, String[] expected, List<ITestResult> found) {
    List<String> resultMethods = new ArrayList<>();
    for( ITestResult result : found ) {
      resultMethods.add( result.getName() );
    }

    Assertions.assertThat(resultMethods.toArray(new String[0]))
            .describedAs(title)
            .isEqualTo(expected);
  }

  public static void ppp(String s) {
    //System.out.println("[CommandLineTest] " + s);
  }
}
