IMAGE_TAG:=$(shell git describe --long --always --match='v*.*' | sed 's/v//; s/-/./')fai
DOCKER_IMAGE=data-gen# Docker Image Name
DOCKER_IMAGE_URL=${DOCKER_PREGISTRY_URL}/${DOCKER_IMAGE}:${IMAGE_TAG}


build: check-env
	docker build -t ${DOCKER_IMAGE_URL} .

push: check-env
	docker push ${DOCKER_IMAGE_URL}

deploy: check-env
	cortex actions deploy --actionName ${DOCKER_IMAGE} --actionType job --docker ${DOCKER_IMAGE_URL} --podspec podspec.json --project ${PROJECT_NAME}
	# cortex types save -y types.yaml --project ${PROJECT_NAME}
	cortex skills save -y skill.yaml --project ${PROJECT_NAME}

tests: check-env
	cortex skills invoke --params-file ./tests/payload.json ${DOCKER_IMAGE} controller_run_params --project ${PROJECT_NAME}

all: build push deploy get

get: check-env
	cortex skills describe ${DOCKER_IMAGE} --project ${PROJECT_NAME}

check-env:
ifndef DOCKER_PREGISTRY_URL
	$(error environment variable DOCKER_PREGISTRY_URL is not set. Set it like <docker-registry-url>/<namespace-org>)
endif
ifndef PROJECT_NAME
	$(error environment variable PROJECT_NAME is not set. Set this to Cortex project name.)
endif
