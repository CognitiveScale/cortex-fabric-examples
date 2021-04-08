SHELL:=/bin/bash -O expand_aliases

## Cortex Profile details
CORTEX_PROFILE:=$(shell cat ~/.cortex/config | jq ".currentProfile")
CORTEX_URL:=$(shell cat ~/.cortex/config | jq ".profiles" | jq ".[\"${CORTEX_PROFILE}\"]" | jq -r ".url")
CORTEX_TOKEN=$(shell cortex configure token)

## Docker details
DOCKER_TAG:=$(shell git describe --long --always --match='v*.*' | sed 's/v//; s/-/./').a
DOCKER_REGISTRY=<registry>
#DOCKER_REGISTRY=`curl -s -H "Content-Type: application/json" -H "Authorization: Bearer ${CORTEX_TOKEN}" ${CORTEX_URL}/fabric/v4/actions/_config | jq -r .config.dockerPrivateRegistryUrl`
DOCKER_USERNAME=<usernaame>
DOCKER_PASSWORD=<password>


help:
	echo "Usage : make <build / push / deploy>"

agents.describe:
	cortex agents describe merge-resutls-example > agent.json

agents.save:
	cortex agents save agent.json

docker.login:
	@echo "Docker login"
	docker login -u ${DOCKER_USERNAME} -p "${DOCKER_PASSWORD}" ${DOCKER_REGISTRY}

build:
	@echo "Building skills"
	docker build -f hello-daemon/Dockerfile -t hello-daemon .
	docker build -f sessions-skill-a/Dockerfile -t sessions-skill-a .
	docker build -f sessions-skill-b/Dockerfile -t sessions-skill-b .
	docker build -f writer-skill/Dockerfile -t writer-skill .


push:docker.login
	@echo "Pushing image of hello-daemon"
	docker tag hello-daemon ${DOCKER_REGISTRY}/hello-daemon:${DOCKER_TAG}
	docker push ${DOCKER_REGISTRY}/hello-daemon:${DOCKER_TAG}

	@echo "Pushing image of sessions-skill-a"
	docker tag sessions-skill-a ${DOCKER_REGISTRY}/sessions-skill-a:${DOCKER_TAG}
	docker push ${DOCKER_REGISTRY}/sessions-skill-a:${DOCKER_TAG}

	@echo "Pushing image of sessions-skill-b"
	docker tag sessions-skill-b ${DOCKER_REGISTRY}/sessions-skill-b:${DOCKER_TAG}
	docker push ${DOCKER_REGISTRY}/sessions-skill-b:${DOCKER_TAG}

	@echo "Pushing image of writer-skill"
	docker tag writer-skill ${DOCKER_REGISTRY}/writer-skill:${DOCKER_TAG}
	docker push ${DOCKER_REGISTRY}/writer-skill:${DOCKER_TAG}

deploy:
	## Deplot actions
	@echo "Deploying action hello-daemon"
	cortex actions deploy --actionName hello-daemon --actionType daemon --docker ${DOCKER_REGISTRY}/hello-daemon:${DOCKER_TAG} --port 5000

	@echo "Deploying action sessions-skill-a"
	cortex actions deploy --actionName sessions-skill-a --actionType daemon --docker ${DOCKER_REGISTRY}/sessions-skill-a:${DOCKER_TAG} --port 5000

	@echo "Deploying action sessions-skill-b"
	cortex actions deploy --actionName sessions-skill-b --actionType daemon --docker ${DOCKER_REGISTRY}/sessions-skill-b:${DOCKER_TAG} --port 5000

	@echo "Deploying action writer-skill"
	cortex actions deploy --actionName writer-skill --actionType daemon --docker ${DOCKER_REGISTRY}/writer-skill:${DOCKER_TAG} --port 5000

	## Deploy skills
	@echo "Deploying skill hello-daemon"
	cortex skills save -y hello-daemon/skill.yaml

	@echo "Deploying skill sessions-skill-a"
	cortex skills save -y sessions-skill-a/skill.yaml

	@echo "Deploying skill sessions-skill-b"
	cortex skills save -y sessions-skill-b/skill.yaml

	@echo "Deploying skill writer-skill"
	cortex skills save -y writer-skill/skill.yaml

all:build push deploy agents.save