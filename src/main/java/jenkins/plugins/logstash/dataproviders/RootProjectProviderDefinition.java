package jenkins.plugins.logstash.dataproviders;

import java.io.Serializable;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Run;

public class RootProjectProviderDefinition extends DefaultDataProviderDefinition implements StartDataProvider, LineDataProvider, EndDataProvider
{

  private static final long serialVersionUID = 8751820442773040089L;

  @DataBoundConstructor
  public RootProjectProviderDefinition()
  {
  }

  @Override
  public DataProvider getDataProvider(Run<?, ?> run)
  {
    return new RootProjectProvider(run);
  }

  @Extension(ordinal=-60)
  @Symbol("RootProjectData")
  public static class RootDataDescriptor extends DefaultDataProviderDescriptor
  {

    @Override
    public String getDisplayName()
    {
      return "Root Project Data";
    }

    @Override
    public DataProviderDefinition getDefaultDataProviderDefinition()
    {
      return new RootProjectProviderDefinition();
    }
  }

}
