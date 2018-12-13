package jenkins.plugins.logstash.dataproviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import net.sf.json.JSONObject;

public class TestDataProvider extends DataProvider
{
  private static final Logger LOGGER = Logger.getLogger(TestDataProvider.class.getName());

  private static final String TEST_RESULTS = "testResults";

  private transient boolean recordPassingTests;
  private transient boolean listSkippedTests;

  private transient JSONObject data;

  private transient AbstractTestResultAction<?> testResultAction;

  private int totalCount, skipCount, failCount, passCount;
  private List<ExecutedTest> failedTestsWithErrorDetail;
  private List<String> failedTests;
  private List<ExecutedTest> passedTestsWithErrorDetail;
  private List<String> passedTests;
  private List<String> skippedTests;

  public TestDataProvider(Run<?, ?> run, boolean recordPassingTests, boolean listSkippedTests)
  {
    super(run);
    this.recordPassingTests = recordPassingTests;
    this.listSkippedTests = listSkippedTests;

    data = new JSONObject();
    data.element(TEST_RESULTS, toJson());
  }

  public static class ExecutedTest
  {

    private final String fullName, errorDetails;
    private final float duration;

    public ExecutedTest(String fullName, String errorDetails, float duration)
    {
      super();
      this.fullName = fullName;
      this.errorDetails = errorDetails;
      this.duration = duration;
    }

    public String getFullName()
    {
      return fullName;
    }

    public String getErrorDetails()
    {
      return errorDetails;
    }

    public float getDuration()
    {
      return duration;
    }
  }

  private void testListFill(List<? extends TestResult> testResults, List<String> testNames,
      List<ExecutedTest> testDetails)
  {
    for (TestResult result : testResults)
    {
      testNames.add(result.getFullName());
      if (testDetails != null)
      {
        testDetails.add(new ExecutedTest(result.getFullName(), result.getErrorDetails(), result.getDuration()));
      }
    }
  }

  /**
   * Extracts the results from the testResultAction
   *
   * @return true if data was extracted, false otherwise
   */
  private boolean extractTestResults()
  {
    if (testResultAction == null)
    {
      return false;
    }
    LOGGER.log(Level.INFO, "Extracting test results");

    /*
     * If the number of total tests hasn't changed, assume that there are no new tests
     * this is relevant for jobs where we can have multiple invocations of JUnitResultsArchiver, i.e. pipeline
     * jobs
     */
    if (testResultAction.getTotalCount() == totalCount)
    {
      return false;
    }

    totalCount = testResultAction.getTotalCount();
    skipCount = testResultAction.getSkipCount();
    failCount = testResultAction.getFailCount();
    passCount = totalCount - skipCount - failCount;

    failedTests = new ArrayList<>();
    failedTestsWithErrorDetail = new ArrayList<>();
    testListFill(testResultAction.getFailedTests(), failedTests,
        failedTestsWithErrorDetail);

    if (recordPassingTests)
    {
      passedTests = new ArrayList<>();
      passedTestsWithErrorDetail = new ArrayList<>();
      testListFill(testResultAction.getPassedTests(), passedTests, passedTestsWithErrorDetail);
    }
    else
    {
      passedTests = Collections.emptyList();
      passedTestsWithErrorDetail = Collections.emptyList();
    }

    if (listSkippedTests)
    {
      skippedTests = new ArrayList<>();
      testListFill(testResultAction.getSkippedTests(), skippedTests, null);
    }
    else
    {
      skippedTests = Collections.emptyList();
    }
    return true;
  }

  public int getTotalCount()
  {
    return totalCount;
  }

  public int getSkipCount()
  {
    return skipCount;
  }

  public int getFailCount()
  {
    return failCount;
  }

  public int getPassCount()
  {
    return passCount;
  }

  public List<ExecutedTest> getFailedTestsWithErrorDetail()
  {
    return failedTestsWithErrorDetail;
  }

  public List<String> getFailedTests()
  {
    return failedTests;
  }

  /*
   * Looking at the JUnit plugin, it seems it is assumed that each build has only one
   * AbstractTestResultAction.
   * Do we have other plugins that extend AbstractTestResultAction and add it as another action
   * and do not merge the results?
   */
  private void updateTestResults()
  {
    LOGGER.log(Level.INFO, "Updating test results");
    if (testResultAction == null)
    {
      LOGGER.log(Level.INFO, "no test results yet");
      testResultAction = run.getAction(AbstractTestResultAction.class);
    }
    if (extractTestResults())
    {
      data.element(TEST_RESULTS, toJson());
    }
  }

  private JSONObject toJson()
  {
    Gson gson = new GsonBuilder().create();
    String json = gson.toJson(this);
    return JSONObject.fromObject(json);
  }

  @Override
  public JSONObject getData()
  {
    updateTestResults();
    if (testResultAction == null)
    {
      return null;
    }
    return data;
  }

}
