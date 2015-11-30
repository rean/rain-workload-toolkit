Cassandra Workload
==================


## Overview

Apache Cassandra is a massively scalable open source NoSQL database.
Cassandra is perfect for managing large amounts of structured, semi-structured, and unstructured data across multiple data centers and the cloud.
Cassandra delivers continuous availability, linear scalability, and operational simplicity across many commodity servers with no single point of failure, along with a powerful dynamic data model designed for maximum flexibility and fast response times.

The benchmark implements the core functionality of a data store: reading, writing,, deleting, and scanning keys.

Apache Cassandra is an open source (Apache License 2.0 licensed) software available at the [Apache Cassandra](http://cassandra.apache.org/) web site.


## Implementation in RAIN

The current implementation of the Cassandra workload in RAIN only supports a limited set of Cassandra operations and data structures.
In particular, this implementation is based on the Cassandra [Thrift](http://thrift.apache.org/) API which is the legacy API used by older version of Cassandra.
The Thrift support will probably be removed since version 3.0.
For this reason, this workload can only be used for versions of Cassandra less than 3.0.

### Type of Operations

Currently, this implementation supports the following Cassandra operations:
- **DELETE**: delete a key.
- **READ**: retrieve the value of a key.
- **WRITE**: set the value of a key (cab be either an insertion or an update).
- **SCAN**: retrieve the values of a slice of keys (via multiget_slice call).

### Configuration Properties

The Cassandra workload can be customized by means of a set of properties.
Of particular interest are properties for the *generatorParameters* and *loadProfile* keys of the *profiles.config.rubis.json*.

For the *generatorParameters* key, the supported configuration properties are the following:
- **clusterName**: the name of the Cassandra cluster.
  Default value is: *rainclstr*.
- **columnFamilyName**: the name of the Cassandra column family.
  Default value is: *raincf*.
- **debug**: a boolean value (i.e., either as *"true"* or *"false"* string) indicating whether debugging messages must be displayed or not.
  Default value is: *"false"*.
- **keyspaceName**: the name of the Cassandra keyspace.
  Default value is: *rainks*.
- **rngSeed**: an integer number representing the seed used to initialize the random number generator used by the Cassandra generator; if set to `-1`, the random number generator will be initialized with the Java's default (i.e., to a value very likely to be distinct from any other invocation of the `java.util.Random` default constructor).
  Default value is: *-1*.
- **usePooling**: a boolean value (i.e., either as *"true"* or *"false"* string) indicating whether object pooling must be enabled or not.
  Default value is: *"true"*.

For the *loadProfile* key, there can be one or more *load profile* section (i.e., a piece of JSON code delimited by a pair of braces), each of which represents a specific workload to run against the Cassandra application.
The configuration properties supported by each load profile section are the ones defined by the load profile class (see property *loadProfileClass*), which in this case are inherited from both the `radlab.rain.util.storage.StorageLoadProfile` and `radlab.rain.LoadProfile` classes.
Those of particular interest are the following:
- **deletePct**: a non-negative real number representing the percentage of DELETE operations.
  Default value is: *0*.
- **hotSet**: an array of non-negative integer numbers each of which representing the key of a hotspot object. This property takes precedence over the *numHotObjects* property.
  Default value is: *[]*.
- **hotTrafficFraction**: a non-negative real number representing the fraction of traffic to be destined to hotspots.
  Default value is: *0*.
- **interval**: a non-negative integer number representing the duration (in seconds) of this profile.
  Default value is: *0*.
- **keyGenerator**: a string representing the key generator class used in this profile (e.g., `radlab.rain.util.storage.UniformKeyGenerator`).
  Default value is: *""*.
- **keyGeneratorConfig**: an associative array containing properties for key generation.
  The supported properties are at least those defined by the `radlab.rain.util.storage.KeyGenerator` and possibly other ones provided by the key generator class (see property *keyGenerator*):
  - **maxKey**: a non-negative integer number representing the minimum value a key can take.
    Default value is: *0*.
  - **minKey**: a non-negative integer number representing the maximum value a key can take.
    Default value is: *0*.
  - **rngSeed**: an integer number representing the seed used to initialize the random number generator used by the key generator; if set to `-1`, the random number generator will be initialized with the Java's default (i.e., to a value very likely to be distinct from any other invocation of the `java.util.Random` default constructor).
    Default value is: *1*.
- **numHotObjects**: a non-negative integer number representing the number of hotspots to pick.
  Default value is: *0*.
- **readPct**: a non-negative real number representing the percentage of READ operations.
  Default value is: *0*.
- **scanPct**: a non-negative real number representing the percentage of SCAN operations.
  Default value is: *0*.
- **size**: a non-negative integer number representing the size (in bytes) of each object.
  Default value is: *0*.
- **users**: a non-negative integer number representing the number of users to generate in this profile.
  Default value is: *0*.
- **writePct**: a non-negative real number representing the percentage of WRITE operations.
  Default value is: *0*.


### Compilation and Execution

#### Compilation

To compile the Cassandra workload, simply enter the following command:

    $ ant package-cassandra

#### Initialization

Initialization is not usually necessary unless your workload is primarily made of READ or DELETE operations.
In such cases, you may want to preload some keys in the Cassandra data store.
To this end, you can either load them by means of either the [Cassandra CQLSH client](https://wiki.apache.org/cassandra/GettingStarted) or the `radlab.rain.workload.cassandra.CassandraUtil` class:

    $ java -Xmx1g -Xms256m -cp rain.jar:workloads/cassandra.jar radlab.rain.workload.cassandra.CassandraUtil <cassandra host> <cassandra port> <cluster name> <keyspace> <column family> <min key> <max key> <size>


#### Workload Driver Execution

To run the Cassandra workload, simply enter the following command:

    $ java -Xmx1g -Xms256m -cp rain.jar:workloads/cassandra.jar radlab.rain.Benchmark config/rain.config.cassandra.json

### Assumptions

None.

It is not necessary to explicitly create the cluster, keyspace and column family (specified in the configuration file) in the Cassandra data store.
Indeed, if one of them don't exist, this implementation will create it for you.
