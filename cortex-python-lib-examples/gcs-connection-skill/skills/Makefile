TAG=latest
SKILL_NAME=gcs-connection-skill
ACTION_NAME=gcs-connection-reader
PROJECT_NAME=laguirre-testi-69257
DOCKER_PREGISTRY_URL=private-registry.dci-dev.dev-eks.insights.ai
DOCKER_IMAGE_URL=${DOCKER_PREGISTRY_URL}/${ACTION_NAME}:${TAG}

build: check-env
	docker build --no-cache -t ${DOCKER_IMAGE_URL} -f gcs-connection-skill/Dockerfile gcs-connection-skill/

push: check-env
	docker push ${DOCKER_IMAGE_URL}

deploy: install build push 
	cortex skills save skill.json

install: 
	pip install -r gcs-connection-skill/requirements.txt

tests: check-env
	cortex skills invoke --params-file ./payload.json ${SKILL_NAME} params

save-skill: check-env
	cortex skills save skill.json

save-connection: check-env
	cortex connections save connections/connection.json

all: save-connection deploy tests
	echo "Done"

check-env:
ifndef DOCKER_PREGISTRY_URL
	$(error environment variable DOCKER_PREGISTRY_URL is not set. Set it like <docker-registry-url>/<namespace-org>)
endif
ifndef PROJECT_NAME
	$(error environment variable PROJECT_NAME is not set. Set this to Cortex project name.)
endif
