   VERSION=$1

   mvn javadoc:aggregate

<<<<<<< HEAD
   mkdir  ../javalite.github.io/activejdbc/$VERSION

   cp -rf target/site/apidocs/* ../javalite.github.io/activejdbc/$VERSION
=======
   mkdir  ../javalite.github.io/activeweb/$VERSION

   cp -rf target/site/apidocs/* ../javalite.github.io/activeweb/$VERSION
>>>>>>> ce4b409c52a78d28b6d19ab5e869e3aedf502e3f
   cd ../javalite.github.io/
   git add .
   git commit -m "added version $VERSION"

   echo "Done creating the JavaDoc for $VERSION. Please, execute ***git push*** if you are happy"
