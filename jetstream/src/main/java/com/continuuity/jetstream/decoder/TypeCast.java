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

import java.math.BigInteger;
import java.util.Hashtable;
import java.util.Map;

/**
 * TypeCast
 */
enum TypeCast {
  BOOL("bool", boolean.class),
  USHORT("ushort", int.class),
  UINT("uint", long.class),
  INT("int", int.class),
  ULLONG("ullong", BigInteger.class),
  LLONG("llong", BigInteger.class),
  FLOAT("float", float.class),
  STRING("string", String.class);

  private static final Map<String, Class<?>> castMap = new Hashtable<String, Class<?>>();
  private String incoming;
  private Class<?> out;

  private TypeCast(String in, Class<?> out) {
    this.incoming = in;
    this.out = out;
  }

  public String getTypeName() {
    return incoming;
  }

  public Class<?> getCast() {
    return out;
  }

  static {
    for (TypeCast f : TypeCast.values()) {
      castMap.put(f.getTypeName(), f.getCast());
    }
  }

  public static Class<?> cast(String in) {
    return castMap.get(in);
  }
}