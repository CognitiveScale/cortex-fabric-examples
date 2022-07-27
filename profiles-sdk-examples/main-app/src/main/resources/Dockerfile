ARG base_img
FROM $base_img

USER root

COPY --from=redboxoss/scuttle:latest /scuttle /bin/scuttle
COPY ./resources/main/python/ .
RUN pip3 install -r requirements.txt

# Include the application in Spark Jars, so app source is in the classpath of these jars, otherwise config-based
# implementation injection will raise a ClassNotFoundException for modules in the app.
COPY ./libs/main-app-1.0.0-SNAPSHOT.jar /opt/spark/jars

# Copy the Application Jar
COPY ./libs/main-app-1.0.0-SNAPSHOT.jar /app/libs/app.jar
COPY ./resources/main/lib/*.jar /opt/spark/jars/

# Copy Spark Configurations files
COPY ./resources/main/spark-conf /opt/spark/conf
COPY ./resources/main/conf /app/conf

ENTRYPOINT [ "scuttle", "/opt/entrypoint.sh"]
USER ${spark_uid}
