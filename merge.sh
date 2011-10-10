#!/bin/sh

# merges all necessary jars into a single file if it doesn't exist yet
# (we assume the library jars don't change, so if they do, simply delete
# the output jar and run this script again)
# then updates all class files in the new jar

if [ -z $MAIN ]; then
  MAIN=hub.AwarenessHub
fi

if [ -z $LOGGING ]; then
  LOGGING=log4j
fi

LIBS=$(echo $LIBS lib/*.jar)
LOG=$(echo lib/slf4j/slf4j-$LOGGING*.jar)
JARS="$LIBS $LOG"
OUT=mate.jar

if [ ! -e $OUT ]; then
    zipmerge $OUT $JARS
fi

mkdir -p bin/META-INF
cp -u manifest.mf bin/META-INF/MANIFEST.MF

zip -u $OUT log4j.properties

cd bin
exec find . -type f -print0 | xargs -0 zip -u ../$OUT
