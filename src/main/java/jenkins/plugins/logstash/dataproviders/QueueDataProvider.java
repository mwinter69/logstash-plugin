package jenkins.plugins.logstash.dataproviders;

import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.model.Run;
import jenkins.metrics.impl.TimeInQueueAction;
import net.sf.json.JSONObject;

public class QueueDataProvider extends DataProvider
{
  private static Logger LOGGER = Logger.getLogger(QueueDataProvider.class.getName());


  public QueueDataProvider(Run<?, ?> run)
  {
    super(run);
  }

  @Override
  public JSONObject getStaticData()
  {
    TimeInQueueAction q = run.getAction(TimeInQueueAction.class);
    if (q != null)
    {
      JSONObject queue = new JSONObject();
      JSONObject data = new JSONObject();
      queue.element("blockedDurationMillis", q.getBlockedDurationMillis());
      queue.element("blockedTimeMillis", q.getBlockedTimeMillis());
      queue.element("buildableDurationMillis", q.getBuildableDurationMillis());
      queue.element("buildableTimeMillis", q.getBuildableTimeMillis());
      queue.element("executingTimeMillis", q.getExecutingTimeMillis());
      queue.element("executorUtilization", q.getExecutorUtilization());
      queue.element("queuingDurationMillis", q.getQueuingDurationMillis());
      queue.element("queuingTimeMillis", q.getQueuingTimeMillis());
      queue.element("subTaskCount", q.getSubTaskCount());
      queue.element("totalDurationMillis", q.getTotalDurationMillis());
      queue.element("waitingDurationMillis", q.getWaitingDurationMillis());
      queue.element("waitingTimeMillis", q.getWaitingTimeMillis());
      data.element("queue", queue);
      return data;
    }
    return null;
  }
}
