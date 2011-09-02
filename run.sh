#!/bin/sh

# runs the specified main method with class-path and configuration file
# set up

if [ -z $MAIN ]; then
  MAIN=hub.AwarenessHub
fi

if [ -z $LOGGING ]; then
  LOGGING=log4j
fi

LIBS=$(echo $LIBS lib/*.jar | tr ' ' ':')
LOG=$(echo lib/slf4j/slf4j-$LOGGING*.jar)

exec java -Dlog4j.configuration=log4j.properties -cp .:bin:src:$LIBS:$LOG $MAIN $*
