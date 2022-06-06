# Profiles JAR Examples
Example project utilizing the Fabric Profiles Spark JAR - WIP.

The project has a main CLI entrypoint with subcommands for running different examples. Additional modules can be added
to the main-app to facilitate your specific workflow.

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
# Docker Image
make create-app-image

# Clean Build and Create Docker Image
make clean build create-app-image

# Clean and Build Projects
make build

# Lots of dead layers occur during build process, clean up once in a while
docker system prune
```

## Run Commands

The application can be run in a Docker image using compatible Spark & Hadoop dependencies provided by the base Docker
image. The image uses spark-submit to run the application. Sample spark-submit configs are under
`main-app/src/main/resources/conf/`. These samples are used below.

The logging configuration for the example application is under `main-app/src/main/resources/spark-conf/logback.xml`.

### Run CLI
* `make build`
* `cd main-app/build/distributions`
* `unzip cortex-profiles-1.0.0-SNAPSHOT.zip`
* `cd cortex-profiles-1.0.0-SNAPSHOT`
* `./bin/cortex-profiles join-conns -p mctest30 -l member-base-file -r member-flu-risk-file -w member-joined-file`

### Run spark-submit in standalone (local) mode

The sample spark-submit config file is under:  `main-app/src/main/resources/conf/local.json`. The config uses a local
spark session and local (mock) clients for interacting with Cortex (see the volume mounts for 'Catalog').

```
# vol 1 is the spark-submit config files
# vol 2 is the local catalog specs - this  is required to 
# vol 3 is the output of the joined connection
docker run -p 4040:4040 -e "CONN_AWS_SECRET=xxxxx" -e "CORTEX_TOKEN=xxxxxxxxx" --entrypoint="python" \
-v $(pwd)/main-app/src/main/resources/conf:/app/conf \
-v $(pwd)/main-app/src:/opt/spark/work-dir/src \
-v $(pwd)/main-app/build:/opt/spark/work-dir/build \
profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/local.json\"}}"
```

```
# Take a look around at the container
docker run -p 4040:4040 -e "CONN_AWS_SECRET=xxxxx" -e "CORTEX_TOKEN=xxxxxxxxx" --entrypoint="" \
-v $(pwd)/main-app/src/main/resources/conf:/app/conf \
-v $(pwd)/main-app/src:/opt/spark/work-dir/src \
-v $(pwd)/main-app/build:/opt/spark/work-dir/build \
-it profiles-example /bin/bash
```

### Run spark-submit in standalone (local) mode attached to dci-dev

The config file for this example is under: `main-app/src/main/resources/conf/dci-dev.json`. The config uses spark standalone mode,
but interacts with an actual Cortex Fabric instance (dci-dev) for the Catalog resources.

Prerequisites:
- Access to Cortex Fabric instance and knowledge of backend configuration (Cortex Catalog/Manged Content)

Steps:
0. Generate a CORTEX_TOKEN
1. Update the Spark config file to set arguments for the example to run (e.g. `--project`, which connections to join,
   output connection). The default example is Joining two connections and saves the result in another connection (see
   `main-app/src/main/java/com/c12e/cortex/examples/JoinConnections.java`).
3. If still running the join-connections example, then ensure the connections (left, right, and output) exist in
   Cortex. **NOTE: The underlying file for the output connection does not need to exist, but the connection must be
   defined in Cortex.**
4. Update the name of the secret used by the example for your connections. The default secret name is `aws_secret`
5. Set the below environment variables, to run against the cluster (thse may vary depending on the connections & cluster)
    * For Fabric authentication
        - `CORTEX_TOKEN=xxxx`
    * For Cortex backend storage (i.e. Catalog/Managed Content)
        - `STORAGE_TYPE=s3`
        - `AWS_ACCESS_KEY_ID=xxxxx`
        - `AWS_SECRET_KEY=xxxxx`
        - `S3_ENDPOINT=http://host.docker.internal:9000`, exposes the host port (this should be the exact value used by dci-dev)
    * For the `JoinConnections` example
       - `CONN_AWS_SECRET=xxxxx`, this should be the secret key that can access the AWS remote S3 Connection. (This is
         because a mock client is used for secret access, because secrets are only accessible from WITHIN the cluster).

