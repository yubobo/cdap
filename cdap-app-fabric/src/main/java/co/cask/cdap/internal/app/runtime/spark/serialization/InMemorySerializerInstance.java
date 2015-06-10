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
import co.cask.cdap.api.metrics.MetricsCollector;
import co.cask.cdap.app.services.SerializableServiceDiscoverer;
import co.cask.cdap.proto.Id;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import org.apache.spark.serializer.DeserializationStream;
import org.apache.spark.serializer.SerializationStream;
import org.apache.spark.serializer.SerializerInstance;
import org.apache.twill.discovery.DiscoveryServiceClient;
import scala.reflect.ClassTag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
final class InMemorySerializerInstance extends AbstractSerializerInstance {
  private static final ConcurrentMap<Id.Application, DiscoveryServiceClient> discoveryServiceClients =
    Maps.newConcurrentMap();

  public InMemorySerializerInstance(SerializerInstance delegate) {
    super(delegate);
  }

  @Override
  <T> void writeObject(T type, ObjectOutputStream oos) throws IOException {
    if (type instanceof ServiceDiscoverer) {
      SerializableServiceDiscoverer discoverer = (SerializableServiceDiscoverer) type;
      oos.writeUTF(discoverer.getNamespaceId());
      oos.writeUTF(discoverer.getApplicationId());
      DiscoveryServiceClient client = discoverer.getDiscoveryServiceClient();
      Id.Application appId = Id.Application.from(discoverer.getNamespaceId(), discoverer.getApplicationId());
      // Presumes that if you get a newer DiscoveryServiceClient for an appId, it is the latest
      discoveryServiceClients.put(appId, client);
    }
    // TODO: Metrics
  }

  @Override
  <T> T readObject(ObjectInputStream ois, Class<T> typeClass) throws IOException {
    T toReturn = null;
    if (typeClass.isAssignableFrom(SerializableServiceDiscoverer.class)) {
      String namespaceId = ois.readUTF();
      String applicationId = ois.readUTF();
      Id.Application appId = Id.Application.from(namespaceId, applicationId);
      DiscoveryServiceClient client = discoveryServiceClients.get(appId);
      Preconditions.checkNotNull(client, "Cannot deserialize unserialized service discoverer for application %s",
                                 appId);
      toReturn = typeClass.cast(new SerializableServiceDiscoverer(appId, client));
    }
    // TODO: Metrics
    return toReturn;
  }
}
