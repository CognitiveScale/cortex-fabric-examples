FROM python:3.11.0a7-bullseye

ADD requirements.txt requirements.txt

RUN pip install --upgrade -r requirements.txt \
    && apt-get purge unzip -y \
    && apt-get clean

ADD . /code

WORKDIR /code

ENTRYPOINT ["python3", "src/daemon.py"]
