
TAG=latest
SKILL_NAME=e2e-example
ACTION_NAME_TRAIN=train-example
ACTION_NAME_PREDICT=predict-example
DOCKER_IMAGE_URL_TRAIN=${DOCKER_PREGISTRY_URL}/${ACTION_NAME_TRAIN}:${TAG}
DOCKER_IMAGE_URL_PREDICT=${DOCKER_PREGISTRY_URL}/${ACTION_NAME_PREDICT}:${TAG}

build: check-env
	docker build --no-cache -t ${DOCKER_IMAGE_URL_TRAIN} -f actions/train/Dockerfile actions/train
	docker build --no-cache -t ${DOCKER_IMAGE_URL_PREDICT} -f actions/predict/Dockerfile actions/predict

push: check-env
	docker push ${DOCKER_IMAGE_URL_TRAIN}
	docker push ${DOCKER_IMAGE_URL_PREDICT}

tests: check-env
	cortex skills invoke --params-file ./tests/test_train.json ${SKILL_NAME} train --project ${PROJECT_NAME}
	cortex skills invoke --params-file ./tests/test_predict.json ${SKILL_NAME} predict --project ${PROJECT_NAME}

check-env:
ifndef DOCKER_PREGISTRY_URL
	$(error environment variable DOCKER_PREGISTRY_URL is not set. Set it like <docker-registry-url>/<namespace-org>)
endif
ifndef PROJECT_NAME
	$(error environment variable PROJECT_NAME is not set. Set this to Cortex project name.)
endif