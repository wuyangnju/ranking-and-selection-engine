#!/bin/bash

if [ $# -ne 3 ]; then
    echo "usage: $0 conf slaveCount trialCount"
    exit 1
fi

trialCount=$3;

min=true
alpha=0.05
delta=1
n0=10
fix=false

altsConf=$(pwd)/$1

masterHost=localhost
masterPort=5567
masterAltBufSize=8
masterSampleBufSize=1024

agentAltBufSize=4
agentSampleBufSize=1024

slaveIdOffset=0
slaveLocalCount=$2
slaveTotalCount=$2
slaveSampleGenerator=Normal
slaveSampleCountStep=1

cd $(pwd)/../../../..

pkill java
for trialId in $(seq 0 $(($trialCount-1))); do
    mvn clean -D jetty.port=$masterPort jetty:run 2>&1 &
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
    args=${args}"&slaveSampleGenerator=$slaveSampleGenerator"
    args=${args}"&slaveSampleCountStep=$slaveSampleCountStep"
    curl -d ${args} http://$masterHost:$masterPort/activateAgent

    result=$(curl http://$masterHost:$masterPort/rasResult)
    while [ $result -lt 0 ]; do
        sleep 1;
        result=$(curl http://$masterHost:$masterPort/rasResult)
    done
    echo $trialId", "$result
    pkill java

done
