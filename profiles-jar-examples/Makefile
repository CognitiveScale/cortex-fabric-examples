.PHONY: clean build create-app-image

create-app-image: build
	cd ./main-app/build && \
	docker build --build-arg base_img=c12e/spark-template:profile-jar-base-6.3.0-M.1 -t profiles-example:latest -f resources/main/Dockerfile .

build: clean
	./gradlew build

clean:
	./gradlew clean
