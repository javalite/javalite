activejdbc
==========


ActiveJDBC is a Java implementation of Active Record design pattern. 
It was inspired by [ActiveRecord ORM](https://github.com/rails/rails/tree/master/activerecord) from Ruby on Rails.

## Design principles
* Should infer metadata from DB (like ActiveRecord)
* Should be very easy to work with
* Should reduce amount of code to a minimum
* No configuration, just conventions
* Some conventions are overridable in code
* No need to learn another language
* No need to learn another QL &mdash; SQL is sufficient
* Code must be lightweight and intuitive, should read like English
* No sessions, no "attaching, re-attaching"
* No persistence managers
* No classes outside your own models
* Models are lightweight, no transient fields
* No proxying. What you write is what you get (WYSIWYG:))
* Should have the least possible resistance to startup a project
* No useless getters and setters (they just pollute code). You can still write them if you like.
* No DAOs and DTOs &mdash; this is mostly junk code anyway

## Simple example

For a simple example we will use a table called `people` created with this MySQL DDL:

<pre>
CREATE TABLE people (
  id  int(11) NOT NULL auto_increment PRIMARY KEY, 
  name VARCHAR(56) NOT NULL, 
  last_name VARCHAR(56), 
  dob DATE, 
  graduation_date DATE, 
  created_at DATETIME, 
  updated_at DATETIME
);
</pre>

ActiveJDBC infers DB schema parameters from a database. This means you do not have to provide it in code, the simplest example model looks like this:

```java
public class Person extends Model {}
```

Despite the fact that there is no code in it, it is fully functional and will map to a table called `people` automatically.

Here is a simple query how to use the `Person` model:
 
```Java
List<Person> people = Person.where("name = 'John'");
Person aJohn =  people.get(0);
String johnsLastName = aJohn.get("last_name");
```

As you can see, the amount of code is reduced to a level when it is actually readable. Finder methods can also be parametrized like this:

```Java
List<Person> people = Person.where("name = ?", "John");
Person aJohn =  people.get(0);
String johnsLastName = aJohn.get("last_name");
```

## Documentation
All documentation is  placed here: [http://javalt.org/p/activejdbc](http://javalt.org/p/activejdbc)

##News

__March 20 2012__: ActiveWeb and ActiveJDBC were presented to the Software Craftsmanship McHenry County - got great reception. Part of the presentation was to build a simple live application with test coverage.

__January 25 2012__: Support for Microsoft SQL Server was added. This brings the number of databases supported by ActiveJDBC to 5: MySQL, PostgreSQL, Oracle, H2, MS SQLServer. Kudos to John Richardson for this contribution. Support of MS SQLServer includes both Microsoft and TDS drivers.

__January 3 2012__: I did a presentation at Groupon Geekfest on ActiveWeb and ActiveJDBC: http://geekfest.gathers.us/events/geekfest-activeweb-and-activejdbc. It was very well received. Some reactions from Ruby developers were: "I'd die to have this when I did Java", as well as help in developing support for Microsoft SQLServer

__September 18 2011__: A new caching provider has been integrated into the project: EHCache. This was done because it seems that OSCache, however good of a project, is retired by OpenSymphony.

__August 10 2011__: Dear community, we are preparing for a first formal release of ActiveJDBC and publishing it to the Maven Central. In preparations, we upgraded the version from 1.1-SNAPSHOT to 1.2-SNAPSHOT and also moved all classes from package: activejdbc to package: org.javalite.activejdbc

We also moved group artifact from: activejdbc to: org.javalite.activejdbc

All these superficial changes are required by Sonatype to publish to the Maven Central. We apologize for the temporary inconvenience, but this change is something we did not anticipate. However, with ActiveWeb following the suit, it too will be released under org.javalite group ID and package.

The old repository you are using for 1.1-SNAPSHOT: http://ipsolutionsdev.com/snapshots/ will stay untouched indefinitely, so you can upgrade to 1.2-SNAPSHOT at any time in the future, if you can wait till a fixed release 1.2 will be published to Maven Central (~ 2 weeks)

The new repository we are publishing snapshots is hosted by Sonatype: https://oss.sonatype.org/content/repositories/snapshots/org/javalite/

__July 20 2011__: Made JavaDoc accessible: http://ipsolutionsdev.com/activejdbc/

__May 22 2011__: Phil Suh added support for H2 Database

__February 23 2011__: Added a long awaited generation of JSON into classes Model and LazyList, for more information navigate to GenerationOfJson

__January 12 2011__: ActiveJDBC now has ability to load metadata on demand - when a database is first accessed. This means that in cases when a system has more than one database, their metadata is not loaded up all at once. This gives more flexibility because you do not need to have all connections available in places where you only need one.

__November 9 2010__: ActiveJDBC now has ability to generate stock XML from models and lists of models. Follow this link for more information: GenerationOfXml

__September 28 2010__: ActiveJDBC validations framework has been extended to take in dynamic parameters, and reflect internationalized messages. For more information, take a look at Validations

__September 7 2010__: All artifacts from this project are published to a new snapshot repository: http://ipsolutionsdev.com/snapshots/.

__August 18 2010__: Well, it did finally happen: yesterday I ran a presentation on ActibeJDBC at Thoughtworks. Attendance was at healthy 25 people, and all were engaged and asked a lot of questions. To those attended: thank you for interest in the framework, your questions and suggestions! This proves once again that there is room for a new Java ORM system. If you'd like to get the presentation slides, follow this link: https://activejdbc.googlecode.com/svn/trunk/doc/activejdbc-cjug.pdf.

Happy coding!

__August 11 2010__: Chicago Java User Group accepted my proposal for a presentation on ActiveJDBC. Thoughtworks will be hosting it on August 17th.

__July 19 2010__: ActiveJDBC sources are published on Google Code under Apache 2.0 License.
