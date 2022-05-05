FROM amazoncorretto:8

RUN yum -y update
RUN yum -y install yum-utils
RUN yum -y groupinstall development

RUN yum list python3*
RUN yum -y install python3 python3-dev python3-pip python3-virtualenv

RUN python -V
RUN python3 -V

ENV PYSPARK_DRIVER_PYTHON python3
ENV PYSPARK_PYTHON python3
ENV CORTEX_URI=${CORTEX_URI}

COPY emr-container-image/src/ src/
RUN rm -f /usr/bin/python && ln -s /usr/bin/python3 /usr/bin/python

RUN pip3 install --upgrade pip
RUN pip3 install -r src/requirements.txt
