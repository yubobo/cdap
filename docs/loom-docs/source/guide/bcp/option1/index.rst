.. _overview_multi_data_center_high-availability:
.. index::
   single: Hot-Hot : Synchronous DB Cluster

================================
Hot-Hot : Synchronous DB Cluster
================================

.. _synchronous-repl:
.. figure:: /_images/ha_synchronous_repl.png
    :align: center
    :alt: Synchronous DB Architecture Diagram
    :figclass: align-center

Overview
--------
#. DB Cluster is synchronously replicated across all data centers.
#. In-flight queues on Zoo Keeper will still remain local to a data center.

Failover
--------
#. When a data center fails, since the DB is synchronously replicated, the other data centers will not be affected.
#. Traffic from the failed data center will be re-routed to other data centers automatically.

Pros
----
#. Consistent view of data at all times from all data centers.

Cons
----
#. Write operations can be slower due to synchronous replication. 
#. All transactions in progress in a data center when it goes down will be lost.
#. On data center failure, the jobs in that data center will not make any progress.
