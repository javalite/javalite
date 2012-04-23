#
# This script is an example of running instrumentation without Maven or Ant, just a simple command line.
#


# delete all classes
rm -rf classes/*

#create classpath
export CLASSPATH=lib/activejdbc-1.3-SNAPSHOT.jar
export CLASSPATH=$CLASSPATH:lib/javalite-common-1.3-SNAPSHOT.jar
export CLASSPATH=$CLASSPATH:lib/slf4j-api-1.5.10.jar

javac -cp $CLASSPATH -d classes src/activejdbc/examples/simple/*.java  
