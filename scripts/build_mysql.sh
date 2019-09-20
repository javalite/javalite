#!/usr/bin/env bash
mvn clean install -Pmysql,instrument -DargLine="-Dactivejdbc.log"
