 1) git clone https://github.com/yungsters/rain-workload-toolkit.git rain.git
 2) cd rain.git
 3) ant package-with-mr
 4) ant package-mapreduce
 5) Download hadoop-0.20.2.tar.gz
 6) copy into rain.git/thirdparty
 7) cd into rain.git/thirdparty
 8) mv hadoop-0.20.2/conf hadoop-0.20.2/conf.example
 9) tar -xzvf hadoop-0.20.2.tar.gz
10) cd hadoop-0.20.2
11) cp conf.example/* conf/

Modify conf/hadoop-env.sh such that the Rain mapreduce classes/jars are
in Hadoop's classpath
12) export HADOOP_CLASSPATH=.:<path-to-rain checkout>/rain.git/workloads/mapreduce.jar
e.g., export HADOOP_CLASSPATH=.:/home/rean/work/rain.git/workloads/mapreduce.jar

For single-node testing setup passphraseless ssh:
13) ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa
14) cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys
15) bin/hadoop namenode -format
16) bin/start-all.sh

Load some data into HDFS
17) cd ../..
18) java -cp .:rain.jar:workloads/mapreduce.jar radlab.rain.workload.mapreduce.HdfsLoader hdfs://localhost:9000/user/rean/input_test 5368709120 hdfs://localhost:9000 localhost:9001
19) Edit config/profiles.config.mapreduce.json to set the parameters for the workload generator. Specifically: hdfs input path, hdfs output path, job tracker, total bytes in hdfs (from step #18), trace file to replay,
any limits on map output or max hdfs bytes
20) Edit the run duration in config/rain.config.mapreduce.json
21) ant Benchmark-mapreduce
