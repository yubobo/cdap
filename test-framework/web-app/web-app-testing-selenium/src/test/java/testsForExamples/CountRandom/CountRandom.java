package testsForExamples.CountRandom;

import static drivers.Global.ROOT_URL;
import drivers.Global;

/** class for CountRandom example.
 * 
 * @author elmira
 *
 */
public class CountRandom {
  private String nameOfJar = Global.properties.getProperty("countRandom");
  private String description = Global.properties.getProperty("crDescription");
  private int collect = 0;
  private int process = 1;
  private int store = 1;
  private int query = 0;
  
  public String getDescription() {
    return description;
  }
  public String getNameOfJar() {
    return nameOfJar;
  }
  public String getAppUrl() {
    String apps = Global.properties.getProperty("appsPage");
    return ROOT_URL + apps + this.getNameOfJar();
  }
  public int getCollect() {
    return collect;
  }
  public int getProcess() {
    return process;
  }
  public int getStore() {
    return store;
  }
  public int getQuery() {
    return query;
  }
  
}
