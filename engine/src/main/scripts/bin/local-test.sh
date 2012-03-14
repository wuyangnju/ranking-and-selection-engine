#!/bin/bash

if [ $# -ne 2 ]; then
    echo "usage: $0 version conf"
    exit 1
fi

masterHost=localhost
masterPort=5567

curl -F slaveCount=1 -F rasConf=@$2 http://$masterHost:$masterPort/activateMaster

curl -d "masterHost=$masterHost&masterPort=$masterPort&slaveIdOffset=0&localSlaveCount=1" http://$masterHost:$masterPort/activateAgent

nohup sh $(dirname $0)/local-monitor.sh $masterHost $masterPort 2>&1 1>>$(date +%Y-%m-%d-%H-%M-%S).log &