You will need to expose minio by port forwarding 9000, run:
```
docker run -p 4040:4040 -e "CORTEX_TOKEN=$(echo $CORTEX_TOKEN)" \
-e "STORAGE_TYPE=s3" \
-e "AWS_ACCESS_KEY_ID=$(echo $AWS_ACCESS_KEY_ID)" \
-e "AWS_SECRET_KEY=$(echo $AWS_SECRET_KEY)" \
-e "S3_ENDPOINT=http://host.docker.internal:9000" \
-e "CONN_AWS_SECRET=$(echo $CONN_AWS_SECRET)" \
--entrypoint="python" \
-v $(pwd)/main-app/src/main/resources/conf:/app/conf \
profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/dci-dev.json\"}}"
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
-v $(pwd)/main-app/src/main/resources/conf:/app/conf \
-v ~/.kube/config:/kube/config \
profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/dci-dev-local-cluster.json\"}}"
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

Need to set env vars
* DOCKER_PREGISTRY_URL=xxxx <-- private registry url
* PROJECT_NAME=xxxx <-- for the skill, action and types save
* CORTEX_TOKEN=xxxx <-- for connecting to api-server

```
# Building and pushing the skill container, saving the skill and the types
make deploy-skill

# Tag the latest create-app-image built container
make tag-container

# Push the container
make push-container

# Deploy the action and save the skill
make skill-save

# Save types
make types-save

# Invoke the skill with payload
make tests
```
#### Update the payload.json file with the right params before invoking the skill


### For CData example
* Get CData driver jars from http://cdatabuilds.s3.amazonaws.com/support/JDBC_JARS_21.0.8059.zip
* Add required driver jars and CData Spark SQL jar to `profiles-examples/main-app/src/main/resources/lib`
* Update `query`, `url`, `driver` in CData/JDBC spec `connectors.yml`. Get these syntax details from CData documentation https://cdn.cdata.com/help/RVF/jdbc/pg_JDBCconnectcode.htm
* For database password, SSL certs, service account JSON file update `CData.java` example and add secrets through env var or file
* Docker command to run in local
```
docker run -p 4040:4040 -e "CORTEX_TOKEN=xxxxxxxxx" -e "CDATA_OEM_KEY=<CData OEM Key>"  -e "CDATA_PRODUCT_CHECKSUM=<CData product checksum>"  --entrypoint="python" -v $(pwd)/main-app/src/main/resources/conf:/app/conf -v $(pwd)/main-app/src:/opt/spark/work-dir/src -v $(pwd)/main-app/build:/opt/spark/work-dir/build profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/local.json\"}}"
```
* We can't use cortex-cdata-plugin to create JDBC connection or parsing cortex connection because Spark SQL has specific requirement for JDBC https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html

#### Notes on CData BigQuery connection
Get GCP Service Account JSON as described in https://docs.google.com/document/d/1T1u8RMZhDYMIXHk7v3lLF2rzag7xLTr5CLHC-49UiYU/edit#heading=h.756ioo8pxy08. and put into `profiles-examples/main-app/src/main/resources/credentials`
```
docker run -p 4040:4040 -e "CORTEX_TOKEN=xxxxxxxxx" -e "CDATA_OEM_KEY=<CData OEM Key>"  -e "CDATA_PRODUCT_CHECKSUM=<CData product checksum>"  --entrypoint="python" -v $(pwd)/main-app/src/main/resources/credentials/:/secure-storage/ -v $(pwd)/main-app/src/main/resources/conf:/app/conf -v $(pwd)/main-app/src:/opt/spark/work-dir/src -v $(pwd)/main-app/build:/opt/spark/work-dir/build profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/local.json\"}}"
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
docker run -p 4040:4040 -e "CORTEX_TOKEN=xxxxxxxxx" --entrypoint="python" -v $(pwd)/main-app/src/main/resources/credentials/:/secure-storage/ -v $(pwd)/main-app/src/main/resources/conf:/app/conf -v $(pwd)/main-app/src:/opt/spark/work-dir/src -v $(pwd)/main-app/build:/opt/spark/work-dir/build profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/local-bigquery.json\"}}"
```
