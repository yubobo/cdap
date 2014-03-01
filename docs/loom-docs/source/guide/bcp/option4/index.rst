.. _overview_multi_data_center_high-availability:
.. index::
   single: Hot-Hot : Custom Replication
=================================
Hot-Hot : With Custom Replication
=================================
.. _custom-replication:
.. figure:: /_images/ha_custom.png
    :align: center
    :alt: Custom Replication Architecture Diagram
    :figclass: align-center

Overview
========
#. Architecturally very similar to Multiple Master approach, except for using custom replication.
#. Data will be divided into shards, and divided amongst DBs across all data centers. Each shard will have one owner DB.
#. Each shard is synchronously replicated locally, and at least one copy on a remote data center. 
#. Replication occurs in chunks. Data replication upon completion of operations, and any state information to restart in progress operations are replicated.
#. Data partitioning will prevent conflicts.
#. Regular operations in a data center will happen on local shards (local DB).
#. DB calls to other shards will be routed to the DB that is the owner of the shard.
#. In-flight queues on Zoo Keeper will still remain local to a data center.

Failover
========
#. When a data center fails, the remote data center having the shards of the failed data center will become the new owner of the shards.
#. All calls to failed over shards will need to be routed to the new owner DB.

Pros
====
#. Reads/writes to local shards (majority of operations) are optimal.
#. Consistent view of data at all times from all data centers due to routing.
#. Highly scalable.
#. Minimal data loss on data center failure.

Cons
====
#. Complex to implement.
#. Reads/writes to non-local shards will be slow.
#. All transactions in progress in a data center when it goes down will be lost.

