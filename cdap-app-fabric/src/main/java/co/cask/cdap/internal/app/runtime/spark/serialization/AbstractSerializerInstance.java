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
import com.google.common.base.Throwables;
import org.apache.spark.serializer.DeserializationStream;
import org.apache.spark.serializer.SerializationStream;
import org.apache.spark.serializer.SerializerInstance;
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
    Class<?> aClass = evidence.runtimeClass();
    if (!(aClass.isAssignableFrom(ServiceDiscoverer.class) || aClass.isAssignableFrom(MetricsCollector.class))) {
      return delegate.deserialize(bytes, evidence);
    }
    // TODO: Verify second argument
    return (T) readObject(bytes, aClass);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T deserialize(ByteBuffer bytes, ClassLoader loader, ClassTag<T> evidence) {
    Class<?> aClass = evidence.runtimeClass();
    if (!(aClass.isAssignableFrom(ServiceDiscoverer.class) || aClass.isAssignableFrom(MetricsCollector.class))) {
      return delegate.deserialize(bytes, loader, evidence);
    }
    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(loader);
    // TODO: Verify second argument
    T toReturn = (T) readObject(bytes, aClass);
    Thread.currentThread().setContextClassLoader(oldClassLoader);
    return toReturn;
  }

  @Override
  public SerializationStream serializeStream(OutputStream s) {
    try {
      return new CDAPSparkSerializationStream(this, s, delegate);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public DeserializationStream deserializeStream(InputStream s) {
    try {
      return new CDAPSparkDeserializationStream(this, s, delegate);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private Type getGenericReturnType(Class<?> classz, String methodName, Class... parameterTypes) {
    try {
      return classz.getMethod(methodName, parameterTypes).getGenericReturnType().getClass();
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
    private final SerializationStream delegate;
    private final ObjectOutputStream oos;

    CDAPSparkSerializationStream(AbstractSerializerInstance serializerInstance,
                                 OutputStream outputStream, SerializerInstance delegateInstance)
      throws IOException {
      this.serializerInstance = serializerInstance;
      this.oos = new ObjectOutputStream(outputStream);
      this.delegate = delegateInstance.serializeStream(oos);
    }

    @Override
    public <T> SerializationStream writeObject(T t, ClassTag<T> evidence) {
      if (!(t instanceof ServiceDiscoverer || t instanceof MetricsCollector)) {
        return delegate.writeObject(t, evidence);
      }
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
    private final DeserializationStream delegate;

    CDAPSparkDeserializationStream(AbstractSerializerInstance serializerInstance, InputStream inputStream,
                                   SerializerInstance delegateInstance)
      throws IOException {
      this.serializerInstance = serializerInstance;
      this.ois = new ObjectInputStream(inputStream);
      this.delegate = delegateInstance.deserializeStream(ois);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readObject(ClassTag<T> evidence) {
      Class<?> aClass = evidence.runtimeClass();
      if (!(aClass.isAssignableFrom(ServiceDiscoverer.class) || aClass.isAssignableFrom(MetricsCollector.class))) {
        return delegate.readObject(evidence);
      }
      try {
        return (T) serializerInstance.readObject(ois, aClass);
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
