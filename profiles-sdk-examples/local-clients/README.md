# Local Clients

This example is intended to introduce working with the Cortex Profiles SDK in local development environment. This
example specifically introduces:
- creating a `CortexSession` with [Spark](https://spark.apache.org/docs/latest/index.html) in local mode
- using a local [Cortex Catalog](../docs/catalog.md)
- using a local [Cortex Secret Client](../docs/secrets.md)
- loading Cortex configuration properties from a Spark Config file

## Introduction

The entrypoint to the Profiles SDK is the `CortexSession`, a session based API around Spark and the `SparkSession`.
The `CortexSession` can be created via a static factory method after having initialized a `SparkSession`. Configuration
options will be taken from the provided `SparkSession` and can also be overridden, e.g.
```java
public void createCortexSession(SparkSession sparkSession) {
    // create the session
    CortexSession session = CortexSession.newSession(sparkSession);

    // create the session and apply properties programmatically
    CortexSession alternativeSession = CortexSession.newSession(sparkSession, Map.of(
        "spark.cortex.catalog.impl", "com.c12e.cortex.phoenix.LocalCatalog",
        "spark.cortex.catalog.local.dir",  "src/main/resources/spec",

        // a static variable is used for the secret client implementation key
        // the '.getName()' method is useful for setting an implementation value
        CortexSession.SECRETS_CLIENT_KEY, CustomSecretClient.class.getName()
        ));
}
```

See [SessionExample.java](src/main/java/com/c12e/cortex/examples/local/SessionExample.java) for an example.

### Catalog

The application is configured to use a local Cortex Catalog with the catalog directory pointing to `spec/` in
the [application resources](../main-app/src/main/resources/spec). The configuration options can be seen in
the `spark-conf.json` files in other examples, e.g.

```json
{
  "options": {
    "spark.cortex.catalog.impl": "com.c12e.cortex.phoenix.LocalCatalog",
    "spark.cortex.catalog.local.dir":  "src/main/resources/spec"
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

#### Connections

`Connections` for the local catalog are defined in [connectors.yaml](../main-app/src/main/resources/spec/connectors.yml). There
are 4 connections defined which are each associated with local csv/parquet [files](../main-app/src/main/resources/data):
- [member-base-file](../main-app/src/main/resources/data/members_100_v14.csv) - contains base member information
- [member-feedback-file](../main-app/src/main/resources/data/feedback_100_v14.csv) - contains member feedback information
- [member-flu-risk-file](../main-app/src/main/resources/data/member_flu_risk_100_v14.parquet) - contains a predicted member flu risk score
- member-joined-file - this Connection will contain the results of merging the other Connections

See [join-connections](../join-connections/README.md) for an example of using Connections.

#### Data Sources

`DataSources` in the local Catalog are defined in [datasources.yml](../main-app/src/main/resources/spec/datasources.yml). There
are 3 DataSources defined, each associated with a corresponding connection:
- member-base-ds
- member-feedback-file-ds
- member-flu-risk-file-ds

See [datasource-refresh](../datasource-refresh/README.md) for an example of refreshing  a DataSource.

#### Profile Schemas

`ProfileSchemas` in the local Catalog are defined in [profileSchemas.yml](../main-app/src/main/resources/spec/profileSchemas.yml). There
are 2 ProfileSchemas defined in the catalog:
- `member-profile`, this `ProfileSchema` represents a member and is the result of joining the `member-base-ds` and `member-flu-risk-file-ds` DataSouces.
- `member-profile-no-job`

See [build-profiles](../build-profiles/README.md) for an example of using ProfileSchemas.

### Secrets

The example is configured to use a [local Secret Client](../docs/secrets.md) implemented in this package. The
configuration options can be seen in the `spark-conf.json` files:

```json
{
  "options": {
    "spark.cortex.client.secrets.impl": "com.c12e.cortex.examples.local.CustomSecretsClient"
  }
}
```

The Secret Client is not intended to be accessed directly from the `CortexSession`. The specified implementation is used
internally when using Connections.

See [CustomSecretsClient.java](src/main/java/com/c12e/cortex/examples/local/CustomSecretsClient.java) for an example of the above.

### Local Cortex Backend

The example is configured to use the local filesystem (`./main-app/build/test-data`) as the backend storage implementation for Cortex.
See [Local Managed Content and Profile Data](../docs/catalog.md#local-managed-content-and-profile-data) for more details.
