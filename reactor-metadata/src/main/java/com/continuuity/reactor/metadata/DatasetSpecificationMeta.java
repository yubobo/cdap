/*
 * Copyright 2014 Continuuity, Inc.
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

package com.continuuity.reactor.metadata;

import java.util.SortedMap;

/**
 * Metadata object representing {@link DatasetSpecification}.
 */
public class DatasetSpecificationMeta {

  private final String name;
  private final String type;
  private final SortedMap<String, String> properties;
  private final SortedMap<String, DatasetSpecificationMeta> datasetSpecs;


  public DatasetSpecificationMeta(String name, String type, SortedMap<String, String> properties,
                                  SortedMap<String, DatasetSpecificationMeta> datasetSpecs) {
    this.name = name;
    this.type = type;
    this.properties = properties;
    this.datasetSpecs = datasetSpecs;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public SortedMap<String, String> getProperties() {
    return properties;
  }

  public SortedMap<String, DatasetSpecificationMeta> getDatasetSpecs() {
    return datasetSpecs;
  }
}
