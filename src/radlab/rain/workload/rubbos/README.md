RUBBoS Workload
==============


## Overview

RUBBoS is a bulletin board benchmark modeled after an online news forum like [Slashdot](http://slashdot.org).

RUBBoS implements the essential bulletin board features of the Slashdot site.
In particular, as in Slashdot, it supports discussion threads.
A discussion thread is a logical tree, containing a story at its root and a number of comments for that story, which may be nested.
Users have two different levels of authorized access: regular user and moderator.
Regular users browse and submit stories and comments.
Moderators in addition review stories and rate comments.

RUBBoS is a free, open source initiative available at the [OW2 Consortium](http://jmob.ow2.org/rubbos.html) web site.


## Incarnations

The [OW2 RUBBoS](http://rubbos.ow2.org) web site offers several implementations of the RUBBoS benchmark, that use three different technologies: PHP, Java servlets and Enterprise Java Bean (EJB).

In PHP and Java servlets, the application programmer is responsible for writing the SQL queries.
An EJB server provides a number of services such as database access (JDBC), transactions (JTA), messaging (JMS), naming (JNDI) and management support (JMX). The EJB server manages one or more EJB containers. The container is responsible for providing component pooling and lifecycle management, client session management, database connection pooling, persistence, transaction management, authentication and access control.EJB containers automatically manage bean persistence, relieving the programmer of writing SQL code.


## Implementation in RAIN

The current implementation of the RUBBoS workload in RAIN is based on the version 1.2 of the [OW2 RUBBoS](http://jmob.ow2.org/rubbos.html) (which, at the time of writing, is the last available version).
Specifically, the implementation is currently based on the PHP and Java servlets versions of RUBBoS.

As a final remark, since the OW2 RUBBoS seems to be an abandoned project, I strongly suggest you to use the following RUBBoS version:

[RUBBoS](https://github.com/michaelmior/RUBBoS)

which is a patched and possibly enhanced version of OW2 RUBBoS version 1.2, or maybe the following RUBBoS version:

[RUBBoS](https://github.com/sguazt/RUBBoS)

which is a my fork to the *michealmior*'s RUBBoS version that may contain patches not yet merged with the former project.

### Type of Operations

All the 24 RUBBoS operations are supported, included the two special operations *Back to the Previous Page* and *End of Session*, specifically:
- **Stories of the Day**: the home page that shows the last stories.
- **Register**: register a new user.
- **Register User**: register a new user in the database.
- **Browse**: browse stories by different filters.
- **Browse Categories**: browse categories.
- **Browse Stories by Category**: browse stories by a selected category.
- **Older Stories**: browse stories back in the past.
- **View Story**: view the detail of a selected story.
- **Post Comment**: insert a comment for a selected story.
- **Store Comment**: store the comment for a selected story in the database.
- **View Comment**: view a comment for a selected story.
- **Moderate Comment**: moderate the comment for a selected story.
- **Store Moderator Log**: store the moderation to a comment for a selected storyi in the database.
- **Submit Story**: insert a new story.
- **Store Story**: store the new story in the database.
- **Search**: search in RUBBoS according to different filters.
- **Search in Stories**: search in stories for a given keyword.
- **Search in Comments**: search in comments for a given keyword.
- **Search in Users**: search in users for a given keyword.
- **Author Login**: sign in a previously registered user with *author* role.
- **Author Tasks**: an author selects which administrative tasks to perform.
- **Review Stories**: an author reviews a submitted story.
- **Accept Story**: an author accepts a submitted story.
- **Reject Story**: an author rejects a submitted story.
- **Back to the Previous Page**: back to the previous page.
- **End of Session**: end of current user session.

### Configuration Properties

The RUBBoS workload can be customized by means of a set of properties inside the *generatorParameters* key of the *profiles.config.rubbos.json*.
Currently, the supported configuration properties are the following:
- **rubbos.dictionaryFile**: a string representing the path to the RUBBoS dictionary file; this is the RAIN counterpart of the *database\_regions\_file* RUBBoS property.
  Default value is: *"resources/rubbos-ebay_full_categories.txt"*.
- **rubbos.incarnation**: a case-insensitive string representing the RUBBoS incarnation one wants to use.
  Possible values are:
  - *"php"*,
  - *"servlet"*.

  Default value is: *"servlet"*.
- **rubbos.initOp**: a case-insensitive string representing the RUBBoS operation from which a user session starts.
  Possible values are:
  - *"Home"*,
  - *"Register"*,
  - *"RegisterUser"*,
  - *"Browse"*,
  - *"BrowseCategories"*,
  - *"SearchItemsByCategory"*,
  - *"BrowseRegions"*,
  - *"BrowseCategoriesByRegions"*,
  - *"SearchItemsByRegion"*,
  - *"ViewItem"*,
  - *"ViewUserInfo"*,
  - *"ViewBidHistory"*,
  - *"BuyNowAuth"*,
  - *"BuyNow"*,
  - *"StoreBuyNow"*,
  - *"PutBidAuth"*,
  - *"PutBid"*,
  - *"StoreBid"*,
  - *"PutCommentAuth"*,
  - *"PutComment"*,
  - *"StoreComment"*,
  - *"Sell"*,
  - *"SelectCategoryToSellItem"*,
  - *"SellItemForm"*,
  - *"RegisterItem"*,
  - *"AboutMeAuth"*,
  - *"AboutMe"*.

  Default value is: *"Home"*.
- **rubbos.maxBidsPerItem**: a non-negative integer value representing the maximum number of bids per item; this is the RAIN counterpart of the *max\_bids\_per\_item* RUBBoS property.
  Default value is: *20*.
- **rubbos.maxCommentLen**: a non-negative integer value representing the maximum length of a comment to an item; this is the RAIN counterpart of the *comment\_max\_length* RUBBoS property.
  Default value is: *2048*.
- **rubbos.maxItemBaseBidPrice**: a non-negative real value representing the maximum base "bid" price for an item.
  Default value is: *10*.
- **rubbos.maxItemBaseBuyNowPrice**: a non-negative real value representing the maximum base "buy now" price for an item.
  Default value is: *1000*.
- **rubbos.maxItemBaseReservePrice**: a non-negative real value representing the maximum base reserve price for an item.
  Default value is: *1000*.
- **rubbos.maxItemDescrLen**: a string value representing the maximum length for the description of an item; this is the RAIN counterpart of the *item\_description\_length* RUBBoS property.
  Default value is: *8192*.
- **rubbos.maxItemDuration**: a non-negative integer value representing the maximum duration (in days) of an item.
  Default value is: *7*.
- **rubbos.maxItemInitPrice**: a non-negative real value representing the maximum initial price for an item.
  Default value is: *5000*.
- **rubbos.maxItemQuantity**: a non-negative integer value representing the maximum quantity for multiple items; this is the RAIN counterpart of the *max\_quantity\_for_multiple\_items* RUBBoS property.
  Default value is: *10*.
- **rubbos.maxWordLen**: a non-negative integer value representing the maximum length of a randomly generated word.
  Default value is: *12*.
- **rubbos.numItemsPerPage**: a non-negative integer value representing the maximum number of items to display in a single page; this is the RAIN counterpart of the *workload\_number\_of\_items\_per\_page* RUBBoS property.
  Default value is: *20*.
- **rubbos.numOldItems**: a non-negative integer value representing the number of items whose auction date is over; this is the RAIN counterpart of the *database\_number\_of\_old\_items* RUBBoS property.
  Default value is: *1000000*.
- **rubbos.numPreloadedUsers**: a non-negative integer value representing the number of user that have been already preloaded inside the RUBBoS database; this is the RAIN counterpart of the *database\_number\_of\_users* RUBBoS property.
  Default value is: *1*.
- **rubbos.percentItemsBuyNow**: a non-negative real value between 0 and 100 (inclusive) represnting the percentage of items that users can "buy now"; this is the RAIN couterpart of the *percentage\_of_items\_with\_reserve\_price* RUBBoS property.
  Default value is: *80*.
- **rubbos.percentItemsReserve**: a non-negative real value between 0 and 100 (inclusive) representing the percentage of items with a reserve price; this is the RAIN counterpart of the *percentage\_of\_buy\_now\_items* RUBBoS property.
  Default value is: *40*.
- **rubbos.percentUniqueItems**: a non-negative real value between 0 and 100 (inclusive) representing the percentage of unique items; this is the RAIN counterpart of the *percentage\_of\_unique\_items* RUBBoS property.
  Default value is: *10*.
- **rubbos.regionsFile**: a string representing the path to the RUBBoS regions file; this is the RAIN counterpart of the *database\_regions\_file* RUBBoS property.
  Default value is: *"resources/rubbos-ebay_regions.txt"*.
- **rubbos.rngSeed**: an integer number representing the seed used to initialize the random number generator used by the RUBBoS generator; if set to `-1`, the random number generator will be initialized with the Java's default (i.e., to a value very likely to be distinct from any other invocation of the `java.util.Random` default constructor).
  Default value is: *-1*.
- **rubbos.serverHtmlPath**: the URL path pointing to the base location where HTML files on the RUBBoS server.
  Default value is: */*.
- **rubbos.serverScriptPath**: the URL path pointing to the base location where script files on the RUBBoS server.
  Default value is: */*.


The order of the operations in the traffic mix matrix is the same of the one specified in the previous section (i.e., 0 is the *Home Page* operation, 1 is the *Register* operation, and so on).

### Assumptions

The current implementation assumes that:
- One and only one instance of RAIN RUBBoS workload is running. This assumption is needed to assign _unique_ identifiers to both users and items.
- There is at least one preloaded user inside the RUBBoS database.

### Known Issues

- In the native client, it is possible to dynamically switch from a user workload profile to an author workload profile and back.
  This is not currently possible in the RAIN version of RUBBoS.
  Instead, you have to decide which workload profiles to use before you run the RAIN experiment by properly configuring the the *behavior* section of the configuration file.
