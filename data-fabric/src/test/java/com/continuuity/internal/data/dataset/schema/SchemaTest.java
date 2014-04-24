package com.continuuity.internal.data.dataset.schema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

public class SchemaTest {

  @Test
  public void testCompare() throws Exception {
    Assert.assertEquals(FieldType.INT, FieldType.INT);
  }

  @Test
  public void testBuilder() {
    Schema schema = Schema.builder()
      .add("name", FieldType.STRING)
      .add("age", FieldType.INT)
      .add("net worth", FieldType.LONG)
      .add("alive", FieldType.BOOLEAN)
      .add("weight", FieldType.FLOAT)
      .add("kloutscore", FieldType.DOUBLE)
      .add("secret", FieldType.BINARY)
      .add("kids", FieldType.list(FieldType.record(
        Schema.builder()
              .add("name", FieldType.STRING)
              .add("age", FieldType.INT)
              .build())))
      .add("address", FieldType.map(FieldType.STRING, FieldType.STRING))
      .build();

    Assert.assertTrue(schema.hasField("weight"));
    Assert.assertTrue(schema.getType("kids").isList());
    Assert.assertTrue(schema.getType("kids").getElementType().isRecord());

    Record record = DefaultRecord.of(schema)
      .set("name", "Poorna")
      .set("age", 34)
      .set("net worth", 10000000000L)
      .set("alive", true)
      .set("weight", 123.4F)
      .set("kloutscore", 123456.123456)
      .set("secret", new byte[] {0, 1, 2})
      .set("kids", ImmutableList.of(
        DefaultRecord.of(schema.getType("kids").getElementType().getRecordSchema())
        .set("name", "Anna")
        .set("age", 12)
        .build()
      ))
      .set("address", ImmutableMap.of("city", "sunnyvale"))
      .build();
  }
}

// {name:STRING,age:INT,...,kids:[{name:STRING,age:INT}],address:(STRING->STRING)}
// {
//   name: STRING,
//   age: INT,...,
//   kids:[
//     {
//       name:STRING,
//       age:INT
//     }
//   ],
//   "net:worth ": LONG,
//   address: (STRING->STRING)}

