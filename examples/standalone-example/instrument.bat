
set CLASSPATH=classes
set CLASSPATH=%CLASSPATH%;lib\activejdbc-1.4.1.jar
set CLASSPATH=%CLASSPATH%;lib\javalite-common-1.4.1.jar
set CLASSPATH=%CLASSPATH%;lib\mysql-connector-java-5.0.4.jar
set CLASSPATH=%CLASSPATH%;lib\slf4j-api-1.5.10.jar
set CLASSPATH=%CLASSPATH%;lib\slf4j-simple-1.5.10.jar
set CLASSPATH=%CLASSPATH%;build_time_libs\activejdbc-instrumentation-1.4.1.jar
set CLASSPATH=%CLASSPATH%;build_time_libs\javassist-3.8.0.GA.jar

java -classpath %CLASSPATH% -DoutputDirectory=classes org.javalite.instrumentation.Main
