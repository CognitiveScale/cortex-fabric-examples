#!/bin/bash -eux

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DOCKER_REGISTRY=private-registry.dci-dev.dev-eks.insights.ai
IMAGE_TAG=test1

DEBUG=* cortex docker login

# BUILD, PUSH, DEPLOY, AND SAVE word-count-daemon
docker build -t ${DOCKER_REGISTRY}/word-count-daemon:${IMAGE_TAG} -f "${SCRIPT_DIR}"/skills/word-count-daemon/Dockerfile "${SCRIPT_DIR}"/skills/word-count-daemon
docker push ${DOCKER_REGISTRY}/word-count-daemon:${IMAGE_TAG}

cortex actions deploy --actionName 'word-count-daemon' --actionType 'daemon' --port 5000 --docker ${DOCKER_REGISTRY}/word-count-daemon:${IMAGE_TAG}
cortex skills save "${SCRIPT_DIR}"/skills/word-count-daemon/skill.yaml -y


# Deploy agent
cortex agents save "${SCRIPT_DIR}"/agent.yaml --yaml

