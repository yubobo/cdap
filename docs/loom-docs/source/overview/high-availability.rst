.. _overview_high-availability:
.. index::
   single: High Availability
============
High Availability
============

We first describe how high availability is achieved within a single datacenter, then describe several 
possible high availability setups across datacenters.

Within a Datacenter
===================

.. _single-dc:
.. figure:: /_images/ha_within_colo.png
    :align: center
    :alt: Within Datacenter Architecture Diagram

Overview
--------
#. Multiple UIs with HAProxy in front of them
#. ZooKeeper quorum of at least 3 nodes, with an odd number of total nodes.
#. Multiple Loom Servers with HAProxy in front of them.  HA achieved through leader election.
#. Multiple Provisioners to perform tasks.
#. Database replication with automatic failover in case one of them goes down.

Single Master
=============

.. _single-master:
.. figure:: /_images/ha_single_master.png
    :align: center
    :alt: Single Master Architecture Diagram
    :figclass: align-center

Overview
--------
#. Single Master Loom DB at any point of time across all data centers. 
#. Master DB will get replicated to DBs in other data centers using asynchronous Master-Slave replication. 
#. All the reads and writes from Loom Servers in other data centers will go to the Master DB. 
#. In-flight queues on Zoo Keeper will still remain local to a data center.

Failover
--------
#. When data center with master DB fails, the failover to promote a slave to be master can be automatic or manual. 
#. When a slave becomes master, all requests from loom servers will need to be directed to it (can be automatic).

Changes
-------
#. Resource leak detection and fix job.
#. Loom Server HA within a data center.
#. ID generation will have to move to Master DB.
#. Loom Servers will have to handle data inconsistency when Master DB goes down.

Pros
----
#. Minimal changes to existing design.
#. Consistent view of data at all times from all data centers.

Cons
----
#. All DB operations (read/write) from non-master data centers will be slow. This may lead to degradation of UI responsiveness especially if Master DB is halfway across the world.
#. Single Master DB may not scale as data size and operations per sec increase.
#. Data not yet replicated will be lost in the event of a data center failure.
#. All transactions in progress across all data centers when Master data center goes down will be lost.
#. On data center failure, the jobs in that data center will not make any progress.
#. No control on what data gets replicated, can lead to massive change logs.


Multiple Masters
=============

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

Changes
-------
#. Resource leak detection and fix job.
#. Loom Server HA within a data center.
#. Partition data into shards.
#. Loom Server that becomes a new owner of a shard will have to handle in-progress tasks of failed shard.

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


Custom Replication
==================

.. _custom-replication:
.. figure:: /_images/ha_custom.png
    :align: center
    :alt: Custom Replication Architecture Diagram
    :figclass: align-center

Overview
--------
#. Architecturally very similar to Multiple Master approach, except for using custom replication.
#. Data will be divided into shards, and divided amongst DBs across all data centers. Each shard will have one owner DB.
#. Each shard is synchronously replicated locally, and at least one copy on a remote data center. 
#. Replication occurs in chunks. Data replication upon completion of operations, and any state information to restart in progress operations are replicated.
#. Data partitioning will prevent conflicts.
#. Regular operations in a data center will happen on local shards (local DB).
#. DB calls to other shards will be routed to the DB that is the owner of the shard.
#. In-flight queues on Zoo Keeper will still remain local to a data center.

Failover
--------
#. When a data center fails, the remote data center having the shards of the failed data center will become the new owner of the shards.
#. All calls to failed over shards will need to be routed to the new owner DB.

Changes
-------
#. Resource leak detection and fix job.
#. Loom Server HA within a data center.
#. Partition data into shards.
#. Loom Server that becomes a new owner of a shard will have to handle in-progress tasks of failed shard.
#. Custom replication.

Pros
----
#. Reads/writes to local shards (majority of operations) are optimal.
#. Consistent view of data at all times from all data centers due to routing.
#. Highly scalable.
#. Minimal data loss on data center failure.

Cons
----
#. Complex to implement.
#. Reads/writes to non-local shards will be slow.
#. All transactions in progress in a data center when it goes down will be lost.

