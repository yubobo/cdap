/*
 * Copyright © 2014 Cask Data, Inc.
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

package co.cask.cdap.data2.dataset2.lib.table;

import co.cask.cdap.api.dataset.DatasetProperties;
import co.cask.cdap.data2.dataset2.AbstractDatasetTest;
import co.cask.cdap.proto.ProgramRecord;
import co.cask.cdap.proto.ProgramType;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * PreferenceTable Dataset Tests.
 */
public class PreferenceTableDatasetTest extends AbstractDatasetTest {

  @Test
  public void testBasics() throws Exception {
    addModule("prefTableModule", new PreferenceTableModule());
    Map<String, String> content = Maps.newHashMap();
    content.put("k1", "v1");
    content.put("k2", "v2");
    content.put("key1", "v1");
    content.put("key2", "v2");

    createInstance(PreferenceTable.class.getName(), "myPrefTable", DatasetProperties.EMPTY);
    PreferenceTable myPrefTable = getInstance("myPrefTable");

    ProgramRecord record = new ProgramRecord(ProgramType.FLOW, "MyApp", "MyFlow");
    Assert.assertEquals(null, myPrefTable.getNote(record, "key1"));
    myPrefTable.setNote(record, "key1", "val1");
    Assert.assertEquals("val1", myPrefTable.getNote(record, "key1"));
    Assert.assertEquals(null, myPrefTable.getNote(record, "key2"));
    myPrefTable.setNotes(record, content);
    Map<String, String> notes = myPrefTable.getNotes(record);
    Assert.assertEquals(4, notes.size());
    Assert.assertEquals("v1", notes.get("k1"));
    Assert.assertEquals("v1", notes.get("key1"));
    deleteModule("prefTableModule");
  }
}