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

The current implementation of the RUBiS workload in RAIN is based on the version 1.4.3 of the [OW2 RUBiS](http://rubis.ow2.org/) (which, at the time of writing, is the last available version).
Specifically, the implementation is currently based on the PHP and Java servlets versions of RUBiS (but it shouldn't be too difficult to adapt it to work with other RUBiS incarnations).

As a final remark, since the OW2 RUBiS seems to be an abandoned project, I strongly suggest you to use the following RUBiS version:

[dcsj-rubis](https://github.com/sguazt/dcsj-rubis)

which is a my patched and possibly enhanced version of OW2 RUBiS version 1.4.3.

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
- **rubis.categoriesFile**: a string representing the path to the RUBiS categories file; this is the RAIN counterpart of the *database\_categories\_file* RUBiS property.
  Default value is: *"resources/rubis-ebay_full_categories.txt"*.
- **rubis.incarnation**: a case-insensitive string representing the RUBiS incarnation one wants to use.
  Possible values are:
  - *"php"*,
  - *"servlet"*.

  Default value is: *"servlet"*.
- **rubis.initOp**: a case-insensitive string representing the RUBiS operation from which a user session starts.
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
- **rubis.maxBidsPerItem**: a non-negative integer value representing the maximum number of bids per item; this is the RAIN counterpart of the *max\_bids\_per\_item* RUBiS property.
  Default value is: *20*.
- **rubis.maxCommentLen**: a non-negative integer value representing the maximum length of a comment to an item; this is the RAIN counterpart of the *comment\_max\_length* RUBiS property.
  Default value is: *2048*.
- **rubis.maxItemBaseBidPrice**: a non-negative real value representing the maximum base "bid" price for an item.
  Default value is: *10*.
- **rubis.maxItemBaseBuyNowPrice**: a non-negative real value representing the maximum base "buy now" price for an item.
  Default value is: *1000*.
- **rubis.maxItemBaseReservePrice**: a non-negative real value representing the maximum base reserve price for an item.
  Default value is: *1000*.
- **rubis.maxItemDescrLen**: a string value representing the maximum length for the description of an item; this is the RAIN counterpart of the *item\_description\_length* RUBiS property.
  Default value is: *8192*.
- **rubis.maxItemDuration**: a non-negative integer value representing the maximum duration (in days) of an item.
  Default value is: *7*.
- **rubis.maxItemInitPrice**: a non-negative real value representing the maximum initial price for an item.
  Default value is: *5000*.
- **rubis.maxItemQuantity**: a non-negative integer value representing the maximum quantity for multiple items; this is the RAIN counterpart of the *max\_quantity\_for_multiple\_items* RUBiS property.
  Default value is: *10*.
- **rubis.maxWordLen**: a non-negative integer value representing the maximum length of a randomly generated word.
  Default value is: *12*.
- **rubis.numItemsPerPage**: a non-negative integer value representing the maximum number of items to display in a single page; this is the RAIN counterpart of the *workload\_number\_of\_items\_per\_page* RUBiS property.
  Default value is: *20*.
- **rubis.numOldItems**: a non-negative integer value representing the number of items whose auction date is over; this is the RAIN counterpart of the *database\_number\_of\_old\_items* RUBiS property.
  Default value is: *1000000*.
- **rubis.numPreloadedUsers**: a non-negative integer value representing the number of user that have been already preloaded inside the RUBiS database; this is the RAIN counterpart of the *database\_number\_of\_users* RUBiS property.
  Default value is: *1*.
- **rubis.percentItemsBuyNow**: a non-negative real value between 0 and 100 (inclusive) represnting the percentage of items that users can "buy now"; this is the RAIN couterpart of the *percentage\_of_items\_with\_reserve\_price* RUBiS property.
  Default value is: *80*.
- **rubis.percentItemsReserve**: a non-negative real value between 0 and 100 (inclusive) representing the percentage of items with a reserve price; this is the RAIN counterpart of the *percentage\_of\_buy\_now\_items* RUBiS property.
  Default value is: *40*.
- **rubis.percentUniqueItems**: a non-negative real value between 0 and 100 (inclusive) representing the percentage of unique items; this is the RAIN counterpart of the *percentage\_of\_unique\_items* RUBiS property.
  Default value is: *10*.
- **rubis.regionsFile**: a string representing the path to the RUBiS regions file; this is the RAIN counterpart of the *database\_regions\_file* RUBiS property.
  Default value is: *"resources/rubis-ebay_regions.txt"*.
- **rubis.rngSeed**: an integer number representing the seed used to initialize the random number generator used by the RUBiS generator; if set to `-1`, the random number generator will be initialized with the Java's default (i.e., to a value very likely to be distinct from any other invocation of the `java.util.Random` default constructor).
  Default value is: *-1*.
- **rubis.serverHtmlPath**: the URL path pointing to the base location where HTML files on the RUBiS server.
  Default value is: */*.
- **rubis.serverScriptPath**: the URL path pointing to the base location where script files on the RUBiS server.
  Default value is: */*.


The order of the operations in the traffic mix matrix is the same of the one specified in the previous section (i.e., 0 is the *Home Page* operation, 1 is the *Register* operation, and so on).

### Compilation and Execution

#### Compilation

To compile the RUBiS workload, simply enter the following command:

    $ ant package-rubis

#### Database Initialization

If you need to setup the RUBiS database, you can execute the following commands:
- Download the JDBC driver for the DBMS managing the RUBiS database.
  For instance, for a *MySQL* DBMS go to the [MySQL site](http://dev.mysql.com) and download the *MySQL Connector/J* version 5.1 or greater.
- Put the JDBC JAR into a readable path (say `./lib`), for instance:

		$ mkdir -p lib
		$ cd lib
		$ wget ftp://na.mirror.garr.it/mirrors/MySQL/Downloads/Connector-J/mysql-connector-java-5.1.27.zip
		$ unzip mysql-connector-java-5.1.27.zip mysql-connector-java-5.1.27/mysql-connector-java-5.1.27-bin.jar
		$ mv mysql-connector-java-5.1.27/mysql-connector-java-5.1.27-bin.jar .
		$ rm mysql-connector-java-5.1.27.zip

- Run the DB population command to populate the RUBiS database:

		$ java -cp rain.jar:workloads/rubis.jar:lib/mysql-connector-java-5.1.27-bin.jar radlab.rain.workload.rubis.util.InitDbDriver -verbose -dburl "jdbc:mysql://$DBMS_HOST/rubis" -dbusr rubis -dbpwd rubis

  where `$DBMS_HOST` is the name or IP address of the host running the DBMS.
  Note, if you don't use MySQL you have to suitably adapt the above command.

#### Workload Driver Execution

To run the RUBiS workload, simply enter the following command:

    $ java -Xmx1g -Xms256m -cp rain.jar:workloads/rubis.jar radlab.rain.Benchmark config/rain.config.rubis.json

### Assumptions

The current implementation assumes that:
- One and only one instance of RAIN RUBiS workload is running. This assumption is needed to assign _unique_ identifiers to both users and items.
- There is at least one preloaded user inside the RUBiS database.
