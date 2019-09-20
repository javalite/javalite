#!/usr/bin/env bash
mvn clean install -Ptds,instrument -DargLine="-Dactivejdbc.log"

