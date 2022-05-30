FROM c12e/cortex-amp-spark-submit:6.11.141-g53e0e403-ubi8

USER root

COPY src/ src/ 
     
COPY submit_job.py submit_job.py 
COPY config.json config.json 
COPY driverTemplate.yaml driverTemplate.yaml 

RUN pip install -r src/main/python/requirements.txt

ENV JAVA_HOME /usr/local/openjdk-11
ENV SPARK_HOME /opt/spark
ENV ENVOY_ADMIN_API=http://localhost:15000
ENV ISTIO_QUIT_API=http://localhost:15020

RUN apt-get update -y \
 && apt-get install -y wget \
 && wget https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/3.3.1/hadoop-aws-3.3.1.jar -P ${SPARK_HOME}/jars/ \
 && wget https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.11.901/aws-java-sdk-bundle-1.11.901.jar -P ${SPARK_HOME}/jars/ \
 && wget https://repo1.maven.org/maven2/io/delta/delta-core_2.12/1.1.0/delta-core_2.12-1.1.0.jar -P ${SPARK_HOME}/jars/ \
 && apt-get purge wget -y \
 && apt-get clean

WORKDIR /opt/spark/work-dir
RUN chmod g+w /opt/spark/work-dir
RUN chmod a+x /opt/decom.sh

ENTRYPOINT ["scuttle", "python3", "submit_job.py", "config.json", "driverTemplate.yaml"]

USER ${spark_uid}