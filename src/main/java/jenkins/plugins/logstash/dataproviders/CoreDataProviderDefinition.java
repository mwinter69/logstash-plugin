package jenkins.plugins.logstash.dataproviders;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Run;

/**
 * This provider contributes the core data of a build
 * project name, full project name, build number, duration and the result.
 * It can't be disabled as this data is essential for correlating the events in an indexer
 */
public class CoreDataProviderDefinition extends DataProviderDefinition implements StartDataProvider, LineDataProvider, EndDataProvider
{

  private static final long serialVersionUID = 7832728184305500772L;

  @DataBoundConstructor
  public CoreDataProviderDefinition()
  {
  }

  @Override
  public DataProvider getDataProvider(Run<?, ?> run)
  {
    return new CoreDataProvider(run);
  }

  @Extension(ordinal=-100)
  @Symbol("CoreData")
  public static class CoreDataDescriptor extends DataProviderDescriptor
  {

    @Override
    public String getDisplayName()
    {
      return "Core Data";
    }
  }

}
