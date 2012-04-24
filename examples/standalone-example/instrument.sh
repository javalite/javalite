#
# This script is an example of running instrumentation without Maven or Ant, just a simple command line.
#

#create classpath
export OUTDIR=classes

export CLASSPATH=$OUTDIR
export CLASSPATH=$CLASSPATH:build_time_libs/activejdbc-instrumentation-1.2.2-SNAPSHOT.jar
export CLASSPATH=$CLASSPATH:build_time_libs/javassist-3.8.0.GA.jar
export CLASSPATH=$CLASSPATH:lib/activejdbc-1.2.2-SNAPSHOT.jar

java -cp $CLASSPATH -DoutputDirectory=$OUTDIR org.javalite.instrumentation.Main
