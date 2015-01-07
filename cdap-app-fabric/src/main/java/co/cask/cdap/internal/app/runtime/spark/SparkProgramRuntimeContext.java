/*
 * Copyright © 2015 Cask Data, Inc.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.internal.app.runtime.spark;

import co.cask.cdap.api.spark.SparkContext;
import co.cask.cdap.api.spark.SparkProgram;

/**
 * Created by rsinha on 1/6/15.
 */
public class SparkProgramRuntimeContext {
  
  private final String userProgramClassName;
  private Class<? extends SparkProgram> userProgramClass;
  private BasicSparkContext basicSparkContext;
  private SparkContext sparkContext;
  private boolean scalaProgram;

  // TODO: Get around Spark's limitation of only one SparkContext in a JVM and support multiple spark context:
  // CDAP-4
  private boolean sparkProgramSuccessful;
  private boolean sparkProgramRunning;

  public SparkProgramRuntimeContext(String className, BasicSparkContext basicSparkContext) {
    this.userProgramClassName = className;
    this.basicSparkContext = basicSparkContext;
  }

  private Class<? extends SparkProgram> loadUserSparkClass() throws ClassNotFoundException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Class<?> cls = classLoader.loadClass(userProgramClassName);
    if (!SparkProgram.class.isAssignableFrom(cls)) {
      throw new IllegalArgumentException("User class " + userProgramClassName +
                                           " does not implements " + SparkProgram.class.getName());
    }
    return (Class<? extends SparkProgram>) cls;
  }
}
