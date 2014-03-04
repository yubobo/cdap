==================================
Multi-Datacenter High Availability
==================================

Loom can be setup for high availability across multiple data centers so that when one data center goes down, other data centers can take over the traffic of the failed data center. Except for the Database, all other Loom components deal with only local data. Hence high availability configuration for multi-datacenter mostly depends on how the DB is setup.



.. toctree::
   :maxdepth: 2

   option1/index
   option4/index
