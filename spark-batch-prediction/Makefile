TAG := $(shell cat /dev/urandom | env LC_CTYPE=C tr -dc 'a-zA-Z0-9' | fold -w 6 | head -n 1)
SPARK_BASE_IMG := $(shell cat config.json | jq '.spark_base' | sed -e 's/^"//' -e 's/"$///')

docker.build.spark-base:
	sh spark-base/build-spark-image.sh

docker.push.spark-base:
	docker tag spark-base-python:latest $(SPARK_BASE_IMG):spark-base-$(TAG)
	docker push $(SPARK_BASE_IMG):spark-base-$(TAG)

docker.build.k8s.container:
	docker build -t spark-container -f spark-k8s-container-image/Dockerfile .

docker.push.k8s.container:
	docker tag spark-container:latest $(SPARK_BASE_IMG):spark-container-$(TAG)
	docker push $(SPARK_BASE_IMG):spark-container-$(TAG)

docker.build.spark-batch-predict:
	docker build -t spark-batch-predict .

deploy.spark-batch-predict:
	sh deploy-job.sh

types.save:
	 cortex types save -y types/types.yaml --project shared

skill.save:
	cortex skills save -y skill.yaml --project shared