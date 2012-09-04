#!/bin/bash

awk -F ',' 'BEGIN{sum=0}{if(NR>1){sum+=$2-$1}}END{print sum}' master_perf1.csv
awk -F ',' 'BEGIN{sum=0;max=0}{for(i=1;i<=NF;i++){if($i>0){sum+=$i};if($i>max){max=$i}}}END{print sum,max}' master_sift.csv