IMAGE_TAG=$(shell git describe --long --always --match='v*.*' | sed 's/v//; s/-/./')
ACTION_NAME=batch-predict
DOCKER_IMAGE_URL=${DOCKER_PREGISTRY_URL}/${ACTION_NAME}:${IMAGE_TAG}

build: check-env
	docker build -t ${DOCKER_IMAGE_URL} .

push: check-env
	docker push ${DOCKER_IMAGE_URL}

deploy: check-env
	cortex actions deploy --actionName batch-predict --actionType job --docker ${DOCKER_IMAGE_URL} --project shared --cmd '["scuttle", "python3", "predict/main.py"]'
	cortex types save -y types/types.yaml --project shared
	cortex skills save -y skill.yaml --project shared

tests: check-env
	cortex skills invoke --params-file ./test/payload.json ${SKILL_NAME} params --project ${PROJECT_NAME}

all: build push deploy

get: check-env
	cortex skills describe ${SKILL_NAME} --project ${PROJECT_NAME}

check-env:
ifndef DOCKER_PREGISTRY_URL
	$(error environment variable DOCKER_PREGISTRY_URL is not set. Set it like <docker-registry-url>/<namespace-org>)
endif