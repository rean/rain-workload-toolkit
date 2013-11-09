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
- **Store Moderate Log**: store the moderation to a comment for a selected storyi in the database.
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
- **rubbos.dictionaryFile**: a string representing the path to the RUBBoS dictionary file; this is the RAIN counterpart of the *database\_story\_dictionary* RUBBoS property.
  Default value is: *"resources/rubbos-dictionary.txt"*.
- **rubbos.incarnation**: a case-insensitive string representing the RUBBoS incarnation one wants to use.
  Possible values are:
  - *"php"*,
  - *"servlet"*.

  Default value is: *"servlet"*.
- **rubbos.initOp**: a case-insensitive string representing the RUBBoS operation from which a user session starts.
  Possible values are:
  - *"StoriesOfTheDay"*,
  - *"Register"*,
  - *"RegisterUser"*,
  - *"Browse"*,
  - *"BrowseCategories"*,
  - *"BrowseStoriesByCategory"*,
  - *"OlderStories"*,
  - *"ViewStory"*,
  - *"PostComment"*,
  - *"StoreComment"*,
  - *"ViewComment"*,
  - *"ModerateComment"*,
  - *"StoreModerateLog"*,
  - *"SubmitStory"*,
  - *"StoreStory"*,
  - *"Search"*,
  - *"SearchInStories"*,
  - *"SearchInComments"*,
  - *"SearchInUsers"*,
  - *"AuthorLogin"*,
  - *"AuthorTasks"*,
  - *"ReviewStories"*,
  - *"AcceptStory"*,
  - *"RejectStory"*.

  Default value is: *"StoriesOfTheDay"*.
- **rubbos.maxCommentLen**: a non-negative integer value representing the maximum length of a comment to a story; this is the RAIN counterpart of the *database\_comment\_max\_length* RUBBoS property.
  Default value is: *1024*.
- **rubbos.maxStoryLen**: a non-negative integer value representing the maximum length of a story; this is the RAIN counterpart of the *database\_story\_max\_length* RUBBoS property.
  Default value is: *1024*.
- **rubbos.newestStoryMonth**: a non-negative integer value between 1 and 31 (inclusive) representing the month of the newest story in the RUBBoS database; this is the RAIN counterpart of the *database\_oldest\_story\_month* RUBBoS property.
  Default value is: *1*.
- **rubbos.newestStoryYear**: a non-negative integer value representing the year of the newest story in the RUBBoS database; this is the RAIN counterpart of the *database\_oldest\_story\_year* RUBBoS property.
  Default value is: *this year*.
- **rubbos.numPreloadedAuthors**: a non-negative integer value representing the number of authors that have been already preloaded inside the RUBBoS database; this is the RAIN counterpart of the *database\_number\_of\_authors* RUBBoS property.
  Default value is: *1*.
- **rubbos.numPreloadedUsers**: a non-negative integer value representing the number of users that have been already preloaded inside the RUBBoS database; this is the RAIN counterpart of the *database\_number\_of\_users* RUBBoS property.
  Default value is: *1*.
- **rubbos.numStoriesPerPage**: a non-negative integer value representing the maximum number of stories to display in a single page; this is the RAIN counterpart of the *workload\_number\_of\_stories\_per\_page* RUBBoS property.
  Default value is: *20*.
- **rubbos.oldestStoryMonth**: a non-negative integer value between 1 and 31 (inclusive) representing the month of the oldest story in the RUBBoS database; this is the RAIN counterpart of the *database\_oldest\_story\_month* RUBBoS property.
  Default value is: *1*.
- **rubbos.oldestStoryYear**: a non-negative integer value representing the year of the oldest story in the RUBBoS database; this is the RAIN counterpart of the *database\_oldest\_story\_year* RUBBoS property.
  Default value is: *this year*.
- **rubbos.rngSeed**: an integer number representing the seed used to initialize the random number generator used by the RUBBoS generator; if set to `-1`, the random number generator will be initialized with the Java's default (i.e., to a value very likely to be distinct from any other invocation of the `java.util.Random` default constructor).
  Default value is: *-1*.
- **rubbos.serverHtmlPath**: the URL path pointing to the base location where HTML files on the RUBBoS server.
  Default value is: */PHP*.
- **rubbos.serverScriptPath**: the URL path pointing to the base location where script files on the RUBBoS server.
  Default value is: */PHP*.


The order of the operations in the traffic mix matrix is the same of the one specified in the previous section (i.e., 0 is the *Stories of the Day* operation, 1 is the *Register* operation, and so on).

### Compilation and Execution

To compile the RUBBoS workload, simply enter the following command:

	$ ant package-rubbos

To run the RUBBoS workload, simply enter the following command:

	$ java -Xmx1g -Xms256m -cp rain.jar:workloads/rubbos.jar radlab.rain.Benchmark config/rain.config.rubbos.json

### Assumptions

The current implementation assumes that:
- One and only one instance of RAIN RUBBoS workload is running. This assumption is needed to assign _unique_ identifiers to both users and items.
- There is at least one preloaded user inside the RUBBoS database.

### Known Issues

- In the native client, it is possible to dynamically switch from a user workload profile to an author workload profile and back.
  This is not currently possible in the RAIN version of RUBBoS.
  Instead, you have to decide which workload profiles to use before you run the RAIN experiment by properly configuring the the *behavior* section of the configuration file.
- In the native client, in some situations (e.g., when an error happens) there is a transition back to the previously visited operation; in other situations (e.g., no more story to review), the user session is reinitialized.
  Instead, in RAIN, we currently set the operation as failed and let the user session restart.
