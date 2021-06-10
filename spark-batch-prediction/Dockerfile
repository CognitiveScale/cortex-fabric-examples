FROM c12e/spark-template:spark-container-BiyRw4

USER root

COPY --from=redboxoss/scuttle:latest /scuttle /bin/scuttle
ENV ENVOY_ADMIN_API=http://localhost:15000
ENV ISTIO_QUIT_API=http://localhost:15020

COPY src/ src/
COPY target/*.jar target/
COPY submit_job.py submit_job.py

RUN rm -f /usr/bin/python && ln -s /usr/bin/python3 /usr/bin/python

RUN pip3 install -r src/main/python/requirements.txt

ENTRYPOINT ["scuttle", "python3", "submit_job.py"]

USER ${spark_uid}