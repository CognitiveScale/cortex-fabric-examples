TAG := $(shell git describe --long --always --match='v*.*' | sed 's/v//; s/-/./')
SPARK_BASE_IMG := $(shell cat config.json | jq '.spark_base' | sed -e 's/^"//' -e 's/"$///')
DOCKER_IMAGE=emr-job-submit# Docker Image Name
DOCKER_IMAGE_URL=${DOCKER_PREGISTRY_URL}/${DOCKER_IMAGE}:skill-image-${TAG}

docker.build.emr.container:
	docker build -t emr-container -f emr-container-image/Dockerfile .
	docker tag emr-container $(SPARK_BASE_IMG):spark-container-$(TAG)
	docker push $(SPARK_BASE_IMG):spark-container-$(TAG)

docker.build.skill.container: check-env
	docker build -t ${DOCKER_IMAGE_URL} .
	docker push ${DOCKER_IMAGE_URL}

config.update.spark.image:
	python update-container-image.py $(SPARK_BASE_IMG):spark-container-$(TAG) ${CORTEX_URI} ${CORTEX_TOKEN}

types.save: check-env
	cortex types save -y types/types.yaml --project ${PROJECT_NAME}


skill.save: check-env
	cortex actions deploy --actionName ${DOCKER_IMAGE} --actionType job --docker ${DOCKER_IMAGE_URL} --project ${PROJECT_NAME}
	cortex skills save -y skill.yaml --project ${PROJECT_NAME}

deploy.all: check-env docker.build.emr.container config.update.spark.image docker.build.skill.container types.save skill.save

tests: check-env
	cortex skills invoke --params-file ./tests/test_req.json hw/${DOCKER_IMAGE} request --project ${PROJECT_NAME}

check-env:
ifndef DOCKER_PREGISTRY_URL
	$(error environment variable DOCKER_PREGISTRY_URL is not set. Set it like <docker-registry-url>/<namespace-org>)
endif
ifndef PROJECT_NAME
	$(error environment variable PROJECT_NAME is not set. Set this to Cortex project name.)
endif
ifndef CORTEX_URI
	$(error environment variable CORTEX_URI is not set. Set this using `eval $(cortex configure env)`)
endif
ifndef CORTEX_TOKEN
	$(error environment variable CORTEX_TOKEN is not set. Set this using `eval $(cortex configure env)`)
endif

