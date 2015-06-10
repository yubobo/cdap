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

import co.cask.cdap.common.guice.DiscoveryRuntimeModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.apache.spark.serializer.Serializer;
import org.apache.spark.serializer.SerializerInstance;
import org.apache.twill.discovery.DiscoveryServiceClient;

/**
 *
 */
public final class DistributedSerializer extends Serializer {

  private final DiscoveryServiceClient discoveryServiceClient;
  private final Serializer delegate;

  public DistributedSerializer() {
    Injector injector = Guice.createInjector(new DiscoveryRuntimeModule().getDistributedModules(),
                                             new SparkRuntimeModule().getDistributedModules());
    this.discoveryServiceClient = injector.getInstance(DiscoveryServiceClient.class);
    this.delegate = injector.getInstance(Key.get(Serializer.class, Names.named("spark-serializer")));
  }

  @Override
  public SerializerInstance newInstance() {
    return new DistributedSerializerInstance(discoveryServiceClient, delegate.newInstance());
  }
}
