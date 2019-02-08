package jenkins.plugins.logstash.dataproviders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

/**
 * Holds the data to be sent for events.
 */
public class BuildData
{

  private List<DataProvider> dataProviders = new ArrayList<>();
  private JSONObject data = new JSONObject();
  private String timestamp;


  /**
   * Creates the BuildData instance that constructs the data to be sent for events
   *
   * @param dataProviders The list of dataProviders with dynamic data.
   *
   * @param data The data object
   */
  public BuildData(List<DataProvider> dataProviders, String timestamp)
  {
    this.dataProviders = dataProviders;
    this.timestamp = timestamp;
    collectStaticData();
  }

  public String getTimestamp() {
    return timestamp;
  }

  private void collectStaticData()
  {
    for (DataProvider provider: dataProviders)
    {
      Map<String, Object> dataMap = provider.getStaticData();
      if (dataMap != null)
      {
        for (Entry<String, Object> entry: dataMap.entrySet())
        {
          data.element(entry.getKey(), entry.getValue());
        }
      }
    }
  }

  public JSONObject toJson() {

    for (DataProvider provider: dataProviders)
    {
      Map<String, Object> dataMap = provider.getData();
      if (dataMap != null)
      {
        for (Entry<String, Object> entry: dataMap.entrySet())
        {
          data.element(entry.getKey(), entry.getValue());
        }
      }
    }
    return data;
  }

  public static void main(String[] args)
  {
  }

}


