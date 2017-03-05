# JavaLite DB-Migrator is a database migration system for Java

Please, see <a href="http://en.wikipedia.org/wiki/Schema_migration">Schema_migration</a> to
understand what database migrations are.

## Maven plugin

Current implementation of this project is a Maven Plugin. Future releases will include a standalone library for non-Maven projects.

## How to use

Generate a new migration:

```
mvn db-migrator:new -Dname=create_people_table
...
[INFO] Created new migration: src/migrations/20140211113507_create_people_table.sql
...
```
This creates an empty file. Go ahead and add raw SQL to the file

```
create table people ( name varchar (10));
```
Run migration:
```
mvn db-migrator:migrate
...
[INFO] Migrating jdbc:mysql://localhost/test_project using migrations from src/migrations/
[INFO] Migrating database, applying 1 migration(s)
[INFO] Running migration 20140211113507_create_people_table.sql
...
```
Alternatively, you can just run the build.

## All other goals

You can execute plugin help goal to get all information on all other goals:

 ```
 mvn  db-migrator:help
 ...
 [INFO] db-migrator:drop
 [INFO]   drops database configured in pom
 [INFO] db-migrator:create
 [INFO]   creates database configured in pom
 [INFO] db-migrator:new
 [INFO]   creates a new migration file
 [INFO] db-migrator:check
 [INFO]   checks that no pending migrations remain. This can be used in build lifecycle to fail the build if pending migrations are found
 [INFO] db-migrator:migrate
 [INFO]   migrates all pending migrations
 [INFO] db-migrator:validate
 [INFO]   validates and prints a report listing pending migrations
 [INFO] db-migrator:reset
 [INFO]   drops/re-creates the database, and runs all migrations, effectively resetting database to pristine state
 [INFO] db-migrator:help
 [INFO]   prints this message
 ```


## Where to get

Generally, just add a plugin configuration to your pom, as described below. If you want to download, you can
do so here: <a href="http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22db-migrator-maven-plugin%22">db-migrator-maven-plugin</a>

## Maven configuration

Here is an example of simple configuration:

```xml
<plugin>
    <groupId>org.javalite</groupId>
    <artifactId>db-migrator-maven-plugin</artifactId>
    <version>1.4.9</version>
    <configuration>
        <driver>com.mysql.jdbc.Driver</driver>
        <url>jdbc:mysql://localhost/test_project</url>
        <username>your user</username>
        <password>your password</password>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.25</version>
        </dependency>
    </dependencies>
</plugin>
```

In a more realistic project, you will have more than one database, such as test, development, production, etc.
In order to migrate multiple databases, use Maven executions:

First, configure the plugin in `pluginManagement`:
```xml
<pluginManagement>
    <plugins>
        <plugin>
            <groupId>org.javalite</groupId>
            <artifactId>db-migrator-maven-plugin</artifactId>
            <version>1.4.9</version>
            <configuration>
                <username>${jdbc.user}</username>
                <password>${jdbc.password}</password>
                <driver>${jdbc.driver}</driver>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>mysql</groupId>
                    <artifactId>mysql-connector-java</artifactId>
                    <version>5.1.25</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</pluginManagement>
```
where user, password and driver are configured as project properties.

 After that, you can configure the plugin to execute multiple databases by adding many executions.
 Here is example of one execution:
```xml
<plugin>
    <groupId>org.javalite</groupId>
    <artifactId>db-migrator-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>dev_migrations</id>
            <phase>validate</phase>
            <goals>
                <goal>migrate</goal>
            </goals>
        </execution>
        <execution>
            <id>test_migrations</id>
            <phase>validate</phase>
            <goals>
                <goal>migrate</goal>
            </goals>
            <configuration>
                <url>${jdbc.test.url}</url>
            </configuration>
        </execution>
    </executions>
</plugin>
```
As you can see, the plugin is tied to `validate` phase, which will ensure that it will migrate
schema at the very start of the build. Add more executions to run against multiple databases. You can use Maven profiles
with this plugin to migrate databases in different environments, such as production.



## Configuration properties

* `url` - JDBC connection URL
* `driver` - JDBC connection driver
* `username` - JDBC connection user name
* `password` - JDBC connection password
* `migrationsPath` - location of migration files, defaults to  `src/migrations/`
* `createSql` - create database SQL, defaults to `create database {$your database}`
* `dropSql` - drop database SQL, defaults to `drop database {$your database}`
* `encoding` - encoding to use when reading migration files. Defaults to UTF-8 if missing