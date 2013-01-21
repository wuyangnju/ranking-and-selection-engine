#!/bin/bash

MAIN_CLASS="hk.ust.felab.rase.Branch"

CLASSPATH="$(dirname $0)/../conf"
for i in $(dirname $0)/../lib/*
do
    CLASSPATH="${CLASSPATH}:${i}"
done

mkdir -p "$(dirname $0)/../logs"
nohup java -Xdebug -Xrunjdwp:transport=dt_socket,address=5568,server=y,suspend=n \
-cp "${CLASSPATH}" -Dlog.dir="$(dirname $0)/../logs" \
"${MAIN_CLASS}" "$@" 2>&1 1>> "$(dirname $0)/../logs/app.log" &
