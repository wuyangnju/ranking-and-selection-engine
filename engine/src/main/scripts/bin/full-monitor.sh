#!/bin/bash

if [ $# -ne 3 ]; then
    echo "usage: $0 masterHost port frequency"
    exit 1
fi

while [ true ]; do
    date "+%Y-%m-%d %H:%M:%S"
    echo
    curl http://$1:$2/rasStatus 2>/dev/null | tr ']' '\n' | sed '$d' | awk '{print $0} END {printf("survival count: %d", NR)}'
    echo
    sleep $3
done
