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
UNPACKED=unpacked
ZIPOPTS=-1

if [ ! -e $OUT ]; then
    echo "creating $OUT from jars"
    if which zipmergee > /dev/null; then
        echo "using zipmerge"
        zipmerge $OUT $JARS
    else
        echo "no zipmerge, falling back to unpacking and zipping"
        mkdir -p $UNPACKED
        pushd $UNPACKED
        for JAR in $JARS; do
            unzip ../$JAR
            zip $ZIPOPTS ../$OUT -r *
            rm -Rf *
        done
        popd
        rm -Rf $UNPACKED
    fi
fi

mkdir -p bin/META-INF
cp -u manifest.mf bin/META-INF/MANIFEST.MF

zip $ZIPOPTS -u $OUT log4j.properties

cd bin
exec find . -type f -print0 | xargs -0 zip -u ../$OUT
