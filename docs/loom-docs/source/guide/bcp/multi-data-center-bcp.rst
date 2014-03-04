==================================
Multi-Datacenter High Availability
==================================

When running across multiple data centers, Loom can be configured to be resilient to data center failures. This document describes the recommended configuration 
for setting up Loom for HA across mulitple data centers. Together with :doc:`HA inside a data center <data-center-bcp>`, this setup provides for a comprehensive plan for Loom HA.

In this setup Loom runs in active mode in all data centers (Hot-Hot). In case of a data center failure, traffic from the failed data center will be automatically routed to other data centers by the load balancer. This ensures that service is not affected on a data center failure.

As we discussed in the previous section all components of Loom, except for Database, deal with local data or are stateless. Hence HA configuration setup for multi-datacenter mostly depends on how the DB is setup as discussed in the next sections.


.. toctree::
   :maxdepth: 2

   option1/index
   option4/index
