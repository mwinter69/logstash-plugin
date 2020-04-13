package jenkins.plugins.logstash.pipeline;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.log.TaskListenerDecorator;

import hudson.Extension;
import hudson.model.Queue;
import hudson.model.Run;
import jenkins.plugins.logstash.LogstashConfiguration;
import jenkins.plugins.logstash.LogstashOutputStream;
import jenkins.plugins.logstash.LogstashWriter;

public class GlobalDecorator extends TaskListenerDecorator
{
  private static final Logger LOGGER = Logger.getLogger(GlobalDecorator.class.getName());

  private static final long serialVersionUID = 1L;

  private transient Run<?, ?> run;

  public GlobalDecorator(Run<?, ?> run)
  {
    LOGGER.log(Level.INFO, "Creating decorator for {0}", run.toString());
    this.run = run;
  }

  @Override
  public OutputStream decorate(OutputStream logger) throws IOException, InterruptedException
  {
    LogstashWriter writer = new LogstashWriter(run, logger, null, StandardCharsets.UTF_8);
    LogstashOutputStream out = new LogstashOutputStream(logger, writer);
    return out;
  }

  @Extension
  public static final class Factory implements TaskListenerDecorator.Factory {

      @Override
      public TaskListenerDecorator of(FlowExecutionOwner owner)
      {
        if (!LogstashConfiguration.getInstance().isEnableGlobally())
        {
          return null;
        }
        try {
          Queue.Executable executable = owner.getExecutable();
          if (executable instanceof Run) { // we need at least getStartTimeInMillis
              return new GlobalDecorator((Run<?, ?>) executable);
          }
      } catch (IOException x) {
          LOGGER.log(Level.WARNING, null, x);
      }
      return null;      }
  }


}
