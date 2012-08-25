#!/bin/bash

if [ $# -ne 2 ]; then
    echo "usage: #ofAlt BestAlt"
    exit 1
fi

for i in $(seq 1 $((${2}-1))); do
    echo $i 0 1
done

echo $2 -1 1

for i in $(seq $((${2}+1)) $1); do
    echo $i 0 1
done
