#
# This script is an example of running instrumentation without Maven or Ant, just a simple command line.
#

export CLASSPATH=classes

for file in `ls lib` ; do export  CLASSPATH=$CLASSPATH:lib/$file; done
for file in `ls build_time_libs` ; do export  CLASSPATH=$CLASSPATH:build_time_libs/$file; done

java -cp $CLASSPATH -DoutputDirectory=classes org.javalite.instrumentation.Main
