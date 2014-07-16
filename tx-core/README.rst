==================
Continuuity Tephra
==================


Continuuity Tephra provides globally consistent transactions on top of Apache HBase.  While HBase
provides strong consistency and row-level or region-level ACID operations, it sacrifices
cross-region and cross-table consistency in favor of scalability.  This trade-off requires
application developers to handle the complexity of ensuring consistency when their modifications
span region boundaries.  By providing support for global transactions that span regions, tables, or
multiple RPCs, Tephra simplifies application development on top of HBase, without a significant
impact on performance or scalability for many workloads.

How It Works
------------

Tephra leverages HBase's native data versioning to provide multi-versioned concurrency control for
transactional reads and writes.  A central transaction manager generates a globally unique,
time-based transaction ID for each transaction that is started, and maintains the state of all
in-progress and recently committed transactions for conflict detection.

Getting Started
---------------

You can get started with Tephra by building directly from the latest source code::

  git clone https://github.com/continuuity/tephra.git
  cd tephra
  mvn clean install assembly:single

After the build completes, add the following dependencies to any Apache Maven projects, in
order to make use of Tephra classes::

  <dependency>
    <groupId>com.continuuity</groupId>
    <artifactId>tephra-api</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </dependency>
  <dependency>
    <groupId>com.continuuity</groupId>
    <artifactId>tephra-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </dependency>

Since the HBase APIs have changed between version 0.94.x and 0.96.x, you will need to select the
appropriate HBase compatibility library.

For HBase 0.94.x::

  <dependency>
    <groupId>com.continuuity</groupId>
    <artifactId>tephra-hbase-compat-0.94</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </dependency>

For HBase 0.96.x and 0.98.x::

  <dependency>
    <groupId>com.continuuity</groupId>
    <artifactId>tephra-hbase-compat-0.96</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </dependency>


Deployment & Configuration
--------------------------

Tephra makes use of a central transaction server to assign unique transaction IDs for data
modifications and to perform conflict detection.  Only a single transaction server can actively
handle client requests at a time, however, additional transaction server instances can be run
simultaneously, providing automatic failover if the active server becomes unreachable.

Transaction Server Configuration
................................

The transaction server supports the following configuration properties:

+---------------------------+------------+---------------------------------------------------------+
| Name                      | Default    | Description                                             |
+===========================+============+=========================================================+
| data.tx.bind.port         | 15165      | Port to bind to                                         |
+---------------------------+------------+---------------------------------------------------------+
| data.tx.bind.address      | 0.0.0.0    | Server address to listen on                             |
+---------------------------+------------+---------------------------------------------------------+
| data.tx.server.io.threads | 2          | Num. of threads for socket IO                           |
+---------------------------+------------+---------------------------------------------------------+
| data.tx.server.threads    | 20         | Num. of handler threads                                 |
+---------------------------+------------+---------------------------------------------------------+
| data.tx.timeout           | 30         | Timeout for a transaction to complete (seconds)         |
+---------------------------+------------+---------------------------------------------------------+
| data.tx.cleanup.interval  | 10         | Frequency to check for timed out transactions (seconds) |  
+---------------------------+------------+---------------------------------------------------------+
| data.tx.snapshot.dir      |            | HDFS directory used to store snapshots of tx state      |
+---------------------------+------------+---------------------------------------------------------+
| data.tx.snapshot.interval | 300        | Frequency to write new snapshots                        |
+---------------------------+------------+---------------------------------------------------------+
| data.tx.snapshot.retain   | 10         | Num. of old transaction snapshots to retain             |
+---------------------------+------------+---------------------------------------------------------+


Client Configuration
....................

The transaction service client supports the following configuration properties:

+--------------------------------------+-----------+-----------------------------------------------+
| Name                                 | Default   | Description                                   |
+======================================+===========+===============================================+
| data.tx.client.timeout               | 30000     | Client socket timeout (milliseconds)          |
+--------------------------------------+-----------+-----------------------------------------------+
| data.tx.client.provider              | pool      | Client provider strategy ("pool" uses a pool  |
|                                      |           | of clients, "thread-local" a client per       |
|                                      |           | thread).                                      |
+--------------------------------------+-----------+-----------------------------------------------+
| data.tx.client.count                 | 5         | Max num. of clients for "pool" provider       |
+--------------------------------------+-----------+-----------------------------------------------+
| data.tx.client.retry.strategy        | backoff   | Client retry strategy ("backoff" for backoff  |
|                                      |           | between attempts, "n-times" for fixed num. of |
|                                      |           | tries).                                       |
+--------------------------------------+-----------+-----------------------------------------------+
| data.tx.client.retry.attempts        | 2         | Num. of times to retry ("n-times" strategy)   |
+--------------------------------------+-----------+-----------------------------------------------+
| data.tx.client.retry.backoff.initial | 100       | Initial sleep time ("backoff" strategy)       |
+--------------------------------------+-----------+-----------------------------------------------+
| data.tx.client.retry.backoff.factor  | 4         | Multiplication factor for sleep time          |
+--------------------------------------+-----------+-----------------------------------------------+
| data.tx.client.retry.backoff.limit   | 30000     | Exit when sleep time reaches this limit       |
+--------------------------------------+-----------+-----------------------------------------------+


HBase Coprocessors
..................

In addition to the transaction server, Tephra requires an HBase coprocessor to be installed on all
tables where transactional reads and writes will be performed.  Configure the coprocessor by
adding the following to ``hbase-site.xml``.

For HBase 0.94::
  <property>
    <name>hbase.coprocessor.region.classes</name>
    <value>com.continuuity.data2.transaction.coprocessor.hbase94.TransactionDataJanitor</value>
  </property>

For HBase 0.96 and 0.98::
  <property>
    <name>hbase.coprocessor.region.classes</name>
    <value>com.continuuity.data2.transaction.coprocessor.hbase96.TransactionDataJanitor</value>
  </property>


Deployment
----------

Transaction Server Requirements
...............................


Client Requirements
...................



Running

Getting Involved

Known Issues / Limitations
- Deletes are only supported as writes of empty values


How to Contribute
-----------------

Interested in helping to improve Tephra? We welcome all contributions, whether in filing detailed
bug reports, submitting pull requests for code changes or improvements, or asking questions and
assisting others on the mailing list.

Bug Reports & Feature Requests
..............................

Bugs and tasks and tracked in a public JIRA issue tracker.  Details on access will be forthcoming.

Pull Requests
.............
We have a simple pull-based development model with a consensus-building phase, similar to Apache's
voting process. If you’d like to help make Tephra better by adding new features, enhancing existing
features, or fixing bugs, here's how to do it:

#. If you are planning a large change or contribution, discuss your plans on the ``tephra-dev``
   mailing list first.  This will help us understand your needs and best guide your solution in a
   way that best fits the project.
#. Fork Tephra into your own GitHub repository
#. Create a topic branch with an appropriate name
#. Work on the code to your heart's content
#. Once you’re satisfied, create a pull request from your GitHub repo (it’s helpful if you fill in
   all of the description fields)
#. After we review and accept your request we’ll commit your code to the continuuity/tephra
   repository

Thanks for helping to improve Tephra!

Mailing List
............

Tephra User Group and Development Discussions: tephra-dev@googlegroups.com


License
-------

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License
is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
or implied. See the License for the specific language governing permissions and limitations under
the License.
