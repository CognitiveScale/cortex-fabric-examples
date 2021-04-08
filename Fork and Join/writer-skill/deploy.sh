#!/usr/bin/env bash

## Docker details
DOCKER_REG=<registry>
PASSWORD=<password>
USER=<username>
TAG=latest

## Action name

ACTION_NAME=writer-skill
IMAGE_NAME=writer-skill

## Docker login
docker login -u ${USER} -p ${PASSWORD} ${DOCKER_REG}

## Docker build
docker build -t ${IMAGE_NAME}:latest  -f writer-skill/Dockerfile

## Docker tag and push
docker tag ${IMAGE_NAME}:latest ${DOCKER_REG}/${IMAGE_NAME}:${TAG}
docker push ${DOCKER_REG}/${IMAGE_NAME}:${TAG}

## Deploy
cortex actions deploy --actionName ${ACTION_NAME} --actionType daemon --docker ${DOCKER_REG}/${IMAGE_NAME}:${TAG} --port 5000
cortex skills save -y writer-skill/skill.yaml