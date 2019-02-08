package jenkins.plugins.logstash.dataproviders;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.model.Run;

/**
 * Provides data about the BuildEnvironment
 * This provider can include data during the start event as it s
 */
public class BuildEnvProviderDefinition extends DefaultDataProviderDefinition implements LineDataProvider
{

  private static final long serialVersionUID = 2557285210097474907L;
  private boolean elasticSearchCompatibility = false;

  @DataBoundConstructor
  public BuildEnvProviderDefinition()
  {
  }

  public boolean isElasticSearchCompatibility()
  {
    return elasticSearchCompatibility;
  }

  @DataBoundSetter
  public void setElasticSearchCompatibility(boolean elasticSearchCompatibility)
  {
    this.elasticSearchCompatibility = elasticSearchCompatibility;
  }

  @Override
  public DataProvider getDataProvider(Run<?, ?> run)
  {
    return new BuildEnvProvider(run, null, elasticSearchCompatibility);
  }

  @Extension(ordinal=-80)
  @Symbol("BuildEnv")
  public static class BuildEnvDescriptor extends DefaultDataProviderDescriptor
  {

    @Override
    public String getDisplayName()
    {
      return "Build Environment";
    }

    @Override
    public DataProviderDefinition getDefaultDataProviderDefinition()
    {
      return new BuildEnvProviderDefinition();
    }
  }
}

