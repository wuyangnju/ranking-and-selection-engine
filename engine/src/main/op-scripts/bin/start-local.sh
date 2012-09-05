#!/bin/bash

if [ $# -ne 2 ]; then
    echo "usage: $0 rasConf altsConf"
    exit 1
fi

rasConf=$(pwd)/$1
if [ ! -f $rasConf ]; then
    rasConf=$1
fi

. $rasConf

altsConf=$(pwd)/$2
if [ ! -f $altsConf ]; then
    altsConf=$2
fi

masterHost=localhost
masterPort=5567
masterAltBufSize=256
masterSampleBufSize=1024

agentAltBufSize=128
agentSampleBufSize=1024

slaveIdOffset=0
slaveLocalCount=4
slaveTotalCount=4

cd $(dirname $0)/../../../..

pkill java
for trialId in $(seq 0 $(($trialCount-1))); do
    mvn clean \
-D log4j.configuration="$(dirname $0)/../conf/log4j/log4j-full.properties" \
-D jetty.port=$masterPort jetty:run 2>&1 &
    sleep 10

    args="-F masterAltBufSize=$masterAltBufSize"
    args=${args}" -F masterSampleBufSize=$masterSampleBufSize"
    args=${args}" -F min=$min"
    args=${args}" -F alpha=$alpha"
    args=${args}" -F delta=$delta"
    args=${args}" -F n0=$n0"
    args=${args}" -F fix=$fix"
    args=${args}" -F altsConf=@$altsConf"
    curl $args http://$masterHost:$masterPort/activateMaster

    args="trialId=$trialId"
    args=${args}"&masterHost=$masterHost"
    args=${args}"&masterPort=$masterPort"
    args=${args}"&agentAltBufSize=$agentAltBufSize"
    args=${args}"&agentSampleBufSize=$agentSampleBufSize"
    args=${args}"&slaveIdOffset=$slaveIdOffset"
    args=${args}"&slaveLocalCount=$slaveLocalCount"
    args=${args}"&slaveTotalCount=$slaveTotalCount"
    args=${args}"&sampleGenerator=$sampleGenerator"
    args=${args}"&sampleCountStep=$sampleCountStep"
    curl -d ${args} http://$masterHost:$masterPort/activateAgent

    result=$(curl http://$masterHost:$masterPort/rasResult)
    while [ $result -lt 0 ]; do
        sleep 1;
        result=$(curl http://$masterHost:$masterPort/rasResult)
    done
    echo $trialId", "$result
    pkill java

    mv rase/log rase/log_$trialId
done
