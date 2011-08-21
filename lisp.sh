#!/bin/sh

ABCL=/usr/share/abcl/lib/abcl.jar
MAIN=org.armedbear.lisp.Main

cd /home/rudolf/uni/masterarbeit/mate/

exec java -cp bin:src:$(echo lib/*.jar | tr ' ' ':'):$ABCL $MAIN "$*"
