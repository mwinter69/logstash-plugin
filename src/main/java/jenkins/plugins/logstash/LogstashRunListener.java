package jenkins.plugins.logstash;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.acegisecurity.ui.AbstractProcessingFilter;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;

@Extension
public class LogstashRunListener extends RunListener<Run<?,?>>
{

  private static final Logger LOGGER = Logger.getLogger(LogstashRunListener.class.getName());

  @Override
  public void onFinalized(Run<?, ?> run)
  {
    LogstashConfiguration config = LogstashConfiguration.getInstance();
    if (config.isEnabled() && config.getGlobalMode() == GloballyEnabledMode.NOTIFIERMODE)
    {
      LOGGER.log(Level.INFO, "Notifier mode is enabled");
      if (run.getParent() instanceof AbstractProject)
      {
        LOGGER.log(Level.INFO, "Abstract Project");
        AbstractProject<?, ?> project = (AbstractProject<?, ?>) run.getParent();
        //don't run if project has a the wrapper explicitly enabled or disabled global settings
        if (project.getProperty(LogstashJobProperty.class) != null)
        {
          LOGGER.log(Level.INFO, "Job Property is set. Disabling global notifier mode.");
          return;
        }
        //don't run if project has a the notifier explicitly enabled
        if (PluginImpl.getLogstashNotifier(project) != null)
        {
          LOGGER.log(Level.INFO, "Job has explicit Notifier. Disabling global notifier mode.");
          return;
        }
      }
      LogstashWriter logstash = new LogstashWriter(run, null, null, run.getCharset());
      logstash.writeBuildLog(config.getMaxLines());
    }
  }
}
