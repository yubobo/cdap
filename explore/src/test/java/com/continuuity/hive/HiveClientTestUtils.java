package com.continuuity.hive;

import com.continuuity.hive.client.HiveClient;

import junit.framework.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Utility class for hive tests.
 */
public class HiveClientTestUtils {
  private static final Logger LOG = LoggerFactory.getLogger(HiveClientTestUtils.class);

  public static void assertCmdFindPattern(HiveClient hiveClient, String cmd, String pattern) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    hiveClient.sendCommand(cmd, out, null);
    String res = out.toString("UTF-8");
    LOG.error("Command result {}", res);
    try {
      Assert.assertTrue(Pattern.compile(pattern, Pattern.DOTALL).matcher(res).find());
    } finally {
      out.close();
    }
  }
}
