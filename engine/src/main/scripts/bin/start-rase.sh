#!/bin/bash

if [ $# -ne 2 ]; then
    echo "usage: $0 version conf"
    exit 1
fi

conf=$2

version=$1
m2Dir=/home/ielm/m2/hk/ust/felab/rase/$version
m2Dist=rase-$version-bin.zip

raseRoot=/home/ielm/rase
raseLog=/home/ielm/rase_log

masterHost=192.168.1.1
masterPort=5567

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

function foreach_ssh2()
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

foreach_ssh pkill java
foreach_ssh mkdir -p $raseRoot
foreach_ssh rm -rf $raseRoot/*
foreach_scp ${m2Dir}/${m2Dist} $raseRoot/
foreach_ssh unzip $raseRoot/${m2Dist}
foreach_ssh2 ${raseRoot}/bin/run.sh

cat <<- AGENTS > agents.conf
felab-1 48
felab-2 48
felab-3 4
felab-4 4
felab-5 4
felab-6 4
felab-7 4
felab-8 4
felab-9 4
AGENTS

set -x
sleep 5

slaveCount=0;
while read agentHost localSlaveCount
do
    slaveCount=$(($slaveCount+$localSlaveCount))
done < agents.conf
curl -F slaveCount=$slaveCount -F rasConf=@$conf http://$masterHost:$masterPort/activateMaster

mkdir -p $(dirname $0)/../log
nohup sh $(dirname $0)/compact-monitor.sh $masterHost $masterPort 10 2>&1 1>>$(dirname $0)/../log/$(date +%Y-%m-%d-%H-%M-%S_compact).log &
sleep 1
nohup sh $(dirname $0)/full-monitor.sh $masterHost $masterPort 600 2>&1 1>>$(dirname $0)/../log/$(date +%Y-%m-%d-%H-%M-%S_full).log &
sleep 1
nohup sh $(dirname $0)/deleted-monitor.sh $masterHost $masterPort 1800 2>&1 1>>$(dirname $0)/../log/$(date +%Y-%m-%d-%H-%M-%S_deleted).log &

slaveIdOffset=0;
while read agentHost localSlaveCount
do
    echo $agentHost
    curl -d "masterHost=$masterHost&masterPort=$masterPort&slaveIdOffset=$slaveIdOffset&localSlaveCount=$localSlaveCount" http://$agentHost:$masterPort/activateAgent
    slaveIdOffset=$(($slaveIdOffset+$localSlaveCount))
done < agents.conf

rm -rf agents.conf
