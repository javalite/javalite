#!/usr/bin/env bash
 VERSION=$1

 mvn javadoc:aggregate

 mkdir  ../javalite.github.io/activeweb/$VERSION

 cp -rf target/site/apidocs/* ../javalite.github.io/activeweb/$VERSION

 cd ../javalite.github.io/

 git add .

 git commit -m "added version $VERSION"

 echo "Done creating the JavaDoc for $VERSION. Please, execute ***git push*** if you are happy"
