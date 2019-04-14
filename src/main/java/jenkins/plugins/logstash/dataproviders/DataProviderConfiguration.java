package jenkins.plugins.logstash.dataproviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class DataProviderConfiguration extends AbstractDescribableImpl<DataProviderConfiguration>
{

  private List<DataProviderDefinition> dataProviders;

  @DataBoundConstructor
  public DataProviderConfiguration()
  {
    dataProviders = new ArrayList<>();
  }

  public List<DataProviderDefinition> getDataProviders()
  {
    if (dataProviders == null)
    {
      return Collections.emptyList();
    }
    return dataProviders;
  }

  @DataBoundSetter
  public void setDataProviders(List<DataProviderDefinition> dataProviders)
  {
    if (dataProviders == null)
    {
      dataProviders = new ArrayList<>();
    }
    this.dataProviders = dataProviders;
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<DataProviderConfiguration>
  {
  }
}
