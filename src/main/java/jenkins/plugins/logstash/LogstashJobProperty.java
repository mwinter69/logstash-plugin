package jenkins.plugins.logstash;

import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.DescribableList;
import jenkins.model.OptionalJobProperty;
import jenkins.plugins.logstash.dataproviders.DataProviderConfiguration;
import jenkins.plugins.logstash.dataproviders.DataProviderDefinition;
import jenkins.plugins.logstash.dataproviders.DataProviderDefinition.DataProviderDescriptor;
import net.sf.json.JSONObject;

/**
 * This JobProperty is a marker to decide if logs should be sent to an indexer.
 *
 */
public class LogstashJobProperty extends OptionalJobProperty<Job<?, ?>>
{

  private DataProviderConfiguration dataProviderConfiguration;

  @DataBoundConstructor
  public LogstashJobProperty()
  {}

  public DataProviderConfiguration getDataProviderConfiguration()
  {
    return dataProviderConfiguration;
  }

  @DataBoundSetter
  public void setDataProviderConfiguration(DataProviderConfiguration dataProviderConfiguration)
  {
    this.dataProviderConfiguration = dataProviderConfiguration;
  }

  @Extension
  public static class DescriptorImpl extends OptionalJobPropertyDescriptor
  {

    @Override
    public String getDisplayName()
    {
      return Messages.DisplayName();
    }

    @Override
    public boolean isApplicable(Class<? extends Job> jobType)
    {
      return AbstractProject.class.isAssignableFrom(jobType);
    }
  }
}
