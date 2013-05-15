#
# This script is an example of running instrumentation without Maven or Ant, just a simple command line.
#

export CLASSPATH=classes
for file in `ls lib` ; do export  CLASSPATH=$CLASSPATH:lib/$file; done

java -cp $CLASSPATH -DoutputDirectory=$OUTDIR activejdbc.examples.simple.SimpleExample
