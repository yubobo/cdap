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
import co.cask.cdap.proto.Id;
import org.apache.twill.api.RunId;

import java.util.Map;

/**
 * Records state changes during program runtime
 */
public interface ProgramStateChangeRecorder {
  void start(Id.Program program, RunId runId, ProgramController.State currentState,
             Map<String, String> runtimeArguments);
  void suspend(Id.Program program, RunId runId);
  void resume(Id.Program program, RunId runId);
  void stop(Id.Program program, RunId runId, ProgramController.State completionState);
}
