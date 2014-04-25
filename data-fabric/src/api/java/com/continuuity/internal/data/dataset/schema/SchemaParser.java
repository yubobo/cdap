package com.continuuity.internal.data.dataset.schema;

import com.continuuity.common.utils.ImmutablePair;

/**
 * Parses a String schema representation into a Schema.
 */
public final class SchemaParser {
  private final String schemaString;
  private int index;

  // To prevent instantiation of the class.
  private SchemaParser(String schemaString) {
    this.schemaString = schemaString;
  }

  public static Schema parse(String string) {
    SchemaParser schemaParser = new SchemaParser(string);
    return schemaParser.parseSchema();
  }

  private boolean hasNextChar() {
    return index < schemaString.length();
  }

  private Schema parseSchema() {
    Schema schema = parseInnerSchema();
    if (hasNextChar()) {
      throw new IllegalArgumentException(String.format("Expected end of schema string. Got \"%s\"",
                                                       schemaString.substring(index)));
    }
    return schema;
  }

  private Schema parseInnerSchema() {
    Schema.Builder schemaBuilder = Schema.builder();
    while (true) {
      ignoreWhiteSpace();
      ImmutablePair<String, FieldType> schemaField = parseNameAndType();
      schemaBuilder.add(schemaField.getFirst(), schemaField.getSecond());
      ignoreWhiteSpace();

      if (!hasNextChar() || schemaString.charAt(index) != ',') {
        break;
      }
      expect(',');
    }
    return schemaBuilder.build();
  }

  private ImmutablePair<String, FieldType> parseNameAndType() {
    String name = parseName();
    ignoreWhiteSpace();
    expect(':');
    ignoreWhiteSpace();

    return new ImmutablePair<String, FieldType>(name, parseType());
  }

  private FieldType parseType() {
    if (schemaString.charAt(index) == '[') {
      return parseList();
    } else if (schemaString.charAt(index) == '(') {
      return parseMap();
    } else if (schemaString.charAt(index) == '{') {
      return parseRecord();
    }
    return parsePrimitive();
  }

  private FieldType parseRecord() {
    expect('{');
    ignoreWhiteSpace();
    Schema schema = parseInnerSchema();
    ignoreWhiteSpace();
    expect('}');

    return FieldType.record(schema);
  }

  private FieldType parseMap() {
    expect('(');
    ignoreWhiteSpace();
    FieldType keyType = parseType();
    ignoreWhiteSpace();
    expect('-');
    expect('>');
    ignoreWhiteSpace();
    FieldType valueType = parseType();
    ignoreWhiteSpace();
    expect(')');

    if (!keyType.equals(FieldType.STRING)) {
      throw new IllegalArgumentException("Map key type can only be STRING. Got " + keyType.getType());
    }

    return FieldType.map(keyType, valueType);
  }

  private FieldType parseList() {
    expect('[');
    ignoreWhiteSpace();
    FieldType fieldType = parseType();
    ignoreWhiteSpace();
    expect(']');
    return FieldType.list(fieldType);
  }

  private FieldType parsePrimitive() {
    String typeName = parseName();
    FieldType.Type type;
    try {
      type = FieldType.Type.valueOf(typeName);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Type " + typeName + " not found");
    }
    return new FieldType(type);
  }

  private String parseName() {
    int start = index;
    while (hasNextChar() && isNameChar(schemaString.charAt(index))) {
      ++index;
    }

    if (start == index) {
      throw new IllegalArgumentException(String.format("Empty name at position %d: \"%s\"",
                                                       index, schemaString.substring(index)));
    }
    return schemaString.substring(start, index);
  }

  private void expect(char c) {
    if (schemaString.charAt(index) == c) {
      ++index;
    } else {
      throw new IllegalArgumentException(String.format("Expected '%s', but got \"%s\" at position %d",
                                                    c, schemaString.charAt(index), index));
    }
  }

  private boolean isNameChar(char c) {
    return Character.isLetterOrDigit(c) || c == '_';
  }

  private void ignoreWhiteSpace() {
    while (hasNextChar() && Character.isWhitespace(schemaString.charAt(index))) {
      ++index;
    }
  }
}
