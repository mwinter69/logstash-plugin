package jenkins.plugins.logstash.dataproviders;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import jenkins.plugins.logstash.LogstashConfiguration;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*"})
@PrepareForTest(LogstashConfiguration.class)
public class BuildDataTest
{
  @Rule
  JenkinsRule j = new JenkinsRule();

  @Mock
  Run run;
  @Mock
  AbstractBuild build;
  @Mock
  AbstractBuild rootBuild;
  @Mock
  Job job;
  @Mock
  AbstractProject project;
  @Mock
  Date mockDate;
  @Mock
  AbstractTestResultAction mockTestResultAction;
  @Mock
  TestResult failedTest, skippedTest, passedTest;
  @Mock LogstashConfiguration logstashConfiguration;

  private List<DataProviderDefinition> dataProviderDefinitions;
  private Date start;

  @Before
  public void setup()
  {
    PowerMockito.mockStatic(LogstashConfiguration.class);
    when(LogstashConfiguration.getInstance()).thenReturn(logstashConfiguration);
    when(logstashConfiguration.getDateFormatter()).thenCallRealMethod();

    start = new Date();
    dataProviderDefinitions = new ArrayList<>();
    when(run.getParent()).thenReturn(job);
    when(run.getStartTimeInMillis()).thenReturn(start.getTime());
    when(job.getName()).thenReturn("myJob");
    when(job.getFullName()).thenReturn("myFolder/myJob");
    when(run.getNumber()).thenReturn(99);
    when(mockDate.getTime()).thenReturn(60L);

    when(build.getParent()).thenReturn(job);
    when(build.getStartTimeInMillis()).thenReturn(start.getTime());
    when(build.getNumber()).thenReturn(99);

  }


  @Test
  public void coreData() throws IOException, InterruptedException
  {
    List<DataProvider> dataProviders = DataProviderDefinition.getEffectiveDataProviders(run, dataProviderDefinitions);
    BuildData buildData = new BuildData(dataProviders, "Now");
    JSONObject json = buildData.toJson();
    assertCoreData(json);
    when(run.getResult()).thenReturn(Result.SUCCESS);
    json = buildData.toJson();
    assertThat(json.get("result"), equalTo("SUCCESS"));
    assertThat(json.get("buildDuration").toString(),matchesPattern("\\d+"));
  }

  private void assertCoreData(JSONObject json)
  {
    assertThat(json.get("projectName"), equalTo("myJob"));
    assertThat(json.get("fullProjectName"), equalTo("myFolder/myJob"));
    assertThat(json.get("buildNum"), equalTo(99));
    assertThat(json.has("result"),equalTo(false));
  }

  @Test
  public void evilData() throws IOException, InterruptedException
  {
    dataProviderDefinitions.add(new EvilDataProviderDefintion());
    List<DataProvider> dataProviders = DataProviderDefinition.getEffectiveDataProviders(run, dataProviderDefinitions);

    BuildData buildData = new BuildData(dataProviders, "Now");
    JSONObject json = buildData.toJson();
    assertCoreData(json);
  }

  @Test
  public void extendedData() throws IOException, InterruptedException
  {
    when(run.getId()).thenReturn("123456");
    when(run.getDisplayName()).thenReturn("BuildData Test");
    when(run.getFullDisplayName()).thenReturn("BuildData Test #123456");
    when(run.getUrl()).thenReturn("http://localhost:8080/jenkins/jobs/PROJECT_NAME/123456");
    when(run.getDescription()).thenReturn("Mock project for testing BuildData");

    dataProviderDefinitions.add(new ExtendedProjectDataProviderDefinition());
    List<DataProvider> dataProviders = DataProviderDefinition.getEffectiveDataProviders(run, dataProviderDefinitions);
    BuildData buildData = new BuildData(dataProviders, "Now");
    JSONObject json = buildData.toJson();
    assertCoreData(json);
    assertThat(json.get("buildHost"), equalTo("master"));
    assertThat(json.get("buildLabel"), equalTo("master"));
    assertThat(json.get("id"), equalTo("123456"));
    assertThat(json.get("displayName"), equalTo("BuildData Test"));
    assertThat(json.get("fullDisplayName"), equalTo("BuildData Test #123456"));
    assertThat(json.get("url"), equalTo("http://localhost:8080/jenkins/jobs/PROJECT_NAME/123456"));
    assertThat(json.get("id"), equalTo("123456"));
  }

