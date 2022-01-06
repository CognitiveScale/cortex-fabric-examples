IMAGE_TAG=latest
SKILL_NAME=cdata-jdbc-connector
DOCKER_IMAGE_URL=${DOCKER_PREGISTRY_URL}/${SKILL_NAME}:${IMAGE_TAG}

build: check-env
	docker build -t ${DOCKER_IMAGE_URL} .

push: check-env
	docker push ${DOCKER_IMAGE_URL}

deploy: check-env
	cortex actions deploy --actionName cdata-jdbc-connector --actionType daemon --cmd '["/bin/sh","-c","java -jar /app/server.jar"]' --project ${PROJECT_NAME} --docker ${DOCKER_IMAGE_URL}
	cortex actions deploy --actionName grc-jdbc-connector --actionType daemon --cmd '["/bin/sh","-c","java -jar /app/server.jar"]' --project ${PROJECT_NAME} --docker ${DOCKER_IMAGE_URL}
	cortex skills save -y skill.yaml --project ${PROJECT_NAME}
	sleep 10

tests: check-env
	cortex skills invoke --params '{}' ${SKILL_NAME} request --project ${PROJECT_NAME}

all: build push deploy get tests

get: check-env
	cortex skills describe ${SKILL_NAME} --verbose --project ${PROJECT_NAME}

check-env:
ifndef DOCKER_PREGISTRY_URL
	$(error environment variable DOCKER_PREGISTRY_URL is not set. Set it like <docker-registry-url>/<namespace-org>)
endif
ifndef PROJECT_NAME
	$(error environment variable PROJECT_NAME is not set. Set this to Cortex project name.)
endif
