# Profiles JAR Examples
Example repository utilizing the Fabric Profiles Spark JAR WIP

Additional modules can be added to the main-app to facilitate your specific workflow

#### TODO
* additional unit tests
* scripting work and templating of profile jar version and created docker image

### Milestone 1
  * Connector and DataSource work

## Prerequisites
1. Java 11 (use openjdk, see link in References section)
1. JFrog Artifactory credentials (shared in LastPass with everyone in `Shared-Engineering` folder)

## Developer Setup
1. Install IntelliJ IDEA with the latest Kotlin plugin enabled
1. Put JFrog Artifactory credentials in `$USER_HOME/.gradle/gradle.properties` file. See `gradle.properties.template` for instructions.

## Recommended JVM Settings
These can also be set/controlled through the `~/.gradle/gradle.properties` file that needs to be setup.
```
export GRADLE_OPTS="-Dorg.gradle.jvmargs='-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+OptimizeStringConcat'"
```

## References
Some useful links:

* [Spark Submit](https://spark.apache.org/docs/latest/submitting-applications.html)
* [Spark On Kubernetes](https://spark.apache.org/docs/latest/running-on-kubernetes.html)
* [Spark Configurations](https://spark.apache.org/docs/latest/index.html)

## Build Commands

```
# Clean Build and Create Docker Image
make create-app-image

# Clean and Build Projects
make build

# Lots of dead layers occur during build process, clean up once in a while
docker system prune
```


## Run Commands
Running the profiles-examples, can replace with your application

Set log levels profiles-examples/main-app/src/main/resources/spark-conf/logback.xml

Sample spark-submit configs are at profiles-examples/main-app/src/main/resources/conf


### Run spark-submit in standalone (local) mode
```
# vol 1 is the spark-submit config files
# vol 2 is the local catalog specs
# vol 3 is the output of the joined connection
docker run -p 4040:4040 -e "CORTEX_TOKEN=xxxxxxxxx" --entrypoint="python" \
-v $(pwd)/main-app/src/main/resources/conf:/opt/myconf \
-v $(pwd)/main-app/src:/opt/spark/work-dir/src \
-v $(pwd)/main-app/build:/opt/spark/work-dir/build \
profiles-example submit_job.py /opt/myconf/local.json

# Take a look around at the container
docker run -p 4040:4040 -e "CORTEX_TOKEN=xxxxxxxxx" --entrypoint="" \
-v $(pwd)/main-app/src/main/resources/conf:/opt/myconf \
-v $(pwd)/main-app/src:/opt/spark/work-dir/src \
-v $(pwd)/main-app/build:/opt/spark/work-dir/build \
-it profiles-example /bin/bash
```
### Run spark-submit in standalone (local) mode attached to dci-dev

Need to set env vars
- CORTEX_TOKEN=xxxx <-- for connecting to api-server
- STORAGE_TYPE=s3
- AWS_ACCESS_KEY_ID=changeme
- AWS_SECRET_KEY=changeme
- S3_ENDPOINT=http://host.docker.internal:9000  <-- exposes host port
- CONN_AWS_SECRET=xxxxx <-- secret for aws remote

Will need to expose minio by port forwarding 9000
```
docker run -p 4040:4040 -e "CORTEX_TOKEN=$(echo $CORTEX_TOKEN)" \
-e "STORAGE_TYPE=s3" \
-e "AWS_ACCESS_KEY_ID=$(echo $AWS_ACCESS_KEY_ID)" \
-e "AWS_SECRET_KEY=$(echo $AWS_SECRET_KEY)" \
-e "S3_ENDPOINT=http://host.docker.internal:9000" \
-e "CONN_AWS_SECRET=$(echo $CONN_AWS_SECRET)" \
--entrypoint="python" \
-v $(pwd)/main-app/src/main/resources/conf:/opt/myconf \
profiles-example submit_job.py /opt/myconf/dci-dev.json
```

### Run spark-submit in cluster mode attached to dci-dev
This will run the driver and any configured executors directly on the cluster
1. proxy to cluster: `kubectl proxy --port=6443`
2. Update profiles-examples/main-app/src/main/resources/Dockerfile entrypoint with scuttle: `ENTRYPOINT [ "scuttle", "/opt/entrypoint.sh" ]`
3. Run `make create-app-image`
4. Your docker image will need pushed to the cluster

Running from the container:
I ran into issue with this due to my kube config needing the aws executable
```
docker run -p 4040:4040 -e "CORTEX_TOKEN=$(echo $CORTEX_TOKEN)" \
--entrypoint="python" \
--env KUBECONFIG=/kube/config \
-v $(pwd)/main-app/src/main/resources/conf:/opt/myconf \
-v ~/.kube/config:/kube/config \
profiles-example submit_job.py /opt/myconf/dci-dev-local-cluster.json
```
Running outside the container:
1. `docker export CONTAINER_ID > container.tar`
2. `mkdir container && cd container && mv ../container.tar .`
3. `tar -xvf container.tar`
4. `cd opt/spark`
5. `export SPARK_HOME=$(pwd)`
6. `cd work-dir`
7. update dci-dev-local-cluster.json `"--master": "k8s://http://host.docker.internal:6443"` -> `"--master": "k8s://http://localhost:6443"`
8. `python(3) submit_job.py /path/to/dci-dev-local-cluster.json`

### Run spark-submit in cluster mode as skill


### For CData example
* Get CData driver jars from http://cdatabuilds.s3.amazonaws.com/support/JDBC_JARS_21.0.8059.zip
* Add required driver jars and CData Spark SQL jar to `profiles-examples/main-app/src/main/resources/lib`
* Update `query`, `url`, `driver` in CData/JDBC spec `connectors.yml`. Get these syntax details from CData documentation https://cdn.cdata.com/help/RVF/jdbc/pg_JDBCconnectcode.htm
* For database password, SSL certs, service account JSON file update `CData.java` example and add secrets through env var or file
* Docker command to run in local
```
docker run -p 4040:4040 -e "CORTEX_TOKEN=xxxxxxxxx" -e "CDATA_OEM_KEY=<CData OEM Key>"  -e "CDATA_PRODUCT_CHECKSUM=<CData product checksum>"  --entrypoint="python" -v $(pwd)/main-app/src/main/resources/conf:/opt/myconf -v $(pwd)/main-app/src:/opt/spark/work-dir/src -v $(pwd)/main-app/build:/opt/spark/work-dir/build profiles-example submit_job.py /opt/myconf/local.json
```
* We can't use cortex-cdata-plugin to create JDBC connection or parsing cortex connection because Spark SQL has specific requirement for JDBC https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html

#### Notes on CData BigQuery connection
Get GCP Service Account JSON as described in https://docs.google.com/document/d/1T1u8RMZhDYMIXHk7v3lLF2rzag7xLTr5CLHC-49UiYU/edit#heading=h.756ioo8pxy08. and put into `profiles-examples/main-app/src/main/resources/credentials`
```
docker run -p 4040:4040 -e "CORTEX_TOKEN=xxxxxxxxx" -e "CDATA_OEM_KEY=<CData OEM Key>"  -e "CDATA_PRODUCT_CHECKSUM=<CData product checksum>"  --entrypoint="python" -v $(pwd)/main-app/src/main/resources/credentials/:/secure-storage/ -v $(pwd)/main-app/src/main/resources/conf:/opt/myconf -v $(pwd)/main-app/src:/opt/spark/work-dir/src -v $(pwd)/main-app/build:/opt/spark/work-dir/build profiles-example submit_job.py /opt/myconf/local.json
```
Currently, CData BigQuery JDBC connection is failing with
```
 java.sql.SQLException: 'port' is not a valid connection property.
        at XcoreXgooglebigqueryX210X8059.qrc.a(Unknown Source)
        at XcoreXgooglebigqueryX210X8059.qrc.b(Unknown Source)
        at cdata.jdbc.googlebigquery.GoogleBigQueryDriver.connect(Unknown Source)
        at org.apache.spark.sql.execution.datasources.jdbc.connection.BasicConnectionProvider.getConnection(BasicConnectionProvider.scala:49)
        at org.apache.spark.sql.execution.datasources.jdbc.connection.ConnectionProvider$.create(ConnectionProvider.scala:77)
```
Looks like Spark is passing `port` implicitly

### BigQuery example
* Download BigQuery Spark connector https://repo1.maven.org/maven2/com/google/cloud/spark/spark-bigquery-with-dependencies_2.12/0.25.0/spark-bigquery-with-dependencies_2.12-0.25.0.jar in `profiles-examples/main-app/src/main/resources/lib`
* Get GCP Service Account JSON as described in https://docs.google.com/document/d/1T1u8RMZhDYMIXHk7v3lLF2rzag7xLTr5CLHC-49UiYU/edit#heading=h.756ioo8pxy08. and put into `profiles-examples/main-app/src/main/resources/credentials`
* Docker command to run in local
```
docker run -p 4040:4040 -e "CORTEX_TOKEN=xxxxxxxxx" --entrypoint="python" -v $(pwd)/main-app/src/main/resources/credentials/:/secure-storage/ -v $(pwd)/main-app/src/main/resources/conf:/opt/myconf -v $(pwd)/main-app/src:/opt/spark/work-dir/src -v $(pwd)/main-app/build:/opt/spark/work-dir/build profiles-example submit_job.py /opt/myconf/local.json
```
