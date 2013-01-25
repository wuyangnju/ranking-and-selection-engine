#!/bin/bash

MAIN_CLASS="hk.ust.felab.rase.Headquarters"

CLASSPATH="$(dirname $0)/../conf"
for i in $(dirname $0)/../lib/*
do
    CLASSPATH="${CLASSPATH}:${i}"
done

java -cp "${CLASSPATH}" "${MAIN_CLASS}" "$@"
