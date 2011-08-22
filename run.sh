#!/bin/sh

if [ -z $MAIN ]; then
  MAIN=hub.AwarenessHub
fi

if [ -z $LOGGING ]; then
  LOGGING=simple
fi

LIBS=$(echo $LIBS lib/*.jar | tr ' ' ':')
LOG=$(echo lib/slf4j/slf4j-$LOGGING*.jar)

java -cp bin:src:$LIBS:$LOG $MAIN "$*"
