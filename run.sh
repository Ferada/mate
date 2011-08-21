#!/bin/sh

if [ -z $MAIN ]; then
  MAIN=hub.AwarenessHub
fi

java -cp bin:src:$(echo lib/*.jar | tr ' ' ':') $MAIN "$*"
