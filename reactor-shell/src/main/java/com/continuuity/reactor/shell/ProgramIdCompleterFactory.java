/*
 * Copyright 2014 Continuuity, Inc.
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

package com.continuuity.reactor.shell;

import com.continuuity.reactor.client.ReactorAppClient;
import com.continuuity.reactor.shell.completer.reactor.ProgramIdCompleter;
import com.google.common.collect.ImmutableMap;
import jline.console.completer.Completer;

import java.util.Map;
import javax.inject.Inject;

/**
 * Provides {@link ProgramIdCompleter} implementations.
 */
public class ProgramIdCompleterFactory {

  private final Map<ProgramElementType, ProgramIdCompleter> programIdCompleters;

  @Inject
  public ProgramIdCompleterFactory(ReactorAppClient appClient) {
    this.programIdCompleters = ImmutableMap.<ProgramElementType, ProgramIdCompleter>builder()
      .put(ProgramElementType.FLOW,
           new ProgramIdCompleter(appClient, ProgramElementType.FLOW.getProgramType()))
      .put(ProgramElementType.MAPREDUCE,
           new ProgramIdCompleter(appClient, ProgramElementType.MAPREDUCE.getProgramType()))
      .put(ProgramElementType.PROCEDURE,
           new ProgramIdCompleter(appClient, ProgramElementType.PROCEDURE.getProgramType()))
      .put(ProgramElementType.WORKFLOW,
           new ProgramIdCompleter(appClient, ProgramElementType.WORKFLOW.getProgramType()))
      .build();
  }

  public Completer getProgramIdCompleter(ProgramElementType programElementType) {
    return programIdCompleters.get(programElementType);
  }
}
