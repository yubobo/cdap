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

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by rsinha on 6/4/15.
 */
public class MutableURLClassLoader extends URLClassLoader {

  public MutableURLClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
    System.out.println("Intercepted MutableURLClassLoader");
  }

  @Override
  public void addURL(URL url) {
    System.out.println("Intercepted MutableURLClassLoader.add");
    super.addURL(url);
  }

  @Override
  public URL[] getURLs() {
    System.out.println("Intercepted MutableURLClassLoader.get");
    return super.getURLs();
  }
}
