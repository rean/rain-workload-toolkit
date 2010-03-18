#!/bin/bash

for ((  i = 0 ;  i < 5;  i++  ))
do

java SampleJobData ../workload/jobName-hr-$i.tsv ../processedData/inputSizes.tsv > ../workload/input-hr-$i.tsv
java SampleJobData ../workload/jobName-hr-$i.tsv ../processedData/shuffleSizes.tsv > ../workload/shuffle-hr-$i.tsv
java SampleJobData ../workload/jobName-hr-$i.tsv ../processedData/outputSizes.tsv > ../workload/output-hr-$i.tsv

java SampleJobData ../workload/jobName-day-$i.tsv ../processedData/inputSizes.tsv > ../workload/input-day-$i.tsv
java SampleJobData ../workload/jobName-day-$i.tsv ../processedData/shuffleSizes.tsv > ../workload/shuffle-day-$i.tsv
java SampleJobData ../workload/jobName-day-$i.tsv ../processedData/outputSizes.tsv > ../workload/output-day-$i.tsv

java SampleJobData ../workload/jobName-week-$i.tsv ../processedData/inputSizes.tsv > ../workload/input-week-$i.tsv
java SampleJobData ../workload/jobName-week-$i.tsv ../processedData/shuffleSizes.tsv > ../workload/shuffle-week-$i.tsv
java SampleJobData ../workload/jobName-week-$i.tsv ../processedData/outputSizes.tsv > ../workload/output-week-$i.tsv

echo $i

done