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
import co.cask.cdap.app.program.Program;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.discovery.RandomEndpointStrategy;
import co.cask.cdap.common.guice.DiscoveryRuntimeModule;
import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.spark.serializer.DeserializationStream;
import org.apache.spark.serializer.SerializationStream;
import org.apache.spark.serializer.Serializer;
import org.apache.spark.serializer.SerializerInstance;
import org.apache.twill.discovery.Discoverable;
import org.apache.twill.discovery.DiscoveryServiceClient;
import org.apache.twill.discovery.InMemoryDiscoveryService;
import org.apache.twill.discovery.ServiceDiscovered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.reflect.ClassTag;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 *
 */
public class SerializationPlayground {

  /**
   *
   */
  public static class CDAPSerializer extends Serializer {

    private final DiscoveryServiceClient discoveryServiceClient;
    private final Serializer delegate;

    /*@Inject
    public CDAPSerializer(DiscoveryServiceClient discoveryServiceClient,
                          @Named("spark.serializer") Serializer delegate) {
      this.discoveryServiceClient = discoveryServiceClient;
      this.delegate = delegate;
    }*/

    public CDAPSerializer() {
      /*
      if (standalone) {
        discoveryModules = new DiscoveryRuntimeModule().getStandaloneModules();
      } else {
        discoveryModules = new DiscoveryRuntimeModule().getDistributedModules();
      }
       */
      Module discoveryModules = new DiscoveryRuntimeModule().getStandaloneModules();
      Injector injector = Guice.createInjector(discoveryModules);
      this.discoveryServiceClient = injector.getInstance(DiscoveryServiceClient.class);
      // TODO: Fix
      this.delegate = injector.getInstance(Serializer.class);
    }

    @Override
    public SerializerInstance newInstance() {
      return new CDAPSerializerInstance(discoveryServiceClient, delegate.newInstance());
    }

    /**
     *
     */
    public static class CDAPSerializerInstance extends SerializerInstance {
      private static final Type serviceDiscovererType = new TypeToken<ServiceDiscoverer>() { }.getType();
      private static final Type metricsCollectorType = new TypeToken<MetricsCollector>() { }.getType();

      private final DiscoveryServiceClient discoveryServiceClient;
      private final SerializerInstance delegate;

      public CDAPSerializerInstance(DiscoveryServiceClient discoveryServiceClient, SerializerInstance delegate) {
        this.discoveryServiceClient = discoveryServiceClient;
        this.delegate = delegate;
      }

      @Override
      public <T> ByteBuffer serialize(T t, ClassTag<T> evidence) {
        if (!(t instanceof ServiceDiscoverer || t instanceof MetricsCollector)) {
          return delegate.serialize(t, evidence);
        }
        return null;
      }

      @Override
      public <T> T deserialize(ByteBuffer bytes, ClassTag<T> evidence) {
        Type genericType = getGenericReturnType("deserialize", ByteBuffer.class, ClassTag.class);
        if (!(genericType.equals(serviceDiscovererType) || genericType.equals(metricsCollectorType))) {
          return delegate.deserialize(bytes, evidence);
        }
        return null;
      }

      @Override
      public <T> T deserialize(ByteBuffer bytes, ClassLoader loader, ClassTag<T> evidence) {
        Type genericType = getGenericReturnType("deserialize", ByteBuffer.class, ClassLoader.class, ClassTag.class);
        if (!(genericType.equals(serviceDiscovererType) || genericType.equals(metricsCollectorType))) {
          return delegate.deserialize(bytes, loader, evidence);
        }
        return null;
      }

      @Override
      public SerializationStream serializeStream(OutputStream s) {
        return null;
      }

      @Override
      public DeserializationStream deserializeStream(InputStream s) {
        return null;
      }

      private Type getGenericReturnType(String methodName, Class... parameterTypes) {
        try {
          return this.getClass().getMethod(methodName, parameterTypes).getGenericReturnType();
        } catch (NoSuchMethodException e) {
          throw Throwables.propagate(e);
        }
      }
    }
  }

  /**
   *
   */
  public abstract static class AbstractServiceDiscoverer implements ServiceDiscoverer, Externalizable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceDiscoverer.class);

    protected String namespaceId;
    protected String applicationId;

    protected AbstractServiceDiscoverer() {
    }

    public AbstractServiceDiscoverer(Program program) {
      this.namespaceId = program.getNamespaceId();
      this.applicationId = program.getApplicationId();
    }

    @Override
    public URL getServiceURL(String applicationId, String serviceId) {
      String discoveryName = String.format("service.%s.%s.%s", namespaceId, applicationId, serviceId);
      ServiceDiscovered discovered = getDiscoveryServiceClient().discover(discoveryName);
      return createURL(new RandomEndpointStrategy(discovered).pick(1, TimeUnit.SECONDS), applicationId, serviceId);
    }

    @Override
    public URL getServiceURL(String serviceId) {
      return getServiceURL(applicationId, serviceId);
    }

    /**
     * @return the {@link DiscoveryServiceClient} for Service Discovery
     */
    protected abstract DiscoveryServiceClient getDiscoveryServiceClient();

    @Nullable
    private URL createURL(@Nullable Discoverable discoverable, String applicationId, String serviceId) {
      if (discoverable == null) {
        return null;
      }
      InetSocketAddress address = discoverable.getSocketAddress();
      String path = String.format("http://%s:%d%s/namespaces/%s/apps/%s/services/%s/methods/",
                                  address.getHostName(), address.getPort(),
                                  Constants.Gateway.API_VERSION_3, namespaceId, applicationId, serviceId);
      try {
        return new URL(path);
      } catch (MalformedURLException e) {
        LOG.error("Got exception while creating serviceURL", e);
        return null;
      }
    }
  }

  /**
   *
   */
  public static class MySparkDiscoverer extends AbstractServiceDiscoverer {

    private DiscoveryServiceClient discoveryServiceClient;

    public MySparkDiscoverer(DiscoveryServiceClient discoveryServiceClient, Program program) {
      super(program);
      this.discoveryServiceClient = discoveryServiceClient;
    }

    @Override
    protected DiscoveryServiceClient getDiscoveryServiceClient() {
      return null;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
      out.writeChars(namespaceId);
      out.writeChars(applicationId);
      out.writeObject(discoveryServiceClient);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      Object o = in.readObject();
      if (o instanceof DiscoveryServiceClient) {
        discoveryServiceClient = (DiscoveryServiceClient) o;
      }
      applicationId = in.readUTF();
      namespaceId = in.readUTF();
    }
  }

  /**
   *
   */
  public static class Runner {
    private static final Gson GSON = new Gson();
    public static void main(String [] args) {
      /*ZKClientService zkClientService = ZKClientServices.delegate(ZKClients.reWatchOnExpire(
        ZKClients.retryOnFailure(ZKClientService.Builder.of("127.0.0.1:2181/abc")
                                   .setSessionTimeout(10000)
                                   .build(), RetryStrategies.exponentialDelay(100, 2000, TimeUnit.MILLISECONDS))));
      DiscoveryServiceClient client = new ZKDiscoveryService(ZKClients.namespace(zkClientService, ""));*/
      DiscoveryServiceClient client = new InMemoryDiscoveryService();
      String json = GSON.toJson(client);
      System.out.println("client= " + json);
      DiscoveryServiceClient dsc = GSON.fromJson(json, new TypeToken<InMemoryDiscoveryService>() { }.getType());
      System.out.println("dsc = " + dsc);
    }
  }
}
