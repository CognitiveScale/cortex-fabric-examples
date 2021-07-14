ARG base_img
FROM $base_img

USER root

COPY --from=redboxoss/scuttle:latest /scuttle /bin/scuttle
ENV ENVOY_ADMIN_API=http://localhost:15000
ENV ISTIO_QUIT_API=http://localhost:15020

COPY src/ src/

RUN rm -f /usr/bin/python && ln -s /usr/bin/python3 /usr/bin/python

RUN pip3 install -r src/main/python/requirements.txt

RUN apt-get update -y && apt-get install -y wget

RUN wget https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/3.2.0/hadoop-aws-3.2.0.jar -P ${SPARK_HOME}/jars/ \
 && wget https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.11.874/aws-java-sdk-bundle-1.11.874.jar -P ${SPARK_HOME}/jars/ \
 && wget https://repo1.maven.org/maven2/org/mongodb/spark/mongo-spark-connector_2.12/3.0.0/mongo-spark-connector_2.12-3.0.0.jar -P ${SPARK_HOME}/jars/ \
 && wget https://repo1.maven.org/maven2/org/mongodb/bson/4.1.1/bson-4.1.1.jar -P ${SPARK_HOME}/jars/ \
 && wget https://repo1.maven.org/maven2/org/mongodb/mongodb-driver-core/4.1.1/mongodb-driver-core-4.1.1.jar -P ${SPARK_HOME}/jars/ \
 && wget https://repo1.maven.org/maven2/org/mongodb/mongodb-driver-sync/4.1.1/mongodb-driver-sync-4.1.1.jar -P ${SPARK_HOME}/jars/

ENTRYPOINT [ "scuttle", "/opt/entrypoint.sh" ]

USER ${spark_uid}