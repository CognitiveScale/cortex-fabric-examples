FROM python:3.9-slim
COPY --from=c12e/scuttle:latest-main /scuttle /bin/scuttle
ENV ENVOY_ADMIN_API=http://localhost:15000
ENV ISTIO_QUIT_API=http://localhost:15020

RUN apt-get update \
    && apt-get upgrade -y

ADD . /app
RUN pip install --upgrade -r /app/requirements.txt \
    && apt-get clean

ENTRYPOINT ["scuttle", "python", "/app/train.py"]