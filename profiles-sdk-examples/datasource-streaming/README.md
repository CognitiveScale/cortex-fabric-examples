# Streaming To a Data Source

This example is contains a CLI application for refreshing a Data Source via streaming. This builds off of
the [Local Clients](../local-clients/README.md) example for its setup but uses a different set of Connections and Data Source for this example defined in [streaming-connections.yml](../main-app/src/main/resources/spec/streaming-connections.yml) and [streaming-datasource.yml](../main-app/src/main/resources/spec/streaming-datasources.yml) respectively.

(See [StreamDataSource.java](./src/main/java/com/c12e/cortex/examples/streaming/StreamingDataSource.java) for the full source.)

## Prerequisites:

**NOTE:** Streaming is only supported for S3 File Stream and GCS File Stream Connection types. (See [Connection Types](https://cognitivescale.github.io/cortex-fabric/docs/reference-guides/connection-types).)
This example assumes an S3 File Stream is being used, but you can update Connection definition and Secrets accordingly.

* Upload data to S3 that can be used for the Connection. You can optionally use the [member dataset](../main-app/src/main/resources/data/members_100_v14.csv) used in local examples.
* Update the `member-base-s3-stream` [S3 File Stream](https://cognitivescale.github.io/cortex-fabric/docs/reference-guides/connection-types#s3-file-stream-connections)
  Connection parameters with your data. Specifically: `uri`, `streamReadDir`, `s3Endpoint`, `publicKey`, and `secretKey`
  (this should be a [Secret](https://cognitivescale.github.io/cortex-fabric/docs/administration/secrets)).
* Update the [CustomSecretsClient](../local-clients/README.md#secrets) to load the Secret. For example, supposing the Connection's `secretKey` is `#SECURE.streaming-secret` and the key is loaded from the `STREAMING_SECRET_KEY`:
```java
public class CustomSecretsClient extends LocalSecretClient {
    private static final String STREAMING_SECRET_ENV = "STREAMING_SECRET_KEY";
    private static final LocalSecretClient.LocalSecrets localSecrets = new LocalSecretClient.LocalSecrets();
    static {{
        localSecrets.setSecretsForProject("local", Map.of(
                "streaming-secret", System.getenv(STREAMING_SECRET_ENV)
        ));
    }}
    public CustomSecretsClient() {
        super(localSecrets);
        if (System.getenv(STREAMING_SECRET_ENV) == null) { // missing Secret key
            throw new RuntimeException(String.format("Missing environment variable '%s' for local Secrets client", STREAMING_SECRET_ENV));
        }
    }
}
```

This example additionally uses a `StreamingQueryListener` to log the streaming progress, which ends after a sequence of empty polls.

## Run Locally

To run this example locally with local Cortex clients (from the parent directory):
1. Build the application.
    ```
    make build
    ```
2. Export the Secret value for your streaming Connection.
    ```
    export STREAMING_SECRET_KEY=<value>
    ```
4. Run the application with Gradle.
    ```
   ./gradlew main-app:run --args="ds-streaming --project local --data-source member-base-s3-stream-write"
    ```

The end of the log output should be similar to:
```
> Task :main-app:run
...

14:54:14.115 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionStreamReader - Inferred schema from sample of connection (CSV) - project: 'local', connectionName: 'member-base-s3-stream'
14:54:14.558 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionStreamReader - Finished reading static sample from connection (CSV) - project: 'local', connectionName: 'member-base-s3-stream'
14:54:14.558 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionStreamReader - Loading streaming dataset - project: 'local', connectionName: 'member-base-s3-stream', maxFilesPerTrigger: '1'
14:54:14.787 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionStreamReader - Finished loading streaming dataset - project: 'local', connectionName: 'member-base-s3-stream', maxFilesPerTrigger: '1'
14:54:15.398 [main] INFO  c.c.c.e.s.StreamingDataSource - Static dataframe has 100 rows
14:54:15.398 [main] INFO  c.c.c.e.s.StreamingDataSource - Starting stream
14:54:19.349 [main] WARN  o.a.s.s.e.s.ResolveWriteToStream - spark.sql.adaptive.enabled is not supported in streaming DataFrames/Datasets and will be disabled.
14:54:19.490 [stream execution thread for [id = ff33409b-dbf4-458d-bec3-d032e51614bb, runId = dfc864d3-b8fe-47fe-b088-85262463864b]] INFO  c.c.c.e.s.StreamingDataSource - STREAMING LISTENER: Streaming Query started
14:54:20.561 [stream execution thread for [id = ff33409b-dbf4-458d-bec3-d032e51614bb, runId = dfc864d3-b8fe-47fe-b088-85262463864b]] DEBUG c.c.c.p.m.d.DefaultCortexDataSourceStreamWriter - Starting to merge batch '0' while writing stream to DataSource - project: 'local', sourceName: 'member-base-s3-stream-write'
14:54:23.387 [stream execution thread for [id = ff33409b-dbf4-458d-bec3-d032e51614bb, runId = dfc864d3-b8fe-47fe-b088-85262463864b]] WARN  o.a.spark.sql.catalyst.util.package - Truncated the string representation of a plan since it was too large. This behavior can be adjusted by setting 'spark.sql.debug.maxToStringFields'.
14:54:26.039 [stream execution thread for [id = ff33409b-dbf4-458d-bec3-d032e51614bb, runId = dfc864d3-b8fe-47fe-b088-85262463864b]] DEBUG c.c.c.p.m.d.DefaultCortexDataSourceStreamWriter - Finished to merge batch '0' when writing stream to DataSource - project: 'local', sourceName: 'member-base-s3-stream-write'
14:54:26.164 [spark-listener-group-streams] INFO  c.c.c.e.s.StreamingDataSource - STREAMING LISTENER: Streaming Query in progress
14:54:26.177 [spark-listener-group-streams] INFO  c.c.c.e.s.StreamingDataSource - {
  "id" : "ff33409b-dbf4-458d-bec3-d032e51614bb",
  "runId" : "dfc864d3-b8fe-47fe-b088-85262463864b",
  "name" : null,
  "timestamp" : "2022-07-08T19:54:19.522Z",
  "batchId" : 0,
  "numInputRows" : 10000,
  "inputRowsPerSecond" : 0.0,
  "processedRowsPerSecond" : 1509.2061575611228,
  "durationMs" : {
    "addBatch" : 5534,
    "getBatch" : 292,
    "latestOffset" : 569,
    "queryPlanning" : 23,
    "triggerExecution" : 6625,
    "walCommit" : 97
  },
  "stateOperators" : [ ],
  "sources" : [ {
    "description" : "FileStreamSource[s3a://fabric-dev/members-streaming]",
    "startOffset" : null,
    "endOffset" : {
      "logOffset" : 0
    },
    "latestOffset" : null,
    "numInputRows" : 10000,
    "inputRowsPerSecond" : 0.0,
    "processedRowsPerSecond" : 1509.2061575611228
  } ],
  "sink" : {
    "description" : "ForeachBatchSink",
    "numOutputRows" : -1
  }
}
14:54:26.177 [spark-listener-group-streams] INFO  c.c.c.e.s.StreamingDataSource - STREAMING LISTENER: No processing occurred in last poll, stopping in 3 poll intervals
14:54:40.086 [spark-listener-group-streams] INFO  c.c.c.e.s.StreamingDataSource - STREAMING LISTENER: Streaming Query in progress
14:54:40.087 [spark-listener-group-streams] INFO  c.c.c.e.s.StreamingDataSource - {
  "id" : "ff33409b-dbf4-458d-bec3-d032e51614bb",
  "runId" : "dfc864d3-b8fe-47fe-b088-85262463864b",
  "name" : null,
  "timestamp" : "2022-07-08T19:54:40.003Z",
  "batchId" : 1,
  "numInputRows" : 0,
  "inputRowsPerSecond" : 0.0,
  "processedRowsPerSecond" : 0.0,
  "durationMs" : {
    "latestOffset" : 81,
    "triggerExecution" : 82
  },
  "stateOperators" : [ ],
  "sources" : [ {
    "description" : "FileStreamSource[s3a://fabric-dev/members-streaming]",
    "startOffset" : {
      "logOffset" : 0
    },
    "endOffset" : {
      "logOffset" : 0
    },
    "latestOffset" : null,
    "numInputRows" : 0,
    "inputRowsPerSecond" : 0.0,
    "processedRowsPerSecond" : 0.0
  } ],
  "sink" : {
    "description" : "ForeachBatchSink",
    "numOutputRows" : -1
  }
}
14:54:40.087 [spark-listener-group-streams] INFO  c.c.c.e.s.StreamingDataSource - STREAMING LISTENER: No processing occurred in last poll, stopping in 2 poll intervals
14:54:50.161 [spark-listener-group-streams] INFO  c.c.c.e.s.StreamingDataSource - STREAMING LISTENER: Streaming Query in progress
14:54:50.161 [spark-listener-group-streams] INFO  c.c.c.e.s.StreamingDataSource - {
  "id" : "ff33409b-dbf4-458d-bec3-d032e51614bb",
  "runId" : "dfc864d3-b8fe-47fe-b088-85262463864b",
  "name" : null,
  "timestamp" : "2022-07-08T19:54:50.003Z",
  "batchId" : 1,
  "numInputRows" : 0,
  "inputRowsPerSecond" : 0.0,
  "processedRowsPerSecond" : 0.0,
  "durationMs" : {
    "latestOffset" : 156,
    "triggerExecution" : 157
  },
  "stateOperators" : [ ],
  "sources" : [ {
    "description" : "FileStreamSource[s3a://fabric-dev/members-streaming]",
    "startOffset" : {
      "logOffset" : 0
    },
    "endOffset" : {
      "logOffset" : 0
    },
    "latestOffset" : null,
    "numInputRows" : 0,
    "inputRowsPerSecond" : 0.0,
    "processedRowsPerSecond" : 0.0
  } ],
  "sink" : {
    "description" : "ForeachBatchSink",
    "numOutputRows" : -1
  }
}
14:54:50.161 [spark-listener-group-streams] INFO  c.c.c.e.s.StreamingDataSource - STREAMING LISTENER: No processing occurred in last poll, stopping in 1 poll intervals
14:55:10.088 [spark-listener-group-streams] INFO  c.c.c.e.s.StreamingDataSource - STREAMING LISTENER: Streaming Query in progress
14:55:10.088 [spark-listener-group-streams] INFO  c.c.c.e.s.StreamingDataSource - STREAMING LISTENER: Initiating Streaming Query stop
14:55:10.100 [main] INFO  c.c.c.e.s.StreamingDataSource - Finished process
14:55:10.101 [spark-listener-group-streams] INFO  c.c.c.e.s.StreamingDataSource - {
  "id" : "ff33409b-dbf4-458d-bec3-d032e51614bb",
  "runId" : "dfc864d3-b8fe-47fe-b088-85262463864b",
  "name" : null,
  "timestamp" : "2022-07-08T19:55:10.004Z",
  "batchId" : 1,
  "numInputRows" : 0,
  "inputRowsPerSecond" : 0.0,
  "processedRowsPerSecond" : 0.0,
  "durationMs" : {
    "latestOffset" : 82,
    "triggerExecution" : 84
  },
  "stateOperators" : [ ],
  "sources" : [ {
    "description" : "FileStreamSource[s3a://fabric-dev/members-streaming]",
    "startOffset" : {
      "logOffset" : 0
    },
    "endOffset" : {
      "logOffset" : 0
    },
    "latestOffset" : null,
    "numInputRows" : 0,
    "inputRowsPerSecond" : 0.0,
    "processedRowsPerSecond" : 0.0
  } ],
  "sink" : {
    "description" : "ForeachBatchSink",
    "numOutputRows" : -1
  }
}
```

**NOTE**: Because this is a running with a local Catalog and local filesystem as the Cortex backend, the result of
the `member-base-s3-stream` Data Source is written to a local
Delta Table (`../main-app/build/test-data/cortex-profiles/sources/local/member-base-s3-stream-write-delta/`) that does
not exist prior to running.

## Run Locally in a Docker Container With Spark-submit

Make sure to update the [Spark-submit config file](./src/main/resources/conf/spark-conf.json) with the appropriate Connection name.
To run this example in a Docker container with local Cortex clients (from the parent directory):

1. Build the application.
    ```
    make build
    ```
1. Create the Skill Docker image.
    ```
    make create-app-image
    ```
2. Export the Secret value for your streaming Connection and a Cortex token.
    ```
    export STREAMING_SECRET_KEY=<value>
    export CORTEX_TOKEN=<token>
    ```
4. Run the application with Docker.
    ```
    docker run -p 4040:4040 --entrypoint="python" \
      -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
      -e STREAMING_SECRET_KEY="${STREAMING_SECRET_KEY}" \
      -v $(pwd)/datasource-streaming/src/main/resources/conf:/app/conf \
      -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
      -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
      profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"
    ```
   NOTES:
    * The `$CORTEX_TOKEN` environment variable is required by the Spark-submit wrapper and needs to be a valid JWT token. You can generate this via: `cortex configure token`.
    * Port 4040 is forwarded from the container to expose the Spark UI (for debugging).
    * The first volume mount is sharing the [Spark-submit config file](./src/main/resources/conf/spark-conf.json).
    * The second volume mount shares the LocalCatalog contents and other local application resources.
    * The third volume mount sharing the output of the local Data Source.

## Run as a Skill

### Prerequisites
* Ensure that the Cortex resources exist, specifically the Project, Data Source, and underlying Connection.
* Generate a `CORTEX_TOKEN`.
* Update the [spark-conf.json](./src/main/resources/conf/spark-conf.json) file to:
   - Use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the in-cluster GraphQL API endpoint (`"http://cortex-api.cortex.svc.cluster.local:8080"`) and removing the Local Catalog implementation (`spark.cortex.catalog.impl`).
   - Use the [remote storage client](../docs/backendstorage.md#remote-storage-client) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint, and remove the local storage client implementation (`spark.cortex.client.storage.impl`).
   - Remove the local Secret client implementation (`spark.cortex.client.secrets.impl`).
   - Update the `app_command` arguments to match your Cortex Project and Data Source (`--project`,  `--data-source`).

To run this example in Spark cluster mode against a Cortex Cluster with access to the Catalog and Secrets, you must
update the spark configuration file used by the main application (`main-app/src/main/resources/conf/spark-conf.json`) to
match configuration for this example.

Refer to the [instructions for running the Skill Template](../README.md#skill-template) in the top level README for
deploying and invoking the skill.

### Example
```json
{
   "pyspark": {
      "pyspark_bin": "bin/spark-submit",
      "app_command": [
         "ds-streaming",
         "--project",
         "laguirre-testi-69257",
         "--data-source",
         "members-stream-ac496"
      ],
      "app_location": "local:///app/libs/app.jar",
      "options": {
         "--master": "k8s://https://kubernetes.default.svc:443",
         "--deploy-mode": "cluster",
         "--name": "profile-examples",
         "--class": "com.c12e.cortex.examples.Application",
         "--conf": {
            "spark.app.name": "CortexProfilesExamples",
            "spark.cortex.client.phoenix.url": "http://cortex-api.cortex.svc.cluster.local:8080",
            "spark.cortex.client.secrets.url": "http://cortex-accounts.cortex.svc.cluster.local:5000",
            "spark.cortex.catalog.impl": "com.c12e.cortex.profiles.catalog.CortexRemoteCatalog",
            "spark.executor.cores": 1,
            "spark.executor.instances": 2,
            "spark.executor.memory": "2g",
            "spark.driver.memory": "2g",
            "spark.kubernetes.authenticate.driver.serviceAccountName": "default",
            "spark.kubernetes.namespace": "cortex-compute",
            "spark.kubernetes.driver.master": "https://kubernetes.default.svc",
            "spark.kubernetes.driver.container.image": "private-registry.dci-dev.dev-eks.insights.ai/profiles-example:latest",
            "spark.kubernetes.executor.container.image": "private-registry.dci-dev.dev-eks.insights.ai/profiles-example:latest",
            "spark.kubernetes.driver.podTemplateContainerName": "fabric-action",
            "spark.kubernetes.executor.annotation.traffic.sidecar.istio.io/excludeOutboundPorts": "7078,7079",
            "spark.kubernetes.driver.annotation.traffic.sidecar.istio.io/excludeInboundPorts": "7078,7079",
            "spark.kubernetes.container.image.pullPolicy": "Always",

            "spark.ui.enabled":"false",
            "spark.ui.prometheus.enabled": "false",
            "spark.sql.streaming.metricsEnabled": "false",
            "spark.executor.processTreeMetrics.enabled": "false",
            "spark.metrics.conf.*.sink.prometheusServlet.class": "org.apache.spark.metrics.sink.PrometheusServlet",
            "spark.metrics.conf.*.sink.prometheusServlet.path": "/metrics/prometheus",
            "spark.metrics.conf.master.sink.prometheusServlet.path": "/metrics/master/prometheus",
            "spark.metrics.conf.applications.sink.prometheusServlet.path": "/metrics/applications/prometheus",

            "spark.delta.logStore.gs.impl": "io.delta.storage.GCSLogStore",
            "spark.hadoop.fs.AbstractFileSystem.gs.impl": "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS",
            "spark.sql.shuffle.partitions": "10",
            "spark.hadoop.fs.s3a.impl": "org.apache.hadoop.fs.s3a.S3AFileSystem",
            "spark.hadoop.fs.s3a.fast.upload.buffer": "disk",
            "spark.hadoop.fs.s3a.fast.upload": "true",
            "spark.hadoop.fs.s3a.block.size": "128M",
            "spark.hadoop.fs.s3a.multipart.size": "512M",
            "spark.hadoop.fs.s3a.multipart.threshold": "512M",
            "spark.hadoop.fs.s3a.fast.upload.active.blocks": "2048",
            "spark.hadoop.fs.s3a.committer.threads": "2048",
            "spark.hadoop.fs.s3a.max.total.tasks": "2048",
            "spark.hadoop.fs.s3a.threads.max": "2048",
            "spark.sql.extensions": "io.delta.sql.DeltaSparkSessionExtension",
            "spark.sql.catalog.spark_catalog": "org.apache.spark.sql.delta.catalog.DeltaCatalog",
            "spark.databricks.delta.schema.autoMerge.enabled": "true",
            "spark.databricks.delta.merge.repartitionBeforeWrite.enabled": "true"
         }
      }
   }
}
```
