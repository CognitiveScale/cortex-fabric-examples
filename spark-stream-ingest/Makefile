# TAG := $(shell git describe --long --always --match='v*.*' | sed 's/v//; s/-/./')op
TAG := test9
DOCKER_IMAGE := spark-stream-ingest
DOCKER_PREGISTRY_URL := private-registry.dci-dev.dev-eks.insights.ai
SPARK_CONTAINER := ${DOCKER_PREGISTRY_URL}/${DOCKER_IMAGE}:${TAG}
SKILL_CONTAINER := ${DOCKER_PREGISTRY_URL}/${DOCKER_IMAGE}-skill:${TAG}

build.container: check-env
	docker build -t ${SPARK_CONTAINER} -f Dockerfile.executor .
	docker build -t ${SKILL_CONTAINER} -f Dockerfile .

push.container: check-env
	docker push ${SPARK_CONTAINER}
	docker push ${SKILL_CONTAINER}

skill.save: check-env
	cortex actions deploy --actionName ${DOCKER_IMAGE} --actionType job --docker ${SKILL_CONTAINER} --project ${PROJECT_NAME}
	cortex skills save -y skill.yaml --project ${PROJECT_NAME}

types.save: check-env
	cortex types save -y types/types.yaml --project ${PROJECT_NAME}

tests: check-env
	cortex skills invoke ${DOCKER_IMAGE} params --project ${PROJECT_NAME}

all: build.container push.container types.save skill.save

check-env:
ifndef DOCKER_PREGISTRY_URL
	$(error environment variable DOCKER_PREGISTRY_URL is not set. Set it like <docker-registry-url>/<namespace-org>)
endif
ifndef PROJECT_NAME
	$(error environment variable PROJECT_NAME is not set. Set this to Cortex project name.)
endif