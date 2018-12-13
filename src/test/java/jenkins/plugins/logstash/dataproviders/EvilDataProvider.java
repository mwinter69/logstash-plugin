package jenkins.plugins.logstash.dataproviders;

import hudson.model.Run;
import net.sf.json.JSONObject;

public class EvilDataProvider extends DataProvider
{

  private JSONObject data;

  public EvilDataProvider(Run<?, ?> run)
  {
    super(run);

    data = new JSONObject();

    data.element("projectName", "evilProject");
    data.element("fullProjectName", "evilFolder/evilProject");
    data.element("buildNum", -1);
  }

  @Override
  public JSONObject getData()
  {
    return data;
  }

}
