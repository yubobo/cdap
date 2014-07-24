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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * POJOCreator
 */

public class POJOCreator {

  public Map<String, Map<Method, Object>> parse(Map<String, Map<String, GDATRecord>> dataStream, Map<String, List<Method>> methodList) {
    Map<String, Map<Method, Object>> outList = new Hashtable<String, Map<Method, Object>>();
    for (String query : dataStream.keySet()) {
      Map<Method, Object> pojoMap = new Hashtable<Method, Object>();
      for (Method m : methodList.get(query)) {
        Class<?> c = m.getParameterTypes()[0];
        Object pojo;
        try {
          pojo = c.newInstance();
        } catch (Exception e) {
          throw new RuntimeException("Error instantiating object of type " + c.getName());
        }
        for (Field f : m.getParameterTypes()[0].getDeclaredFields()) {
          GDATRecord gdatRecord = dataStream.get(query).get(f.getName());
          Object field = decode(TypeCast.cast(gdatRecord.getField().getType().getTypeName()), gdatRecord.getBytes());
          try {
            f.set(pojo, field);
          } catch (Exception e) {
            throw new RuntimeException("Error casting field from " + gdatRecord.getField().getName() + " to " + field.getClass().getName());
          }
          pojoMap.put(m, pojo);
        }
      }
      outList.put(query, pojoMap);
    }
    return outList;
  }

  public Object decode(Class<?> field, byte[] bytes) {
    ByteArrayDecoder binaryDecoder = new ByteArrayDecoder(bytes);
    String outClass = field.getSimpleName();
    try {
      if (outClass.equals("String")) {
        return binaryDecoder.getString();
      } else if (outClass.equals("boolean")) {
        return binaryDecoder.getBool();
      } else if (outClass.equals("int")) {
        return binaryDecoder.getInt();
      } else if (outClass.equals("long")) {
        return binaryDecoder.getLong();
      } else if (outClass.equals("float")) {
        return binaryDecoder.getFloat();
      } else if (outClass.equals("BigInteger")) {
        return binaryDecoder.getBigInt();
      }
    } catch (Exception e) {
      throw new RuntimeException("Error cannot decode byte stream into " + outClass);
    }
    return null;
  }

}


interface GDATField {
  /**
   * Get the Field Name.
   * @return Field Name.
   */
  String getName();

  /**
   * Get the Field Type.
   * @return Field Type.
   */
  GDATFieldType getType();

  /**
   * Sliding Window Type.
   * @return the Sliding Window Type.
   */
  GDATSlidingWindowAttribute getSlidingWindowType();
}

enum GDATFieldType {
  BOOL("bool"),
  USHORT("ushort"),
  UINT("uint"),
  INT("int"),
  ULLONG("ullong"),
  LLONG("llong"),
  FLOAT("float"),
  STRING("string");

  private String type;

  private GDATFieldType(String type) {
    this.type = type;
  }

  public String getTypeName() {
    return type;
  }
}

class DefaultGDATField implements GDATField {

  private String name;
  private GDATFieldType fieldType;
  private GDATSlidingWindowAttribute windowType;

  public DefaultGDATField(String name, GDATFieldType fieldType, GDATSlidingWindowAttribute windowType) {
    this.name = name;
    this.fieldType = fieldType;
    this.windowType = windowType;
  }

  public DefaultGDATField(String name, GDATFieldType fieldType) {
    this.name = name;
    this.fieldType = fieldType;
    this.windowType = GDATSlidingWindowAttribute.NONE;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public GDATFieldType getType() {
    return fieldType;
  }

  @Override
  public GDATSlidingWindowAttribute getSlidingWindowType() {
    return windowType;
  }
}

enum GDATSlidingWindowAttribute {
  INCREASING(" (increasing) "),
  DECREASING(" (decreasing) "),
  NONE("");

  private String attribute;

  private GDATSlidingWindowAttribute(String attribute) {
    this.attribute = attribute;
  }

  public String getAttribute() {
    return attribute;
  }
}

class GDATRecord {
  private GDATField field;
  private byte[] bytes;

  public GDATRecord(GDATField g, byte[] b) {
    this.field = g;
    this.bytes = b;
  }

  public GDATField getField() {
    return field;
  }

  public byte[] getBytes() {
    return bytes;
  }
}