  @Test
  public void rootProjectData() throws IOException, InterruptedException
  {
    when(build.getRootBuild()).thenReturn(rootBuild);
    when(rootBuild.getProject()).thenReturn(project);
    when(project.getName()).thenReturn("rootProject");
    when(project.getFullName()).thenReturn("full/rootProject");
    when(rootBuild.getDisplayName()).thenReturn("Root Project");
    when(rootBuild.getNumber()).thenReturn(321);

    dataProviderDefinitions.add(new RootProjectProviderDefinition());
    List<DataProvider> dataProviders = DataProviderDefinition.getEffectiveDataProviders(build, dataProviderDefinitions);
    BuildData buildData = new BuildData(dataProviders, "Now");
    JSONObject json = buildData.toJson();
    assertCoreData(json);
    assertThat(json.get("rootProjectName"), equalTo("rootProject"));
    assertThat(json.get("rootFullProjectName"), equalTo("full/rootProject"));
    assertThat(json.get("rootProjectDisplayName"), equalTo("Root Project"));
    assertThat(json.get("rootBuildNum"), equalTo(321));
  }

  @Test
  public void testData() throws IOException, InterruptedException
  {
    List<TestResult> failedTests = new ArrayList<>();
    List<TestResult> skippedTests = new ArrayList<>();
    List<TestResult> passedTests = new ArrayList<>();
    failedTests.add(failedTest);
    skippedTests.add(skippedTest);
    passedTests.add(passedTest);
    when(run.getAction(AbstractTestResultAction.class)).thenReturn(mockTestResultAction);
    when(mockTestResultAction.getTotalCount()).thenReturn(3);
    when(mockTestResultAction.getSkipCount()).thenReturn(1);
    when(mockTestResultAction.getFailCount()).thenReturn(1);
    when(mockTestResultAction.getFailedTests()).thenReturn(failedTests);
    when(mockTestResultAction.getPassedTests()).thenReturn(passedTests);
    when(mockTestResultAction.getSkippedTests()).thenReturn(skippedTests);
    when(failedTest.getFullName()).thenReturn("ThisTestFailed");
    when(failedTest.getDuration()).thenReturn((float)0.5);
    when(failedTest.getErrorDetails()).thenReturn("Just an Error");
    when(passedTest.getFullName()).thenReturn("ThisTestPassed");
    when(passedTest.getDuration()).thenReturn((float)1.5);
    when(skippedTest.getFullName()).thenReturn("TestIsSkipped");

    dataProviderDefinitions.add(new TestDataProviderDefinition(true, true));
    List<DataProvider> dataProviders = DataProviderDefinition.getEffectiveDataProviders(run, dataProviderDefinitions);
    BuildData buildData = new BuildData(dataProviders, "Now");
    JSONObject json = buildData.toJson();
    assertCoreData(json);
    JSONObject testResult = json.getJSONObject("testResults");

    assertThat(testResult.get("totalCount"), equalTo(3));
    assertThat(testResult.get("skipCount"), equalTo(1));
    assertThat(testResult.get("failCount"), equalTo(1));
    assertThat(testResult.get("passCount"), equalTo(1));

    JSONArray jsonFailedTests = testResult.getJSONArray("failedTests");
    assertThat(jsonFailedTests.size(),equalTo(1));
    assertThat(jsonFailedTests.get(0), equalTo("ThisTestFailed"));

    JSONArray jsonSkippedTests = testResult.getJSONArray("skippedTests");
    assertThat(jsonSkippedTests.size(),equalTo(1));
    assertThat(jsonSkippedTests.get(0), equalTo("TestIsSkipped"));

    JSONArray jsonPassedTests = testResult.getJSONArray("passedTests");
    assertThat(jsonPassedTests.size(),equalTo(1));
    assertThat(jsonPassedTests.get(0), equalTo("ThisTestPassed"));

    JSONArray passedTestsWithErrorDetail = testResult.getJSONArray("passedTestsWithErrorDetail");
    assertThat(passedTestsWithErrorDetail.size(),equalTo(1));
    JSONObject jsonPassedTest = passedTestsWithErrorDetail.getJSONObject(0);
    assertThat(jsonPassedTest.get("fullName"), equalTo("ThisTestPassed"));
    assertThat(jsonPassedTest.get("duration"), equalTo(1.5));

    JSONArray failedTestsWithErrorDetail = testResult.getJSONArray("failedTestsWithErrorDetail");
    assertThat(failedTestsWithErrorDetail.size(),equalTo(1));
    JSONObject jsonFailedTest = failedTestsWithErrorDetail.getJSONObject(0);
    assertThat(jsonFailedTest.get("fullName"), equalTo("ThisTestFailed"));
    assertThat(jsonFailedTest.get("errorDetails"), equalTo("Just an Error"));
    assertThat(jsonFailedTest.get("duration"), equalTo(0.5));

  }

