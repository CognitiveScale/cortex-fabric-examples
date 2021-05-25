FROM svangapallycs/spark-base:fLmLt0

USER root

COPY target/*.jar target/

RUN apt-get update -y && apt-get install -y wget && wget https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/2.7.0/hadoop-aws-2.7.0.jar -P ${SPARK_HOME}/jars/ && wget https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk/1.7.4/aws-java-sdk-1.7.4.jar -P ${SPARK_HOME}/jars/ && wget https://repo1.maven.org/maven2/com/googlecode/json-simple/json-simple/1.1/json-simple-1.1.jar -P ${SPARK_HOME}/jars/

USER ${spark_uid}
