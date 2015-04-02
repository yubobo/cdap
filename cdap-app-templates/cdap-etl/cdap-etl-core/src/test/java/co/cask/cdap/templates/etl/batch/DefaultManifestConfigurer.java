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

package co.cask.cdap.templates.etl.batch;

import co.cask.cdap.api.schedule.Schedule;
import co.cask.cdap.api.templates.ManifestConfigurer;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 *
 */
public class DefaultManifestConfigurer implements ManifestConfigurer {

  private Schedule schedule;
  private int instances;
  private Map<String, String> arguments = Maps.newHashMap();

  @Override
  public void setSchedule(Schedule schedule) {
    this.schedule = schedule;
  }

  @Override
  public void setInstances(int instances) {
    this.instances = instances;
  }

  @Override
  public void addRuntimeArguments(Map<String, String> arguments) {
    this.arguments.putAll(arguments);
  }

  @Override
  public void addRuntimeArgument(String key, String value) {
    this.arguments.put(key, value);
  }

  public Schedule getSchedule() {
    return schedule;
  }

  public Map<String, String> getArguments() {
    return arguments;
  }

  public int getInstances() {
    return instances;
  }
}