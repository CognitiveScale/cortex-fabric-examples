# Reading from BigQuery

This example is a CLI application that writes data from a Google BigQuery Table to the location of a Cortex Connection.
This builds off of the [Local Clients](../local-clients/README.md) example for its initial setup.

(See [BigQuery.java](./src/main/java/com/c12e/cortex/examples/bigquery/BigQuery.java) for the source code.)

## Prerequisites

* The BigQuery Spark Connector is a required dependency to run this
  example (`com.google.cloud.spark:spark-bigquery-with-dependencies_2.12:0.25.0`). Download
  the [BigQuery Spark connector](https://repo1.maven.org/maven2/com/google/cloud/spark/spark-bigquery-with-dependencies_2.12/0.25.0/spark-bigquery-with-dependencies_2.12-0.25.0.jar)
  and save the file in [../main-ap/src/main/resources/lib/](../main-app/src/main/resources/lib/).
* Get a GCP Service Account JSON as described in: https://docs.google.com/document/d/1T1u8RMZhDYMIXHk7v3lLF2rzag7xLTr5CLHC-49UiYU/edit#heading=h.756ioo8pxy08.
  Save this file in `profiles-examples/main-app/src/main/resources/credentials` for future use (e.g. `gcs-service-account.json`).

## Run Locally

To run this example locally with local Cortex clients (from the parent directory):
1. Build the application.
    ```
    make build
    ```
2. Export the path to the GCP Service Account JSON file.
    ```
    export BIGQUERY_CREDS_FILE=$(PWD)/main-app/src/main/resources/credentials/gcs-service-account.json
    ```
3. Run the application with Gradle.
    ```
    ./gradlew main-app:run --args="bigquery --project local --google-project fabric-qa --table bigquery-public-data.samples.shakespeare --output sink"
    ```

The end of the log output should be similar to:
```
(spark.databricks.delta.schema.autoMerge.enabled,true), (spark.cortex.catalog.local.dir,src/main/resources/spec), (spark.sql.warehouse.dir,file:/Users/laguirre/cortex/cortex-fabric-examples/profiles-sdk-examples/main-app/spark-warehouse)15:25:10.695 [main] INFO  c.g.c.s.b.d.DirectBigQueryRelation - |Querying table bigquery-public-data.samples.shakespeare, parameters sent from Spark:|requiredColumns=[word,word_count,corpus,corpus_date],|filters=[]
15:25:13.052 [main] INFO  c.g.c.s.b.direct.BigQueryRDDFactory - Created read session for table 'bigquery-public-data.samples.shakespeare': projects/fabric-qa/locations/us/sessions/CAISDHVzbzczR0xLdjVLbBoCamQaAmpmGgJpchoCb2oaAmpxGgJuYRoCb3MaAm93GgJqchoCaXcaAmpjGgJwehoCcHkaAmpzGgJweBoCb3YaAmppGgJpYRoCaWMaAnBs
15:25:14.476 [Executor task launch worker for task 0.0 in stage 0.0 (TID 0)] INFO  c.g.c.s.b.r.o.a.a.m.BaseAllocator - Debug mode disabled.
15:25:14.480 [Executor task launch worker for task 0.0 in stage 0.0 (TID 0)] INFO  c.g.c.s.b.r.o.a.a.m.DefaultAllocationManagerOption - allocation manager type not specified, using netty as the default type
15:25:14.481 [Executor task launch worker for task 0.0 in stage 0.0 (TID 0)] INFO  c.g.c.s.b.r.o.a.a.m.CheckAllocator - Using DefaultAllocationManager at memory/DefaultAllocationManagerFactory.class
+----+----------+-------+-----------+
|word|word_count| corpus|corpus_date|
+----+----------+-------+-----------+
|LVII|         1|sonnets|          0|
+----+----------+-------+-----------+
only showing top 1 row

15:25:15.268 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Writing to connection: './build/tmp/test-data/sink-ds/'
15:25:15.315 [main] INFO  c.g.c.s.b.d.DirectBigQueryRelation - |Querying table bigquery-public-data.samples.shakespeare, parameters sent from Spark:|requiredColumns=[word,word_count,corpus,corpus_date],|filters=[]
15:25:16.092 [main] INFO  c.g.c.s.b.direct.BigQueryRDDFactory - Created read session for table 'bigquery-public-data.samples.shakespeare': projects/fabric-qa/locations/us/sessions/CAISDE1sYzBITDJnYnd1MRoCamQaAmpmGgJpchoCb2oaAmpxGgJuYRoCb3MaAm93GgJqchoCaXcaAmpjGgJwehoCcHkaAmpzGgJweBoCb3YaAmppGgJpYRoCaWMaAnBs
15:25:17.820 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@333c8791{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
15:25:17.821 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://c02wq091htdf.attlocal.net:4040

Deprecated Gradle features were used in this build, making it incompatible with Gradle 8.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

See https://docs.gradle.org/7.4/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 21s
```

The sink connection is defined in the [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml) file
and can be found at `./main-app/build/tmp/test-data/sink-ds` after running the command. The above example is writing
data to the Connection from a [BigQuery Sample table](https://cloud.google.com/bigquery/public-data#sample_tables).

## Run Locally in a Docker Container With Spark-submit

To run this example in a Docker container with local Cortex clients (from the parent directory):
1. Build the application.
    ```
    make build
    ```
2. Crate the Skill Docker image.
    ```
    make create-app-image
    ```
3. Export the path to the GCP Service Account JSON file and a Cortex Token. The path to the JSON file should be within the Docker container.
    ```
    export CORTEX_TOKEN=<token>
    export BIGQUERY_CREDS_FILE=/opt/spark/work-dir/src/main/resources/credentials/gcs-service-account.json
    ```
4. Run the application with Docker.
    ```
    docker run -p 4040:4040 \
      --entrypoint="python" \
      -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
      -e BIGQUERY_CREDS_FILE="${BIGQUERY_CREDS_FILE}" \
      -v $(pwd)/bigquery-connection/src/main/resources/conf:/app/conf \
      -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
      -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
      profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/spark-conf.json\"}}"
    ```
   NOTES:
    * Port 4040 is forwarded from the container to expose the Spark UI (for debugging).
    * The `BIGQUERY_CREDS_FILE` environment variable is the path to the GCP Service Account JSON file in the container.
    * The first volume mount is sharing the [Spark-submit config file](./src/main/resources/conf/spark-conf.json).
    * The second volume mount shares the LocalCatalog contents and other local application resources.
    * The third volume mount is the output location of the joined connection.

The end of the logs should be similar to:
```
19:29:19.134 [main] INFO  c.g.c.s.b.direct.BigQueryRDDFactory - Created read session for table 'bigquery-public-data.samples.shakespeare': projects/fabric-qa/locations/us/sessions/CAISDC1DWTRobUhUWThuTxoCamQaAmpmGgJpchoCb2oaAmpxGgJuYRoCb3MaAm93GgJqchoCaXcaAmpjGgJwehoCcHkaAmpzGgJweBoCb3YaAmppGgJpYRoCaWMaAnBs
19:29:21.220 [Executor task launch worker for task 0.0 in stage 0.0 (TID 0)] INFO  c.g.c.s.b.r.o.a.a.m.BaseAllocator - Debug mode disabled.
19:29:21.224 [Executor task launch worker for task 0.0 in stage 0.0 (TID 0)] INFO  c.g.c.s.b.r.o.a.a.m.DefaultAllocationManagerOption - allocation manager type not specified, using netty as the default type
19:29:21.225 [Executor task launch worker for task 0.0 in stage 0.0 (TID 0)] INFO  c.g.c.s.b.r.o.a.a.m.CheckAllocator - Using DefaultAllocationManager at memory/DefaultAllocationManagerFactory.class
+----+----------+-------+-----------+
|word|word_count| corpus|corpus_date|
+----+----------+-------+-----------+
|LVII|         1|sonnets|          0|
+----+----------+-------+-----------+
only showing top 1 row

19:29:21.946 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Writing to connection: 'file:///opt/spark/work-dir/build/tmp/test-data/sink-ds/'
19:29:22.001 [main] INFO  c.g.c.s.b.d.DirectBigQueryRelation - |Querying table bigquery-public-data.samples.shakespeare, parameters sent from Spark:|requiredColumns=[word,word_count,corpus,corpus_date],|filters=[]
19:29:22.738 [main] INFO  c.g.c.s.b.direct.BigQueryRDDFactory - Created read session for table 'bigquery-public-data.samples.shakespeare': projects/fabric-qa/locations/us/sessions/CAISDHU1U1BuWS1BbEVOQxoCamQaAmpmGgJpchoCb2oaAmpxGgJuYRoCb3MaAm93GgJqchoCaXcaAmpjGgJwehoCcHkaAmpzGgJweBoCb3YaAmppGgJpYRoCaWMaAnBs
19:29:25.032 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@2b03d52f{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
19:29:25.035 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://9784fa1fbb70:4040
Pod Name:
Container State:
Termination Reason:
Exit Code: 0
```

The sink connection is defined in the [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml) file,
and can be found at `./main-app/build/tmp/test-data/sink-ds` after running the command.

### Run as a Skill

### Prerequisites
* Ensure that the Cortex resources exist, specifically the Cortex Project and Connection. **The underlying source of the Connection does not need to exist.**
* Generate a `CORTEX_TOKEN`.
* Save the GCP Service Account JSON file as a Cortex Secret by base64 encoding the value. The JSON file was exposed to
  the Skill Docker Image as a mounted volume when [running locally](#run-locally-in-a-docker-container-with-spark-submit).
  However, these credentials will not be available when deployed. The Application includes a `--secret` option for
  referencing the Secret you saved. Example encoding the JSON value:
   ```
   jq '. | @base64' < main-app/src/main/resources/credentials/gcs-service-account.json
   ```
* Update the [spark-conf.json](./src/main/resources/conf/spark-conf.json) file to:
    - Use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the in-cluster GraphQL API endpoint (`"http://cortex-api.cortex.svc.cluster.local:8080"`) and removing the Local Catalog implementation (`spark.cortex.catalog.impl`).
    - Use the [remote storage client](../docs/backendstorage.md#remote-storage-client) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint, and remove the local storage client implementation (`spark.cortex.client.storage.impl`).
    - Remove the local Secret client implementation (`spark.cortex.client.secrets.impl`).
    - Update the `app_command` arguments to match your Cortex Project and Profile Schema (`--project`, `--google-project`, `--table`, `--output`, `--secret`).

### Example

```json
{
  "pyspark": {
    "pyspark_bin": "bin/spark-submit",
    "app_command": [
      "bigquery",
      "--project",
      "testi-69257",
      "--google-project",
      "fabric",
      "--table",
      "bigquery-public-data.samples.shakespeare",
      "--output",
      "bigquery-shake-c2942",
      "--secret",
      "gcp-fabric"
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

Notes on the above example:
* The `--master` and `--deploy-mode` have been set to run the Spark job in the Cortex (Kubernetes) Cluster.
* The Phoenix Client URL and Secret Client URL are referring to services in Kubernetes Cluster.
* The Spark Driver and Spark Executors (`"spark.executor.instances"`) have a 2g and 4g of memory respectively. **Adjust the amount of resources used for your cluster/data.**
* The Cortex [Backend Storage configuration](../docs/config.md#cortex-backend-storage) is configured by the default remote  storage client implementation.
* THe `--secret` is set in the `app_command`.
