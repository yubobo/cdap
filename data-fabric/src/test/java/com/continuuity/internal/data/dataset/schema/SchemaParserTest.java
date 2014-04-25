package com.continuuity.internal.data.dataset.schema;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests SchemaParser.
 */
public class SchemaParserTest {
  @Test
  public void testSchemaParsePrimitive() throws Exception {
    Assert.assertEquals(Schema.builder().add("size", FieldType.INT).build(), SchemaParser.parse(" size :  INT "));
    Assert.assertEquals(Schema.builder().add("length", FieldType.DOUBLE).build(), SchemaParser.parse("length:DOUBLE"));
    Assert.assertEquals(Schema.builder().add("name", FieldType.STRING).build(), SchemaParser.parse("name :STRING "));
  }

  @Test
  public void testSchemaParseList() throws Exception {
    Assert.assertEquals(Schema.builder().add("sizes", FieldType.list(FieldType.INT)).build(),
                        SchemaParser.parse(" sizes :\n [ INT ] "));
    Assert.assertEquals(Schema.builder().add("lengths", FieldType.list(FieldType.DOUBLE)).build(),
                        SchemaParser.parse("lengths:[DOUBLE\r]"));
    Assert.assertEquals(Schema.builder().add("names", FieldType.list(FieldType.STRING)).build(),
                        SchemaParser.parse("names :   [STRING ] "));
  }

  @Test
  public void testSchemaParseMap() throws Exception {
    Assert.assertEquals(Schema.builder().add("sizes", FieldType.map(FieldType.STRING, FieldType.INT)).build(),
                        SchemaParser.parse(" sizes : ( STRING ->\t INT ) "));
    Assert.assertEquals(Schema.builder().add("lengths", FieldType.map(FieldType.STRING, FieldType.DOUBLE)).build(),
                        SchemaParser.parse("lengths:(STRING->DOUBLE)"));
    Assert.assertEquals(Schema.builder().add("names", FieldType.map(FieldType.STRING, FieldType.BINARY)).build(),
                        SchemaParser.parse("names :\f   (STRING-> BINARY ) "));
    Assert.assertEquals(Schema.builder().add("lengths",
                                             FieldType.map(FieldType.STRING,
                                                           FieldType.record(Schema.builder().add("size", FieldType.INT)
                                                                              .build())
                                             )).build(),
                        SchemaParser.parse("lengths:(STRING->{ size : INT })"));
  }

  @Test
  public void testSchemaParseRecord() throws Exception {
    Assert.assertEquals(Schema.builder()
                          .add("room_dimension",
                               FieldType.record(Schema.builder()
                                                  .add("length", FieldType.INT)
                                                  .add("width", FieldType.FLOAT)
                                                  .add("height", FieldType.LONG)
                                                  .build()))
                          .build(),
                        SchemaParser.parse(" room_dimension : { length : INT, width:FLOAT ,height :LONG } "));
  }

  @Test
  public void testSchemaParse() throws Exception {
    Schema expectedSchema = Schema.builder()
      .add("name", FieldType.STRING)
      .add("age", FieldType.INT)
      .add("net_worth", FieldType.LONG)
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

    String schemaString =
      "name:STRING," +
        " age : INT ," +
        "net_worth\t : LONG," +
        "alive : BOOLEAN, " +
        "weight : FLOAT," +
        "kloutscore : DOUBLE\n," +
        "secret : BINARY\n," +
        "kids : [" +
        "          {" +
        "             name: STRING," +
        "             age: INT" +
        "          }" +
        "       ]," +
        "address : ( STRING -> STRING)"
      ;

    Assert.assertEquals(expectedSchema, SchemaParser.parse(schemaString));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidName() throws Exception {
    SchemaParser.parse(" size.int :  INT");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyName() throws Exception {
    SchemaParser.parse(" size :  INT, : LONG");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptySchema() throws Exception {
    SchemaParser.parse("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSpaceSchema() throws Exception {
    SchemaParser.parse(" ");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidType() throws Exception {
    SchemaParser.parse(" size : TINYINT ");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidMap() throws Exception {
    SchemaParser.parse("lengths:(INT->DOUBLE)");
  }
}
