#!/usr/bin/env bash

#----- Main Class for spark job to execute
MAIN_CLASS=com.oculusinfo.tilegen.pipeline.examples.GeoHeatmapPipelineApp


#----- Path and name of Main JAR
MAIN_JAR=../lib/tile-generation-assembly.jar

#----- Set Spark Master URL
JOB_MASTER=yarn-client
#JOB_MASTER=local
#JOB_MASTER=spark://hadoop-s1.oculus.local:7077

spark-submit \
    --num-executors 12 \
    --executor-memory 20g \
    --executor-cores 4 \
    --conf spark.executor.extraClassPath=/opt/cloudera/parcels/CDH/lib/hbase/lib/htrace-core-3.1.0-incubating.jar \
    --driver-class-path /opt/cloudera/parcels/CDH/lib/hbase/lib/htrace-core-3.1.0-incubating.jar \
    --jars /opt/cloudera/parcels/CDH/lib/hbase/lib/htrace-core-3.1.0-incubating.jar \
    --master ${JOB_MASTER} \
    --class ${MAIN_CLASS} ${MAIN_JAR} \
    -columnMap ./crossplot_columns.properties \
    -start 2015/01/01.00:00:00.+0000 \
    -end 2015/08/01.00:00:00.+0000 \
    -levels '0,1,2,3,4,5,6,7,8,9' \
    -name heatmapTimeDebugV1 \
    -description heatmapTimeDebug \
    -partitions 200 \
    -source 'hdfs://some.server.here/path/to/data' \
    -io hbase \
    -zookeeperquorum some.server.here \
    -zookeeperport 2181 \
    -hbasemaster some.server.here:60000
