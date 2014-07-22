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

package com.continuuity.reactor.shell.util;

import com.google.common.base.Joiner;
import com.sun.istack.internal.Nullable;

import java.io.PrintStream;
import java.util.List;

/**
 * Utility class to print an ASCII table. e.g.
 *
 * @param <T>
 */
public class AsciiTable<T> {

  @Nullable
  private final Object[] header;
  private final List<T> rows;
  private final RowMaker<T> rowMaker;

  public AsciiTable(@Nullable String[] header, List<T> rows, RowMaker<T> rowMaker) {
    this.header = header;
    this.rows = rows;
    this.rowMaker = rowMaker;
  }

  public void print(PrintStream output) {
    Object[][] contents = new Object[rows.size()][];
    for (int i = 0; i < rows.size(); i++) {
      contents[i] = rowMaker.makeRow(rows.get(i));
    }

    int[] columnWidths = calculateColumnWidths(header, contents);
    String[] fillers = new String[columnWidths.length];
    for (int i = 0; i < columnWidths.length; i++) {
      fillers[i] = "%-" + columnWidths[i] + "s";
    }

    String template = "| " + Joiner.on(" | ").join(fillers) + " |";

    if (header != null) {
      output.println(generateDivider(columnWidths));
      output.printf(template + "\n", header);
    }

    output.println(generateDivider(columnWidths));
    for (Object[] row : contents) {
      output.printf(template + "\n", row);
    }
    output.println(generateDivider(columnWidths));
  }

  private String generateDivider(int[] columnWidths) {
    StringBuilder sb = new StringBuilder();
    sb.append("+");
    for (int columnWidth : columnWidths) {
      sb.append(times("-", columnWidth + 2));
    }

    // one for each divider
    sb.append(times("-", columnWidths.length - 1));
    sb.append("+");
    return sb.toString();
  }

  private String times(String string, int times) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < times; i++) {
      sb.append(string);
    }
    return sb.toString();
  }

  private int[] calculateColumnWidths(Object[] header, Object[][] contents) {
    Object[] row = header != null ? header : contents[0];
    int[] columnWidths = new int[row.length];
    for (int i = 0; i < row.length; i++) {
      columnWidths[i] = Math.max(row.length, getMaxLength(contents, i));
    }
    return columnWidths;
  }

  private int getMaxLength(Object[][] rows, int column) {
    int maxLength = 0;
    for (Object[] row : rows) {
      if (row != null) {
        String string = row[column].toString();
        if (string != null && string.length() > maxLength) {
          maxLength = string.length();
        }
      }
    }
    return maxLength;
  }
}
