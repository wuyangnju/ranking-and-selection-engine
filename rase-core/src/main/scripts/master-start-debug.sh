#!/bin/bash

MAIN_CLASS="hk.ust.felab.rase.master.MasterMain"

CLASSPATH="$(dirname $0)/../conf"
for i in $(dirname $0)/../lib/*
do
    CLASSPATH="${CLASSPATH}:${i}"
done

java -Xdebug -Xrunjdwp:transport=dt_socket,address=5568,server=y,suspend=n \
-cp "${CLASSPATH}" "${MAIN_CLASS}" "$@"
