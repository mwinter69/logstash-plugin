package jenkins.plugins.logstash.dataproviders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import jenkins.model.Jenkins;
import jenkins.plugins.logstash.LogstashConfiguration;
import jenkins.plugins.logstash.dataproviders.CoreDataProviderDefinition.CoreDataDescriptor;
import jenkins.plugins.logstash.dataproviders.DefaultDataProviderDefinition.DefaultDataProviderDescriptor;

/**
 * A DataProvider defines which data should be sent for to the different kinds of events.
 * In line mode a single event is sent when the build starts, for each individual log line and a single event
 * after the build has finished.
 */
public abstract class DataProviderDefinition extends AbstractDescribableImpl<DataProviderDefinition>
    implements ExtensionPoint, Describable<DataProviderDefinition>, Serializable
{

  private static final Logger LOGGER = Logger.getLogger(DataProviderDefinition.class.getName());

  @Override
  public DataProviderDescriptor getDescriptor()
  {
    return (DataProviderDescriptor) super.getDescriptor();
  }

  /**
   * Creates an executor that is called whenever an event is sent
   *
   * @param run
   * @return The DataProviderExecutor or {code null} then no dynamic data is to be included.
   */
  public abstract DataProvider getDataProvider(Run<?, ?> run);

  public static abstract class DataProviderDescriptor extends Descriptor<DataProviderDefinition>
  {
    public boolean isApplicable()
    {
      return true;
    }
  }

  /**
   * Returns all DataProviderDescriptor except the CoreDataDescriptor.
   * CoreData is automatically added to the end and has no configuration.
   *
   * @return
   */
  public static List<DataProviderDescriptor> all()
  {
    List<DataProviderDescriptor> descriptors = new ArrayList<>();
    DescriptorExtensionList<DataProviderDefinition, DataProviderDescriptor> all = Jenkins.getInstance()
        .<DataProviderDefinition, DataProviderDescriptor> getDescriptorList(DataProviderDefinition.class);
    descriptors.addAll(all);
    CoreDataDescriptor core = all.get(CoreDataDescriptor.class);
    descriptors.remove(core);
    return descriptors;
  }

  /**
   * Returns the List of effective data provider definitions
   * In case no data providers are explicitly configured for a job, the notifier or the pipeline steps, the global
   * configuration is used. If this as well returns null the default data provider definitions are returned.
   *
   * @param dataProviders
   * @return
   */
  private static List<DataProviderDefinition> getEffectiveDataProviderDefinitions(List<DataProviderDefinition> dataProviders)
  {
    List<DataProviderDefinition> effectiveDataProviders = new ArrayList<>();
    if (dataProviders != null)
    {
      LOGGER.log(Level.INFO, "DataProviders from argument set.");
      effectiveDataProviders.addAll(dataProviders);
      effectiveDataProviders.add(new CoreDataProviderDefinition());
      return effectiveDataProviders;
    }

    dataProviders = LogstashConfiguration.getInstance().getDataProviders();
    if (dataProviders != null)
    {
      LOGGER.log(Level.INFO, "Centrally configured DataProviders are set.");
      effectiveDataProviders.addAll(dataProviders);
      effectiveDataProviders.add(new CoreDataProviderDefinition());
      return effectiveDataProviders;
    }

    LOGGER.log(Level.INFO, "Falling back to default data providers.");

    for (DataProviderDescriptor definition: all())
    {
      if (definition instanceof DefaultDataProviderDescriptor)
      {
        DefaultDataProviderDescriptor defaultDescriptor = (DefaultDataProviderDescriptor) definition;
        effectiveDataProviders.add(defaultDescriptor.getDefaultDataProviderDefinition());
      }
    }

    effectiveDataProviders.add(new CoreDataProviderDefinition());
    return effectiveDataProviders;
  }

  public static List<DataProvider> getEffectiveDataProviders(Run<?, ?> run, List<DataProviderDefinition> dataProviderDefinitions)
  {
    List<DataProvider> dataProviders = new ArrayList<>();
    for (DataProviderDefinition definition: getEffectiveDataProviderDefinitions(dataProviderDefinitions))
    {
      if (definition.getDescriptor().isApplicable())
      {
        dataProviders.add(definition.getDataProvider(run));
      }
    }
    return dataProviders;
  }
}
