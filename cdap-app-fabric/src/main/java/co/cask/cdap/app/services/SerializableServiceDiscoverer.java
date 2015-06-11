/*
 * Copyright © 2014-2015 Cask Data, Inc.
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

package co.cask.cdap.app.services;

import co.cask.cdap.api.ServiceDiscoverer;
import co.cask.cdap.api.spark.Spark;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.common.guice.DiscoveryRuntimeModule;
import co.cask.cdap.internal.app.runtime.spark.serialization.SparkRuntimeModule;
import co.cask.cdap.proto.Id;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.apache.spark.serializer.Serializer;
import org.apache.twill.discovery.DiscoveryServiceClient;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

/**
 * A {@link Serializable} {@link ServiceDiscoverer}. This is needed for {@link Spark} program which expects
 * object used in the Closure to be {@link Serializable}
 */
public class SerializableServiceDiscoverer extends AbstractServiceDiscoverer implements Externalizable {

  private static final long serialVersionUID = 6547316362453719580L;
  private static final ConcurrentMap<Id.Application, DiscoveryServiceClient> discoveryServiceClients =
    Maps.newConcurrentMap();
  private static final Injector injector = Guice.createInjector(new DiscoveryRuntimeModule().getInMemoryModules(),
                                                                new SparkRuntimeModule().getInMemoryModules());

  private final DiscoveryServiceClient discoveryServiceClient;

  // no-arg constructor required for serialization/deserialization to work
  @SuppressWarnings("unused")
  public SerializableServiceDiscoverer() {
    this.discoveryServiceClient = injector.getInstance(DiscoveryServiceClient.class);
  }

  public SerializableServiceDiscoverer(Id.Application application, DiscoveryServiceClient discoveryServiceClient) {
    super(application);
    this.discoveryServiceClient = discoveryServiceClient;
  }

  @Override
  public void writeExternal(ObjectOutput objectOutput) throws IOException {
    objectOutput.writeUTF(namespaceId);
    objectOutput.writeUTF(applicationId);
  }

  @Override
  public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
    namespaceId = objectInput.readUTF();
    applicationId = objectInput.readUTF();
  }

  @Override
  public DiscoveryServiceClient getDiscoveryServiceClient() {
    return discoveryServiceClient;
  }

  public String getNamespaceId() {
    return namespaceId;
  }

  public String getApplicationId() {
    return applicationId;
  }
}
