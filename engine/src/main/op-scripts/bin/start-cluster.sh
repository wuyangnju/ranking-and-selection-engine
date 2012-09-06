#!/bin/bash

if [ $# -ne 4 ]; then
    echo "usage: $0 version rasConf altsConf log4jConf"
    exit 1
fi

raseRoot=/home/ielm/rase
logDir=$raseRoot/logs

allLogDir=/home/ielm/rase_log

version=$1
m2Dir=/home/ielm/m2/hk/ust/felab/rase/$version
m2Dist=rase-$version-bin.zip

rasConf=$(pwd)/$2
if [ ! -f $rasConf ]; then
    rasConf=$2
fi

. $rasConf

altsConf=$(pwd)/$3
if [ ! -f $altsConf ]; then
    altsConf=$3
fi

log4jConf=$(pwd)/$4
if [ ! -f $log4jConf ]; then
    log4jConf=$4
fi

masterHost=192.168.1.1
masterPort=5567

rm -rf agents.conf
cat <<- AGENTS > agents.conf
felab-1 44
felab-2 44
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
    echo
    for i in $(seq $1)
    do
        ssh felab-$i $2
        if [ $? -ne 0 ]; then
            echo "ssh felab-$i $2 fail."
        fi
    done
    echo "ssh felab-x $2 done."
}

function foreach_scp()
{
    echo
    for i in $(seq $1)
    do
        scp -rq $2 felab-$i:$3
        if [ $? -ne 0 ]; then
            echo "scp $2 felab-$i:$3 fail."
        fi
    done
    echo "scp $2 felab-x:$3 done."
}

function foreach_scp_back()
{
    echo
    for i in $(seq $1)
    do
        scp -rq felab-$i:$2 $3
        if [ $? -ne 0 ]; then
            echo "scp felab-$i:$2 $3 fail."
        fi
    done
    echo "scp felab-x:$2 $3 done."
}

#set -x

slaveTotalCount=0;
while read agentHost slaveLocalCount
do
    slaveTotalCount=$(($slaveTotalCount+$slaveLocalCount))
done < agents.conf

foreach_ssh "9 -1 1" "pkill java ; rm -rf $raseRoot && mkdir -p $raseRoot/conf"
foreach_scp "1 9" "${m2Dir}/${m2Dist}" "$raseRoot/"
foreach_scp "1 9" "$log4jConf" "$raseRoot/conf/log4j.properties"
foreach_ssh "1 9" "unzip $raseRoot/${m2Dist} > /dev/null"
for trialId in $(seq 0 $(($trialCount-1))); do
    foreach_ssh "1 9" "${raseRoot}/bin/run.sh > /dev/null"
    sleep 5

    args="-F masterAltBufSize=$((${slaveTotalCount}*32))"
    args=${args}" -F masterSampleBufSize=$((${slaveTotalCount}*128))"
    args=${args}" -F min=$min"
    args=${args}" -F alpha=$alpha"
    args=${args}" -F delta=$delta"
    args=${args}" -F n0=$n0"
    args=${args}" -F fix=$fix"
    args=${args}" -F altsConf=@$altsConf"
    echo
    curl $args http://$masterHost:$masterPort/activateMaster

    slaveIdOffset=0;
    while read agentHost slaveLocalCount
    do
        args="trialId=$trialId"
        args=${args}"&masterHost=$masterHost"
        args=${args}"&masterPort=$masterPort"
        args=${args}"&agentAltBufSize=$((${slaveLocalCount}*16))"
        args=${args}"&agentSampleBufSize=$((${slaveLocalCount}*64))"
        args=${args}"&slaveIdOffset=$slaveIdOffset"
        args=${args}"&slaveLocalCount=$slaveLocalCount"
        args=${args}"&slaveTotalCount=$slaveTotalCount"
        args=${args}"&sampleGenerator=$sampleGenerator"
        args=${args}"&sampleCountStep=$sampleCountStep"
        echo -ne "\n$agentHost - "
        curl -d ${args} http://$agentHost:$masterPort/activateAgent
        slaveIdOffset=$(($slaveIdOffset+$slaveLocalCount))
    done < agents.conf

    #set +x
    result=$(curl http://$masterHost:$masterPort/rasResult 2>/dev/null)
    while [ $result -lt 0 ]; do
        sleep 1;
        result=$(curl http://$masterHost:$masterPort/rasResult 2>/dev/null)
    done
    echo -e "\n$trialId, $result"
    #set -x

    foreach_ssh "9 -1 1" "pkill java"

    trialLogDir=$allLogDir/$(basename $altsConf)_$(basename $rasConf)_$trialId

    mkdir -p $trialLogDir
    foreach_scp_back "9 -1 1" "$logDir" "$trialLogDir"
    mv $trialLogDir/logs/* $trialLogDir
    rmdir $trialLogDir/logs 
done

rm -rf agents.conf
