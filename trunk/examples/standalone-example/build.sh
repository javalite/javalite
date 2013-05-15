#
# This script is an example of running instrumentation without Maven or Ant, just a simple command line.
#


# delete all classes
rm -rf classes/*
for file in `ls lib` ; do export  CLASSPATH=$CLASSPATH:lib/$file; done


javac -cp $CLASSPATH -d classes src/activejdbc/examples/simple/*.java  
