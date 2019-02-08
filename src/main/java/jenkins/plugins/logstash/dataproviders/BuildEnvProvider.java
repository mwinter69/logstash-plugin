package jenkins.plugins.logstash.dataproviders;

import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import hudson.model.AbstractBuild;
import hudson.model.Environment;
import hudson.model.Run;
import hudson.model.TaskListener;
import net.sf.json.JSONObject;

public class BuildEnvProvider extends DataProvider
{

  private final static Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

  private transient TaskListener listener;
  private transient JSONObject data;
  private transient final boolean elasticSearchCompatibility;

  private Map<String, String> buildVariables;
  private Set<String> sensitiveBuildVariables;

  @DataBoundConstructor
  public BuildEnvProvider(Run<?, ?> run, TaskListener listener, boolean elasticSearchCompatibility)
  {
    super(run);
    this.listener = listener;
    initData();
    data = toJson();
    this.elasticSearchCompatibility = elasticSearchCompatibility;
  }

  private JSONObject toJson()
  {
    Gson gson = new GsonBuilder().create();
    String json = gson.toJson(this);
    LOGGER.log(Level.INFO, "Json: {0}", json);
    return JSONObject.fromObject(json);
  }

  private void initData()
  {
    if (run instanceof AbstractBuild)
    {
      AbstractBuild<?, ?> build = (AbstractBuild<?, ?>)run;
      buildVariables = build.getBuildVariables();
      sensitiveBuildVariables = build.getSensitiveBuildVariables();

      // Get environment build variables and merge them into the buildVariables map
      Map<String, String> buildEnvVariables = new HashMap<>();
      List<Environment> buildEnvironments = build.getEnvironments();
      if (buildEnvironments != null)
      {
        for (Environment env : buildEnvironments)
        {
          if (env == null)
          {
            continue;
          }

          env.buildEnvVars(buildEnvVariables);
          if (!buildEnvVariables.isEmpty())
          {
            buildVariables.putAll(buildEnvVariables);
            buildEnvVariables.clear();
          }
        }
      }
      try
      {
        buildVariables.putAll(build.getEnvironment(listener));
      }
      catch (Exception e)
      {
        // no base build env vars to merge
        LOGGER.log(WARNING, "Unable update logstash buildVariables with EnvVars from " + build.getDisplayName(), e);
      }
      for (String key : sensitiveBuildVariables)
      {
        buildVariables.remove(key);
      }

    }
    else
    {
      try
      {
        // TODO: sensitive variables are not filtered, c.f. https://stackoverflow.com/questions/30916085
        buildVariables = run.getEnvironment(listener);
        LOGGER.log(Level.INFO, "Build vars: {0}", buildVariables == null);
      }
      catch (IOException | InterruptedException e)
      {
        LOGGER.log(WARNING, "Unable to get environment for " + run.getDisplayName(), e);
        buildVariables = new HashMap<>();
      }
    }
    if (elasticSearchCompatibility)
    {
      removeIllegalKeys();
    }
  }

  private void removeIllegalKeys()
  {
    Set<String> illegalKeys = new HashSet<>();
    for (String key : buildVariables.keySet())
    {
      if (key.contains("."))
      {
        illegalKeys.add(key);
      }
    }
    for (String illegalKey : illegalKeys)
    {
      String value = buildVariables.remove(illegalKey);
      buildVariables.put(illegalKey.replace('.', '_'), value);
    }
  }

  @Override
  public JSONObject getStaticData()
  {
    return data;
  }
}
