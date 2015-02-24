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
package co.cask.common.authorization;

/**
 * Represents a subject as defined in {@link ACLEntry}.
 */
public class SubjectId extends TypedId {

  public SubjectId(String type, String id) {
    super(type, id);
  }

  public SubjectId(TypedId typedId) {
    super(typedId.getType(), typedId.getId());
  }

  public static SubjectId fromRep(String rep) {
    return new SubjectId(TypedId.fromRep(rep));
  }

  public String getRep() {
    return getType() + ":" + getId();
  }
}