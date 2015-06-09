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
import com.google.common.base.Throwables;
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

/**
 *
 */
abstract class AbstractSerializerInstance extends SerializerInstance {
  private static final Type serviceDiscovererType = new TypeToken<ServiceDiscoverer>() { }.getType();
  private static final Type metricsCollectorType = new TypeToken<MetricsCollector>() { }.getType();

  private final SerializerInstance delegate;

  public AbstractSerializerInstance(SerializerInstance delegate) {
    this.delegate = delegate;
  }


  @Override
  public <T> ByteBuffer serialize(T t, ClassTag<T> evidence) {
    if (!(t instanceof ServiceDiscoverer || t instanceof MetricsCollector)) {
      return delegate.serialize(t, evidence);
    }
    return writeObject(t);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T deserialize(ByteBuffer bytes, ClassTag<T> evidence) {
    Type genericType = getGenericReturnType("deserialize", ByteBuffer.class, ClassTag.class);
    if (!(serviceDiscovererType.equals(genericType) || metricsCollectorType.equals(genericType))) {
      return delegate.deserialize(bytes, evidence);
    }
    // TODO: Verify second argument
    return (T) readObject(bytes, evidence.runtimeClass());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T deserialize(ByteBuffer bytes, ClassLoader loader, ClassTag<T> evidence) {
    Type genericType = getGenericReturnType("deserialize", ByteBuffer.class, ClassLoader.class, ClassTag.class);
    if (!(genericType.equals(serviceDiscovererType) || genericType.equals(metricsCollectorType))) {
      return delegate.deserialize(bytes, loader, evidence);
    }
    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(loader);
    // TODO: Verify second argument
    T toReturn = (T) readObject(bytes, evidence.runtimeClass());
    Thread.currentThread().setContextClassLoader(oldClassLoader);
    return toReturn;
  }

  @Override
  public SerializationStream serializeStream(OutputStream s) {
    try {
      return new CDAPSparkSerializationStream(this, s);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public DeserializationStream deserializeStream(InputStream s) {
    try {
      return new CDAPSparkDeserializationStream(this, s);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private Type getGenericReturnType(String methodName, Class... parameterTypes) {
    try {
      return this.getClass().getMethod(methodName, parameterTypes).getGenericReturnType();
    } catch (NoSuchMethodException e) {
      throw Throwables.propagate(e);
    }
  }

  private <T> ByteBuffer writeObject(T type) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
        writeObject(type, oos);
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
      return ByteBuffer.wrap(bos.toByteArray());
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }


  private <T> T readObject(ByteBuffer byteBuffer, Class<T> typeClass) {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer.array())) {
      try (ObjectInputStream ois = new ObjectInputStream(bis)) {
        return readObject(ois, typeClass);
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  abstract <T> void writeObject(T type, ObjectOutputStream oos) throws IOException;

  abstract <T> T readObject(ObjectInputStream ois, Class<T> typeClass) throws IOException;

  class CDAPSparkSerializationStream extends SerializationStream {

    private final AbstractSerializerInstance serializerInstance;
    private final ObjectOutputStream oos;

    CDAPSparkSerializationStream(AbstractSerializerInstance serializerInstance, OutputStream outputStream)
      throws IOException {
      this.serializerInstance = serializerInstance;
      this.oos = new ObjectOutputStream(outputStream);
    }

    @Override
    public <T> SerializationStream writeObject(T t, ClassTag<T> evidence) {
      try {
        serializerInstance.writeObject(t, oos);
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
      return this;
    }

    @Override
    public void flush() {
      try {
        oos.flush();
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }

    @Override
    public void close() {
      try {
        oos.close();
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  class CDAPSparkDeserializationStream extends DeserializationStream {

    private final AbstractSerializerInstance serializerInstance;
    private final ObjectInputStream ois;

    CDAPSparkDeserializationStream(AbstractSerializerInstance serializerInstance, InputStream inputStream)
      throws IOException {
      this.serializerInstance = serializerInstance;
      this.ois = new ObjectInputStream(inputStream);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readObject(ClassTag<T> evidence) {
      try {
        return (T) serializerInstance.readObject(ois, evidence.runtimeClass());
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }

    @Override
    public void close() {
      try {
        ois.close();
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
  }
}
