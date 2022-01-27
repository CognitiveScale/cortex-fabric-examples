IMAGE_TAG=latest
DOCKER_IMAGE=sc-chatbot# Docker Image Name
DOCKER_IMAGE_URL=${DOCKER_PREGISTRY_URL}/${DOCKER_IMAGE}:${IMAGE_TAG}

common: check-env
	cp -r ../../common ./src

build: common check-env
	docker build -t ${DOCKER_IMAGE_URL} .

push: check-env
	docker push ${DOCKER_IMAGE_URL}

deploy: check-env
	cortex actions deploy --actionName hw/${DOCKER_IMAGE} --actionType daemon --docker ${DOCKER_IMAGE_URL} --port '6000' --cmd '[]' --podspec podspec.json --project ${PROJECT_NAME}
	cortex types save -y types.yaml --project ${PROJECT_NAME}
	cortex skills save -y skill.yaml --project ${PROJECT_NAME}

tests: check-env
	cortex skills invoke --params-file ./tests/test_req.json hw/${DOCKER_IMAGE} request --project ${PROJECT_NAME}

all: build push deploy get

get: check-env
	cortex skills describe hw/${DOCKER_IMAGE} --project ${PROJECT_NAME}

check-env:
ifndef DOCKER_PREGISTRY_URL
	$(error environment variable DOCKER_PREGISTRY_URL is not set. Set it like <docker-registry-url>/<namespace-org>)
endif
ifndef PROJECT_NAME
	$(error environment variable PROJECT_NAME is not set. Set this to Cortex project name.)
endif
