#!/usr/bin/env bash -eu
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DOCKER_IMAGE=spark-stream-ingest # Docker Image name

# Registry details
REGISTRY=private-registry.dci-dev.dev-eks.insights.ai # Private registry with out 'https//'
# REGISTRY_PASS= # Private Registry Password
# REGISTRY_USER= # Private registry Username for external registries

# Docker Login
# cortex docker login # If using cortex registry, you don't need user & password details
# docker login -u ${REGISTRY_USER} -p ${REGISTRY_PASS} https://${REGISTRY} # In case of external image registry

TAG="$(cat /dev/urandom | env LC_CTYPE=C tr -dc 'a-zA-Z0-9' | fold -w 6 | head -n 1)"
IMAGENAME=${REGISTRY}/${DOCKER_IMAGE}:${TAG}
docker tag ${DOCKER_IMAGE} ${IMAGENAME}
docker push ${IMAGENAME}

cortex actions deploy --actionName ${DOCKER_IMAGE}  --actionType job --docker ${IMAGENAME} --project bptest --cmd '["scuttle", "python3", "submit_job.py", "config.json", "driverTemplate.yaml"]'