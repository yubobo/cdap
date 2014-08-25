/*
 * Copyright 2014 Cask, Inc.
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

package co.cask.cdap.api.security;

/**
 * Represents an entity.
 */
public enum EntityType {
  NAMESPACE("namespaces"),
  APPLICATION("apps"),

  // parts of an application
  FLOW("flows"),
  MAPREDUCE("mapreduce"),

  // parts outside an application
  STREAM("streams");

  private final String pluralForm;

  EntityType(String pluralForm) {
    this.pluralForm = pluralForm;
  }

  public static EntityType fromPluralForm(String pluralForm) {
    for (EntityType type : EntityType.values()) {
      if (pluralForm.equals(type.getPluralForm())) {
        return type;
      }
    }
    throw new IllegalArgumentException("No EntityType found for plural form: " + pluralForm);
  }

  public String getPluralForm() {
    return pluralForm;
  }
}
