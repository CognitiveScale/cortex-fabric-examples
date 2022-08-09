# Build Profiles

This example is a CLI application for building Cortex Profiles. This builds off of
the [Local Clients](../local-clients/README.md) example for its setup.

(See [BuildProfile.java](./src/main/java/com/c12e/cortex/examples/profile/BuildProfile.java) for the source code.)

## Jobs

This example builds Profiles for the `member-profile` [Profile Schema](../local-clients/README.md#profile-schemas) by
using pre-built Job flows for:
- Ingesting a Data Source (`IngestDataSourceJob`)
- Building Profiles (`BuildProfileJob`)

These job flows create Data Sources and Profiles similarly to how they are created in the Fabric Console. Building the
resource using the Profiles SDK provides users with greater control of the ingestion process and the ability to
incorporate additional computational functions.

## Run Locally

To run this example locally with local Cortex Clients (from the parent directory):
1. Build the application.
    ```
    make build
    ```
2. Run the application with Gradle.
    ```
    ./gradlew main-app:run --args="build-profile --project local --profile-schema member-profile"
    ```

The end of the log output should be similar to:
```
16:23:37.979 [main] INFO  org.apache.spark.ui.SparkUI - Bound SparkUI to 0.0.0.0, and started at http://c02wq091htdf.attlocal.net:4040
16:23:38.424 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@4cfa83f9{/metrics/json,null,AVAILABLE,@Spark}
16:23:38.982 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@646cd766{/SQL,null,AVAILABLE,@Spark}
16:23:38.983 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@26457986{/SQL/json,null,AVAILABLE,@Spark}
16:23:38.983 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@b30a50d{/SQL/execution,null,AVAILABLE,@Spark}
16:23:38.984 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@6c742b84{/SQL/execution/json,null,AVAILABLE,@Spark}
16:23:38.993 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@26e0d39c{/static/sql,null,AVAILABLE,@Spark}
16:23:41.702 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: csv, uri: ./src/main/resources/data/members_100_v14.csv, extra
16:23:46.509 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Inferred schema from sample of connection (CSV) - project: 'local', connectionName: 'member-base-file'
16:23:46.536 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Finished reading connection (CSV) - project: 'local', connectionName: 'member-base-file'
16:23:46.539 [main] WARN  c.c.c.p.v.DataSourceWriteValidator - Additional (unexpected) column found in data source - project: 'local', sourceName: 'member-base-ds', column name: 'email'
16:23:46.539 [main] WARN  c.c.c.p.v.DataSourceWriteValidator - Additional (unexpected) column found in data source - project: 'local', sourceName: 'member-base-ds', column name: 'address'
16:23:46.539 [main] WARN  c.c.c.p.v.DataSourceWriteValidator - Additional (unexpected) column found in data source - project: 'local', sourceName: 'member-base-ds', column name: 'pcp_address'
16:23:46.577 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Overwriting delta table: './build/test-data//cortex-profiles/sources/local/member-base-ds-delta'
16:23:54.482 [main] INFO  c.c.c.p.m.d.DefaultCortexDataSourceWriter - Wrote to data source - project: 'local', sourceName: 'member-base-ds'
16:23:55.408 [main] INFO  c.c.c.p.f.DefaultFeatureReportCalculator - Insufficient dataset size for sampling (actual vs MIN_SAMPLE_SIZE): 100 vs 3364. Using entire dataset
16:24:01.870 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: parquet, uri: ./src/main/resources/data/member_flu_risk_100_v14.parquet, extra
16:24:01.870 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Reading connection from file_path './src/main/resources/data/member_flu_risk_100_v14.parquet'
16:24:02.001 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Overwriting delta table: './build/test-data//cortex-profiles/sources/local/member-flu-risk-file-ds-delta'
16:24:03.625 [main] INFO  c.c.c.p.m.d.DefaultCortexDataSourceWriter - Wrote to data source - project: 'local', sourceName: 'member-flu-risk-file-ds'
16:24:03.932 [main] INFO  c.c.c.p.f.DefaultFeatureReportCalculator - Insufficient dataset size for sampling (actual vs MIN_SAMPLE_SIZE): 100 vs 3364. Using entire dataset
16:24:04.899 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: delta, uri: ./build/test-data//cortex-profiles/sources/local/member-base-ds-delta, extra
16:24:04.899 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Reading connection from file_path './build/test-data//cortex-profiles/sources/local/member-base-ds-delta'
Warning: Nashorn engine is planned to be removed from a future JDK release
16:24:05.395 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: delta, uri: ./build/test-data//cortex-profiles/sources/local/member-flu-risk-file-ds-delta, extra
16:24:05.395 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Reading connection from file_path './build/test-data//cortex-profiles/sources/local/member-flu-risk-file-ds-delta'
root
 |-- profile_id: integer (nullable = true)
 |-- state_code: string (nullable = true)
 |-- city: string (nullable = true)
 |-- state: string (nullable = true)
 |-- zip_code: integer (nullable = true)
 |-- gender: string (nullable = true)
 |-- email: string (nullable = true)
 |-- segment: string (nullable = true)
 |-- member_health_plan: string (nullable = true)
 |-- is_PCP_auto_assigned: integer (nullable = true)
 |-- pcp_tax_id: integer (nullable = true)
 |-- address: string (nullable = true)
 |-- phone: string (nullable = true)
 |-- do_not_call: integer (nullable = true)
 |-- channel_pref: string (nullable = true)
 |-- age: integer (nullable = true)
 |-- last_flu_shot_date: string (nullable = true)
 |-- pcp_name: string (nullable = true)
 |-- pcp_address: string (nullable = true)
 |-- _timestamp: timestamp (nullable = false)
 |-- has_phone_number: boolean (nullable = true)
 |-- age_group: string (nullable = true)
 |-- flu_risk_score: double (nullable = true)
 |-- date: string (nullable = true)
 |-- avg_flu_risk: double (nullable = true)
 |-- flu_risk_1_pct: double (nullable = true)
 |-- is_flu_risk_1_pct: boolean (nullable = true)

16:24:06.464 [main] INFO  c.c12e.cortex.phoenix.ProfileEngine - Build Profile Completed
16:24:06.466 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Overwriting delta table: './build/test-data//cortex-profiles/profiles/local/member-profile-delta'
16:24:06.478 [main] WARN  o.a.spark.sql.catalyst.util.package - Truncated the string representation of a plan since it was too large. This behavior can be adjusted by setting 'spark.sql.debug.maxToStringFields'.
16:24:11.422 [main] INFO  c.c.c.p.f.DefaultFeatureReportCalculator - Insufficient dataset size for sampling (actual vs MIN_SAMPLE_SIZE): 100 vs 3364. Using entire dataset
16:24:41.731 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@d946bcc{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
16:24:41.733 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://c02wq091htdf.attlocal.net:4040
```

This will cause the Profiles for the [member-profile](../main-app/src/main/resources/spec/profileSchemas.yml) Profile
Schema to be created by ingesting and joining the `member-base-ds` and `member-flu-risk-file-ds`
[DataSources](../main-app/src/main/resources/spec/datasources.yml). The Profile Schema will additionally have various
computed and bucketed attributes.

The built profiles are saved at: `main-app/build/test-data/cortex-profiles/profiles/local/member-profile-delta`.

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
3. Export Cortex token.
    ```
    export CORTEX_TOKEN=<token>
    ```
4. Run the application with Docker.
    ```
    docker run -p 4040:4040 --entrypoint="python" \
      -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
      -v $(pwd)/build-profiles/src/main/resources/conf:/app/conf \
      -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
      -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
    profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"
    ```

The end of the log output should be similar to:
```
22:01:40.685 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Reading connection from file_path 'src/main/resources/data/cortex-profiles/sources/local/member-flu-risk-file-ds-delta'
root
 |-- profile_id: integer (nullable = true)
 |-- state_code: string (nullable = true)
 |-- city: string (nullable = true)
 |-- state: string (nullable = true)
 |-- zip_code: integer (nullable = true)
 |-- gender: string (nullable = true)
 |-- email: string (nullable = true)
 |-- segment: string (nullable = true)
 |-- member_health_plan: string (nullable = true)
 |-- is_PCP_auto_assigned: integer (nullable = true)
 |-- pcp_tax_id: integer (nullable = true)
 |-- address: string (nullable = true)
 |-- phone: string (nullable = true)
 |-- do_not_call: integer (nullable = true)
 |-- channel_pref: string (nullable = true)
 |-- age: integer (nullable = true)
 |-- last_flu_shot_date: string (nullable = true)
 |-- pcp_name: string (nullable = true)
 |-- pcp_address: string (nullable = true)
 |-- _timestamp: timestamp (nullable = false)
 |-- has_phone_number: boolean (nullable = true)
 |-- age_group: string (nullable = true)
 |-- flu_risk_score: double (nullable = true)
 |-- date: string (nullable = true)
 |-- avg_flu_risk: double (nullable = true)
 |-- flu_risk_1_pct: double (nullable = true)
 |-- is_flu_risk_1_pct: boolean (nullable = true)

22:01:42.281 [main] INFO  c.c12e.cortex.phoenix.ProfileEngine - Build Profile Completed
22:01:42.293 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Overwriting delta table: 'src/main/resources/data/cortex-profiles/profiles/local/member-profile-delta'
22:01:42.308 [main] WARN  o.a.spark.sql.catalyst.util.package - Truncated the string representation of a plan since it was too large. This behavior can be adjusted by setting 'spark.sql.debug.maxToStringFields'.
22:01:49.523 [main] INFO  c.c.c.p.f.DefaultFeatureReportCalculator - Insufficient dataset size for sampling (actual vs MIN_SAMPLE_SIZE): 100 vs 3364. Using entire dataset
22:02:36.035 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@2a331b46{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
22:02:36.039 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://6ff8c687d5da:4040
Pod Name:
Container State:
Termination Reason:
Exit Code: 0
```

## Run as a Skill

### Prerequisites
* Ensure that the Cortex resources exist, specifically the Project, Profile Schema, and any associated Data Sources or Connections.
* Generate a `CORTEX_TOKEN`.
* Update the [spark-conf.json](./src/main/resources/conf/spark-conf.json) file to:
   - Use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the in-cluster GraphQL API endpoint (`"http://cortex-api.cortex.svc.cluster.local:8080"`) and removing the Local Catalog implementation (`spark.cortex.catalog.impl`).
   - Use the [remote storage client](../docs/backendstorage.md#remote-storage-client) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint, and remove the local storage client implementation (`spark.cortex.client.storage.impl`).
   - Remove the local Secret client implementation (`spark.cortex.client.secrets.impl`).
   - Update the `app_command` arguments to match your Cortex Project and Profile Schema (`--project`,  `--profile-schema`).

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
      "build-profile",
      "--project",
      "laguirre-testi-69257",
      "--profile-schema",
      "members-latest"
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
        "spark.cortex.storage.remote.impl": "com.c12e.cortex.phoenix.internal.InternalRemoteStorageClient",
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

Notes on the above example:
* The `--master` and `--deploy-mode` have been set to run the Spark job in the Cortex (Kubernetes) Cluster.
* The Phoenix Client URL and Secret Client URL are referring to services in Kubernetes Cluster
* The Spark Driver and Spark Executors (`"spark.executor.instances"`) have a 2g and 4g of memory respectively. **Adjust the amount of resources used for your cluster/data.**
* The Cortex [Backend Storage configuration](../docs/config.md#cortex-backend-storage) is configured with the default remote
  storage client implementation.
