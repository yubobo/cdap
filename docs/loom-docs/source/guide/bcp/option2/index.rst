.. _overview_multi_data_center_high-availability:
.. index::
   single: Hot-Hot : With Single Master
============================
Hot-Hot : With Single Master
============================

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
#. User traffic to Loom UIs across data centers is routed through HAProxy.
#. In-flight queues on Zoo Keeper will still remain local to a data center.

Failover
--------
#. When data center with master DB fails, the failover to promote a slave to be master can be automatic or manual. 
#. When a slave becomes master, all requests from loom servers will need to be directed to it (can be automatic).
#. User traffic from the failed data center will be re-routed to other data centers automatically by HAProxy.

Pros
----
#. Consistent view of data at all times from all data centers.

Cons
----
#. All DB operations (read/write) from non-master data centers will be slow. This may lead to degradation of UI responsiveness especially if Master DB is halfway across the world.
#. Single Master DB may not scale as data size and operations per sec increase.
#. Data not yet replicated will be lost in the event of a data center failure.
#. All transactions in progress across all data centers when Master data center goes down will be lost.
#. On data center failure, the jobs in that data center will not make any progress.
#. No control on what data gets replicated, can lead to massive change logs.
