#!/bin/sh

if [ -z $MAIN ]; then
  MAIN=hub.AwarenessHub
fi

OUT=mate-single.jar

mkdir -p bin/META-INF
cp -u manifest.mf bin/META-INF/MANIFEST.MF

cd bin

exec find . -type f -print0 | xargs -0 zip -u ../$OUT
