IMAGE_TAG=latest
SKILL_NAME=${SKILL_NAME:-$(notdir $(CURDIR))}
DOCKER_IMAGE_URL=${DOCKER_PREGISTRY_URL}/${SKILL_NAME}:${IMAGE_TAG}

init: check-env
	cortex models save model/model.json --project ${PROJECT_NAME}
	cortex experiments save model/experiment.json --project ${PROJECT_NAME}
	cortex experiments create-run model/run.json --project ${PROJECT_NAME}

build: check-env
	docker build -t ${DOCKER_IMAGE_URL} .

push: check-env
	docker push ${DOCKER_IMAGE_URL}

deploy: check-env
	cortex actions deploy --actionName ${SKILL_NAME} --actionType daemon --docker ${DOCKER_IMAGE_URL} --port '5000' --project ${PROJECT_NAME}
	cortex types save -y types/types.yaml --project ${PROJECT_NAME}
	cortex skills save -y skill.yaml --project ${PROJECT_NAME}

tests: check-env
	cortex skills invoke --params-file ./test/payload.json ${SKILL_NAME} request --project ${PROJECT_NAME}

all: build push deploy get tests

get: check-env
	cortex skills describe ${SKILL_NAME} --project ${PROJECT_NAME}

check-env:
ifndef DOCKER_PREGISTRY_URL
	$(error environment variable DOCKER_PREGISTRY_URL is not set. Set it like <docker-registry-url>/<namespace-org>)
endif
ifndef PROJECT_NAME
	$(error environment variable PROJECT_NAME is not set. Set this to Cortex project name.)
endif
