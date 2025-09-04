# Ecosystem for rapid application development in Java.

See more at: [JavaLite.io](http://javalite.io)

Building is easy, from root directory: 

```
$ docker compose up -d
$ mvn clean install
```

## Understanding ActiveJDBC profiles.

There are a few DB - related  profiles in the ActiveJDBC module: 
* mysql
* mariadb
* oracle 
* etc...

These profile contain JDBC parameters for respective databases. 
These connection parameters correspond to respective configurations in the `docker-compose.yml`
file. Additionally, each profile has a property called `db`. This property is used by `ActiveJDBCTest` in order to initialize 
the database with tables and data before tests, depending on a profile (database) selected.

During tests, the file `activejdbc/src/test/resources/jdbc.properties` gets merged with properties from the current profile, 
and then used by the test infrastructure to connect to the right database and load the right SQL file.  

