mvn javadoc:aggregate
cp -rf target/site/apidocs/* ../javalite.github.io/activeweb/

echo "************************************************************************"
echo "Do not forget to push changes in: ../javalite.github.io/activeweb/"
echo "************************************************************************"


