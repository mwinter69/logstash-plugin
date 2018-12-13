package jenkins.plugins.logstash.dataproviders;

import org.apache.commons.lang.StringUtils;

import hudson.model.Executor;
import hudson.model.Node;
import hudson.model.Run;
import net.sf.json.JSONObject;

/**
 * This provider contributes extended data of builds.
 * It includes display name, full display name, description, url and buildHost and buildLabel
 * This information is static and never changes during a build.
 */
public class ExtendedProjectDataProvider extends DataProvider
{
  private JSONObject data;

  public ExtendedProjectDataProvider(Run<?, ?> run)
  {
    super(run);
    initData(run);
  }

  private void initData(Run<?, ?> run)
  {
    data = new JSONObject();

    Executor executor = run.getExecutor();
    if (executor == null)
    {
      data.element("buildHost", "master");
      data.element("buildLabel", "master");
    }
    else
    {
      Node node = executor.getOwner().getNode();
      if (node == null)
      {
        data.element("buildHost", "master");
        data.element("buildLabel", "master");
      }
      else
      {
        data.element("buildHost", StringUtils.isBlank(node.getDisplayName()) ? "master" : node.getDisplayName());
        data.element("buildLabel", StringUtils.isBlank(node.getLabelString()) ? "master" : node.getLabelString());
      }
    }

    data.element("id", run.getId());
    data.element("displayName", run.getDisplayName());
    data.element("fullDisplayName", run.getFullDisplayName());
    data.element("description", run.getDescription());
    data.element("url", run.getUrl());
  }

  @Override
  public JSONObject getStaticData()
  {
    return data;
  }

}
