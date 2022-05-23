# TAG := $(shell git describe --long --always --match='v*.*' | sed 's/v//; s/-/./')
TAG := test14
SPARK_BASE_IMG := $(shell cat config.json | jq '.spark_base' | sed -e 's/^"//' -e 's/"$///')

docker.build.spark-base:
	sh spark-base/build-spark-image.sh

docker.push.spark-base:
	docker tag spark-base-python:latest $(SPARK_BASE_IMG):spark-base-$(TAG)
	docker push $(SPARK_BASE_IMG):spark-base-$(TAG)

docker.build.k8s.container:
	docker build --build-arg base_img=$(SPARK_BASE_IMG):spark-base-$(TAG) -t spark-container -f spark-k8s-container-image/Dockerfile .

docker.push.k8s.container:
	docker tag spark-container:latest $(SPARK_BASE_IMG):spark-container-$(TAG)
	docker push $(SPARK_BASE_IMG):spark-container-$(TAG)

config.update.spark.image:
	python update-container-image.py $(SPARK_BASE_IMG):spark-container-$(TAG)

docker.push-all.spark-images: docker.build.spark-base docker.push.spark-base docker.build.k8s.container docker.push.k8s.container

docker.build.spark-stream-ingest:
	docker build --build-arg base_img=$(SPARK_BASE_IMG):spark-container-$(TAG) -t spark-stream-ingest .

deploy.spark-stream-ingest:
	sh deploy-job.sh

types.save:
	cortex types save -y types/types.yaml --project bptest

skill.save:
	cortex skills save -y skill.yaml --project bptest

deploy.all: docker.push-all.spark-images docker.build.spark-stream-ingest deploy.spark-stream-ingest types.save skill.save