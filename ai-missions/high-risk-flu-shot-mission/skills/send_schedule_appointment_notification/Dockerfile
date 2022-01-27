FROM python:3.7

ADD requirements.txt requirements.txt

RUN pip install -r requirements.txt

ADD . /code

WORKDIR /code

ENTRYPOINT ["python3", "src/daemon.py"]
