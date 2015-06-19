/*
 * Copyright © 2014-2015 Cask Data, Inc.
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

package co.cask.cdap.test.app;

import co.cask.cdap.api.TxRunnable;
import co.cask.cdap.api.app.AbstractApplication;
import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.api.data.DatasetContext;
import co.cask.cdap.api.dataset.lib.KeyValueTable;
import co.cask.cdap.api.workflow.AbstractWorkflow;
import co.cask.cdap.api.workflow.AbstractWorkflowAction;

/**
 * App which copies data from one KVTable to another using a Workflow Custom Action.
 */
public class DatasetWithCustomActionApp extends AbstractApplication {
  public static final String INPUT_DS = "t1";
  public static final String OUTPUT_DS = "t2";
  public static final String CUSTOM_PROGRAM = "DatasetWithCustomActionApp";
  public static final String CUSTOM_WORKFLOW = "CustomWorkflow";

  @Override
  public void configure() {
    setName(CUSTOM_PROGRAM);
    addWorkflow(new CustomWorkflow());
  }

  public static class CustomWorkflow extends AbstractWorkflow {

    private static class TestAction extends AbstractWorkflowAction {

      private static class DoAction implements TxRunnable {

        @Override
        public void run(DatasetContext context) throws Exception {
          KeyValueTable outputTable = context.getDataset(DatasetWithCustomActionApp.INPUT_DS);
          String read = Bytes.toString(outputTable.read("hello"));

          KeyValueTable inputTable = context.getDataset(DatasetWithCustomActionApp.OUTPUT_DS);
          inputTable.write("hello", read);
        }
      }

      @Override
      public void run() {
        getContext().getWorkflowActionContext().execute(new DoAction());
      }
    }

    @Override
    protected void configure() {
      setName(CUSTOM_WORKFLOW);
      addAction(new TestAction());
    }
  }
}
