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

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * POJOCreatorTest
 */

public class POJOCreatorTest {
  private static Map<String, List<Method>> methodList;
  private static Map<String, Map<String, GDATRecord>> dataStream;



  @BeforeClass
  public static void setup() {
    methodList = new Hashtable<String, List<Method>>();
    dataStream = new Hashtable<String, Map<String, GDATRecord>>();
  }

  @org.junit.Test
  public void testParse() {
    List<Method> methods = Lists.newArrayList();
    methods.add(SampleClass.class.getMethods()[0]);
    methodList.put("TestOutput", methods);
    Map<String, GDATRecord> data = new Hashtable<String, GDATRecord>();
    byte[] val = ByteBuffer.allocate(4).putInt(5).array();
    GDATField valField =  new DefaultGDATField("i", GDATFieldType.INT, GDATSlidingWindowAttribute.NONE);
    data.put(valField.getName(), new GDATRecord(valField, val));
    val = "testString".getBytes();
    valField =  new DefaultGDATField("s", GDATFieldType.STRING, GDATSlidingWindowAttribute.NONE);
    data.put(valField.getName(), new GDATRecord(valField, val));
    val = new byte[]{(byte) (true?1:0)};
    valField =  new DefaultGDATField("b", GDATFieldType.BOOL, GDATSlidingWindowAttribute.NONE);
    data.put(valField.getName(), new GDATRecord(valField, val));
    val = ByteBuffer.allocate(4).putFloat(new Float(1.1)).array();
    valField =  new DefaultGDATField("f", GDATFieldType.FLOAT, GDATSlidingWindowAttribute.NONE);
    data.put(valField.getName(), new GDATRecord(valField, val));
    byte[] valb = {0x7F, 0x7F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7F, 0x7F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7F, 0x7F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7F, 0x7F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7F, 0x7F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    valField =  new DefaultGDATField("bi", GDATFieldType.ULLONG, GDATSlidingWindowAttribute.NONE);
    data.put(valField.getName(), new GDATRecord(valField, valb));
    dataStream.put("TestOutput", data);
    POJOCreator pj = new POJOCreator();
    Map<String, Map<Method, Object>> res = pj.parse(dataStream, methodList);
    Assert.assertEquals(SampleClass.class, res.get("TestOutput").get(SampleClass.class.getMethods()[0]).getClass());
    SampleClass sObj = (SampleClass) res.get("TestOutput").get(SampleClass.class.getMethods()[0]);
    Assert.assertEquals(5, sObj.i);
    Assert.assertEquals("testString", sObj.s);
    Assert.assertEquals(true, sObj.b);
    Assert.assertEquals(1.1, sObj.f, .00001);
    Assert.assertEquals(new BigInteger("1554729475918024756470702392341693071496164156882500747417249199665980298150640278779591433868928079738842989051789351893183632514536079852830720"), sObj.bi);

  }
}


