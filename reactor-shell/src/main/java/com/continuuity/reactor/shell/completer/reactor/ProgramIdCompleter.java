/*
 * Copyright 2012-2014 Continuuity, Inc.
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

package com.continuuity.reactor.shell.completer.reactor;

import com.continuuity.reactor.client.ReactorAppClient;
import com.continuuity.reactor.client.exception.ApplicationNotFoundException;
import com.continuuity.reactor.metadata.ProgramRecord;
import com.continuuity.reactor.metadata.ProgramType;
import com.continuuity.reactor.shell.completer.StringsCompleter;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

/**
 * Completer for program IDs.
 */
public class ProgramIdCompleter extends StringsCompleter {

  private static final Logger LOG = LoggerFactory.getLogger(ProgramIdCompleter.class);

  public ProgramIdCompleter(final ReactorAppClient appClient, final ProgramType programType) {
    super(new Supplier<Collection<String>>() {
      @Override
      public Collection<String> get() {
        try {
          List<ProgramRecord> programs = appClient.listAllPrograms(programType);
          List<String> programIds = Lists.newArrayList();
          for (ProgramRecord programRecord : programs) {
            programIds.add(programRecord.getApp() + "." + programRecord.getId());
          }
          return programIds;
        } catch (IOException e) {
          e.printStackTrace();
          return Lists.newArrayList();
        }
      }
    });
  }
}
