package com.continuuity.hive.hooks;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.hive.context.CConfSerDe;
import com.continuuity.hive.context.HConfSerDe;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hive.ql.HiveDriverRunHook;
import org.apache.hadoop.hive.ql.HiveDriverRunHookContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ContextDriverHook implements HiveDriverRunHook {
  private static final Logger LOG = LoggerFactory.getLogger(ContextDriverHook.class);

  @Override
  public void preDriverRun(HiveDriverRunHookContext hookContext) throws Exception {
    LOG.info("Serializing context in driver hook");
    Configuration conf = hookContext.getConf();
    CConfSerDe.serialize(CConfiguration.create(), conf);
    HConfSerDe.serialize(HBaseConfiguration.create(), conf);
  }

  @Override
  public void postDriverRun(HiveDriverRunHookContext hookContext) throws Exception {
    // nothing to do!
  }
}
