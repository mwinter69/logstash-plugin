package jenkins.plugins.logstash.dataproviders;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Run;

public class ExtendedProjectDataProviderDefinition extends DefaultDataProviderDefinition implements StartDataProvider, LineDataProvider, EndDataProvider
{

  private static final long serialVersionUID = 976097823480594667L;


  @DataBoundConstructor
  public ExtendedProjectDataProviderDefinition()
  {
  }

  @Override
  public DataProvider getDataProvider(Run<?, ?> run)
  {
    return new ExtendedProjectDataProvider(run);
  }


  @Extension(ordinal=-50)
  @Symbol("ExtendedProjectData")
  public static class ExtendedProjectDataDescriptor extends DefaultDataProviderDescriptor
  {

    @Override
    public String getDisplayName()
    {
      return "Extended Project Data";
    }

    @Override
    public DataProviderDefinition getDefaultDataProviderDefinition()
    {
      return new ExtendedProjectDataProviderDefinition();
    }
  }

}
