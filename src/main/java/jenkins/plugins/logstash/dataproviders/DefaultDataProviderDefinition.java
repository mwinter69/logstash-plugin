package jenkins.plugins.logstash.dataproviders;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * A DataProvider for default data. These are the DataProviders that simulate the old legacy behavior
 * when you do not configure the DataProviders.
 */
@Restricted(NoExternalUse.class)
public abstract class DefaultDataProviderDefinition extends DataProviderDefinition
{

  public abstract static class DefaultDataProviderDescriptor extends DataProviderDescriptor
  {
    public abstract DataProviderDefinition getDefaultDataProviderDefinition();
  }
}
