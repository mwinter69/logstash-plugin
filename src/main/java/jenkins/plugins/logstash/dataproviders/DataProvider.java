package jenkins.plugins.logstash.dataproviders;

import hudson.model.Run;
import net.sf.json.JSONObject;

/**
 * The actual class that is used during event creation to provide the data to be included.
 */
public abstract class DataProvider
{

  protected transient Run<?, ?> run;

  public DataProvider(Run<?, ?> run)
  {
    this.run = run;
  }


  /**
   * Returns the data this is dynamic and might change during the build.
   *
   * @return A JSONObject with the data to include or null, if no data is to be included.
   */
  public JSONObject getData()
  {
    return null;
  }


  /**
   * Returns the data that is static for the events sent in line mode.
   * Only {@link LineDataProvider LineDataProviders} need to overwrite this method if they provide static data.
   *
   * @return A JSONObject with the data to include or null, if no data is to be included.
   */
  public JSONObject getStaticData()
  {
    return null;
  }
}
