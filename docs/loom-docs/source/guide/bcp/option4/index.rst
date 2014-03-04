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
If synchronous Database cluster turns out to be not optimal for a Loom installation, then we have to consider an alternative solution where we use local Databases in each data center with custom data replication service. This will allow for all data centers to share the data, while reducing the need to replicate all the data in the Database.

Since we will not be sharing a Databasse across data centers now, we'll need to treat the data a little bit differently than before. 
The data will be divided into shards based on the cluster ID partitioning. Each shard will be exclusively assigned to an owner Database in a data center. Only Loom Servers running locally will be able to write to such a shard. Any requests to update the clusters in non-local shard will be routed to the Loom Server runninig in the data center owning the shard. This update strategy prevents write conflicts since any updates to a cluster can be done only in a single Database.

The custom replication replicates data only upon completion of operations, and any minimal state information to restart any operations in progress when a data center goes down is also replicated. Intermediate state change data will not be replicated, hence reducing the replication overhead.

Also, each shard is synchronously replicated locally (to handle intra-datacenter failover), and also replicated to at least one remote data center.

Since we now do not replicate each state change across data centers, users of a data center will have a delayed view of remote cluster state. The replication implementation should try to make this dealy as minimal as possible.

Failover
========
When a data center fails, the remote data centers having the shards of the failed data center will become the new owners of the shards.
All calls to these shards will need to be routed to the new owner data centers.
User traffic from the failed data center will be re-routed to other data centers automatically by the load balancer.

Pros
====
#. Reads/writes to local shards (majority of operations) are optimal.
#. Data consistency is maintained on updates due to all updates for a cluster getting routed to a single Database.
#. Improves scalability due to selective replication of data.
#. Minimal data loss on data center failure.

Cons
====
#. Complex to implement.
#. Writes to non-local shards will be slow.
#. All transactions in progress in a data center when it goes down will be lost.

