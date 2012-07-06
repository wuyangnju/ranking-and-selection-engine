#!/bin/bash

if [ $# -ne 3 ]; then
    echo "usage: $0 masterHost port frequency"
    exit 1
fi

while [ true ]; do
    echo -ne $(date "+%Y-%m-%d %H:%M:%S")"\t"
    curl http://$1:$2/rasSurvivalCount 2>/dev/null
    echo
    sleep $3
done
