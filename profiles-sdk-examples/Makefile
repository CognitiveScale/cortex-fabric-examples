.PHONY: clean build create-app-image test

TAG := latest
DOCKER_IMAGE := profiles-example
URL=$(shell cat ~/.cortex/config | jq .profiles[.currentProfile].url)
ifndef DOCKER_PREGISTRY_URL
	DOCKER_PREGISTRY_URL=$(shell curl ${URL}/fabric/v4/info | jq -r .endpoints.registry.url | sed 's~http[s]*://~~g')
endif
SPARK_CONTAINER := ${DOCKER_PREGISTRY_URL}/${DOCKER_IMAGE}:${TAG}

all: clean build create-app-image deploy-skill

create-app-image:
	docker build --build-arg base_img=c12e/spark-template:profile-jar-base-6.3.0-M.2.5 -t ${DOCKER_IMAGE}:${TAG} -f ./main-app/build/resources/main/Dockerfile ./main-app/build

# Build the Application
build:
	./gradlew build

# Test the Application
test:
	./gradlew test

clean:
	./gradlew clean


# Tag the latest create-app-image built container
tag-container: check-env
	docker tag ${DOCKER_IMAGE}:${TAG} ${SPARK_CONTAINER}

# Push the container
push-container: check-env
	docker push ${SPARK_CONTAINER}

# Deploy the action and save the skill
skill-save: check-env
	cortex actions deploy --actionName profiles-example --actionType job --docker ${SPARK_CONTAINER} --project ${PROJECT_NAME} --cmd '["scuttle", "python", "submit_job.py"]' --podspec ./templates/podspec.yaml
	cortex skills save -y templates/skill.yaml --project ${PROJECT_NAME}

# Save types
types-save: check-env
	cortex types save -y templates/types.yaml --project ${PROJECT_NAME}

# Building and pushing the skill container, saving the skill and the types
deploy-skill: tag-container push-container types-save skill-save

# Invoke the skill with payload
invoke: check-env
	cortex skills invoke --params-file templates/payload.json profiles-example params --project ${PROJECT_NAME}

check-env:
ifndef DOCKER_PREGISTRY_URL
	$(error environment variable DOCKER_PREGISTRY_URL is not set. Set it like <docker-registry-url>/<namespace-org>)
endif
ifndef PROJECT_NAME
	$(error environment variable PROJECT_NAME is not set. Set this to Cortex project name.)
endif
