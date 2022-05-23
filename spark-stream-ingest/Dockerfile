FROM c12e/cortex-amp-spark-submit:latest-develop-ubi8

USER root

COPY src/ src/ 
     
COPY submit_job.py submit_job.py 
COPY config.json config.json 
COPY driverTemplate.yaml driverTemplate.yaml 

RUN pip install -r requirements.txt

ENV JAVA_HOME /usr/local/openjdk-11
ENV SPARK_HOME /opt/spark
ENV ENVOY_ADMIN_API=http://localhost:15000
ENV ISTIO_QUIT_API=http://localhost:15020

WORKDIR /opt/spark/work-dir
RUN chmod g+w /opt/spark/work-dir
RUN chmod a+x /opt/decom.sh

ENTRYPOINT ["scuttle", "python3", "submit_job.py", "config.json", "driverTemplate.yaml"]

USER ${spark_uid}