#!/bin/sh

# runs the specified main method with class-path and configuration file
# set up

if [ -z $JAR ]; then
  JAR=mate.jar
fi

OPTS=-Dlog4j.configuration=log4j.properties

if [ -z $MAIN ]; then
  exec java $OPTS -jar $JAR $*
else
  exec java $OPTS -cp $JAR $MAIN $*
fi
