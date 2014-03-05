.. _overview_single_data_center:
.. index::
   single: Data Center High Availability
=============================
Datacenter High Availability
=============================

Loom can be configured to be resilient to machine or component failures. This document describes the recommended configuration 
for setting up Loom for HA within a data center. Please refer to :doc:`multi-data center HA <multi-data-center-bcp>` documentation
for configuring HA across multiple data centers.

In order to support resiliency against machine or component failures within a data center, Loom components can be configured to 
run with redundancies on multiple machines. Each machine running Loom can have a maximum of -

* One loom-ui process
* One loom-server process
* Multiple loom-provisioner processes (See config LOOM_NUM_WORKERS in :doc:`installation guide </guide/installation/index>`)
* One ZooKeeper process
* One Database process

The diagram below shows the logical deployment diagram of Loom for HA in a data center-

.. _single-dc:
.. figure:: /_images/ha_within_colo.png
    :align: center
    :alt: Within Datacenter Architecture Diagram

Loom UI
------
Loom UI (loom-ui) is stateless, and communicates with Loom Server using REST endpoints. Hence Loom UI can be easily run on multiple machines. User traffic is routed to multiple instances of Loom UI using load balancers (like HAproxy or Varnish or VIP).

Loom Provisioner
----------------
Loom Provisioner (loom-provisioner) is also stateless, and communicates with Loom Server using REST endpoints. Hence Loom Provisioner can be easily run on multiple machines.

Loom Server
-----------
Loom Server (loom-server) can be run on multiple machines too. When run in this mode, there will be a load balancer fronting the Loom Servers. Loom UI and Loom Provisioners will be configured to communicate via a load balancer with the Loom Server. Also, all Loom Servers in a datacenter should connect to the same ZooKeeper quorum.

ZooKeeper
---------
A ZooKeeper quorum of at least 3 machines is required for redundancy. Note that ZooKeeper needs to run with an odd number of total machines for redundancy.

Database
--------
Database needs to be replicated with automatic failover in case the master Database goes down. Database is also fronted by a proxy that directs all operations to Master Database, and Loom Server connects to Database using the proxy.

