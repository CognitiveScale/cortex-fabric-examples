FROM python:3.9-slim

WORKDIR /app/hadron
COPY requirements.txt /app/hadron

RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install git -y \
    && pip install --upgrade -r requirements.txt \
    && apt-get purge git -y \
    && apt-get clean

COPY . /app/hadron

RUN echo "hadron:x:1001:0:Default Application User:/app:/sbin/nologin" >> /etc/passwd && chown -R 1001 /app
USER hadron
COPY --from=c12e/scuttle:latest-main /scuttle /bin/scuttle
ENV ENVOY_ADMIN_API=http://localhost:15000
ENV ISTIO_QUIT_API=http://localhost:15020
ENV SCUTTLE_LOGGING=false


ENTRYPOINT ["scuttle", "python", "__main__.py"]