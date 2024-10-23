## Test project showcasing how to start TOmcat with JNDI in an embedded mode  

It includes a Maven Shade Plugin to generate a single executable file

How to execute:
### Start a docker  container 

Include a database matching the connection parameters in the file `database.properties`

## Create a database 

Execute the Migration Plugin:

```
mvn db-migrations:reset
```

This will drop a previous database, re-create a new one and will run migrations in a single step.

## Build the project:

```
mvn clean install
```
This will create a single executable jar file, which you can execute to start your app with an embedded Tomcat and a configured 
database connection pool: 
```
java -jar -Dactive_reload=true target/embedded-tomcat-integration-test-3.4-SNAPSHOT.jar
```

The name of the file will likely change in the future, so  pay attention to the build output. 

> Note: the `-Dactive_reload=true` option will indicate that the controllers will be recompiled 
> and reloaded on every request. This option only works in the presence of the sources, so
> use this option only in the developer environment. 




