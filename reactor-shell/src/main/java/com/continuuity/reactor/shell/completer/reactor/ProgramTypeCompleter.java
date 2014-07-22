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

import jline.console.completer.StringsCompleter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import javax.inject.Inject;

/**
 * Completer for program types.
 */
public class ProgramTypeCompleter extends StringsCompleter {

  private static final Logger LOG = LoggerFactory.getLogger(ProgramTypeCompleter.class);

  @Inject
  public ProgramTypeCompleter() {
    super("flows", "procedures", "mapreduce", "workflows", "services");
  }

  @Override
  public int complete(String buffer, int cursor, List<CharSequence> candidates) {
    int result = super.complete(buffer, cursor, candidates);
//    LOG.debug("complete({}, {}, candidates) = {}", buffer, cursor, result);
    return result;
  }
}
