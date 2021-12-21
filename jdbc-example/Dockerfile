FROM adoptopenjdk/openjdk8:x86_64-ubi-minimal-jre8u312-b07
ADD /build/libs/jdbc-example-all-1.0-SNAPSHOT.jar jdbc-example-all-1.0-SNAPSHOT.jar
EXPOSE 5000
CMD ["java", "-cp", "lib","-jar", "jdbc-example-all-1.0-SNAPSHOT.jar"]
