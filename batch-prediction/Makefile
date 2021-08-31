IMAGE_TAG=$(shell git describe --long --always --match='v*.*' | sed 's/v//; s/-/./')
SKILL_NAME=$(notdir $(CURDIR))
DOCKER_IMAGE_URL=${DOCKER_PREGISTRY_URL}/${SKILL_NAME}:${IMAGE_TAG}
URL=$(shell cat ~/.cortex/config | jq .profiles[.currentProfile].url)
ifndef DOCKER_PREGISTRY_URL
	DOCKER_PREGISTRY_URL=$(shell curl ${URL}/fabric/v4/info | jq -r .endpoints.registry.url | sed 's~http[s]*://~~g')
endif

build: check-env
	docker build -t ${DOCKER_IMAGE_URL} .

push: check-env
	docker push ${DOCKER_IMAGE_URL}

deploy: check-env
	cortex actions deploy --actionName batch-prediction --actionType job --docker ${DOCKER_IMAGE_URL} --project ${PROJECT_NAME} --cmd '["scuttle", "python3", "predict/main.py"]'
	cortex types save -y types/types.yaml --project ${PROJECT_NAME}
	cortex skills save -y skill.yaml --project ${PROJECT_NAME}

tests: check-env
	cortex skills invoke --params-file ./test/payload.json ${SKILL_NAME} params --project ${PROJECT_NAME}

all: build push deploy

get: check-env
	cortex skills describe ${SKILL_NAME} --project ${PROJECT_NAME}

check-env:
ifndef PROJECT_NAME
	$(error environment variable PROJECT_NAME is not set. Set this to Cortex project name.)
endif