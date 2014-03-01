.. _overview_single_data_center:
.. index::
   single: Data Center High Availability
=============================
Datacenter High Availability
=============================

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

