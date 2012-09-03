#!/bin/bash

if [ $# -ne 3 ]; then
    echo "usage: $0 version altsConf trialCount"
    exit 1
fi

version=$1
m2Dir=/home/ielm/m2/hk/ust/felab/rase/$version
m2Dist=rase-$version-bin.zip

raseRoot=/home/ielm/rase
raseLog=/home/ielm/rase_log

altsConf=$(pwd)/$2
if [ ! -f $altsConf ]; then
    altsConf=$2
fi

if [ -d $raseLog/$(basename $altsConf) ]; then
    echo "log exists, exit..."
    exit 1
fi

min=true
alpha=0.05
delta=1
n0=20
fix=true
trialCount=$3;

masterHost=192.168.1.1
masterPort=5567

slaveIdOffset=0
#slaveSampleGenerator=DelayedNormal
slaveSampleGenerator=Normal
slaveSampleCountStep=1

rm -rf agents.conf
cat <<- AGENTS > agents.conf
felab-1 40
felab-2 40
felab-3 4
felab-4 4
felab-5 4
felab-6 4
felab-7 4
felab-8 4
felab-9 4
AGENTS

function foreach_ssh()
{
    for i in $(seq 1 9)
    do
        ssh felab-$i $*
        if [ $? -eq 0 ]; then
            echo "ssh felab-$i $* done."
        else
            echo "ssh felab-$i $* fail."
        fi
    done
}

function reverse_foreach_ssh()
{
    for i in $(seq 9 -1 1)
    do
        ssh felab-$i $*
        if [ $? -eq 0 ]; then
            echo "ssh felab-$i $* done."
        else
            echo "ssh felab-$i $* fail."
        fi
    done
}

function foreach_async_ssh()
{
    for i in $(seq 1 9)
    do
        ssh felab-$i $* &
        if [ $? -eq 0 ]; then
            echo "ssh felab-$i $* done."
        else
            echo "ssh felab-$i $* fail."
        fi
    done
}

function foreach_scp()
{
    for i in $(seq 1 9)
    do
        scp $1 felab-$i:$2
        if [ $? -eq 0 ]; then
            echo "scp $1 felab-$i:$2 done."
        else
            echo "scp $1 felab-$i:$2 fail."
        fi
    done
}

function collect_log()
{
    for i in $(seq 2 9)
    do
        scp -r felab-$i:$1 $2
        if [ $? -eq 0 ]; then
            echo "scp felab-$i:$1 $2 done."
        else
            echo "scp felab-$i:$1 $2 fail."
        fi
    done
}

#set -x

slaveTotalCount=0;
while read agentHost slaveLocalCount
do
    slaveTotalCount=$(($slaveTotalCount+$slaveLocalCount))
done < agents.conf

reverse_foreach_ssh pkill java
foreach_ssh mkdir -p $raseRoot
foreach_ssh rm -rf $raseRoot/*
foreach_scp ${m2Dir}/${m2Dist} $raseRoot/
foreach_ssh unzip "$raseRoot/${m2Dist} > /dev/null"
for trialId in $(seq 0 $(($trialCount-1))); do
    foreach_async_ssh ${raseRoot}/bin/run.sh > /dev/null
    sleep 5

    args="-F masterAltBufSize=$((${slaveTotalCount}*32))"
    args=${args}" -F masterSampleBufSize=$((${slaveTotalCount}*128))"
    args=${args}" -F min=$min"
    args=${args}" -F alpha=$alpha"
    args=${args}" -F delta=$delta"
    args=${args}" -F n0=$n0"
    args=${args}" -F fix=$fix"
    args=${args}" -F altsConf=@$altsConf"
    curl $args http://$masterHost:$masterPort/activateMaster

    slaveIdOffset=0;
    while read agentHost slaveLocalCount
    do
        echo $agentHost
        args="trialId=$trialId"
        args=${args}"&masterHost=$masterHost"
        args=${args}"&masterPort=$masterPort"
        args=${args}"&agentAltBufSize=$((${slaveLocalCount}*16))"
        args=${args}"&agentSampleBufSize=$((${slaveLocalCount}*64))"
        args=${args}"&slaveIdOffset=$slaveIdOffset"
        args=${args}"&slaveLocalCount=$slaveLocalCount"
        args=${args}"&slaveTotalCount=$slaveTotalCount"
        args=${args}"&slaveSampleGenerator=$slaveSampleGenerator"
        args=${args}"&slaveSampleCountStep=$slaveSampleCountStep"
        curl -d ${args} http://$agentHost:$masterPort/activateAgent
        slaveIdOffset=$(($slaveIdOffset+$slaveLocalCount))
    done < agents.conf

    #set +x
    result=$(curl http://$masterHost:$masterPort/rasResult 2>/dev/null)
    while [ $result -lt 0 ]; do
        sleep 1;
        result=$(curl http://$masterHost:$masterPort/rasResult 2>/dev/null)
    done
    echo $trialId", "$result
    #set -x

    reverse_foreach_ssh pkill java

    mkdir -p $raseLog/$(basename $altsConf)/$trialId/
    collect_log $raseRoot/log $raseLog/$(basename $altsConf)/$trialId/
    mv $raseLog/$(basename $altsConf)/$trialId/log/* $raseLog/$(basename $altsConf)/$trialId/
    rmdir $raseLog/$(basename $altsConf)/$trialId/log
    mv $raseRoot/log/* $raseLog/$(basename $altsConf)/$trialId/
done

rm -rf agents.conf
