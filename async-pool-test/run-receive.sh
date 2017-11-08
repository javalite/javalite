#!/usr/bin/env bash
mvn clean compile exec:java -Dexec.mainClass="org.javalite.async.pooltest.TestReceive"
