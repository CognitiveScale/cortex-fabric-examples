#!/bin/bash

spark_version=3.0.0
hadoop_version=2.7

cd spark-base

curl https://archive.apache.org/dist/spark/spark-${spark_version}/spark-${spark_version}-bin-hadoop${hadoop_version}.tgz -o spark.tgz

tar -xf spark.tgz && cd spark-${spark_version}-bin-hadoop${hadoop_version}

docker build -t spark-base -f kubernetes/dockerfiles/spark/Dockerfile .