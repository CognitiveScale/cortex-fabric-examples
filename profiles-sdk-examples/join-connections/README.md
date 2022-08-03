# Join Connections

This example is a CLI application for Joining two Cortex Connections and saving the resulting dataset to
another Cortex Connection. This builds off of the [Local Clients](../local-clients/README.md) example for its setup.

(See [JoinConnections.java](./src/main/java/com/c12e/cortex/examples/joinconn/JoinConnections.java) for the full source.)

## Run Locally

To run this example locally with local Cortex clients (from the parent directory):
1. Build the application.
    ```
    make build
    ```
1. Run the application with Gradle.
    ```
    ./gradlew main-app:run --args="join-connections --project local -l member-base-file -r member-flu-risk-file -w member-joined-file -c member_id"
    ```

This will merge the following connections defined in the local Catalog, and will populate the `member-joined-file` [Connection](../local-clients/README.md#connections) at `main-app/build/tmp/test-data/joined_v14.csv`:
- `member-base-file`
- `member-flu-risk-file`

<!-- Flowchart showing merge of Connections to result connection. I wish mermaid had [venn diagrams](https://github.com/mermaid-js/mermaid/issues/2583) -->
```mermaid
flowchart TB
  member-base-file --> id1{"Inner Join on member_id"}
  member-flu-risk-file --> id1{"Inner Join on member_id"}
  id1{"Inner Join on member_id"} --> member-joined-file
```

**NOTE:** Because this is running with a local Catalog and local filesystem as the Cortex backend, the result of the
Connection will be written to a local file and does not exist prior to running the above.

## Run Locally in a Docker Container With Spark-submit

To run this example in a Docker container with local Cortex clients (from the parent directory):
1. Build the application.
    ```
    make build
    ```
2. Create the Skill Docker image.
    ```
    make create-app-image
    ```
3. Export a Cortex token.
    ```
    export CORTEX_TOKEN=<token>
    ```
4. Run the application with Docker.
    ```
    docker run -p 4040:4040 --entrypoint="python" \
      -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
      -v $(pwd)/join-connections/src/main/resources/conf:/app/conf \
      -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
      -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
      profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"
    ```
   NOTES:
    * The `$CORTEX_TOKEN` environment variable is required by the Spark-submit wrapper, and needs to be a valid JWT token. You can generate this via: `cortex configure token`.
    * Port 4040 is forwarded from the container to expose the Spark UI (for debugging)
    * The first volume mount is sharing the [Spark-submit config file](./src/main/resources/conf/spark-conf.json).
    * The second volume mount shares the LocalCatalog contents and other local application resources.
    * The third volume mount is the output location of the joined connection.

The logs should be similar to:
```
['/opt/spark/bin/spark-submit', '--master', 'local[*]', '--class', 'com.c12e.cortex.examples.Application', '--conf', 'spark.app.name=CortexProfilesExamples', '--conf', 'spark.ui.enabled=true', '--conf', 'spark.ui.prometheus.enabled=true', '--conf', 'spark.sql.streaming.metricsEnabled=true', '--conf', 'spark.cortex.catalog.impl=com.c12e.cortex.phoenix.LocalCatalog', '--conf', 'spark.cortex.catalog.local.dir=src/main/resources/spec', '--conf', 'spark.cortex.client.secrets.impl=com.c12e.cortex.examples.local.CustomSecretsClient', '--conf', 'spark.cortex.storage.storageType=file', '--conf', 'spark.cortex.storage.file.baseDir=src/main/resources/data', '--conf', 'spark.kubernetes.driverEnv.CORTEX_TOKEN=eyJhbGciOiJFZERTQSIsImtpZCI6Im5YMHZfcjdiMGJKOC1UVW5Sc3U2cHB2OFVUX0szYVMzdE11d3JzVVp1aEEifQ.eyJzdWIiOiI5ZGMxMjY2Mi1jZDUxLTQ5NDYtYTdmYy0zMTJmZWNlNzg5NTEiLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20iLCJpYXQiOjE2NTcxMzkxMDUsImV4cCI6MTY1Nzc0MzkwNX0.XoOTCwmbe6Ja2HQKNug5YSxze8r9pCdFEYzFJULhkTPBb8OPe9CGNvuymPwgBWyooBSy9rMrOehhv9ay8LGbCw', '--conf', 'spark.cortex.phoenix.token=eyJhbGciOiJFZERTQSIsImtpZCI6Im5YMHZfcjdiMGJKOC1UVW5Sc3U2cHB2OFVUX0szYVMzdE11d3JzVVp1aEEifQ.eyJzdWIiOiI5ZGMxMjY2Mi1jZDUxLTQ5NDYtYTdmYy0zMTJmZWNlNzg5NTEiLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20iLCJpYXQiOjE2NTcxMzkxMDUsImV4cCI6MTY1Nzc0MzkwNX0.XoOTCwmbe6Ja2HQKNug5YSxze8r9pCdFEYzFJULhkTPBb8OPe9CGNvuymPwgBWyooBSy9rMrOehhv9ay8LGbCw', 'local:///app/libs/app.jar', 'join-connections', '-p', 'local', '-l', 'member-base-file', '-r', 'member-flu-risk-file', '-w', 'member-joined-file', '-c', 'member_id']
  ...

21:29:16.717 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: csv, uri: ./src/main/resources/data/members_100_v14.csv, extra
21:29:20.747 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Inferred schema from sample of connection (CSV) - project: 'local', connectionName: 'member-base-file'
21:29:20.778 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Finished reading connection (CSV) - project: 'local', connectionName: 'member-base-file'
21:29:20.787 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: parquet, uri: ./src/main/resources/data/member_flu_risk_100_v14.parquet, extra
21:29:20.787 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Reading connection from file_path './src/main/resources/data/member_flu_risk_100_v14.parquet'
21:29:21.231 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Writing to connection: './build/tmp/test-data/joined_v14.csv'
21:29:22.610 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@534c6767{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
21:29:22.613 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://ac5171d970ce:4040
Pod Name:
Container State:
Termination Reason:
Exit Code: 0
```

## Run Locally Against a Cortex Cluster

**Note**: Because this is running outside the Cortex Cluster you must know the value of any Secrets used by
the Connections. For access to Cortex Secrets try [running as a Skill](#run-as-a-skill).

### Prerequisites

To run this example in Spark local mode against a Cortex Cluster with access to remote storage, the Cortex Catalog, and
local Secrets, you must:
* Know the [backend storage configuration](../docs/config.md#cortex-backend-storage) for the Cluster, which includes
  names of buckets and access keys for remote storage. If you do not know this, then try [running as a Skill](#run-as-a-skill).
* Know the values of Cortex Secrets used by the Connection.
* Generate a `CORTEX_TOKEN`.
* Ensure the Cortex resources exist, specifically the Project and Joining Connections (Left, Right, and Result).
* Update the [spark-conf.json](./src/main/resources/conf/spark-conf.json) file to:
  - Use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint (e.g. `https://api.<domain>/fabric/v4/graphql`), and remove the local Catalog implementation (`spark.cortex.catalog.impl`).
  - Use the [remote storage client](../docs/backendstorage.md#remote-storage-client) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint, and remove the local storage client implementation (`spark.cortex.client.storage.impl`).
  - Update [Local Secret Client](../local-clients/README.md#secrets) with any Secrets required by your Connection(s). Ensure to update the project, Secret name, and Secret value.
  - Update the `app_command` arguments to match your Cortex project and Connections (`--project`,  `--left-conn`, `--right-conn`, `--write-conn` `--column`).

**NOTE**: If your connections do not use Cortex Secrets because the Cortex cluster has [IRSA enabled](https://cognitivescale.github.io/cortex-charts/docs/platforms/aws/aws-irsa), then you may
not be able to run this example without editing the Connection. This is because IRSA provides authentication within the
cluster, and cannot be leveraged when running locally. Try [running the example as a Skill](#run-as-a-skill).

### Example

**NOTE**: The cortex backend storage configuration could be set in the `spark-conf.json`, but environment variables are
used below to avoid hardcoding access keys in the source.

The below example command is assuming:
* The Cortex backend is using the `minio` instance packaged in  the [Cortex Charts](https://github.com/CognitiveScale/cortex-charts) with access and secret keys `xxxxx`/`xxxxx`.
* The [CustomSecretClient](../local-clients/README.md#secrets) provides the Secret used by the Connections (loaded from `CONNECTION_SECRET_VALUE`).

From the parent directory:
1. Build the application.
    ```
   make build
    ```
2. Create the Skill Docker image.
    ```
    make create-app-image
    ```
3. Export the Cortex token and (optionally) the Secret value used by your connection.
    ```
    export CORTEX_TOKEN=<token>
    export CONNECTION_SECRET_VALUE=<secret-value>
    ```
4. Run the application with Docker.
    ```
    docker run -p 4040:4040 --entrypoint="python" \
      -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
      -e CONNECTION_SECRET_VALUE="${CONNECTION_SECRET_VALUE}" \
      -e STORAGE_TYPE=s3 \
      -e AWS_ACCESS_KEY_ID=xxxxx \
      -e AWS_SECRET_KEY=xxxxx \
      -e S3_ENDPOINT=http://host.docker.internal:9000 \
      -v $(pwd)/join-connections/src/main/resources/conf:/app/conf \
      -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
      -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
    profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"
    ```

The tail end of the logs should look similar to:
```
23:37:27.873 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: csv, uri: s3a://fabric-dev-laguirre/members/members_v14_100.csv, extra
23:37:28.110 [main] WARN  o.a.h.metrics2.impl.MetricsConfig - Cannot locate configuration: tried hadoop-metrics2-s3a-file-system.properties,hadoop-metrics2.properties
23:37:33.636 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Inferred schema from sample of connection (CSV) - project: 'laguirre-testi-69257', connectionName: 'member-base'
23:37:34.071 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Finished reading connection (CSV) - project: 'laguirre-testi-69257', connectionName: 'member-base'
23:37:34.163 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: csv, uri: s3a://fabric-dev-laguirre/members/members_flu_risk_v14_100.csv, extra
23:37:34.302 [main] WARN  o.a.h.metrics2.impl.MetricsConfig - Cannot locate configuration: tried hadoop-metrics2-s3a-file-system.properties,hadoop-metrics2.properties
23:37:35.555 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Inferred schema from sample of connection (CSV) - project: 'laguirre-testi-69257', connectionName: 'members-flu-risk'
23:37:35.982 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Finished reading connection (CSV) - project: 'laguirre-testi-69257', connectionName: 'members-flu-risk'
23:37:36.201 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Writing to connection: 's3a://fabric-dev-laguirre/members/members_joined.csv'
23:37:36.541 [main] WARN  o.a.spark.sql.catalyst.util.package - Truncated the string representation of a plan since it was too large. This behavior can be adjusted by setting 'spark.sql.debug.maxToStringFields'.
23:37:37.641 [main] WARN  o.a.h.f.s.c.AbstractS3ACommitterFactory - Using standard FileOutputCommitter to commit work. This is slow and potentially unsafe.
23:37:39.262 [Executor task launch worker for task 0.0 in stage 5.0 (TID 13)] WARN  o.a.h.f.s.c.AbstractS3ACommitterFactory - Using standard FileOutputCommitter to commit work. This is slow and potentially unsafe.
23:37:43.945 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@c6c82aa{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
23:37:43.948 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://a0a8fdbb79c2:4040
Pod Name:
Container State:
Termination Reason:
Exit Code: 0
```

## Run as a Skill

### Prerequisites
* Ensure that the Cortex resources exist, specifically the Project and Joining Connections (Left, Right, and Result), and any corresponding Cortex Secrets.
* Generate a `CORTEX_TOKEN`.
* Update the [spark-conf.json](./src/main/resources/conf/spark-conf.json) file to:
  - Use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the in-cluster GraphQL API endpoint (`"http://cortex-api.cortex.svc.cluster.local:8080"`) and removing the Local Catalog implementation (`spark.cortex.catalog.impl`).
  - Use the [remote storage client](../docs/backendstorage.md#remote-storage-client) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint, and remove the local storage client implementation (`spark.cortex.client.storage.impl`).
  - Remove the local Secret client implementation (`spark.cortex.client.secrets.impl`).
  - Update the `app_command` arguments to match your Cortex project and Connections (`--project`,  `--left-conn`, `--right-conn`, `--write-conn` `--column`).

To run this example in Spark cluster mode against a Cortex Cluster with access to the Catalog and Secrets, you must
update the spark configuration file used by the main application (`main-app/src/main/resources/conf/spark-conf.json`) to
match configuration for this example.

Refer to the [instructions for running the Skill Template](../README.md#skill-template) in the top level README for
deploying and invoking the skill.

### Example

Example Spark configuration (`spark-conf.json`):
```json
{
  "pyspark": {
    "pyspark_bin": "bin/spark-submit",
    "app_command": [
      "join-connections",
      "-p",
      "testi-69257",
      "-l",
      "member-base",
      "-r",
      "members-flu-risk",
      "-w",
      "members-join",
      "-c",
      "member_id"
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
        "spark.executor.memory": "4g",
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

Notes on the above example:
* The `--master` and `--deploy-mode` have been set to run the Spark job in the Cortex (Kubernetes) Cluster.
* The Phoenix Client URL and Secret Client URL are referring to services in Kubernetes Cluster
* The Spark Driver and Spark Executors (`"spark.executor.instances"`) have a 2g and 4g of memory respectively. **Adjust the amount of resources used for your cluster/data.**
* The Cortex [Backend Storage configuration](../docs/config.md#cortex-backend-storage) is configured by the default remote
    storage client implementation.
