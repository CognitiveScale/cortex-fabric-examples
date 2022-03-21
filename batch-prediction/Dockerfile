FROM python:3.9-slim

WORKDIR /app

ADD . /app

RUN apt-get update \
    && apt-get upgrade -y \
    && pip install --upgrade -r requirements.txt \
    && apt-get clean

COPY --from=redboxoss/scuttle:latest /scuttle /bin/scuttle
ENV ENVOY_ADMIN_API=http://localhost:15000
ENV ISTIO_QUIT_API=http://localhost:15020

ENTRYPOINT ["scuttle", "python3", "predict/main.py"]
