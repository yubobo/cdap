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

package co.cask.cdap.examples.purchase;

import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.api.data.batch.RecordScannable;
import co.cask.cdap.api.data.batch.RecordScanner;
import co.cask.cdap.api.data.batch.Split;
import co.cask.cdap.api.dataset.DatasetSpecification;
import co.cask.cdap.api.dataset.lib.AbstractDataset;
import co.cask.cdap.api.dataset.lib.KeyValue;
import co.cask.cdap.api.dataset.lib.KeyValueTable;
import co.cask.cdap.api.dataset.module.EmbeddedDataset;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;

/**
 *
 */
public final class RecordDataset extends AbstractDataset implements RecordScannable<Record> {
  private static final Logger LOG = LoggerFactory.getLogger(RecordDataset.class);
  private static final Gson GSON = new Gson();
  private final KeyValueTable table;
  private final Class recordClass;

  public RecordDataset(DatasetSpecification spec,
                       @EmbeddedDataset("table") KeyValueTable table) throws ClassNotFoundException {
    super(spec.getName(), table);
    this.table = table;
    this.recordClass = Record.class;
  }

  public Object getRecord(String key) {
    byte[] serializedRecord = table.read(key);
    if (serializedRecord == null) {
      return null;
    }
    return GSON.fromJson(Bytes.toString(table.read(key)), recordClass);
  }

  public void writeRecord(String key, Object object) {
    LOG.info("Writing object of type: {}. Expected type: {}", object.getClass(), recordClass.getName());
    Preconditions.checkArgument(recordClass.isInstance(object));
    table.write(key, GSON.toJson(object, recordClass));
  }

  @Override
  public Type getRecordType() {
    return Record.class;
  }

  @Override
  public List<Split> getSplits() {
    return table.getSplits();
  }

  @Override
  public RecordScanner<Record> createSplitRecordScanner(Split split) {
    final RecordScanner<KeyValue<byte[], byte[]>> scanner = table.createSplitRecordScanner(split);
    return new RecordScanner<Record>() {
      @Override
      public void initialize(Split split) throws InterruptedException {
        scanner.initialize(split);
      }

      @Override
      public boolean nextRecord() throws InterruptedException {
        return scanner.nextRecord();
      }

      @Override
      public Record getCurrentRecord() throws InterruptedException {
        KeyValue<byte[], byte[]> record = scanner.getCurrentRecord();
        return new Gson().fromJson(Bytes.toString(record.getValue()), Record.class);
      }

      @Override
      public void close() {
        scanner.close();
      }
    };
  }
}
