#!/bin/sh

# compiles all java files from the src to the bin directory

if [ -z $@ ]; then
  FILES=$(find src -name "*.java")
else
  FILES=$(find src -name "$1")
fi

if [ ! -d bin ]; then
  mkdir bin
fi

CLASSPATH=.:bin:src:$(echo lib/*.jar | tr ' ' ':')

javac -d bin -Xlint:{unchecked,deprecation} -cp $CLASSPATH $FILES
