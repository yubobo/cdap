==================================
Multi-Datacenter High Availability
==================================

When running across multiple data centers, Loom can be configured to be resilient to data center failures. This document describes the recommended configuration 
for setting up Loom for HA across multiple data centers. Together with :doc:`Datacenter High Availability <data-center-bcp>`, this setup provides for a comprehensive plan for Loom HA.

In this setup Loom runs in active mode in all data centers (Hot-Hot). In case of a data center failure, traffic from the failed data center will be automatically routed to other data centers by the load balancer. This ensures that service is not affected on a data center failure.

A couple of things need to be considered when configuring Loom to run across multiple data centers for HA-

* As discussed in the previous section all components of Loom, except for Database, deal with local data or are stateless. The most important part of the HA setup is to share the data across data centers in a consistent manner. HA configuration setup for multi-datacenter mostly depends on how the DB is setup as discussed in the next sections.
* Since Loom Servers across all data centers run in Hot-Hot mode, we have to make sure that they do not conflict while creating cluster IDs. The ID space needs to be partitioned amongst the Loom Servers. This can be done using ``loom.ids.start.num`` and ``loom.ids.increment.by`` server config parameters. For more information on the config parameters see :doc:`Server Configuration </guide/admin/server-config>` section. Also note that Loom Servers in a data center can share the same ID space.



Recommended multi-datacener HA configurations for Loom-

.. toctree::
   :maxdepth: 2

   option1/index
   option4/index
