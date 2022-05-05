ARG base_img
FROM $base_img

USER root

COPY --from=c12e/scuttle:latest-main /scuttle /bin/scuttle
ENV ENVOY_ADMIN_API=http://localhost:15000
ENV ISTIO_QUIT_API=http://localhost:15020

COPY src/ src/
COPY submit_job.py submit_job.py

RUN rm -f /usr/bin/python && ln -s /usr/bin/python3 /usr/bin/python

RUN pip3 install -r src/main/python/requirements.txt

ENTRYPOINT ["scuttle", "python3", "submit_job.py"]

USER ${spark_uid}