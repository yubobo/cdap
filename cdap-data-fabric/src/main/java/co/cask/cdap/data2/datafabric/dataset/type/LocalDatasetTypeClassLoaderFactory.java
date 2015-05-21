/*
 * Copyright © 2014 Cask Data, Inc.
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

package co.cask.cdap.data2.datafabric.dataset.type;

import co.cask.cdap.common.lang.jar.BundleJarUtil;
import co.cask.cdap.common.utils.DirUtils;
import co.cask.cdap.proto.DatasetModuleMeta;
import co.cask.common.lang.DirectoryClassLoader;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Creates a {@link ClassLoader} for a {@link DatasetModuleMeta} in local mode. Unpacks the jar if it is not unpacked
 * already, and uses a {@link DirectoryClassLoader} on the unpacked jar.
 */
public class LocalDatasetTypeClassLoaderFactory implements DatasetTypeClassLoaderFactory {
  private static final Logger LOG = LoggerFactory.getLogger(LocalDatasetTypeClassLoaderFactory.class);

  @Override
  public ClassLoader create(DatasetModuleMeta moduleMeta, ClassLoader parentClassLoader) throws IOException {
    URI jarLocation = moduleMeta.getJarLocation();
    if (jarLocation == null) {
      return parentClassLoader;
    }

    File jarFile = new File(jarLocation);
    String dirName = String.format("%s.%s", moduleMeta.getName(), Files.hash(jarFile, Hashing.md5()).toString());
    File expandDir = new File(jarFile.getParent(), dirName);

    if (!expandDir.isDirectory()) {
      // If not expanded before, expand the jar to a tmp folder and rename into the intended one.
      // It's needed if there are multiple threads try to get the same dataset.
      File tempDir = Files.createTempDir();
      try {
        BundleJarUtil.unpackProgramJar(Files.newInputStreamSupplier(jarFile), tempDir);
        if (!tempDir.renameTo(expandDir) && !expandDir.isDirectory()) {
          throw new IOException("Failed to rename expanded jar directory from " + tempDir + " to " + expandDir);
        }
      } finally {
        try {
          if (tempDir.exists()) {
            DirUtils.deleteDirectoryContents(tempDir);
          }
        } catch (IOException e) {
          // Just log, no propagate
          LOG.warn("Failed to delete temp directory {}", tempDir, e);
        }
      }
    }
    return new DirectoryClassLoader(expandDir, parentClassLoader, "lib");
  }
}
