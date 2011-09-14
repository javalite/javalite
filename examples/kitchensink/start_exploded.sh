mvn clean install -Pgwt -Dmaven.test.skip=true -o
mvn jetty:run-exploded  -Dmaven.test.skip=true -o
