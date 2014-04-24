package com.continuuity.internal.data.dataset.schema;

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

    Assert.assertTrue(schema.hasField("weight"));
    Assert.assertTrue(schema.getType("kids").isList());
    Assert.assertTrue(schema.getType("kids").getElementType().isRecord());
  }
}
