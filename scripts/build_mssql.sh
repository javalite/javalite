#!/usr/bin/env bash
mvn clean install -Pmssql,instrument -DargLine="-Dactivejdbc.log"
