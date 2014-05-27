package com.continuuity.hive.context;

import com.continuuity.data2.transaction.Transaction;
import com.continuuity.data2.transaction.TransactionSystemClient;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializes and deserializes a Transaction into a Configuration object.
 */
public class TxnSerDe {
  private static final Logger LOG = LoggerFactory.getLogger(TxnSerDe.class);
  private static final Gson GSON = new Gson();

  public static void serialize(Transaction tx, Configuration conf) {
    LOG.debug("Serializing transaction {}", tx);
    conf.set(Constants.TX_QUERY, GSON.toJson(tx));
  }

  public static Transaction deserialize(Configuration conf) {
    String txJson = conf.get(Constants.TX_QUERY);
//    Preconditions.checkNotNull(txJson, "Transaction ID is empty.");
    Transaction tx;
    // Extremely dirty hack
    // This is the case when running a "select *" query. Then, preHook is executed, then
    // postHook, then goes here. And the conf doesn't have the tx, for some reason that
    // we have to investigate
    if (txJson == null) {
      TransactionSystemClient txClient = ContextManager.getTxClient(conf);
      tx = txClient.startLong();
      try {
        txClient.canCommit(tx, ImmutableList.<byte[]>of());
        txClient.commit(tx);
      } catch (Exception e) {
        // do nothing
      }
    } else {
      tx = GSON.fromJson(txJson, Transaction.class);
    }
    LOG.debug("De-serializing transaction {}", tx);
    return tx;
  }
}
