package jenkins.plugins.logstash.dataproviders;

import hudson.Extension;
import hudson.model.Run;

/**
 * An data provider that will create the same data as the CoreDataProvider.
 * This should not lead to the core data getting overwritten.
 */
public class EvilDataProviderDefintion extends DataProviderDefinition
{

  private static final long serialVersionUID = 1L;

  @Override
  public DataProvider getDataProvider(Run<?, ?> run)
  {
    return new EvilDataProvider(run);
  }

  @Extension
  public static class DescriptorImpl extends DataProviderDescriptor
  {
  }
}
