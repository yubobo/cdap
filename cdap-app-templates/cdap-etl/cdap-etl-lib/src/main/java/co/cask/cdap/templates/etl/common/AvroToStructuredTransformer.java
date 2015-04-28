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

package co.cask.cdap.templates.etl.common;

import co.cask.cdap.api.data.format.StructuredRecord;
import co.cask.cdap.api.data.schema.Schema;
import com.google.common.collect.Maps;
import org.apache.avro.generic.GenericRecord;

import java.util.Map;

/**
 * Creates StructuredRecord from GenericRecord, with caching for schemas. The assumption is that most of the
 * records it transforms have the same schema.
 */
public class AvroToStructuredTransformer {
  private final Map<Integer, Schema> schemaCache = Maps.newHashMap();

  public StructuredRecord transform(GenericRecord genericRecord) throws Exception {
    org.apache.avro.Schema avroSchema = genericRecord.getSchema();

    int hashCode = avroSchema.hashCode();
    Schema schema;

    if (schemaCache.containsKey(hashCode)) {
      schema = schemaCache.get(hashCode);
    } else {
      schema = Schema.parseJson(avroSchema.toString());
      schemaCache.put(hashCode, schema);
    }

    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    for (org.apache.avro.Schema.Field field : avroSchema.getFields()) {
      String fieldName = field.name();
      builder.set(fieldName, genericRecord.get(fieldName));
    }
    return builder.build();
  }
}
