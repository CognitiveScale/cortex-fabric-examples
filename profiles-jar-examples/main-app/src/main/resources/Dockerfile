ARG base_img
FROM $base_img

USER root

COPY ./resources/main/python/ .
RUN pip3 install -r requirements.txt

COPY ./libs/main-app-1.0.0-SNAPSHOT.jar /app/libs/app.jar
COPY ./resources/main/spark-conf /opt/spark/conf
COPY ./resources/main/lib/*.jar /opt/spark/jars/
COPY ./resources/main/conf /app/conf

ENTRYPOINT [ "/opt/entrypoint.sh" ]
USER ${spark_uid}
