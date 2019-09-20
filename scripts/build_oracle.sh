#!/usr/bin/env bash
mvn clean install -Poracle,instrument -DargLine="-Dactivejdbc.log"
