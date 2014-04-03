package testsForExamples.HelloWorld;

import static drivers.Global.ROOT_URL;
import drivers.Global;

/** class for HelloWorld example.
 * 
 * @author elmira
 *
 */
public class HelloWorld {
  private String nameOfJar = Global.properties.getProperty("helloWorld");
  private String description = Global.properties.getProperty("hwDescription");
  private int collect = 1;
  private int process = 1;
  private int store = 1;
  private int query = 1;
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
