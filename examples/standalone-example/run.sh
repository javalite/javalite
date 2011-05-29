#
# This script is an example of running instrumentation without Maven or Ant, just a simple command line.
#

export OUTDIR=classes

export CLASSPATH=$OUTDIR
export CLASSPATH=$CLASSPATH:lib/activejdbc-1.1-SNAPSHOT.jar:
export CLASSPATH=$CLASSPATH:lib/javalite-common-1.1-SNAPSHOT.jar
export CLASSPATH=$CLASSPATH:lib/mysql-connector-java-5.0.4.jar
export CLASSPATH=$CLASSPATH:lib/slf4j-api-1.5.10.jar
export CLASSPATH=$CLASSPATH:lib/slf4j-simple-1.5.10.jar

echo $CLASSPATH
java -cp $CLASSPATH -DoutputDirectory=$OUTDIR activejdbc.examples.simple.SimpleExample