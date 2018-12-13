package jenkins.plugins.logstash.dataproviders;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Run;
import jenkins.metrics.impl.TimeInQueueAction;

public class QueueDataProviderDefinition extends DataProviderDefinition implements EndDataProvider
{
  private static final long serialVersionUID = -1653413163469654223L;

  @DataBoundConstructor
  public QueueDataProviderDefinition()
  {
  }

  @Override
  public DataProvider getDataProvider(Run<?, ?> run)
  {
    return new QueueDataProvider(run);
  }


  @Extension
  @Symbol("queueData")
  public static class DescriptorImpl extends DataProviderDescriptor
  {
    @Override
    public boolean isApplicable()
    {
      try
      {
        Class.forName("jenkins.metrics.impl.TimeInQueueAction");
        return true;
      }
      catch (ClassNotFoundException e)
      {
        return false;
      }
    }

    @Override
    public String getDisplayName()
    {
      return "Queue Data";
    }
  }
}
