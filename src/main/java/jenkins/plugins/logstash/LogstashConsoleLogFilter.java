package jenkins.plugins.logstash;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.Extension;
import hudson.console.ConsoleLogFilter;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import jenkins.plugins.logstash.dataproviders.DataProviderDefinition;
import jenkins.plugins.logstash.dataproviders.LineDataProvider;

@Extension(ordinal = 1000)
public class LogstashConsoleLogFilter extends ConsoleLogFilter implements Serializable
{

  private static final Logger LOGGER = Logger.getLogger(LogstashConsoleLogFilter.class.getName());

  private transient Run<?, ?> run;
  private transient List<DataProviderDefinition> dataProviders;

  public LogstashConsoleLogFilter()
  {}

  public LogstashConsoleLogFilter(Run<?, ?> run, List<DataProviderDefinition> dataProviders)
  {
    this.run = run;
    this.dataProviders = dataProviders;
    LOGGER.log(Level.INFO, "Initializing ConsoleLogFilter.");
  }

  private static final long serialVersionUID = 1L;

  @Override
  public OutputStream decorateLogger(Run build, OutputStream logger) throws IOException, InterruptedException
  {
    LOGGER.log(Level.INFO, "Decorating ConsoleLogFilter.");
    LogstashConfiguration configuration = LogstashConfiguration.getInstance();
    if (!configuration.isEnabled())
    {
      LOGGER.log(Level.FINE, "Logstash is disabled. Logs will not be forwarded.");
      return logger;
    }

    if (build != null && build instanceof AbstractBuild<?, ?>)
    {
      if (isLogstashEnabled(build))
      {
        LogstashWriter logstash = getLogStashWriter(build, logger);
        return new LogstashOutputStream(logger, logstash);
      }
      else
      {
        return logger;
      }
    }
    if (run != null)
    {
      LogstashWriter logstash = getLogStashWriter(run, logger);
      return new LogstashOutputStream(logger, logstash);
    }
    else
    {
      return logger;
    }
  }

  LogstashWriter getLogStashWriter(Run<?, ?> build, OutputStream errorStream)
  {
    List<DataProviderDefinition> dataProviders = getEffectiveDataProviderDefinitions(build);
    return new LogstashWriter(build, errorStream, null, build.getCharset(), dataProviders);
  }

  private List<DataProviderDefinition> getEffectiveDataProviderDefinitions(Run<?, ?> build)
  {
    List<DataProviderDefinition> dataProviders = null;

    if (build.getParent() instanceof AbstractProject)
    {
      AbstractProject<?, ?> project = (AbstractProject<?, ?>)build.getParent();
      LogstashJobProperty property = project.getProperty(LogstashJobProperty.class);
      if (property != null && property.getDataProviderConfiguration() != null)
      {
        dataProviders = property.getDataProviderConfiguration().getDataProviders();
      }
    }
    else
    {
      dataProviders = this.dataProviders;
    }

    dataProviders = filterLineDataProviders(dataProviders);

    return dataProviders;
  }

  private List<DataProviderDefinition> filterLineDataProviders(
      List<DataProviderDefinition> dataProviderDefinitions)
  {
    if (dataProviderDefinitions == null)
    {
      return null;
    }

    List<DataProviderDefinition> dataProviders = new ArrayList<>();
    for (DataProviderDefinition provider: dataProviderDefinitions)
    {
      if (provider instanceof LineDataProvider)
      {
        dataProviders.add(provider);
      }
    }
    return dataProviders;
  }

  private boolean isLogstashEnabled(Run<?, ?> build)
  {
    LogstashConfiguration configuration = LogstashConfiguration.getInstance();
    if (configuration.isEnableGlobally())
    {
      return true;
    }

    if (build.getParent() instanceof AbstractProject)
    {
      AbstractProject<?, ?> project = (AbstractProject<?, ?>)build.getParent();
      if (project.getProperty(LogstashJobProperty.class) != null)
      {
        return true;
      }
    }
    return false;
  }

}
