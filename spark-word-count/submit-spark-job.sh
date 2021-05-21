#!/bin/bash

echo ${SPARK_HOME}

# Local
${SPARK_HOME}/bin/spark-submit --class SparkWordCount --master local local:///${SPARK_HOME}/work-dir/target/spark-word-count-1.0-SNAPSHOT.jar s3a://test-mb/test/bible.txt

# K8s
# ${SPARK_HOME}/bin/spark-submit --master k8s://https://192.168.64.15:8443 --deploy-mode cluster --name spark-word-count --class SparkWordCount --conf spark.executor.instances=2 --conf spark.kubernetes.container.image=svangapallycs/spark-word-count:latest --conf spark.kubernetes.authenticate.submission.caCertFile=selfsigned_certificate.pem local:///${SPARK_HOME}/work-dir/target/spark-word-count-1.0-SNAPSHOT.jar s3a://test-mb/test/bible.txt 
