RUBiS Workload
==============


## Overview

RUBiS is an auction site prototype modeled after [eBay.com](http://www.ebay.com) that is used to evaluate application design patterns and application servers performance scalability.

The benchmark implements the core functionality of an auction site: selling, browsing and bidding. No other complementary services like instant messaging or newsgroups are currently implemented. In RUBiS there are three kinds of user sessions: visitor, buyer, and seller. For a visitor session, users need not register but are only allowed to browse. Buyer and seller sessions require registration. In addition to the functionality provided during visitor sessions, during a buyer session users can bid on items and consult a summary of their current bids, rating and comments left by other users. Seller sessions require a fee before a user is allowed to put up an item for sale. An auction starts immediately and lasts typically for no more than a week. The seller can specify a reserve (minimum) price for an item.

RUBiS is a free, open source initiative available at the [OW2 Consortium](http://rubis.ow2.org/) web site.


## Incarnations

The [OW2 RUBiS](http://rubis.ow2.org) web site offers several implementations of the RUBiS benchmark, that use three different technologies: PHP, Java servlets and Enterprise Java Bean (EJB).

In PHP and Java servlets, the application programmer is responsible for writing the SQL queries.
An EJB server provides a number of services such as database access (JDBC), transactions (JTA), messaging (JMS), naming (JNDI) and management support (JMX). The EJB server manages one or more EJB containers. The container is responsible for providing component pooling and lifecycle management, client session management, database connection pooling, persistence, transaction management, authentication and access control.EJB containers automatically manage bean persistence, relieving the programmer of writing SQL code.


## Implementation in RAIN

The current implementation of the RUBiS workload in RAIN is based on the version 1.4.3 of the [OW2 RUBiS](http://http://rubis.ow2.org/) (which, at the time of writing, is the last available version).
Specifically, the implementation is currently based on the Java servlets version of RUBiS.

### Configuration Properties

The RUBiS workload can be customized by means of a set of properties inside the *generatorParameters* key of the *profiles.config.rubis.json*.
Currently, the supported configuration properties are the following:
- *rubis.categoriesFile*: a string representing the path to the RUBiS categories file; this is the RAIN counterpart of the *database\_regions\_file* RUBiS property.
- *rubis.maxBidsPerItem*: a non-negative integer value representing the maximum number of bids per item; this is the RAIN counterpart of the *max\_bids\_per\_item* RUBiS property.
- *rubis.maxCommentLen*: a non-negative integer value representing the maximum length of a comment to an item; this is the RAIN counterpart of the *comment\_max\_length* RUBiS property.
- *rubis.maxItemBaseBuyNowPrice*: a non-negative real value representing the maximum base "buy now" price for an item.
- *rubis.maxItemBaseReservePrice*: a non-negative real value representing the maximum base reserve price for an item.
- *rubis.maxItemDescrLen*: a string value representing the maximum length for the description of an item; this is the RAIN counterpart of the *item\_description\_length* RUBiS property.
- *rubis.maxItemDuration*: a non-negative interger value representing the maximum duration (in days) of an item.
- *rubis.maxItemInitPrice*: a non-negative real value representing the maximum initial price for an item.
- *rubis.maxItemQuantity*: a non-negative integer value representing the maximum quantity for multiple items; this is the RAIN counterpart of the *max\_quantity\_for_multiple\_items* RUBiS property.
- *rubis.maxWordLen*: a non-negative integer value representing the maximum length of a randomly generated word.
- *rubis.numItemsPerPage*: a non-negative integer value representing the maximum number of items to display in a single page; this is the RAIN counterpart of the *workload\_number\_of\_items\_per\_page* RUBiS property.
- *rubis.numPreloadedUsers*: a non-negative integer value representing the number of user that have been already preloaded inside the RUBiS database; this is the RAIN counterpart of the *database\_number\_of\_users* RUBiS property.
- *rubis.percentItemsBuyNow*: a non-negative real value between 0 and 100 (inclusive) represnting the percentage of items that users can "buy now"; this is the RAIN couterpart of the *percentage\_of_items\_with\_reserve\_price* RUBiS property.
- *rubis.percentItemsReserve*: a non-negative real value between 0 and 100 (inclusive) representing the percentage of items with a reserve price; this is the RAIN counterpart of the *percentage\_of\_buy\_now\_items* RUBiS property.
- *rubis.percentUniqueItems*: a non-negative real value between 0 and 100 (inclusive) representing the percentage of unique items; this is the RAIN counterpart of the *percentage\_of\_unique\_items* RUBiS property.
- *rubis.regionsFile*: a string representing the path to the RUBiS regions file; this is the RAIN counterpart of the *database\_regions\_file* RUBiS property.
- *rubis.rngSeed*: an integer number representing the seed used to initialize the random number generator used by the RUBiS generator; if set to `-1`, the random number generator will be initialized with the Java's default (i.e., to a value very likely to be distinct from any other invocation of the `java.util.Random` default constructor).

### Assumptions

The current implementation assumes that:
- One and only one instance of RAIN RUBiS workload is running. This assumption is needed to assign _unique_ identifiers to both users and items.
- There is at least one preloaded user inside the RUBiS database.
