Redis Workload
==============


## Overview

Redis is an in-memory data structure store, used as database, cache and message broker.
It supports data structures such as strings, hashes, lists, sets, sorted sets with range queries, bitmaps, hyperloglogs and geospatial indexes with radius queries.
Redis has built-in replication, Lua scripting, LRU eviction, transactions and different levels of on-disk persistence, and provides high availability via Redis Sentinel and automatic partitioning with Redis Cluster.

The benchmark implements the core functionality of a data store: setting, getting, and deleting keys.

Redis is an open source (BDS licensed) software available at the [Redis](http://redis.io/) web site.


## Implementation in RAIN

The current implementation of the Redis workload in RAIN only supports a limited set of Redis operations and data structures.
For instance, lists and hashes are not supported yet.

### Type of Operations

Currently, this implementation supports the following Redis operations:
- **DEL**: delete a key.
- **GET**: retrieve the value of a key.
- **SET**: set the value of a key.

### Configuration Properties

The Redis workload can be customized by means of a set of properties.
Of particular interest are properties for the *generatorParameters* and *loadProfile* keys of the *profiles.config.rubis.json*.

For the *generatorParameters* key, the supported configuration properties are the following:
- **usePooling**: a boolean value (i.e., either as *"true"* or *"false"* string) indicating whether object pooling must be enabled or not.
  Default value is: *"true"*.
- **rngSeed**: an integer number representing the seed used to initialize the random number generator used by the Redis generator; if set to `-1`, the random number generator will be initialized with the Java's default (i.e., to a value very likely to be distinct from any other invocation of the `java.util.Random` default constructor).
  Default value is: *-1*.
- **debug**: a boolean value (i.e., either as *"true"* or *"false"* string) indicating whether debugging messages must be displayed or not.
  Default value is: *"false"*.

For the *loadProfile* key, there can be one or more *load profile* section (i.e., a piece of JSON code delimited by a pair of braces), each of which represents a specific workload to run against the Redis application.
The configuration properties supported by each load profile section are the ones defined by the load profile class (see property *loadProfileClass*), which in this case are inherited from both the `radlab.rain.util.storage.StorageLoadProfile` and `radlab.rain.LoadProfile` classes.
Those of particular interest are the following:
- **deletePct**: a non-negative real number representing the percentage of DEL operations.
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
- **readPct**: a non-negative real number representing the percentage of GET operations.
  Default value is: *0*.
- **size**: a non-negative integer number representing the size (in bytes) of each object.
  Default value is: *0*.
- **users**: a non-negative integer number representing the number of users to generate in this profile.
  Default value is: *0*.
- **writePct**: a non-negative real number representing the percentage of SET operations.
  Default value is: *0*.


### Compilation and Execution

#### Compilation

To compile the Redis workload, simply enter the following command:

    $ ant package-redis

#### Initialization

Initialization is not usually necessary unless your workload is primarily made of GET or DEL operations.
In such cases, you may want to preload some keys in the Redis data store.
To this end, you can either load them by means of either a [Redis client](http://redis.io/clients) or the `radlab.rain.workload.redis.RedisUtil` class:

    $ java -Xmx1g -Xms256m -cp rain.jar:workloads/redis.jar radlab.rain.workload.redis.RedisUtil <redis host> <redis port> <min key> <max key> <size>


#### Workload Driver Execution

To run the Redis workload, simply enter the following command:

    $ java -Xmx1g -Xms256m -cp rain.jar:workloads/redis.jar radlab.rain.Benchmark config/rain.config.redis.json

### Assumptions

None.
