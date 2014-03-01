.. _overview_multi_data_center_high-availability:
.. index::
   single: Hot-Hot : With Multiple Master
===============================
Hot-Hot : With Multiple Masters
===============================

.. _multiple-masters:
.. figure:: /_images/ha_multi_master.png
    :align: center
    :alt: Multiple Masters Architecture Diagram
    :figclass: align-center

Overview
--------
#. Data will be divided into shards, and divided amongst DBs across all data centers. Each shard will have one owner DB.
#. All DBs will have Master-Master asynchronous replication between them. 
#. Data partitioning will prevent conflicts.
#. Regular operations in a data center will happen on local shards (local DB).
#. DB calls to other shards will be routed to the DB that is the owner of the shard.
#. In-flight queues on Zoo Keeper will still remain local to a data center.

Failover
--------
#. When a data center fails, the shards local to the data center will need a new owner DB.
#. All calls to failed over shards will need to be routed to the new owner DB.

Pros
----
#. Reads/writes to local shards (majority of operations) are optimal.
#. Consistent view of data at all times from all data centers due to routing.

Cons
----
#. Data, of a shard, not yet replicated will be lost in the event of a data center failure.
#. All transactions in progress in a data center when it goes down will be lost.
#. Reads/writes to non-local shards will be slow.
#. No control on what data gets replicated, can lead to massive change logs.
