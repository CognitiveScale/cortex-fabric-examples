# Local Clients

This example application is intended to introduce working the Cortex Profiles SDK in local development environment. This
example specifically introduces:
- creating a `CortexSession` with [Spark](https://spark.apache.org/docs/latest/index.html) in local mode
- using a local Cortex Catalog
- using a local Secrets Client
- loading Cortex configuration properties from a Spark Config file
- reading and writing Cortex DataSources and Connections

The logging configuration for this project is controlled by the [logback.xml](./src/main/resources/logback.xml) file.
This example is structured as standalone Java application that can also be run from the parent application. In addition,
all resources and data used by this application are defined in this module.

## Introduction

The entrypoint to the Profiles SDK is the `CortexSession`, a session based API around Spark and the `SparkSession`.
The `CortexSession` can be created via a static factory method after having initialized a `SparkSession`. Configuration
options will be taken from the provided `SparkSession` and can also be overridden, e.g.
```java
public void createCortexSession(SparkSession sparkSession) {
    // create the session
    CortexSession session = CortexSession.newSession(sparkSession);

    // create the session and apply properties for the CortexSession
    CortexSession alternativeSession = CortexSession.newSession(sparkSession, Map.of(
        "spark.cortex.catalog.impl", "com.c12e.cortex.phoenix.LocalCatalog",
        "spark.cortex.catalog.local.dir",  "src/main/resources/spec",
    ));
}
```

See [SessionExample.java](src/main/java/com/c12e/cortex/examples/local/SessionExample.java) for an example.

### Catalog

The application is configured to use a local Cortex Catalog with the catalog directory pointing to `spec/` in
the [application resources](./src/main/resources/spec). The configuration options can be seen in
the [spark-conf.json](./src/main/resources/spark-conf.json) file.

```json
{
  "options": {
    "spark.cortex.catalog.impl": "com.c12e.cortex.phoenix.LocalCatalog",
    "spark.cortex.catalog.local.dir":  "src/main/resources/spec",
  }
}
```

The `CortexSession` exposes a client to the [Cortex Catalog](../docs/catalog.md) and can be used to access cortex resources.
```java
public void useCortexCatalog(CortexSession cortexSession) {
    Catalog catalog = cortexSession.catalog();
    // get a connection
    Connection connection = catalog.getConnection("project", "conn-name");
    System.out.println(connection.getName());

    // list the connections in a project
    Iterable<Connection> connections = catalog.listConnections("project");

    // delete and re-create a data source in the Cortex Catalog
    DataSource ds = catalog.getDataSource("project", "data-source-name");
    catalog.deleteDataSource("project", "data-source-name");
    catalog.saveDataSource(ds);
}
```
See [SessionExample.java](src/main/java/com/c12e/cortex/examples/local/SessionExample.java) for an example of the above.

**NOTE**: While the Cortex Catalog is accessible from the `CortexSession`, it is not the entrypoint for reading and writing data!

### Connections

`Connections` for the local catalog are defined in [connections.yaml](./src/main/resources/spec/connections.yaml). There
are 4 connections defined which are each associated with local csv/parquet [files](./src/main/resources/data):
- [member-base-file](./src/main/resources/data/members_100_v14.csv) - contains base member information
- [member-feedback-file](./src/main/resources/data/feedback_100_v14.csv) - contains member feedback information
- [member-flu-risk-file](./src/main/resources/data/member_flu_risk_100_v14.parquet) - contains a predicted member flu risk score
- member-joined-file - this Connection will contain the results of merging the other Connections

See [JoinConnections.java](./src/main/java/com/c12e/cortex/examples/local/JoinConnections.java) for a full example.
```bash
./gradlew main-app:run --args="join-connections -p local -l member-base-file -r member-feedback-file -w member-joined-file -c member_id"
```

### Data Sources

`DataSources` in the local Catalog are defined in [datasources.yaml](./src/main/resources/spec/datasources.yaml). There
are 3 DataSources defined in the Catalog, each associated with a corresponding connection:
- member-base-ds
- member-feedback-file-ds
- member-flu-risk-file-ds

See [DataSourcesRW.java](./src/main/java/com/c12e/cortex/examples/local/DataSourceRW.java) for an example of refreshing
a DataSource.

Run this example from the parent directory:
```bash
./gradlew main-app:run --args="datasource-refresh -p local -d member-base-ds"
```

Run this example in a docker container:
```bash
make clean build create-app-image

docker run -p 4040:4040 --entrypoint="python" -e CORTEX_TOKEN="xxx" \
  -v $(pwd)/local-clients/src/main/resources/conf:/app/conf \
  -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
  -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"
```

Notes:
* The `CORTEX_TOKEN` environment variable is required by the Spark Submit wrapper
* Port 4040 is forwarded from the container to expose the Spark UI
* The 1st volume mount is sharing the options 
* The 2cd volume mount shares the LocalCatalog contents with the container, and the Spark-submit python script
* The 3rd volume mount is the output of the joined connection

### Secrets

The example is configured to use a [local Secret Client](../docs/secrets.md) implemented in this package. The
configuration options can be seen in the [spark-conf.json](./src/main/resources/spark-conf.json) file:

```json
{
  "options": {
    "spark.cortex.clients.secrets.impl": "com.c12e.cortex.examples.local.CustomSecretsClient"
  }
}
```

The Secret Client is not intended to be accessed from the `CortexSession`. The specified implementation is used
internally when using Connections.

See [CustomSecretsClient.java](src/main/java/com/c12e/cortex/examples/local/CustomSecretsClient.java) for an example of the above.

## Instructions

To run the example locally w/ gradle (from the parent directory): 
```
# run JoinConnections example
./gradlew main-app:run --args="join-connections -p local -l member-base-file -r member-feedback-file -w member-joined-file -c member_id"

# run DataSource refresh example
./gradlew main-app:run --args="datasource-refresh -p local -d member-base-ds"
```

<!-- To run this example in a docker image: -->
The Spark Configuration for this example is available at [spark-conf.json](./src/main/resources/spark-conf.json).

## Resources
* [Picocli](https://picocli.info/)