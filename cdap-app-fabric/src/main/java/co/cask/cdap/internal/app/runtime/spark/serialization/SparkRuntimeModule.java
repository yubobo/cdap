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

import co.cask.cdap.common.runtime.RuntimeModule;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import org.apache.spark.SparkConf;
import org.apache.spark.serializer.Serializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.annotation.Nullable;

/**
 *
 */
final class SparkRuntimeModule extends RuntimeModule {
  @Override
  public Module getInMemoryModules() {
    return new SparkModule();
  }

  @Override
  public Module getStandaloneModules() {
    return new SparkModule();
  }

  @Override
  public Module getDistributedModules() {
    return new SparkModule();
  }

  private static final class SparkModule extends AbstractModule {

    private final SparkConf sparkConf;

    SparkModule() {
      this(new SparkConf());
    }

    SparkModule(SparkConf sparkConf) {
      this.sparkConf = sparkConf;
    }

    @Override
    protected void configure() {
      bind(SparkConf.class).toInstance(sparkConf);
    }

    @SuppressWarnings("unused")
    @Provides @Named("spark-serializer")
    Serializer providesSerializer() {
      String originalSerializer = sparkConf.get("spark.serializer", "org.apache.spark.serializer.JavaSerializer");
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      Class<? extends Serializer> classz;
      try {
        classz = classLoader.loadClass(originalSerializer).asSubclass(Serializer.class);
      } catch (ClassNotFoundException e) {
        throw Throwables.propagate(e);
      }

      try {
        Serializer instance = createInstance(classz);
        Preconditions.checkNotNull(instance, "Unable to instantiate Spark Serializer class. Only zero argument " +
          "constructors and constructors with a single argument of type SparkConf are supported.");
        return instance;
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }

    @Nullable
    private Serializer createInstance(Class<? extends Serializer> clazz)
      throws IllegalAccessException, InstantiationException, InvocationTargetException {
      for (Constructor<?> constructor : clazz.getConstructors()) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        if (parameterTypes.length == 0) {
          constructor.setAccessible(true);
          return (Serializer) constructor.newInstance();
        } else if (parameterTypes.length == 1 && SparkConf.class.equals(parameterTypes[0])) {
          constructor.setAccessible(true);
          return (Serializer) constructor.newInstance(sparkConf);
        }
      }
      return null;
    }
  }
}
