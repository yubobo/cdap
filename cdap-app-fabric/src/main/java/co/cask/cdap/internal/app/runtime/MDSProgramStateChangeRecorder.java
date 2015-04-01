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

package co.cask.cdap.internal.app.runtime;

import co.cask.cdap.app.runtime.ProgramController;
import co.cask.cdap.app.store.Store;
import co.cask.cdap.proto.Id;
import com.google.inject.Inject;
import org.apache.twill.api.RunId;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ProgramStateChangeRecorder} that records state changes in the MDS
 */
public class MDSProgramStateChangeRecorder implements ProgramStateChangeRecorder {
  private final Store store;

  @Inject
  public MDSProgramStateChangeRecorder(Store store) {
    this.store = store;
  }

  @Override
  public void start(Id.Program program, RunId runId, ProgramController.State currentState,
                    Map<String, String> runtimeArguments) {
    store.setStart(program, runId.getId(), TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    if (isFinished(currentState)) {
      stop(program, runId, currentState);
    }
  }

  @Override
  public void suspend(Id.Program program, RunId runId) {
  }

  @Override
  public void resume(Id.Program program, RunId runId) {
  }

  @Override
  public void stop(Id.Program program, RunId runId, ProgramController.State completionState) {
    store.setStop(program, runId.getId(),
                  TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS),
                  completionState.getRunStatus());
  }

  private boolean isFinished(ProgramController.State currentState) {
    return currentState == ProgramController.State.COMPLETED ||
      currentState == ProgramController.State.ERROR ||
      currentState == ProgramController.State.KILLED;
  }
}
