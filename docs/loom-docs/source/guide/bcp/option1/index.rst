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
In this configuration a Database cluster with synchronous replication is shared across all data centers. User traffic to Loom UI is routed through HAProxy.

Failover
--------
#. When a data center fails, since the DB is synchronously replicated the data of the failed data center will be available to other data centers.
#. Traffic from the failed data center will be re-routed to other data centers automatically by HAProxy.

Pros
----
#. Consistent view of data at all times from all data centers.

Cons
----
#. Write operations can be slower due to synchronous replication. 
#. All transactions in progress in a data center when it goes down will be lost.
#. On data center failure, the jobs in that data center will not make any progress.
