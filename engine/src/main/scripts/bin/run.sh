#!/bin/bash

MAIN_CLASS="hk.ust.felab.rase.common.EmbededJettyRunner"
PORT=5567
CONTEXT_PATH=/
LOG_FILE=app.log

CLASSPATH="$(dirname $0)/../classes"
for i in $(dirname $0)/../lib/*
do
    CLASSPATH="${CLASSPATH}:${i}"
done

mkdir -p "$(dirname $0)/../log"
nohup java -cp "${CLASSPATH}" "${MAIN_CLASS}" -p "${PORT}" -c "${CONTEXT_PATH}" -w $(dirname $0)/../war "$@" 2>&1 1>> $(dirname $0)/../log/${LOG_FILE} &
