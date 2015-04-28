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

package co.cask.cdap.templates.etl.batch.sources;

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Name;
import co.cask.cdap.api.annotation.Plugin;
import co.cask.cdap.api.data.format.StructuredRecord;
import co.cask.cdap.api.dataset.lib.FileSet;
import co.cask.cdap.api.dataset.lib.FileSetArguments;
import co.cask.cdap.api.dataset.lib.FileSetProperties;
import co.cask.cdap.api.dataset.lib.KeyValue;
import co.cask.cdap.api.templates.plugins.PluginConfig;
import co.cask.cdap.templates.etl.api.Emitter;
import co.cask.cdap.templates.etl.api.PipelineConfigurer;
import co.cask.cdap.templates.etl.api.batch.BatchSource;
import co.cask.cdap.templates.etl.api.batch.BatchSourceContext;
import co.cask.cdap.templates.etl.api.config.ETLStage;
import co.cask.cdap.templates.etl.common.AvroToStructuredTransformer;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.avro.SchemaParseException;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyInputFormat;
import org.apache.avro.mapreduce.AvroKeyOutputFormat;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.twill.filesystem.LocalLocationFactory;
import org.apache.twill.filesystem.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * HDFS Source
 */
@Plugin(type = "source")
@Name("FileSet")
@Description("HDFS FileSet Batch Source")
public class HDFSSource extends BatchSource<AvroKey<GenericRecord>, NullWritable, StructuredRecord> {

  private static final Logger LOG = LoggerFactory.getLogger(HDFSSource.class);

  private static final String NAME_DESCRIPTION = "Name of the fileset to read.";
  private static final String BASE_PATH_DESC = "The base path for the time partitioned fileset. Defaults to the " +
    "name of the dataset";
  private static final String FILE_NAME_PATTERN =
    "The pattern to use for generating file name. Must be a pattern that can be parsed by SimpleDateFormat.";
  private static final String RUN_INTERVAL = "The interval at which the adapter runs in seconds.";

  private final String customFileSet = "customFileSet";
  private static final String SCHEMA_DESCRIPTION = "Schema for the fileset records. Schema is used " +
    "in conjunction with format to parse records.";

  private final AvroToStructuredTransformer recordTransformer = new AvroToStructuredTransformer();

  /**
   *
   */
  public static class FileSetConfig extends PluginConfig {
    @Description(NAME_DESCRIPTION)
    private String name;

    @Description(BASE_PATH_DESC)
    @Nullable
    private String basePath;

    @Description(FILE_NAME_PATTERN)
    private String fileNamePattern;

    @Description(RUN_INTERVAL)
    private Integer runInterval;

    @Description(SCHEMA_DESCRIPTION)
    private String schema;

    public FileSetConfig(String name, String basePath, String fileNamePattern, Integer runInterval, String schema) {
      this.name = name;
      this.basePath = basePath;
      this.fileNamePattern = fileNamePattern;
      this.runInterval = runInterval;
      this.schema = schema;
    }

    private void validate() {
      // check the schema if there is one
      if (!Strings.isNullOrEmpty(schema)) {
        parseSchema();
      }
    }

    private org.apache.avro.Schema parseSchema() {
      // try to parse the schema if there is one
      try {
        return schema == null ? null : new org.apache.avro.Schema.Parser().parse(schema);
      } catch (SchemaParseException e) {
        throw new IllegalArgumentException("Invalid schema: " + e.getMessage());
      }
    }
  }

  private final FileSetConfig fileSetConfig;

  public HDFSSource(FileSetConfig fileSetConfig) {
    this.fileSetConfig = fileSetConfig;
  }

  @Override
  public void configurePipeline(PipelineConfigurer pipelineConfigurer) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(fileSetConfig.name), "Fileset name must be provided.");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(fileSetConfig.fileNamePattern),
                                "File name pattern must be provided.");
    Preconditions.checkArgument(fileSetConfig.runInterval != null && fileSetConfig.runInterval > 0,
                                "Adapter run interval must be provided.");
    LOG.debug("Base path = {}", fileSetConfig.basePath);
    LOG.debug("File name pattern = {}", fileSetConfig.fileNamePattern);
    LOG.debug("Run interval = {}", fileSetConfig.runInterval);
    String basePath = fileSetConfig.basePath == null ? fileSetConfig.name : fileSetConfig.basePath;
    pipelineConfigurer.createDataset(customFileSet, "co.cask.cdap.examples.countrandom.CustomFileSetDataset",
                                     FileSetProperties.builder()
                                       .setBasePath(basePath)
                                       .setInputFormat(AvroKeyInputFormat.class)
                                       .setOutputFormat(AvroKeyOutputFormat.class)
                                       .build());
    fileSetConfig.validate();
  }

  @Override
  public void prepareJob(BatchSourceContext batchSourceContext) {
    Map<String, String> sourceArgs = Maps.newHashMap();
    FileSetArguments.setInputPaths(sourceArgs, getInputs(batchSourceContext));
    FileSet source = batchSourceContext.getDataset(customFileSet, sourceArgs);
    batchSourceContext.setInput(customFileSet, source);
    org.apache.avro.Schema avroSchema = new org.apache.avro.Schema.Parser().parse(
      fileSetConfig.parseSchema().toString());
    Job job = batchSourceContext.getHadoopJob();
    AvroJob.setInputKeySchema(job, avroSchema);
  }

  private Set<String> getInputs(BatchSourceContext batchSourceContext) {
    Set<String> files = Sets.newHashSet();
    long startTimeSecs = TimeUnit.MILLISECONDS.toSeconds(batchSourceContext.getLogicalStartTime());
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fileSetConfig.fileNamePattern);
    for (long begin = startTimeSecs - fileSetConfig.runInterval; begin < startTimeSecs; begin++) {
      String file = simpleDateFormat.format(new Date(TimeUnit.SECONDS.toMillis(begin)));
      try {
        Location location = fileSetConfig.basePath == null ? new LocalLocationFactory().create(file) :
          new LocalLocationFactory().create(fileSetConfig.basePath).append(file);
        if (location.exists()) {
          LOG.debug("Generating filename {}", location.toURI());
          files.add(file);
        } else {
          LOG.debug("Ignoring filename {}", location.toURI());
        }
      } catch (IOException e) {
        LOG.error("Got exception for file {}", file, e);
      }
    }
    return files;
  }

  @Override
  public void transform(KeyValue<AvroKey<GenericRecord>, NullWritable> input, Emitter<StructuredRecord> emitter)
    throws Exception {
    LOG.debug("Got record {}", input.getKey().datum());
    emitter.emit(recordTransformer.transform(input.getKey().datum()));
  }
}
