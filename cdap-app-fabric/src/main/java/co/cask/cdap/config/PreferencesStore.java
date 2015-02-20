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

package co.cask.cdap.config;

import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.common.authorization.ObjectIds;
import co.cask.cdap.common.http.SecurityRequestContext;
import co.cask.cdap.proto.ProgramType;
import co.cask.common.authorization.ObjectId;
import co.cask.common.authorization.Permission;
import co.cask.common.authorization.UnauthorizedException;
import co.cask.common.authorization.client.AuthorizationClient;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import java.util.Map;

/**
 * Wrapper around {@link ConfigStore} specifically for Preferences API.
 */
public class PreferencesStore {
  private static final String PREFERENCES_CONFIG_TYPE = "preferences";

  // Id for Properties config stored at the instance level
  private static final String INSTANCE_PROPERTIES = "instance";
  // Namespace where instance level properties are stored
  private static final String EMPTY_NAMESPACE = "";

  private final ConfigStore configStore;
  private final AuthorizationClient authorizationClient;

  @Inject
  public PreferencesStore(ConfigStore configStore, AuthorizationClient authorizationClient) {
    this.configStore = configStore;
    this.authorizationClient = authorizationClient;
  }

  private Map<String, String> getConfigProperties(String namespace, String id) {
    Map<String, String> value = Maps.newHashMap();
    try {
      Config config = configStore.get(namespace, PREFERENCES_CONFIG_TYPE, id);
      value.putAll(config.getProperties());
    } catch (ConfigNotFoundException e) {
      //no-op - return empty map
    }
    return value;
  }

  private void setConfig(String namespace, String id, Map<String, String> propertyMap) {
    Config config = new Config(id, propertyMap);
    configStore.createOrUpdate(namespace, PREFERENCES_CONFIG_TYPE, config);
  }

  private void deleteConfig(String namespace, String id) {
    try {
      configStore.delete(namespace, PREFERENCES_CONFIG_TYPE, id);
    } catch (ConfigNotFoundException e) {
      //no-op
    }
  }

  public Map<String, String> getProperties() throws UnauthorizedException {
    authorizationClient.authorize(ObjectId.GLOBAL, SecurityRequestContext.getSubjects(),
                                  ImmutableList.of(Permission.LIFECYCLE));
    return getConfigProperties(EMPTY_NAMESPACE, getMultipartKey(INSTANCE_PROPERTIES));
  }

  public Map<String, String> getProperties(String namespace) throws UnauthorizedException {
    authorizationClient.authorize(ObjectIds.namespace(namespace), SecurityRequestContext.getSubjects(),
                                  ImmutableList.of(Permission.LIFECYCLE));
    return getConfigProperties(namespace, getMultipartKey(namespace));
  }

  public Map<String, String> getProperties(String namespace, String appId) throws UnauthorizedException {
    authorizationClient.authorize(ObjectIds.application(namespace, appId), SecurityRequestContext.getSubjects(),
                                  ImmutableList.of(Permission.LIFECYCLE));
    return getConfigProperties(namespace, getMultipartKey(namespace, appId));
  }

  public Map<String, String> getProperties(String namespace, String appId, ProgramType programType,
                                           String programId) throws UnauthorizedException {
    authorizationClient.authorize(ObjectIds.program(namespace, appId, programType, programId),
                                  SecurityRequestContext.getSubjects(),
                                  ImmutableList.of(Permission.LIFECYCLE));
    return getConfigProperties(namespace, getMultipartKey(namespace, appId, programType.getCategoryName(), programId));
  }

  public Map<String, String> getResolvedProperties() throws UnauthorizedException {
    return getProperties();
  }

  public Map<String, String> getResolvedProperties(String namespace) throws UnauthorizedException {
    Map<String, String> propMap = getResolvedProperties();
    propMap.putAll(getProperties(namespace));
    return propMap;
  }

  public Map<String, String> getResolvedProperties(String namespace, String appId) throws UnauthorizedException {
    Map<String, String> propMap = getResolvedProperties(namespace);
    propMap.putAll(getProperties(namespace, appId));
    return propMap;
  }

