#!/usr/bin/env bash

DOCKER_IMAGE=data-gen # Docker Image Name

save-skill: check-env
	cortex skills save -y skill.yaml --project ${PROJECT_NAME}

build: check-env
	docker build --no-cache -f Dockerfile -t ${DOCKER_IMAGE} .

deploy: check-env
	sh deploy-function.sh

check-env:
ifndef PROJECT_NAME
	$(error environment variable PROJECT_NAME is not set. Set this to Cortex project name.)
endif