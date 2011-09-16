#!/bin/bash
echo "*******************  BUILDING MODULE  *****************************************"
mvn clean install
echo "*******************  COLLECTING DEPENDENCIES  *********************************"
mvn dependency:copy-dependencies
export CLASPATH=""
for file in `ls target/dependency`; do export CLASSPATH=$CLASSPATH:target/dependency/$file; done
for file in `ls target`; do export CLASSPATH=$CLASSPATH:target/$file; done

echo "*******************  EXECUTING PROGRAM******************************************"
java -cp $CLASSPATH -Dactivejdbc.log activejdbc.examples.multidb.Main
