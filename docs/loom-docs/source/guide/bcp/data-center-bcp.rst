.. _overview_single_data_center:
.. index::
   single: Data Center High Availability
=============================
Datacenter High Availability
=============================

High availability of the service within a data center is achieved by running redundant instances of all components on multiple machines. 

#. Multiple UIs with HAProxy in front of them.
#. ZooKeeper quorum of at least 3 nodes, with an odd number of total nodes.
#. Multiple Provisioners to perform tasks.
#. Multiple Loom Servers with HAProxy in front of them. UI and provisioner connect to Loom Server using HAProxy interface.
#. Database replication with automatic failover in case one of them goes down. DB is also fronted by a proxy, and Loom Server connects to DB using the proxy.

.. _single-dc:
.. figure:: /_images/ha_within_colo.png
    :align: center
    :alt: Within Datacenter Architecture Diagram

