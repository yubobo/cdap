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

package org.apache.spark.util;

import co.cask.cdap.common.io.Locations;
import co.cask.cdap.common.lang.ProgramClassLoader;
import co.cask.cdap.common.lang.jar.BundleJarUtil;
import co.cask.cdap.common.utils.DirUtils;
import co.cask.cdap.internal.app.runtime.spark.SparkProgramWrapper;
import co.cask.cdap.proto.ProgramType;
import com.google.common.base.Throwables;
import org.apache.twill.filesystem.LocalLocationFactory;
import org.apache.twill.filesystem.Location;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.jar.Manifest;

/**
 * Created by rsinha on 6/4/15.
 */
@SuppressWarnings("unused")
public class MutableURLClassLoader extends URLClassLoader {

  private SparkProgramWrapper.CloseableClassLoader programClassloader;

  public MutableURLClassLoader(URL[] urls) {
    super(urls);
  }

  public MutableURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
    super(urls, parent, factory);
  }

  public MutableURLClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
    try {
      programClassloader = createProgramClassloader();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    System.out.println("Intercepted MutableURLClassLoader");
  }

  @Override
  public void addURL(URL url) {
    super.addURL(url);
    System.out.println("Intercepted MutableURLClassLoader.addURL");
  }

  @Override
  public URL[] getURLs() {
    System.out.println("Intercepted MutableURLClassLoader.getURLs");
    return super.getURLs();
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
//    return super.findClass(name);
    System.out.println("Intercepted MutableURLClassLoader.findClass");
    return programClassloader.loadClass(name);
  }

  @Override
  public InputStream getResourceAsStream(String name) {
    System.out.println("Intercepted MutableURLClassLoader.getResourceAsStream");
    return programClassloader.getResourceAsStream(name);
//    return super.getResourceAsStream(name);
  }

  @Override
  public URL findResource(String name) {
    System.out.println("Intercepted MutableURLClassLoader.findResource");
    return programClassloader.getResource(name);
//    return super.findResource(name);
  }

  @Override
  public Enumeration<URL> findResources(String name) throws IOException {
    System.out.println("Intercepted MutableURLClassLoader.findResources");
    return programClassloader.getResources(name);
//    return super.findResources(name);
  }

  private SparkProgramWrapper.CloseableClassLoader createProgramClassloader() throws IOException {
    LocalLocationFactory localLocationFactory = new LocalLocationFactory(new File(System.getProperty("user.dir")));
    Location programJar = localLocationFactory.create(String.format("%s.program.jar",
                                                                    ProgramType.SPARK.name().toLowerCase()));
    final File unpackDir = DirUtils.createTempDir(new File("/tmp"));
    BundleJarUtil.unpackProgramJar(Locations.newInputSupplier(programJar), unpackDir);
    ProgramClassLoader programClassLoader = ProgramClassLoader.create(unpackDir, getClass().getClassLoader(),
                                                                      ProgramType.SPARK);
    return new SparkProgramWrapper.CloseableClassLoader(programClassLoader, new Closeable() {
      @Override
      public void close() throws IOException {
        DirUtils.deleteDirectoryContents(unpackDir);
      }
    });
  }
}
