package jenkins.plugins.logstash.dataproviders;

import java.util.Date;

import hudson.model.Result;
import hudson.model.Run;
import net.sf.json.JSONObject;

/**
 * This provider contributes the core data of a build
 * project name, full project name, build number, duration and the result.
 * It can't be disabled as this data is essential for correlating the events in an indexer
 */
public class CoreDataProvider extends DataProvider
{
  private JSONObject data;

  public CoreDataProvider(Run<?, ?> run)
  {
    super(run);
    initData(run);
  }

  private void initData(Run<?, ?> run)
  {
    data = new JSONObject();

    data.element("projectName", run.getParent().getName());
    data.element("fullProjectName", run.getParent().getFullName());
    data.element("buildNum", run.getNumber());

    updateResult();
  }

  private void updateResult()
  {
    long buildDuration = (new Date()).getTime() - run.getStartTimeInMillis();
    Result result = run.getResult();

    data.element("buildDuration", buildDuration);
    if (result != null)
    {
      data.element("result", result.toString());
    }
  }

  @Override
  public JSONObject getData()
  {
    updateResult();
    return data;
  }

}
