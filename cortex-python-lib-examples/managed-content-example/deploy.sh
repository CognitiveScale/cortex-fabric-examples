#!/bin/bash -eux

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DOCKER_REGISTRY=private-registry.dci-dev.dev-eks.insights.ai
IMAGE_TAG=test2

DEBUG=* cortex docker login

# BUILD, PUSH, DEPLOY, AND SAVE m-content-job
docker build -t ${DOCKER_REGISTRY}/m-content-job:${IMAGE_TAG} -f "${SCRIPT_DIR}"/skills/m-content-job/actions/m-content-job/Dockerfile "${SCRIPT_DIR}"/skills/m-content-job/actions/m-content-job
docker push ${DOCKER_REGISTRY}/m-content-job:${IMAGE_TAG}

cortex actions deploy --actionName 'm-content-job' --actionType 'job' --docker ${DOCKER_REGISTRY}/m-content-job:${IMAGE_TAG}
cortex skills save "${SCRIPT_DIR}"/skills/m-content-job/actions/m-content-job/skill.yaml -y


# Deploy agent
cortex agents save "${SCRIPT_DIR}"/agent.yaml --yaml

