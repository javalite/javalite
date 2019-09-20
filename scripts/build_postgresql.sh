#!/usr/bin/env bash
mvn clean install -Ppostgresql,instrument -DargLine="-Dactivejdbc.log"
