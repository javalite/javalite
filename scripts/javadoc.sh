#!/usr/bin/env bash


if [ $# -eq 0 ]
  then
    echo "Must provide a fixed version or a snapshot"
    exit 1
fi

mvn clean javadoc:aggregate
mkdir ../javalite.github.io/$1
cp -r target/site/apidocs/* ../javalite.github.io/$1
cd ../javalite.github.io/
git add .
git commit -m "JavaDoc update for $1"
git push