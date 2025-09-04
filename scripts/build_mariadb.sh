#!/usr/bin/env bash
mvn clean install -Pinstrument,mariadb -DargLine="-Dactivejdbc.log"