  @Test
  public void buildEnvRun() throws IOException, InterruptedException
  {
    Map<String, String> vars = new HashMap<>();
    vars.put("myVar", "myValue");
    EnvVars env = new EnvVars(vars);
    env.put("var2", "value");

    when(run.getEnvironment(null)).thenReturn(env);

    dataProviderDefinitions.add(new BuildEnvProviderDefinition());
    List<DataProvider> dataProviders = DataProviderDefinition.getEffectiveDataProviders(run, dataProviderDefinitions);
    BuildData buildData = new BuildData(dataProviders, "now");
    JSONObject json = buildData.toJson();
    assertCoreData(json);
    assertThat(json.has("sensitiveBuildVariables"), equalTo(false));
    JSONObject buildVariables = json.getJSONObject("buildVariables");
    assertThat(buildVariables.getString("myVar"), equalTo("myValue"));
    assertThat(buildVariables.getString("var2"), equalTo("value"));
    assertThat(buildVariables.size(), equalTo(2));
  }

  @Test
  public void buildEnvAbstractBuild() throws IOException, InterruptedException
  {
    Map<String, String> vars = new HashMap<>();
    Set<String> sensitiveBuildVariables = new HashSet<>();
    sensitiveBuildVariables.add("sensitive");
    vars.put("myVar", "myValue");
    EnvVars env = new EnvVars(vars);
    env.put("var2", "value");
    env.put("sensitive", "secret");

    when(build.getEnvironment(null)).thenReturn(env);
    when(build.getSensitiveBuildVariables()).thenReturn(sensitiveBuildVariables);

    dataProviderDefinitions.add(new BuildEnvProviderDefinition());
    List<DataProvider> dataProviders = DataProviderDefinition.getEffectiveDataProviders(build, dataProviderDefinitions);
    BuildData buildData = new BuildData(dataProviders, "now");

    JSONObject json = buildData.toJson();
    assertCoreData(json);
    assertThat(json.has("sensitiveBuildVariables"), equalTo(true));
    JSONObject buildVariables = json.getJSONObject("buildVariables");
    assertThat(buildVariables.getString("myVar"), equalTo("myValue"));
    assertThat(buildVariables.getString("var2"), equalTo("value"));
    assertThat(buildVariables.size(), equalTo(2));
    JSONArray sensitive = json.getJSONArray("sensitiveBuildVariables");
    assertThat(sensitive.size(), equalTo(1));
    assertThat(sensitive.get(0), equalTo("sensitive"));
  }
}
