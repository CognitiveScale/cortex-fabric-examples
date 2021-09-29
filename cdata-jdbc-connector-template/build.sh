#!/bin/bash -eux
cd "$(dirname "$0")"
VERSION=$(git describe --long --always --dirty --match='v*.*' | sed 's/v//; s/-/./')
echo "##### BUILDING ${VERSION} ######"
BRANCH=$(git symbolic-ref --short -q HEAD)
IMAGE_NAME=$(git remote -v | grep "(fetch)" | sed -E "s/.*git@.*:.*\/(.*)\.git.*/\1/")
DOCKER_USER=${DOCKER_USER-"c12e"}
docker rmi "${DOCKER_USER}/${IMAGE_NAME}:${BRANCH}" || echo "old image not cleanedup..."
docker run --rm -v $PWD:/build -w /build gradle:4.10.3-jdk8-alpine gradle
docker build -t ${DOCKER_USER}/${IMAGE_NAME}:${BRANCH} .
