#!/usr/bin/env bash

## This script will instrument fast without Maven
## Adjust versions on the classpath if upgrading!
export JAVALITE=3.5-j11-SNAPSHOT

export CLASSPATH=~/.m2/repository/org/javalite/activejdbc-instrumentation/$JAVALITE/activejdbc-instrumentation-$JAVALITE.jar
export CLASSPATH=${CLASSPATH}:~/.m2/repository/org/javassist/javassist/3.18.2-GA/javassist-3.18.2-GA.jar
export CLASSPATH=${CLASSPATH}:~/.m2/repository/org/javalite/activejdbc/$JAVALITE/activejdbc-$JAVALITE.jar
export CLASSPATH=${CLASSPATH}:target/test-classes

java   -classpath $CLASSPATH  -DoutputDirectory=target/test-classes org.javalite.instrumentation.Main

