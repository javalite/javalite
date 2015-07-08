## ActiveJDBC Instrumentation Plugin

The Instrumentation plugin injects magic into models' byte code.

### Static instrumentation

Static instrumentation is a post-compile step, which instruments class files. After static instrumentation, you
can package class files into a jar/war file as usual, execute or deploy on container without any special steps.

Please, see: [ActiveJDBC Instrumentation](http://javalite.io/instrumentation) for more information

### Dynamic instrumentation

Many people complained that static instrumentation gets in the way of development, because IDEs sometimes recompile
classes thus blow away instrumentation. When a developer tries to run a unit test, ActiveJDBC fails and instrumentation
needs to be executed again. It also requires Maven/Ant/script configuration.

Dynamic instrumentation is implemented as a Java Agent (javaagent). The same Maven plugin is also used as a Java Agent.
Available from Maven Central:
[ActiveJDBC Instrumentation plugin](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22activejdbc-instrumentation%22)


### Dynamic instrumentation logging

If you want to see some output from instrumentation process, simply add this system property:

    -Dactivejdbc-instrumentation.log

### How to use dynamic instrumentation

* Download latest build of: [Activejdbc Instrumentation Jar file](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22activejdbc-instrumentation%22) and place it somewhere on your file system.
(You might already have this file in your local Maven repository, might want to look there)
* When executing any program that requires ActiveJDBC, pass this parameter to JVM:

```
java -javaagent:/home/bobthedeveloper/project1/activejdbc-instrumentation.jar example.Main
```

where `/home/bobthedeveloper/project1/activejdbc-instrumentation.jar` is a location of the instrumentation plugin jar.

It is easy to configure defaults in every IDE to add this parameter to JVM when executing tests.

### Dynamic instrumentation and compatilility with Java 8

There are some conflicts using models' static methods inside lambda expressions. So far this is the only issue. Static instrumentation is free of conflicts and works perfectly with Java 8. 

### How to deploy ActiveJDBC applications

ActiveJDBC models require instrumentation. You can use static or dynamic instrumentation, as long
 as models are instrumented before they are executed.

* Static instrumentation configuration is described here: [ActiveJDBC Instrumentation](http://javalite.io/instrumentation)
* Dynamic instrumentation is described above. Any JVM instance, including containers like Tomcat, Jetty, Weblogic, Glassfish,
etc. have startup scripts where you can simply add the `javaagent` parameter as described above.

### Recommendation (static or dynamic??)

We recommend to use both.

> Configure dynamic instrumentation in your local development environment to avoid
> running static instrumentation before running tests.

> Additionally, configure static instrumentation in your build to
> deploy your code onto containers without additional modifications to container scripts.
