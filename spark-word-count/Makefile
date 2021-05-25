TAG := $(shell cat /dev/urandom | env LC_CTYPE=C tr -dc 'a-zA-Z0-9' | fold -w 6 | head -n 1)

mvn.build:
	mvn clean install

docker.build.spark-base:
	sh spark-base/build-spark-image.sh

docker.push.spark-base:
	docker tag spark-base:latest svangapallycs/spark-base:$(TAG)
	docker push svangapallycs/spark-base:$(TAG)

docker.build.k8s.container:
	docker build -t spark-container -f spark-k8s-container-image/Dockerfile .

docker.push.k8s.container:
	docker tag spark-container:latest svangapallycs/spark-container:$(TAG)
	docker push svangapallycs/spark-container:$(TAG)

docker.build.word-count:
	docker build -t spark-word-count .

deploy.word-count:
	sh deploy-job.sh

save-skill:
	cortex skills save -y skill.yaml --project sumanth-hello-world