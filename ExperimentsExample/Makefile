IMAGE_TAG=latest
SKILL_NAME=experiments
ACTION_NAME_TRAIN=train-example
ACTION_NAME_PREDICT=predict-example
DOCKER_IMAGE_URL_TRAIN=${DOCKER_PREGISTRY_URL}/${ACTION_NAME_TRAIN}:${IMAGE_TAG}
DOCKER_IMAGE_URL_PREDICT=${DOCKER_PREGISTRY_URL}/${ACTION_NAME_PREDICT}:${IMAGE_TAG}

build-train: check-env
	docker build -t ${DOCKER_IMAGE_URL_TRAIN} -f train/Dockerfile train

build-predict: check-env
	docker build -t ${DOCKER_IMAGE_URL_PREDICT} -f predict/Dockerfile predict

push-train: check-env
	docker push ${DOCKER_IMAGE_URL_TRAIN}

push-predict: check-env
	docker push ${DOCKER_IMAGE_URL_PREDICT}

deploy-train: check-env
	cortex actions deploy train/action.json --project ${PROJECT_NAME} --docker ${DOCKER_IMAGE_URL_TRAIN}
	cortex skills save -y skill.yaml --project ${PROJECT_NAME}
	sleep 10s # wait for action containers to pull and deploy

deploy-predict: check-env
	cortex actions deploy predict/action.json --project ${PROJECT_NAME} --docker ${DOCKER_IMAGE_URL_PREDICT}
	cortex skills save -y skill.yaml --project ${PROJECT_NAME}
	sleep 10s # wait for action containers to pull and deploy

tests-train: check-env
	cortex skills invoke --params-file ./test/payload.json ${SKILL_NAME} train --project ${PROJECT_NAME}

tests-predict: check-env
	cortex skills invoke --params-file ./test/payload.json ${SKILL_NAME} predict --project ${PROJECT_NAME}

types:
	cortex types save -y types.yaml --project ${PROJECT_NAME}

train: build-train push-train deploy-train tests-train

predict: build-predict push-predict deploy-predict tests-predict

all: types train predict

get: check-env
	cortex skills describe ${SKILL_NAME} --project ${PROJECT_NAME}

check-env:
ifndef DOCKER_PREGISTRY_URL
	$(error environment variable DOCKER_PREGISTRY_URL is not set. Set it like <docker-registry-url>/<namespace-org>)
endif
ifndef PROJECT_NAME
	$(error environment variable PROJECT_NAME is not set. Set this to Cortex project name.)
endif