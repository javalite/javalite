#!/bin/sh

mvn javadoc:aggregate
cp -rf target/site/apidocs/* ../javalite.github.io/activejdbc/snapshot
cd ../javalite.github.io/activejdbc/snapshot
git add .
git commit -m "update JavaDoc"
git push origin master
