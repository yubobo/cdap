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
Among all Loom components, Database is the only component that stores persistent state information. Any HA configuration that runs redundant Loom services across data centers will have to make sure that the services in all data centers have a consistent view of this data. One way of achieving this consistency is to share a single Database cluster across all data centers as discussed below.

In this configuration a Database cluster with synchronous replication is shared across all data centers. Loom Servers in each data center will connect to the local instance of the Database cluster. All other components are configured as mentioned in :doc:`Datacenter High Availability  </guide/bcp/data-center-bcp>` section.

An advantage of this approach is that Loom Servers in all data centers have the same view of data at all times. Hence, users in all data centers will get to see the same state for all clusters at all times.

Failover
--------
When a data center fails in this setup, the data of the failed data center is still available in other data centers due to synchronous replication. 
Hence Loom Servers in other data centers should be able to handle user traffic from the failed data center. 

However, any transaction that was in progress when the datacenter failed will be lost as it was not committed. 
Also, any jobs that were in progress in the failed datacenter will not make any progress when the datacenter goes down.

User traffic from the failed data center will be re-routed to other data centers automatically by the load balancer.
