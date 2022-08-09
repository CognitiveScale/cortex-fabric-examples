# Local Clients

This example is intended to introduce working with the Cortex Profiles SDK in local development environment. This
example specifically introduces:
- Creating a `CortexSession` with [Spark](https://spark.apache.org/docs/latest/index.html) in local mode.
- Using a local [Cortex Catalog](../docs/catalog.md).
- Using a local [Cortex Secret Client](../docs/secrets.md).
- Using a local [Remote Storage Client](../docs/backendstorage.md).
- Loading Cortex configuration properties from a Spark Config file and from programatically.

## Introduction

The entrypoint to the Profiles SDK is the `CortexSession`, a session-based API around Spark and the `SparkSession`.
The `CortexSession` can be created via a static factory method after having initialized a `SparkSession`. Configuration
options are taken from the provided `SparkSession` and can also be overridden, for example
```java
public void createCortexSession(SparkSession sparkSession) {
    // Create the session using options taken from the SparkSession
    CortexSession session = CortexSession.newSession(sparkSession);

    // Create the session and apply properties programmatically
    CortexSession alternativeSession = CortexSession.newSession(sparkSession, Map.of(
        // The '.getName()' method is useful for setting a class implementation.
        "spark.cortex.catalog.impl", LocalCatalog.class.getName(),
        "spark.cortex.catalog.local.dir",  "src/main/resources/spec",

        // A static variable is used for the Secret client implementation key.
        CortexSession.SECRETS_CLIENT_KEY, CustomSecretClient.class.getName()
        ));
}
```

(See [SessionExample.java](src/main/java/com/c12e/cortex/examples/local/SessionExample.java) for the above example).

### Catalog

The application is configured to use a local Cortex Catalog with the catalog directory pointing to `spec/` in
the [application resources](../main-app/src/main/resources/spec). The configuration options can be seen in
the `spark-conf.json` files in other examples, for example:

```json
{
  "options": {
    "spark.cortex.catalog.impl": "com.c12e.cortex.phoenix.LocalCatalog",
    "spark.cortex.catalog.local.dir":  "src/main/resources/spec"
  }
}
```

The `CortexSession` exposes a client to the [Cortex Catalog](../docs/catalog.md) and can be used to access Cortex resources.
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
(See [CatalogExample.java](src/main/java/com/c12e/cortex/examples/local/CatalogExample.java) for the above example.)

**NOTE**: While the Cortex Catalog is accessible from the `CortexSession`, it is not the entrypoint for reading and writing data!

#### Connections

`Connections` for the local catalog are defined in [connectors.yaml](../main-app/src/main/resources/spec/connectors.yml).
Four connections are defined, and each is associated with a local csv or parquet [file](../main-app/src/main/resources/data):
- [member-base-file](../main-app/src/main/resources/data/members_100_v14.csv) - contains base member information
- [member-feedback-file](../main-app/src/main/resources/data/feedback_100_v14.csv) - contains member feedback information
- [member-flu-risk-file](../main-app/src/main/resources/data/member_flu_risk_100_v14.parquet) - contains a predicted member flu risk score
- member-joined-file - this Connection will contain the results of merging the other Connections

(See [join-connections](../join-connections/README.md) for an example of using Connections.)

#### Data Sources

Data Sources in the local Catalog are defined in [datasources.yml](../main-app/src/main/resources/spec/datasources.yml).
Three Data Sources are defined, and each is associated with a corresponding connection.
- member-base-ds
- member-feedback-file-ds
- member-flu-risk-file-ds

(See [datasource-refresh](../datasource-refresh/README.md) for an example of refreshing a Data Source.)

#### Profile Schemas

`ProfileSchemas` in the local Catalog are defined in [profileSchemas.yml](../main-app/src/main/resources/spec/profileSchemas.yml). 
Two Profile Schemas are defined in the Catalog:
- `member-profile`, this Profile Schema represents a member and is the result of joining the `member-base-ds` and `member-flu-risk-file-ds` Data Sources.
- `member-profile-no-job`

(See [build-profiles](../build-profiles/README.md) for an example of using Profile Schemas.)

### Secrets

The application is configured to use a local [Secret client](../docs/secrets.md) implemented in this package. The
configuration options can be in the `spark-conf.json` files in other examples, for example:
```json
{
  "options": {
    "spark.cortex.client.secrets.impl": "com.c12e.cortex.examples.local.CustomSecretsClient"
  }
}
```

The Secret Client is not intended to be accessed directly from the `CortexSession`. The specified implementation is used
internally when using Connections.

(See [CustomSecretsClient.java](src/main/java/com/c12e/cortex/examples/local/CustomSecretsClient.java) for an example of
using Secrets.)

### Local Cortex Backend

The application is configured to use the local filesystem (`./main-app/build/test-data`) as Cortex's remote storage.
(See [Local Storage Client](../docs/backendstorage.md#local-remote-storage-client) for more details.)
