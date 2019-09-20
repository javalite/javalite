#!/usr/bin/env bash
mvn clean install -Psqlite,instrument -DargLine="-Dactivejdbc.log"
