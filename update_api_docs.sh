mvn javadoc:aggregate
cp -rf target/site/apidocs/* ../javalite.github.io/activeweb/
cd ../javalite.github.io/activeweb/
git add .
git commit -m "update JavaDoc"
git push origin master

