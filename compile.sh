#!/bin/sh

if [[ -z $@ ]]; then
  FILES=src/*/*.java
else
  FILES=$(find src -name "$1")
fi

javac -d bin -Xlint:unchecked -Xlint:deprecation -cp bin:src:$(echo lib/*.jar | tr ' ' ':') $FILES
