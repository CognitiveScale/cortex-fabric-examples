#!/bin/bash
echo ${SPARK_HOME}

input_params=$1

# Local
#${SPARK_HOME}/bin/spark-submit --class SparkWordCount --master local target/spark-word-count-1.0-SNAPSHOT.jar s3a://test-mb/test/bible.txt $input_params

# K8s
${SPARK_HOME}/bin/spark-submit --master k8s://<master-ip>:<port> --deploy-mode cluster --name spark-word-count --class SparkWordCount --conf spark.executor.instances=2 --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark --conf spark.kubernetes.container.image=svangapallycs/spark-container:BZUl6y local:///${SPARK_HOME}/work-dir/target/spark-word-count-1.0-SNAPSHOT.jar s3a://test-mb/test/bible.txt $input_params