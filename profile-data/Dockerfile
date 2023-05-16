ARG PYTHON_IMAGE=python:3.9-slim-bullseye
FROM ${PYTHON_IMAGE}

ADD . /app
COPY --from=c12e/scuttle:latest-main /scuttle /bin/scuttle
RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install git -y \
    && pip install --upgrade -r /app/requirements.txt \
    && apt-get purge git -y \
    && apt-get clean \ 
    && rm -rf /var/lib/apt/lists/*
ENV ENVOY_ADMIN_API=http://localhost:15000
ENV ISTIO_QUIT_API=http://localhost:15020
ENV SCUTTLE_LOGGING=false
ENV HADRON_TRANSITION_NUTRITION_PERSIST_PATH=${HADRON_REPORTS_SINK}
ENV HADRON_TRANSITION_NUTRITION_PERSIST_MODULE="ds_connectors.handlers.managed_content_handlers"
ENV HADRON_TRANSITION_NUTRITION_SOURCE_HANDLER="ManagedContentSourceHandler"
ENV HADRON_TRANSITION_NUTRITION_PERSIST_HANDLER="ManagedContentPersistHandler"
ENTRYPOINT ["scuttle", "python3", "/app/main.py"]
