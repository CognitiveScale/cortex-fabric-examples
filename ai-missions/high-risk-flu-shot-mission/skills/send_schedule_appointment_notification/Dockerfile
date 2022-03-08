FROM python:3.10.0b2

ADD requirements.txt requirements.txt

RUN pip install -r requirements.txt

ADD . /code

WORKDIR /code

ENTRYPOINT ["python3", "src/daemon.py"]
