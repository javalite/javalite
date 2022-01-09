#!/usr/bin/env bash

## This script will instrument fast without Maven
## Adjust versions on the classpath if upgrading!
export JAVALITE=3.0-SNAPSHOT

export CLASSPATH=~/.m2/repository/org/javalite/activejdbc-instrumentation/$JAVALITE/activejdbc-instrumentation-$JAVALITE.jar
export CLASSPATH=${CLASSPATH}:~/.m2/repository/org/javalite/activejdbc/$JAVALITE/activejdbc-$JAVALITE.jar
export CLASSPATH=${CLASSPATH}:~/.m2/repository/org/javalite/javalite-common/3.0-SNAPSHOT/javalite-common-$JAVALITE.jar

export CLASSPATH=${CLASSPATH}:~/.m2/repository/org/javassist/javassist/3.18.2-GA/javassist-3.18.2-GA.jar
export CLASSPATH=${CLASSPATH}:~/.m2/repository/org/slf4j/slf4j-api/1.7.32/slf4j-api-1.7.32.jar
export CLASSPATH=${CLASSPATH}:~/.m2/repository/org/slf4j/slf4j-simple/1.7.32/slf4j-simple-1.7.32.jar

export CLASSPATH=${CLASSPATH}:target/test-classes

java   -classpath $CLASSPATH  -DoutputDirectory=target/test-classes org.javalite.instrumentation.Main

