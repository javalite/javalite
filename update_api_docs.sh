mvn javadoc:aggregate
cp -rf target/site/apidocs/* ../javalite.github.io/activejdbc/

echo "************************************************************************"
echo "Do not forget to push changes in: ../javalite.github.io/activejdbc/"
echo "************************************************************************"


