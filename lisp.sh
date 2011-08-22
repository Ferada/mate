#!/bin/sh

ABCL=/usr/share/abcl/lib/abcl.jar
MAIN=org.armedbear.lisp.Main

LIBS="$LIBS $ABCL"

cd /home/rudolf/uni/masterarbeit/mate/

MAIN=$MAIN LIBS=$LIBS exec sh run.sh "$*"
