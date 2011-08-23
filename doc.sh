#!/bin/sh

if [ -z $LOGGING ]; then
  LOGGING=log4j
fi

LIBS=$(echo $LIBS lib/*.jar | tr ' ' ':')
LOG=$(echo lib/slf4j/slf4j-$LOGGING*.jar)

PACKAGES="board board.vocabulary comm hub mail reasoner sms"

# exec gjdoc -validhtml -verbose -private -d javadoc -all -classpath bin:src:$LIBS:$LOG -s src
exec javadoc -verbose -private -d javadoc -classpath bin:src:$LIBS:$LOG -sourcepath src $PACKAGES
