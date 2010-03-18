#!/bin/bash

clusterSizeRaw=$1
clusterSizeWorkload=$2
inputPartitionSize=$3
inputPartitionCount=$4
scriptDirPath=$5
hdfsInputDir=$6
totalDataPerReduce=$7

javac GenerateWorkloadScript.java

mkdir $scriptDirPath

for ((  i = 0 ;  i < 1;  i++  ))
do

#################

#echo 'hr-'$i' started'

#jobCountsTemp="$(wc -l ../workload/jobName-hr-$i.tsv)"
#jobCountsTempArray=( $jobCountsTemp )
#jobCounts="${jobCountsTempArray[0]}"

#rm -r $scriptDirPath/scripts-hr-$i
#mkdir $scriptDirPath/scripts-hr-$i

#java GenerateWorkloadScript ../workload/interJobArrival-hr-$i.tsv ../workload/jobName-hr-$i.tsv ../workload/input-hr-$i.tsv ../workload/shuffle-hr-$i.tsv ../workload/output-hr-$i.tsv $clusterSizeRaw $clusterSizeWorkload $inputPartitionSize $inputPartitionCount $scriptDirPath/scripts-hr-$i $hdfsInputDir > temp.txt

#grep '# pipe to run-all-jobs.sh' temp.txt > $scriptDirPath/scripts-hr-$i/run-all-jobs.sh

#for (( j = 0 ;  j < "$jobCounts";  j++  ))
#do

#grep '# pipe to run-job'$j'.sh' temp.txt > $scriptDirPath/scripts-hr-$i/run-job$j.sh

#done

#echo 'hr-'$i' done'


#################

echo 'day-'$i' started'

jobCountsTemp="$(wc -l ../workload/jobName-day-$i.tsv)"
jobCountsTempArray=( $jobCountsTemp )
jobCounts="${jobCountsTempArray[0]}"

rm -r $scriptDirPath/scripts-day-$i
mkdir $scriptDirPath/scripts-day-$i

java GenerateWorkloadScript ../workload/interJobArrival-day-$i.tsv ../workload/jobName-day-$i.tsv ../workload/input-day-$i.tsv ../workload/shuffle-day-$i.tsv ../workload/output-day-$i.tsv $clusterSizeRaw $clusterSizeWorkload $inputPartitionSize $inputPartitionCount $scriptDirPath/scripts-day-$i $hdfsInputDir $totalDataPerReduce

#grep '# pipe to run-all-jobs.sh' temp.txt > $scriptDirPath/scripts-day-$i/run-all-jobs.sh

#for (( j = 0 ;  j < "$jobCounts";  j++  ))
#do

#grep '# pipe to run-job'$j'.sh' temp.txt > $scriptDirPath/scripts-day-$i/run-job$j.sh

#done

echo 'day-'$i' done'

#mv run-job*.sh $scriptDirPath/scripts-day-$i

chmod +x $scriptDirPath/scripts-day-$i/*

#################

#echo 'week-'$i' started'

#jobCountsTemp="$(wc -l ../workload/jobName-week-$i.tsv)"
#jobCountsTempArray=( $jobCountsTemp )
#jobCounts="${jobCountsTempArray[0]}"

#rm -r $scriptDirPath/scripts-week-$i
#mkdir $scriptDirPath/scripts-week-$i

#java GenerateWorkloadScript ../workload/interJobArrival-week-$i.tsv ../workload/jobName-week-$i.tsv ../workload/input-week-$i.tsv ../workload/shuffle-week-$i.tsv ../workload/output-week-$i.tsv $clusterSizeRaw $clusterSizeWorkload $inputPartitionSize $inputPartitionCount $scriptDirPath/scripts-week-$i $hdfsInputDir > temp.txt

#grep '# pipe to run-all-jobs.sh' temp.txt > $scriptDirPath/scripts-week-$i/run-all-jobs.sh

#for (( j = 0 ;  j < "$jobCounts";  j++  ))
#do

#grep '# pipe to run-job'$j'.sh' temp.txt > $scriptDirPath/scripts-week-$i/run-job$j.sh

#done

#echo 'week-'$i' done'


done