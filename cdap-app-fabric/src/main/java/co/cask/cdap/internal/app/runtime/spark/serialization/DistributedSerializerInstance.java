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

package co.cask.cdap.internal.app.runtime.spark.serialization;

import co.cask.cdap.api.ServiceDiscoverer;
import co.cask.cdap.app.services.SerializableServiceDiscoverer;
import co.cask.cdap.proto.Id;
import org.apache.spark.serializer.SerializerInstance;
import org.apache.twill.discovery.DiscoveryServiceClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 */
final class DistributedSerializerInstance extends AbstractSerializerInstance {

  private final DiscoveryServiceClient discoveryServiceClient;

  public DistributedSerializerInstance(DiscoveryServiceClient discoveryServiceClient, SerializerInstance delegate) {
    super(delegate);
    this.discoveryServiceClient = discoveryServiceClient;
  }

  @Override
  <T> void writeObject(T type, ObjectOutputStream oos) throws IOException {
    if (type instanceof ServiceDiscoverer) {
      SerializableServiceDiscoverer discoverer = (SerializableServiceDiscoverer) type;
      oos.writeUTF(discoverer.getNamespaceId());
      oos.writeUTF(discoverer.getApplicationId());
    }
    // TODO: Metrics
  }

  @Override
  <T> T readObject(ObjectInputStream ois, Class<T> typeClass) throws IOException {
    T toReturn = null;
    if (typeClass.isAssignableFrom(SerializableServiceDiscoverer.class)) {
      String namespaceId = ois.readUTF();
      String applicationId = ois.readUTF();
      toReturn = typeClass.cast(new SerializableServiceDiscoverer(Id.Application.from(namespaceId, applicationId),
                                                                  this.discoveryServiceClient));
    }
    // TODO: Metrics
    return toReturn;
  }
}
