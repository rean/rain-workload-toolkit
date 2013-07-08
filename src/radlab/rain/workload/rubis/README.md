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


## Known Bugs

The current implementation assumes that one and only one instance of RAIN RUBiS workload is running. This assumption is needed to assign _unique_ identifiers to both users and items.
