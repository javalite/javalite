#!/usr/bin/env bash


if [ $# -eq 0 ]
  then
    echo "Must provide a fixed version or a snapshot"
    exit 1
fi

mvn clean javadoc:aggregate
rm -rf ../javalite.github.io/$1
mkdir ../javalite.github.io/$1
cp -r target/site/apidocs/* ../javalite.github.io/$1
cd ../javalite.github.io/
git add .
git commit -m "JavaDoc update for $1"
git push

echo "Access JavaDoc at: http://javalite.github.io/$1/"
echo "You may want to update page:  https://github.com/javalite/javalite-site/blob/master/content/src/activejdbc/javadoc.md"