#!/usr/bin/env bash
ROOT_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
CURRENT_PROFILE=$(shell cat ~/.cortex/config | jq -r ".currentProfile")
JWT_TOKEN=$(shell cat ~/.cortex/config | jq ".profiles" | jq ".[\"${CURRENT_PROFILE}\"]" | jq -r '.token')
CORTEX_API_ENDPOINT := $(shell cat ~/.cortex/config | jq ".profiles" | jq ".[\"${CURRENT_PROFILE}\"]" | jq -r '.url' )

DOCKER_IMAGE=sch-appointment # Docker Image Name

save-skill: check-env
	cortex skills save -y skill.yaml --project ${PROJECT_NAME}

common:
	cp -r ../../common ./src

build: common check-env
	docker build -t ${DOCKER_IMAGE} .

deploy: check-env
	sh deploy-function.sh

save-type: check-env
	cortex types save -y types.yaml --project ${PROJECT_NAME}

check-env:
ifndef PROJECT_NAME
	$(error environment variable PROJECT_NAME is not set. Set this to Cortex project name.)
endif