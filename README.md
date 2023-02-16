# jpalite

Jpalite is a Lightweight Java Persistence package which builds on the [OrmLite](http://ormlite.com) lightweight Object Relational Mapping (ORM) Java package. 
It is a persistence framework created using the JPA Archecture as a blue print. It is suitable for embedding in resource-constrained platforms such as Android. 
Jpalite implements the main parts of the interface dedined in version 1 of the JPA API - refer <a href="http://docs.oracle.com/javaee/6/api/javax/persistence/package-summary.html">javax.persistence</a> 
package. JPA features taken on board include persistence.xml configuration, an entity manager factory, named queries and transactions which automatically roll back 
if an exception occurs. For more details, refer to [Lightweight JPA in a nutshell](http://cybersearch2.com.au/develop/jpa_intro.html)></a>

   
Jpalite is potentially portable to any database supported by OrmLite - refer [Database-Notes for Supported Databases](http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_2.html) with
builtin support currently provided for H2 and SQLite.

## Getting Started

The instructions for getting started can be found at [Jpalite Home Page](http://cybersearch2.com.au/logic/jpalite.html).
You will need to clone this project from the [Github site](https://github.com/cybersearch2/jpalite) and then use
Maven to install it. Both Java SE version 11 and above and Maven need to be installed in order to proceed. Once
the project is installed you can progress through the examples starting with the [Foreign Collection example](http://cybersearch2.com.au/develop/foreign-collection.html).


   

