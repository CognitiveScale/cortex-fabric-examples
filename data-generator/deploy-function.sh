#!/usr/bin/env bash -eu
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DOCKER_IMAGE=data-gen # Docker Image name


TAG="$(cat /dev/urandom | env LC_CTYPE=C tr -dc 'a-zA-Z0-9' | fold -w 6 | head -n 1)"
REGISTRY=private-registry.dci-dev.dev-eks.insights.ai
docker login
# docker build --no-cache -t ${DOCKER_IMAGE} .
IMAGENAME=${REGISTRY}/${DOCKER_IMAGE}:${TAG}
docker tag ${DOCKER_IMAGE} ${IMAGENAME}
docker push ${IMAGENAME}

cortex actions deploy --actionName ${DOCKER_IMAGE}  --actionType job --docker ${IMAGENAME} --podspec podspec.json

