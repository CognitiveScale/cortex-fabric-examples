# Refresh a Data Source

This example is a CLI application for refreshing a Data Source by reading its Cortex Connection and writing the
dataset to the Data Source. This builds off of the [Local Clients](../local-clients/README.md) example for its setup.

(See [DataSourceRW.java](./src/main/java/com/c12e/cortex/examples/datasource/DataSourceRW.java) for the full source).

## Run Locally

To run this example locally with local Cortex clients (from the parent directory):
1. Build the application.
    ```
    make build
    ```
2. Run the application with Gradle.
    ```
    ./gradlew main-app:run --args="datasource-refresh --project local --data-source member-base-ds"
    ```

This will write the `member-base-ds` [Data Source](../local-clients/README.md#data-sources) to a Delta Table in
`../main-app/build/test-data/cortex-profiles/sources/local/member-base-ds-delta`.

**NOTE:** Because this is running with a local Catalog and local filesystem as the Cortex backend, the result of the
Data Source will be written to a local file and does not exist prior to running the above.

## Run Locally in a Docker Container With Spark-submit

Run this example in a Docker container with local clients (from the parent directory):

1. Build the application.
    ```
    make build
    ```
1. Create the Skill Docker image.
    ```
    make create-app-image
    ```
1. Export a Cortex token.
    ```
    export CORTEX_TOKEN=<token>
    ```
2. Run the application with Docker.
    ```
    docker run -p 4040:4040 --entrypoint="python" -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
      -v $(pwd)/datasource-refresh/src/main/resources/conf:/app/conf \
      -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
      -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
    profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"
    ```
   NOTES:
    * The `$CORTEX_TOKEN` environment variable is required by the Spark-submit wrapper, and needs to be a valid JWT token. You can generate this via: `cortex configure token`.
    * Port 4040 is forwarded from the container to expose the Spark UI (for debugging purposes).
    * The first volume mount is sharing the [Spark-submit config file](./src/main/resources/conf/spark-conf.json).
    * The second volume mount shares the LocalCatalog contents and other local application resources.
    * The third volume mount is the output location of the Data Source.

The logs should be similar to:
```
['/opt/spark/bin/spark-submit', '--master', 'local[*]', '--class', 'com.c12e.cortex.examples.Application', '--conf', 'spark.app.name=CortexProfilesExamples', '--conf', 'spark.ui.enabled=true', '--conf', 'spark.ui.prometheus.enabled=true', '--conf', 'spark.sql.streaming.metricsEnabled=true', '--conf', 'spark.cortex.catalog.impl=com.c12e.cortex.phoenix.LocalCatalog', '--conf', 'spark.cortex.catalog.local.dir=src/main/resources/spec', '--conf', 'spark.cortex.client.secrets.impl=com.c12e.cortex.examples.local.CustomSecretsClient', '--conf', 'spark.cortex.client.storage.impl=com.c12e.cortex.profiles.client.LocalRemoteStorageClient', '--conf', 'spark.cortex.storage.storageType=file', '--conf', 'spark.cortex.storage.file.baseDir=src/main/resources/data', '--conf', 'spark.kubernetes.driverEnv.CORTEX_TOKEN=eyJhbGciOiJFZERTQSIsImtpZCI6Im5YMHZfcjdiMGJKOC1UVW5Sc3U2cHB2OFVUX0szYVMzdE11d3JzVVp1aEEifQ.eyJzdWIiOiI5ZGMxMjY2Mi1jZDUxLTQ5NDYtYTdmYy0zMTJmZWNlNzg5NTEiLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20iLCJpYXQiOjE2NTgyNjY1MTUsImV4cCI6MTY1ODM1MjkxNX0.S9ebZQ0dwdR3LX3KaPTN1Q7c79uUXlD_YPdWeGnWvDw1lhDXQDEDQHcAYXb2SZEGNLhHjqolDUASxYA4R1WuAw', '--conf', 'spark.cortex.phoenix.token=eyJhbGciOiJFZERTQSIaAstpZCI6Im5YMHZfcjdiMGJKOC1UVW5Sc3U2cHB2OFVUX0szYVMzdE11d3JzVVp1aDEIFQ.eyJzdWIiOiI5ZGMxMjY2Mi1jZDUxLTQ5NDYtYTdmYy0zMTJmZWNlNzg5NTEiLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20iLCJpYXQiOjE2NTgyNjY1MTM0bmV0cCI6MTY21DM1MjabNX0.S9ebZQ0dwdR3LX3KaPTN1Q7c79uUXlD_YPdWeGnWvDw1lhDXQDEDQHcAYXb2SZEGNLhHjqolDUASxYA4R1Wuuw', 'local:///app/libs/app.jar', 'datasource-refresh', '--project', 'local', '--data-source', 'member-base-ds']
21:36:11,306 |-INFO in ch.qos.logback.classic.LoggerContext[default] - Could NOT find resource [logback-test.xml]
21:36:11,307 |-INFO in ch.qos.logback.classic.LoggerContext[default] - Found resource [logback.xml] at [file:/opt/spark/conf/logback.xml]
21:36:11,308 |-WARN in ch.qos.logback.classic.LoggerContext[default] - Resource [logback.xml] occurs multiple times on the classpath.
21:36:11,308 |-WARN in ch.qos.logback.classic.LoggerContext[default] - Resource [logback.xml] occurs at [jar:file:/opt/spark/jars/main-app-1.0.0-SNAPSHOT.jar!/logback.xml]
21:36:11,308 |-WARN in ch.qos.logback.classic.LoggerContext[default] - Resource [logback.xml] occurs at [file:/opt/spark/conf/logback.xml]
21:36:11,308 |-WARN in ch.qos.logback.classic.LoggerContext[default] - Resource [logback.xml] occurs at [jar:file:/opt/spark/jars/profiles-sdk-6.3.0-M.2.1.jar!/logback.xml]
21:36:11,382 |-INFO in ch.qos.logback.classic.joran.action.ConfigurationAction - debug attribute not set
21:36:11,385 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - About to instantiate appender of type [ch.qos.logback.core.ConsoleAppender]
21:36:11,388 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - Naming appender as [ROOT]
21:36:11,394 |-INFO in ch.qos.logback.core.joran.action.NestedComplexPropertyIA - Assuming default type [ch.qos.logback.classic.encoder.PatternLayoutEncoder] for [encoder] property
21:36:11,447 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [org.apache.spark] to WARN
21:36:11,447 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [org.apache.spark.sql.execution.CacheManager] to ERROR
21:36:11,447 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [org.apache.spark.ui.SparkUI] to INFO
21:36:11,447 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [com.c12e.cortex.phoenix] to DEBUG
21:36:11,447 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [com.c12e.cortex.profiles] to DEBUG
21:36:11,447 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [com.c12e.cortex.examples] to DEBUG
21:36:11,447 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [org.apache.parquet] to WARN
21:36:11,447 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [org.apache.hadoop] to WARN
21:36:11,447 |-INFO in ch.qos.logback.classic.joran.action.RootLoggerAction - Setting level of ROOT logger to INFO
21:36:11,447 |-INFO in ch.qos.logback.core.joran.action.AppenderRefAction - Attaching appender named [ROOT] to Logger[ROOT]
21:36:11,448 |-INFO in ch.qos.logback.classic.joran.action.ConfigurationAction - End of configuration.
21:36:11,448 |-INFO in ch.qos.logback.classic.joran.JoranConfigurator@485966cc - Registering current configuration as safe fallback point

WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by org.apache.spark.unsafe.Platform (file:/opt/spark/jars/spark-unsafe_2.12-3.2.1.jar) to constructor java.nio.DirectByteBuffer(long,int)
WARNING: Please consider reporting this to the maintainers of org.apache.spark.unsafe.Platform
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
21:36:12.116 [main] WARN  o.a.hadoop.util.NativeCodeLoader - Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
21:36:12.832 [main] INFO  org.sparkproject.jetty.util.log - Logging initialized @2355ms to org.sparkproject.jetty.util.log.Slf4jLog
21:36:12.911 [main] INFO  org.sparkproject.jetty.server.Server - jetty-9.4.43.v20210629; built: 2021-06-30T11:07:22.254Z; git: 526006ecfa3af7f1a27ef3a288e2bef7ea9dd7e8; jvm 11.0.15+10
21:36:12.934 [main] INFO  org.sparkproject.jetty.server.Server - Started @2457ms
21:36:12.978 [main] INFO  o.s.jetty.server.AbstractConnector - Started ServerConnector@36cc9385{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
21:36:13.004 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@4b6d92e{/jobs,null,AVAILABLE,@Spark}
21:36:13.006 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@5e65afb6{/jobs/json,null,AVAILABLE,@Spark}
21:36:13.007 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@8851ce1{/jobs/job,null,AVAILABLE,@Spark}
21:36:13.010 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@767191b1{/jobs/job/json,null,AVAILABLE,@Spark}
21:36:13.011 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@5d21202d{/stages,null,AVAILABLE,@Spark}
21:36:13.012 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@6eb17ec8{/stages/json,null,AVAILABLE,@Spark}
21:36:13.014 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@277bf091{/stages/stage,null,AVAILABLE,@Spark}
21:36:13.017 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@6b2e46af{/stages/stage/json,null,AVAILABLE,@Spark}
21:36:13.018 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@2f37f1f9{/stages/pool,null,AVAILABLE,@Spark}
21:36:13.019 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@2af69643{/stages/pool/json,null,AVAILABLE,@Spark}
21:36:13.020 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@48528634{/storage,null,AVAILABLE,@Spark}
21:36:13.022 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@4047d2d9{/storage/json,null,AVAILABLE,@Spark}
21:36:13.023 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@146dcfe6{/storage/rdd,null,AVAILABLE,@Spark}
21:36:13.024 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@5c77ba8f{/storage/rdd/json,null,AVAILABLE,@Spark}
21:36:13.025 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@7a0ef219{/environment,null,AVAILABLE,@Spark}
21:36:13.026 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@7add323c{/environment/json,null,AVAILABLE,@Spark}
21:36:13.028 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@4760f169{/executors,null,AVAILABLE,@Spark}
21:36:13.030 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@35c12c7a{/executors/json,null,AVAILABLE,@Spark}
21:36:13.032 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@25290bca{/executors/threadDump,null,AVAILABLE,@Spark}
21:36:13.035 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@4ac86d6a{/executors/threadDump/json,null,AVAILABLE,@Spark}
21:36:13.044 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@508a65bf{/static,null,AVAILABLE,@Spark}
21:36:13.046 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@26c47874{/,null,AVAILABLE,@Spark}
21:36:13.048 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@2849434b{/api,null,AVAILABLE,@Spark}
21:36:13.049 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@7a0e7ecd{/metrics,null,AVAILABLE,@Spark}
21:36:13.050 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@3c5dbdf8{/jobs/job/kill,null,AVAILABLE,@Spark}
21:36:13.051 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@7aac8884{/stages/stage/kill,null,AVAILABLE,@Spark}
21:36:13.052 [main] INFO  org.apache.spark.ui.SparkUI - Bound SparkUI to 0.0.0.0, and started at http://05135d581613:4040
21:36:13.559 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@5a545b0f{/metrics/json,null,AVAILABLE,@Spark}
21:36:13.561 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@124dac75{/metrics/prometheus,null,AVAILABLE,@Spark}
21:36:13.988 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@6b8d54da{/SQL,null,AVAILABLE,@Spark}
21:36:13.989 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@217235f5{/SQL/json,null,AVAILABLE,@Spark}
21:36:13.990 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@697a0948{/SQL/execution,null,AVAILABLE,@Spark}
21:36:13.991 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@4776e209{/SQL/execution/json,null,AVAILABLE,@Spark}
21:36:14.001 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@2744dcae{/static/sql,null,AVAILABLE,@Spark}
21:36:17.810 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: csv, uri: ./src/main/resources/data/members_100_v14.csv, extra
21:36:23.137 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Inferred schema from sample of connection (CSV) - project: 'local', connectionName: 'member-base-file'
21:36:23.189 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Finished reading connection (CSV) - project: 'local', connectionName: 'member-base-file'
21:36:23.192 [main] WARN  c.c.c.p.v.DataSourceWriteValidator - Additional (unexpected) column found in data source - project: 'local', sourceName: 'member-base-ds', column name: 'email'
21:36:23.193 [main] WARN  c.c.c.p.v.DataSourceWriteValidator - Additional (unexpected) column found in data source - project: 'local', sourceName: 'member-base-ds', column name: 'address'
21:36:23.193 [main] WARN  c.c.c.p.v.DataSourceWriteValidator - Additional (unexpected) column found in data source - project: 'local', sourceName: 'member-base-ds', column name: 'pcp_address'
21:36:23.244 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Overwriting delta table: 'src/main/resources/data/cortex-profiles/sources/local/member-base-ds-delta'
21:36:33.851 [main] INFO  c.c.c.p.m.d.DefaultCortexDataSourceWriter - Wrote to data source - project: 'local', sourceName: 'member-base-ds'
21:36:33.864 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: delta, uri: src/main/resources/data/cortex-profiles/sources/local/member-base-ds-delta, extra
21:36:33.896 [main] INFO  c.c.c.p.m.d.DefaultCortexDataSourceReader - Read data source - project: 'local', sourceName: 'member-base-ds'
21:36:33.926 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@36cc9385{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
21:36:33.929 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://05135d581613:4040
Pod Name:
Container State:
Termination Reason:
Exit Code: 0
```

## Run Locally Against a Cortex Cluster

**Note**: Because this is running outside the Cortex Cluster you must know the value of any Secrets used by the
Connections. For access to Cortex Secrets, try [running as a Skill](#run-as-a-skill).

### Prerequisites

To run this example in a Spark local mode against a Cortex Cluster with access to remote storage, the Cortex Catalog,
and local Secrets you must:
* Know the [backend storage configuration](../docs/config.md#cortex-backend-storage) for the Cluster, which includes
  names of buckets and access keys for remote storage. If you do not know this, then try [running as a Skill](#run-as-a-skill).
* Know the values of Cortex Secrets used by the Connection.
* Generate a `CORTEX_TOKEN`.
* Ensure that the Cortex resources exist, namely the Project, Data Source, and its Connection.
* Update the [spark-conf.json](./src/main/resources/conf/spark-conf.json) file to:
    - Use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint (e.g. `https://api.<domain>/fabric/v4/graphql`) and removing the Local Catalog implementation (`spark.cortex.catalog.impl`).
    - Use the [remote storage client](../docs/backendstorage.md#remote-storage-client) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint, and remove the local storage client implementation (`spark.cortex.client.storage.impl`).
    - Update [Local Secret Client](../local-clients/README.md#secrets) with any Secrets required by your Connection(s). Ensure to update the project, Secret name, and Secret value.
    - Update the `app_command` arguments to match your Cortex project and Connections (`--project`, `--data-source`).

**NOTE**: If your connections do not use Cortex Secrets because the Cortex cluster has [IRSA enabled](https://cognitivescale.github.io/cortex-charts/docs/platforms/aws/aws-irsa), then you may
not be able to run this example without editing the Connection. This is because IRSA provides authentication within the
cluster, and cannot be leveraged when running locally. Try [running the example as a Skill](#run-as-a-skill).

### Example

**NOTE**: The cortex backend storage configuration could be set in the `spark-conf.json`, but environment variables are
used below to avoid hardcoding access keys in the source.

The below example commanads are assuming:
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
      -e CONNECTION_SECRET_VALUE="${CONNECTION_SECRET_VALUE}"
      -e STORAGE_TYPE=s3 \
      -e AWS_ACCESS_KEY_ID=xxxxx \
      -e AWS_SECRET_KEY=xxxxx \
      -e S3_ENDPOINT=http://host.docker.internal:9000 \
      -v $(pwd)/datasource-refresh/src/main/resources/conf:/app/conf \
      -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
      -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
    profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"
    ```

## Run as a Skill

### Prerequisites
* Ensure that the Cortex resources exist, namely the Project, Data Source, and its Connection.
* Generate a `CORTEX_TOKEN`.
* Update the [spark-conf.json](./src/main/resources/conf/spark-conf.json) file to:
    - Use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the in-cluster GraphQL API endpoint (`"http://cortex-api.cortex.svc.cluster.local:8080"`) and removing the Local Catalog implementation (`spark.cortex.catalog.impl`).
    - Use the [remote storage client](../docs/backendstorage.md#remote-storage-client) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint, and remove the local storage client implementation (`spark.cortex.client.storage.impl`).
    - Remove the Local Secret Client implementation (`spark.cortex.client.secrets.impl`).
    - Update the `app_command` arguments to match your Cortex project and Data Source names (`--project`, `--data-source`).

To run this example in Spark cluster mode against a Cortex Cluster with access to the Catalog and Secrets, you must
update the spark configuration file used by the main application (`main-app/src/main/resources/conf/spark-conf.json`) to
match configuration for this example.

Refer to the [instructions for running the Skill template](../README.md#skill-template) in the top level README for
deploying and invoking the skill.

### Example

```json
{
  "pyspark": {
    "pyspark_bin": "bin/spark-submit",
    "app_command": [
      "datasource-refresh",
      "--project",
      "testi-69257",
      "--data-source",
      "test-members"
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

**NOTES**:
* The `--master` and `--deploy-mode` have been set to run the Spark job in the Cortex (Kubernetes) cluster.
* The Phoenix Client URL and Secret client URL are referring to services in Kubernetes cluster.
