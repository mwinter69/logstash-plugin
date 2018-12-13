package jenkins.plugins.logstash.pipeline;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.logstash.LogstashWriter;
import jenkins.plugins.logstash.Messages;
import jenkins.plugins.logstash.dataproviders.DataProviderConfiguration;
import jenkins.plugins.logstash.dataproviders.DataProviderDefinition;

/**
 * Sends the tail of the log in a single event to a logstash indexer.
 * Pipeline counterpart of the LogstashNotifier.
 */
public class LogstashSendStep extends Step
{

  private int maxLines;
  private boolean failBuild;

  private List<DataProviderDefinition> dataProviders;

  @DataBoundConstructor
  public LogstashSendStep(int maxLines, boolean failBuild)
  {
    this.maxLines = maxLines;
    this.failBuild = failBuild;
  }

  public int getMaxLines()
  {
    return maxLines;
  }

  public boolean isFailBuild()
  {
    return failBuild;
  }

  public List<DataProviderDefinition> getDataProviders()
  {
    return dataProviders;
  }

  @DataBoundSetter
  public void setDataProviders(List<DataProviderDefinition> dataProviders)
  {
    this.dataProviders = dataProviders;
  }

  @Override
  public StepExecution start(StepContext context) throws Exception
  {
    return new Execution(context, maxLines, failBuild, dataProviders);
  }

  @SuppressFBWarnings(value="SE_TRANSIENT_FIELD_NOT_RESTORED", justification="Only used when starting.")
  private static class Execution extends SynchronousNonBlockingStepExecution<Void>
  {

    private static final long serialVersionUID = 1L;

    private transient final int maxLines;
    private transient final boolean failBuild;

    private List<DataProviderDefinition> dataProviders;

    Execution(StepContext context, int maxLines, boolean failBuild,  List<DataProviderDefinition> dataProviders)
    {
      super(context);
      this.maxLines = maxLines;
      this.failBuild = failBuild;
      this.dataProviders = dataProviders;
    }

    @Override
    protected Void run() throws Exception
    {
      Run<?, ?> run = getContext().get(Run.class);
      TaskListener listener = getContext().get(TaskListener.class);
      PrintStream errorStream = listener.getLogger();

      LogstashWriter logstash = new LogstashWriter(run, errorStream, listener, run.getCharset(), dataProviders);
      logstash.writeBuildLog(maxLines);
      if (failBuild && logstash.isConnectionBroken())
      {
        throw new Exception("Failed to send data to Indexer");
      }
      return null;
    }

  }

  @Extension
  public static class DescriptorImpl extends StepDescriptor
  {

    /** {@inheritDoc} */
    @Override
    public String getDisplayName()
    {
      return Messages.DisplayName();
    }

    @Override
    public String getFunctionName()
    {
      return "logstashSend";
    }

    @Override
    public Set<? extends Class<?>> getRequiredContext()
    {
      Set<Class<?>> contexts = new HashSet<>();
      contexts.add(TaskListener.class);
      contexts.add(Run.class);
      return contexts;
    }
  }
}
