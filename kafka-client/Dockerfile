FROM bitnami/kafka
WORKDIR /app
USER root
COPY *.py requirements.txt /app/ 
RUN apt update\
&& apt install -y wget bzip2\
# Debian python libraries are typical old and dynamically linked..
&& wget --quiet https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh  \
&& /bin/bash Miniconda3-latest-Linux-x86_64.sh -b -f -p /opt/anaconda \
&& export PATH=$PATH:/opt/anaconda/bin \
&& conda install -c conda-forge python-confluent-kafka pip \
&& pip install cuid
ENV PATH=/opt/anaconda/bin:/opt/bitnami/kafka/bin:$PATH
CMD ["tail", "-f", "/dev/null"]
# CMD [“/app/venv/bin/python”, main.py”]
