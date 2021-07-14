#!/bin/bash

spark_version=3.0.2
hadoop_version=3.2

cd spark-base

curl https://archive.apache.org/dist/spark/spark-${spark_version}/spark-${spark_version}-bin-hadoop${hadoop_version}.tgz -o spark.tgz

tar -xf spark.tgz && cd spark-${spark_version}-bin-hadoop${hadoop_version}

docker build -t spark-base -f kubernetes/dockerfiles/spark/Dockerfile .

docker build --build-arg base_img=spark-base:latest -t spark-base-python -f kubernetes/dockerfiles/spark/bindings/python/Dockerfile .