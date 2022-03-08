FROM python:3.9.10-buster

WORKDIR /app

ADD . /app

RUN pip install -r /app/requirements.txt

COPY --from=redboxoss/scuttle:latest /scuttle /bin/scuttle
ENV ENVOY_ADMIN_API=http://localhost:15000
ENV ISTIO_QUIT_API=http://localhost:15020

ENTRYPOINT ["scuttle", "python3", "predict/main.py"]
