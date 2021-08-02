IMAGE_TAG=$(shell git describe --long --always --match='v*.*' | sed 's/v//; s/-/./')
SKILL_NAME=$(notdir $(CURDIR))
DOCKER_IMAGE_URL=${DOCKER_PREGISTRY_URL}/${SKILL_NAME}:${IMAGE_TAG}
URL=$(shell cat ~/.cortex/config | jq .profiles[.currentProfile].url)
ifndef DOCKER_PREGISTRY_URL
	DOCKER_PREGISTRY_URL=$(shell curl ${URL}/fabric/v4/info | jq -r .endpoints.registry.url | sed 's~http[s]*://~~g')
endif

build:
	docker build -t ${DOCKER_IMAGE_URL} .

push:
	docker push ${DOCKER_IMAGE_URL}

deploy:
	cortex actions deploy --actionName online-prediction --actionType daemon --project shared --docker ${DOCKER_IMAGE_URL} --cmd '["python3", "predict/main.py"]' --port 5000
	cortex types save -y types/types.yaml --project shared
	cortex skills save -y skill.yaml --project shared

tests:
	cortex skills invoke --params-file ./test/payload.json ${SKILL_NAME} request --project ${PROJECT_NAME}

all: build push deploy

get:
	cortex skills describe ${SKILL_NAME} --project ${PROJECT_NAME}
