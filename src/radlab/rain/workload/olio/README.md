Olio Workload
====================


## Overview

The [Olio](https://incubator.apache.org/olio/) is a is a web2.0 toolkit to help evaluate the suitability, functionality and performance of web technologies.
Olio defines an example web2.0 application ( an events site somewhat like yahoo.com/upcoming).
The toolkit also defines ways to drive load against the application in order to measure performance.

The Olio benchmark can be used for several purposes.
For instance:
- Understand how to use various web2.0 technologies such as AJAX, memcached, mogileFS etc. in the creation of your own application.
  Use the code in the application to understand the subtle complexities involved and how to get around issues with these technologies.
- Evaluate the differences in the three implementations: php, ruby and java to understand which might best work for your situation.
- Within each implementation, evaluate different infrastructure technologies by changing the servers used (e.g: apache vs lighttpd, mysql vs postgre, ruby vs Jruby etc.)
- Drive load against the application to evaluate the performance and scalability of the chosen platform.
- Experiment with different algorithms (e.g. memcache locking, a different DB access API) by replacing portions of code in the application.

## Incarnations

The [Apache Olio](https://incubator.apache.org/olio/) web site offers three implementations of the Olio benchmark, that use three different technologies: PHP, Java servlets and Ruby-on-Rails.

## Implementation in RAIN

The current implementation of the Olio workload in RAIN is based on the version 0.2 of the [Apache Olio](https://incubator.apache.org/olio/) (which, at the time of writing, is the last available version) and support all three incarnations.
You can control what incarnation to use through the RAIN's profile configuration file.

### Type of Operations

All the 29 RUBiS operations are supported, included the two special operations *Back to the Previous Page* and *End of Session*, specifically:
- **Home Page**: the home page.
- **Register**: register a new user.
- **Register User**: register a new user in the database.
- **Browse**: browse categories or regions.
- **Browse Categories**: browse categories.
- **Search Items by Category**: browse items by a selected category.
- **Browse Regions**: browse regions
- **Browse Categories by Region**: browse categories in a regions
- **Search Items by Region**: browse items in a specific region for a given category.
- **View Item**: view a selected item.
- **View User Info**: view user information.
- **View Bid History**: view item bid history.
- **Buy Now Auth**: buy now authentication.
- **Buy Now**: buy now confirmation page.
- **Store Buy Now**: store buy now in the database.
- **Put Bid Auth**: bid authentication.
- **Put Bid**: bid confirmation page.
- **Store Bid**: store bid in the database.
- **Put Comment Auth**: comment authentication page.
- **Put Comment**: comment confirmation page.
- **Store Comment**: store comment in the database.
- **Sell**: sell page.
- **Select Category to Sell Item**: select a category to sell item.
- **Sell Item Form**: sell item confirmation page.
- **Register Item**: store item in the database.
- **About Me Auth**: about me authentication.
- **About Me**: about me information page..
- **Back to the Previous Page**: back to the previous page.
- **End of Session**: end of current user session.

### Configuration Properties

The RUBiS workload can be customized by means of a set of properties inside the *generatorParameters* key of the *profiles.config.rubis.json*.
Currently, the supported configuration properties are the following:
- **rubis.categoriesFile**: a string representing the path to the RUBiS categories file; this is the RAIN counterpart of the *database\_regions\_file* RUBiS property.
- **rubis.maxBidsPerItem**: a non-negative integer value representing the maximum number of bids per item; this is the RAIN counterpart of the *max\_bids\_per\_item* RUBiS property.
- **rubis.maxCommentLen**: a non-negative integer value representing the maximum length of a comment to an item; this is the RAIN counterpart of the *comment\_max\_length* RUBiS property.
- **rubis.maxItemBaseBidPrice**: a non-negative real value representing the maximum base "bid" price for an item.
- **rubis.maxItemBaseBuyNowPrice**: a non-negative real value representing the maximum base "buy now" price for an item.
- **rubis.maxItemBaseReservePrice**: a non-negative real value representing the maximum base reserve price for an item.
- **rubis.maxItemDescrLen**: a string value representing the maximum length for the description of an item; this is the RAIN counterpart of the *item\_description\_length* RUBiS property.
- **rubis.maxItemDuration**: a non-negative interger value representing the maximum duration (in days) of an item.
- **rubis.maxItemInitPrice**: a non-negative real value representing the maximum initial price for an item.
- **rubis.maxItemQuantity**: a non-negative integer value representing the maximum quantity for multiple items; this is the RAIN counterpart of the *max\_quantity\_for_multiple\_items* RUBiS property.
- **rubis.maxWordLen**: a non-negative integer value representing the maximum length of a randomly generated word.
- **rubis.numItemsPerPage**: a non-negative integer value representing the maximum number of items to display in a single page; this is the RAIN counterpart of the *workload\_number\_of\_items\_per\_page* RUBiS property.
- **rubis.numOldItems**: a non-negative integer value representing the number of items whose auction date is over; this is the RAIN counterpart of the *database\_number\_of\_old\_items* RUBiS property.
- **rubis.numPreloadedUsers**: a non-negative integer value representing the number of user that have been already preloaded inside the RUBiS database; this is the RAIN counterpart of the *database\_number\_of\_users* RUBiS property.
- **rubis.percentItemsBuyNow**: a non-negative real value between 0 and 100 (inclusive) represnting the percentage of items that users can "buy now"; this is the RAIN couterpart of the *percentage\_of_items\_with\_reserve\_price* RUBiS property.
- **rubis.percentItemsReserve**: a non-negative real value between 0 and 100 (inclusive) representing the percentage of items with a reserve price; this is the RAIN counterpart of the *percentage\_of\_buy\_now\_items* RUBiS property.
- **rubis.percentUniqueItems**: a non-negative real value between 0 and 100 (inclusive) representing the percentage of unique items; this is the RAIN counterpart of the *percentage\_of\_unique\_items* RUBiS property.
- **rubis.regionsFile**: a string representing the path to the RUBiS regions file; this is the RAIN counterpart of the *database\_regions\_file* RUBiS property.
- **rubis.rngSeed**: an integer number representing the seed used to initialize the random number generator used by the RUBiS generator; if set to `-1`, the random number generator will be initialized with the Java's default (i.e., to a value very likely to be distinct from any other invocation of the `java.util.Random` default constructor).
The order of the operations in the traffic mix matrix is the same of the one specified in the previous section (i.e., 0 is the *Home Page* operation, 1 is the *Register* operation, and so on).

### Assumptions

The current implementation assumes that:
- One and only one instance of RAIN RUBiS workload is running. This assumption is needed to assign _unique_ identifiers to both users and items.
- There is at least one preloaded user inside the RUBiS database.
