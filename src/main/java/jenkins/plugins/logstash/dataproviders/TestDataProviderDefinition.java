package jenkins.plugins.logstash.dataproviders;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Run;

/**
 * This provider collects the JUnit results of builds
 */
public class TestDataProviderDefinition extends DefaultDataProviderDefinition implements LineDataProvider, EndDataProvider
{
  private static final long serialVersionUID = 2640489929262704740L;
  private boolean recordPassingTests;
  private boolean listSkippedTests;

  @DataBoundConstructor
  public TestDataProviderDefinition(boolean recordPassingTests, boolean listSkippedTests)
  {
    this.recordPassingTests = recordPassingTests;
    this.listSkippedTests = listSkippedTests;
  }

  public boolean isRecordPassingTests()
  {
    return recordPassingTests;
  }

  public boolean isListSkippedTests()
  {
    return listSkippedTests;
  }

  @Override
  public DataProvider getDataProvider(Run<?, ?> run)
  {
    return new TestDataProvider(run, recordPassingTests, listSkippedTests);
  }

  @Extension(ordinal=-90)
  @Symbol("JUnitTestData")
  public static class TestDataDescriptor extends DefaultDataProviderDescriptor
  {

    @Override
    public String getDisplayName()
    {
      return "JUnit Test Data";
    }

    @Override
    public DataProviderDefinition getDefaultDataProviderDefinition()
    {
      return new TestDataProviderDefinition(false, false);
    }

  }
}
