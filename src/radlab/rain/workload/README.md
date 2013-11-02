Olio Workload
==============


## Overview

Olio is a Web 2.0 toolkit to help evaluate the suitability, functionality and performance of web technologies.
Olio defines an example Web 2.0 application (a social event site somewhat like to the already died [Yahoo! Upcoming](yahoo.com/upcoming) service - see the [ArchiveTeam](http://archiveteam.org/index.php?title=Yahoo!_Upcoming) to get an idea about it) and provides three initial implementations: PHP, Java Enterprise Edition, and Ruby-On-Rails.
The toolkit also defines ways to drive load against the application in order to measure performance.

Olio can be used for several purpose, for instance:
- To understand how to use various Web 2.0 technologies such as AJAX, memcached, mogileFS etc. in the creation of a Web 2.0 application (e.g., to understand the subtle complexities involved and how to get around issues with these technologies).
- To evaluate the differences in the different implementations and to understand which might best work for a specific situation.
- Within each implementation, to evaluate different infrastructure technologies by changing the servers used (e.g: Apache vs Lighttpd, or MySQL vs Postgres, or Ruby vs JRuby)
- To drive load against the application to evaluate the performance and scalability of the chosen platform.
- To experiment with different algorithms (e.g. memcache locking, a different DB access API) by replacing portions of code in the application.

Olio is a free, open source initiative available at the [Apache Olio Incubator](https://incubator.apache.org/olio/) web site.


## Incarnations

The [Apache Olio](https://incubator.apache.org/olio/) web site offers several implementations of the Olio benchmark, that use three different technologies: PHP, Java Enterprise Edition, and Ruby-On-Rails.


## Implementation in RAIN

The current implementation of the Olio workload in RAIN is based on the version 0.2 of the [Apache Olio](https://incubator.apache.org/olio/) (which, at the time of writing, is the last available version).
Specifically, the current implementation has been tested against the PHP and Java Enterprise Edition incarnations (but it should also work with the Ruby-On-Rails incarnation).

As a final remark, since the Apache Olio seems to be an abandoned project, I strongly suggest you to use the following Olio version:

[dcsj-rubis](https://github.com/sguazt/olio)

which is a my patched and possibly enhanced version of Apache Olio version 0.2.

### Type of Operations

All the 7 Olio operations are supported, specifically:
- **Home Page**: the home page.
- **Login**: sign-in a previously registered user.
- **Tag Search**: search events by tag.
- **Event Detail**: show details about an event.
- **Person Detail**: show details about a registered user.
- **Add Person**: register a new user.
- **Add Event**: add a new event.

### Configuration Properties

The Olio workload can be customized by means of a set of properties inside the *generatorParameters* key of the *profiles.config.olio.json*.
Currently, the supported configuration properties are the following:
- **olio.incarnation**: a case-insensitive string representing the Olio incarnation one wants to use. Possible values are:
 - "php",
 - "rails",
 - "java".

Default value is: "rails".
- **olio.numPreloadedEvents**: a non-negative integer value representing the number of social events that have been already preloaded inside the Olio database. Default value is "0".
- **olio.numPreloadedPersons**: a non-negative integer value representing the number of user that have been already preloaded inside the Olio database. Default value is "0".
- **olio.numPreloadedTags**: a non-negative integer value representing the number of social event tags that have been already preloaded inside the Olio database. Default value is "0".
- **olio.rngSeed**: an integer number representing the seed used to initialize the random number generator used by the Olio generator; if set to `-1`, the random number generator will be initialized with the Java's default (i.e., to a value very likely to be distinct from any other invocation of the `java.util.Random` default constructor). Default value is "-1".

The order of the operations in the traffic mix matrix is the same of the one specified in the previous section (i.e., 0 is the *Home Page* operation, 1 is the *Login* operation, and so on).

### Assumptions

The current implementation assumes that:
- One and only one instance of RAIN Olio workload is running. This assumption is needed to assign _unique_ identifiers to both users and items.
