#!/usr/bin/env bash -eu
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DOCKER_IMAGE=spark-word-count # Docker Image name
REGISTRY=private-registry.dci-dev.dev-eks.insights.ai # Private registry with out 'https//'
REGISTRY_PASS=
REGISTRY_USER=docker # Private registry Username

TAG="$(cat /dev/urandom | env LC_CTYPE=C tr -dc 'a-zA-Z0-9' | fold -w 6 | head -n 1)"
docker login -u ${REGISTRY_USER} -p ${REGISTRY_PASS} https://${REGISTRY}
IMAGENAME=${REGISTRY}/${DOCKER_IMAGE}:${TAG}
docker tag ${DOCKER_IMAGE} ${IMAGENAME}
docker push ${IMAGENAME}

cortex actions deploy --actionName ${DOCKER_IMAGE}  --actionType job --docker ${IMAGENAME} --project sumanth-hello-world --cmd '["sh", "submit-spark-job.sh"]'

