ARG base_img
FROM $base_img

USER root

COPY --from=c12e/scuttle:latest-main /scuttle /bin/scuttle
ENV ENVOY_ADMIN_API=http://localhost:15000
ENV ISTIO_QUIT_API=http://localhost:15020

COPY src/ src/

RUN rm -f /usr/bin/python && ln -s /usr/bin/python3 /usr/bin/python

RUN pip3 install -r src/main/python/requirements.txt

RUN apt-get update -y && apt-get install -y wget

RUN wget https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/3.3.1/hadoop-aws-3.3.1.jar -P ${SPARK_HOME}/jars/ \
 && wget https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.11.901/aws-java-sdk-bundle-1.11.901.jar -P ${SPARK_HOME}/jars/ \
 && wget https://repo1.maven.org/maven2/io/delta/delta-core_2.12/1.1.0/delta-core_2.12-1.1.0.jar -P ${SPARK_HOME}/jars/

ENTRYPOINT [ "scuttle", "/opt/entrypoint.sh" ]

USER ${spark_uid}