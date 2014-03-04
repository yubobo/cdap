.. _overview_single_data_center:
.. index::
   single: Data Center High Availability
=============================
Datacenter High Availability
=============================

Loom can be configured to be resilient to machine or component failures. This document describes the recommended configuration 
for setting up Loom for HA within a data center. Please refer to `multi-data center HA <multi-data-center-bcp>` documentation
for configuring HA across multiple data centers.

In order to support resiliency against machine or component failures within a data center Loom component can be configured to 
run with redundancies on multiple machines. Please look at the diagram below:

#. loom-ui can be configured on multiple machines fronted by any widely available load balancers (e.g. HAproxy or Varnish or VIP)
#. Multiple instances of loom-provisioner can be started on a single box, but they can also be configured to run on multiple boxes.
#. ZooKeeper quorum of at least 3 nodes, with an odd number of total nodes.
#. Multiple Loom Servers with HAProxy in front of them. UI and provisioner connect to Loom Server using HAProxy interface.
#. Database replication with automatic failover in case one of them goes down. DB is also fronted by a proxy, and Loom Server connects to DB using the proxy.

.. note:: This configuration requires a minimum of 3 machines since the smallest redundant ZooKeeper quorum needs 3 nodes.

.. _single-dc:
.. figure:: /_images/ha_within_colo.png
    :align: center
    :alt: Within Datacenter Architecture Diagram

