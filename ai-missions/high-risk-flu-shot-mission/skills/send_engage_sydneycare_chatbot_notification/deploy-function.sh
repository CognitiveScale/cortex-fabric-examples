#!/usr/bin/env bash -eu
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DOCKER_IMAGE=sc-chatbot # Docker Image name

TAG="$(cat /dev/urandom | env LC_CTYPE=C tr -dc 'a-zA-Z0-9' | fold -w 6 | head -n 1)"
REGISTRY=${DOCKER_PRIV_REGISTRY_URL:-private-registry.dci-dev.dev-eks.insights.ai}
docker login

IMAGENAME=${REGISTRY}/${DOCKER_IMAGE}:${TAG}
docker tag ${DOCKER_IMAGE} ${IMAGENAME}
docker push ${IMAGENAME}

cortex actions deploy --actionName hw/${DOCKER_IMAGE}  --actionType daemon --docker ${IMAGENAME} --port '6000' --cmd '[]' --podspec ${SCRIPT_DIR}/podspec.json --project ${PROJECT_NAME}
