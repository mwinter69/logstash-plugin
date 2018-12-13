package jenkins.plugins.logstash.dataproviders;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import net.sf.json.JSONObject;

/**
 * A data provider contributing information about the root project, e.g.
 * Matrix jobs for the individual axes have a common root project
 */
public class RootProjectProvider extends DataProvider
{
  private JSONObject data;

  public RootProjectProvider(Run<?, ?> run)
  {
    super(run);
    initData(run);
  }

  private void initData(Run<?, ?> run)
  {
    data = new JSONObject();

    if (run instanceof AbstractBuild)
    {
      AbstractBuild<?, ?> build = (AbstractBuild<?, ?>)run;
      AbstractBuild<?, ?> rootBuild = build.getRootBuild();
      data.element("rootProjectName", rootBuild.getProject().getName());
      data.element("rootFullProjectName", rootBuild.getProject().getFullName());
      data.element("rootProjectDisplayName", rootBuild.getDisplayName());
      data.element("rootBuildNum", rootBuild.getNumber());
    }
    else
    {
      data.element("rootProjectName", run.getParent().getName());
      data.element("rootFullProjectName", run.getParent().getFullName());
      data.element("rootProjectDisplayName", run.getDisplayName());
      data.element("rootBuildNum", run.getNumber());
    }
  }

  @Override
  public JSONObject getStaticData()
  {
    return data;
  }

}
