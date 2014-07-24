/**
 * Copyright 2012-2014 Continuuity, Inc.
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

package com.continuuity.jetstream.decoder;

import org.apache.commons.io.Charsets;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * ByteArrayDecoder
 */

public class ByteArrayDecoder {
  private byte[] bytes;

  public ByteArrayDecoder(byte [] bytes) {
    this.bytes = bytes;
  }

  public int getInt() {
    ByteBuffer wrapper = ByteBuffer.wrap(bytes);
    return wrapper.getInt();
  }

  public Long getLong() {
    ByteBuffer wrapper = ByteBuffer.wrap(bytes);
    return wrapper.getLong();
  }

  public BigInteger getBigInt() {
    return new BigInteger(bytes);
  }

  public boolean getBool() {
    return (bytes[0] == 1);
  }

  public String getString() {
    return new String(bytes, Charsets.UTF_8);
  }

  public float getFloat() {
    ByteBuffer wrapper = ByteBuffer.wrap(bytes);
    return wrapper.getFloat();
  }
}