  public Map<String, String> getResolvedProperties(String namespace, String appId, ProgramType programType,
                                                   String programId) throws UnauthorizedException {
    Map<String, String> propMap = getResolvedProperties(namespace, appId);
    propMap.putAll(getProperties(namespace, appId, programType, programId));
    return propMap;
  }

  public void setProperties(Map<String, String> propMap) throws UnauthorizedException {
    authorizationClient.authorize(ObjectId.GLOBAL, SecurityRequestContext.getSubjects(),
                                  ImmutableList.of(Permission.LIFECYCLE));
    setConfig(EMPTY_NAMESPACE, getMultipartKey(INSTANCE_PROPERTIES), propMap);
  }

  public void setProperties(String namespace, Map<String, String> propMap) throws UnauthorizedException {
    authorizationClient.authorize(ObjectIds.namespace(namespace), SecurityRequestContext.getSubjects(),
                                  ImmutableList.of(Permission.LIFECYCLE));
    setConfig(namespace, getMultipartKey(namespace), propMap);
  }

  public void setProperties(String namespace, String appId, Map<String, String> propMap) throws UnauthorizedException {
    authorizationClient.authorize(ObjectIds.application(namespace, appId), SecurityRequestContext.getSubjects(),
                                  ImmutableList.of(Permission.LIFECYCLE));
    setConfig(namespace, getMultipartKey(namespace, appId), propMap);
  }

  public void setProperties(String namespace, String appId, ProgramType programType, String programId,
                            Map<String, String> propMap) throws UnauthorizedException {
    authorizationClient.authorize(ObjectIds.program(namespace, appId, programType, programId),
                                  SecurityRequestContext.getSubjects(),
                                  ImmutableList.of(Permission.LIFECYCLE));
    setConfig(namespace, getMultipartKey(namespace, appId, programType.getCategoryName(), programId), propMap);
  }

  public void deleteProperties() throws UnauthorizedException {
    authorizationClient.authorize(ObjectId.GLOBAL, SecurityRequestContext.getSubjects(),
                                  ImmutableList.of(Permission.LIFECYCLE));
    deleteConfig(EMPTY_NAMESPACE, getMultipartKey(INSTANCE_PROPERTIES));
  }

  public void deleteProperties(String namespace) throws UnauthorizedException {
    authorizationClient.authorize(ObjectIds.namespace(namespace), SecurityRequestContext.getSubjects(),
                                  ImmutableList.of(Permission.LIFECYCLE));
    deleteConfig(namespace, getMultipartKey(namespace));
  }

  public void deleteProperties(String namespace, String appId) throws UnauthorizedException {
    authorizationClient.authorize(ObjectIds.application(namespace, appId), SecurityRequestContext.getSubjects(),
                                  ImmutableList.of(Permission.LIFECYCLE));
    deleteConfig(namespace, getMultipartKey(namespace, appId));
  }

  public void deleteProperties(String namespace, String appId, ProgramType programType,
                               String programId) throws UnauthorizedException {
    authorizationClient.authorize(ObjectIds.program(namespace, appId, programType, programId),
                                  SecurityRequestContext.getSubjects(),
                                  ImmutableList.of(Permission.LIFECYCLE));
    deleteConfig(namespace, getMultipartKey(namespace, appId, programType.getCategoryName(), programId));
  }

  private String getMultipartKey(String... parts) {
    int sizeOfParts = 0;
    for (String part : parts) {
      sizeOfParts += part.length();
    }

    byte[] result = new byte[sizeOfParts + (parts.length * Bytes.SIZEOF_INT)];

    int offset = 0;
    for (String part : parts) {
      Bytes.putInt(result, offset, part.length());
      offset += Bytes.SIZEOF_INT;
      Bytes.putBytes(result, offset, part.getBytes(), 0, part.length());
      offset += part.length();
    }
    return Bytes.toString(result);
  }
}
