package jenkins.plugins.logstash;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.Extension;
import hudson.console.ConsoleLogFilter;
import hudson.model.AbstractProject;
import hudson.model.Run;

@Extension(ordinal = 1000)
public class LogstashConsoleLogFilter extends ConsoleLogFilter implements Serializable
{

  private static final Logger LOGGER = Logger.getLogger(LogstashConsoleLogFilter.class.getName());

  private transient Run<?, ?> run;

  public LogstashConsoleLogFilter() {}

  public LogstashConsoleLogFilter(Run<?, ?> run)
  {
    this.run = run;
  }

  private static final long serialVersionUID = 1L;

  /**
   * {@inheritDoc}
   *
   * Currently pipeline is not supporting global ConsoleLogFilters. So even when we have it enabled globally
   * we would not log to an indexer without the logstash step.
   * But when pipeline supports global ConsoleLogFilters we don't want to log twice when we have the step in
   * the pipeline.
   * With the LogstashMarkerRunAction we can detect if it is enabled globally and if pipeline is supporting
   * global ConsoleLogFilters.
   * The LogstashMarkerRunAction will be only attached to a WorkflowRun when pipeline supports global
   * ConsoleLogFilters (JENKINS-45693). And the assumption is that the marker action is attached before the
   * logstashStep is initialized.
   * This marker action will also disable the notifier.
   *
   */
  @Override
  public OutputStream decorateLogger(Run build, OutputStream logger) throws IOException, InterruptedException
  {

    LogstashConfiguration configuration = LogstashConfiguration.getInstance();
    if (!configuration.isEnabled())
    {
      LOGGER.log(Level.FINE, "Logstash is disabled. Logs will not be forwarded.");
      return logger;
    }


    // A pipeline step uses the constructor which sets run.
    if (run != null)
    {
      if (run.getAction(LogstashMarkerAction.class) != null)
      {
        LOGGER.log(Level.FINEST, "Logstash is enabled globally. No need to decorate the logger another time for {0}",
            run.toString());
        return logger;
      }
      return getLogstashOutputStream(run, logger);
    }

    // Not pipeline step so @{code build} should be set.
    if (isLogstashEnabled(build))
    {
      if (build.getAction(LogstashMarkerAction.class) == null)
      {
        build.addAction(new LogstashMarkerAction());
      }
      return getLogstashOutputStream(build, logger);
    }

    return logger;
  }

  private LogstashOutputStream getLogstashOutputStream(Run<?, ?> run, OutputStream logger)
  {
    LogstashWriter logstash = getLogStashWriter(run, logger);
    return new LogstashOutputStream(logger, logstash);
  }

  LogstashWriter getLogStashWriter(Run<?, ?> build, OutputStream errorStream)
  {
    return new LogstashWriter(build, errorStream, null, build.getCharset());
  }

  private boolean isLogstashEnabledGlobally()
  {
    LogstashConfiguration configuration = LogstashConfiguration.getInstance();
    if (configuration.isEnableGlobally())
    {
      return true;
    }
    return false;
  }

  private boolean isLogstashEnabled(Run<?, ?> build)
  {
    if (build == null)
    {
      return false;
    }

    if (isLogstashEnabledGlobally())
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
