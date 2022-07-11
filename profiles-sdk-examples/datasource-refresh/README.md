# Datasource Refresh

This example is contains a CLI application for refreshing a DataSource by reading its Cortex Connection and writing the
dataset to the DataSource. This builds off the [Local Clients](../local-clients/README.md) example for its setup 
(see [Data Sources](../local-clients/README.md#data-sources)).

See [DataSourcesRW.java](./src/main/java/com/c12e/cortex/examples/datasource/DataSourceRW.java) for the full source.

## Running Locally

To run this example locally with local Cortex clients:
```
$ make clean build

$ ./gradlew main-app:run --args="datasource-refresh -p local -d member-base-ds"
```

This will write the `member-base-ds` DataSource to a local file. **NOTE:** Because this is running with a local Catalog 
and local filesystem as the Cortex backend, the result of the DataSource will be written to a local file and does
not exist prior to running the above.

The resulting DataSource is saved as a DeltaTable in `../main-app/build/test-data/cortex-profiles/sources/local/member-base-ds-delta`

## Running in a Docker container with Spark-Submit

Run this example in a docker container with local clients (from the parent directory):
```bash
make clean build create-app-image

docker run -p 4040:4040 --entrypoint="python" -e CORTEX_TOKEN="xxx" \
  -v $(pwd)/datasource-refresh/src/main/resources/conf:/app/conf \
  -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
  -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"
```

Notes:
* The `$CORTEX_TOKEN` environment variable is required by the Spark Submit wrapper, and needs to be a valid JWT token. You can generate this via: `cortex configure token`
* Port 4040 is forwarded from the container to expose the Spark UI (for debugging purposes)
* The 1st volume mount is sharing the [Spark submit config file](./src/main/resources/conf/spark-conf.json)
* The 2cd volume mount shares the LocalCatalog contents and other local application resources
* The 3rd volume mount is the output location of the DataSource

## Running locally against a Cortex Cluster

**Note**: Because this is running outside the Cortex Cluster you will need to know the value of any Secrets ued by the
Connections. For access to Cortex Secrets, see [Running as a Skill](#running-as-a-skill).

### Prerequisites

To run this example in a Spark local mode against a Cortex Cluster with access to the Catalog and Secrets, you will need to:
* Know the [backend storage configuration](../docs/config.md#cortex-backend-storage) for the Cluster, which includes
  names of buckets and access keys for remote storage. If you do not know this, then try [Running as a Skill](#running-as-a-skill).
* Know the values of Cortex secrets used by the Connection
* Generate a `CORTEX_TOKEN`
* Ensures the Cortex resources exist, namely the Project, DataSource, and its Connection
* Update the [spark-conf.json](./src/main/resources/conf/spark-conf.json) file to:
    - use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint (e.g. `https://api.<domain>/fabric/v4/graphql`) and removing the Local Catalog implementation (`spark.cortex.catalog.impl`).
    - update [Local Secret Client](../local-clients/README.md#secrets) with any secrets required by your Connection(s). Ensure to update the project, Secret name, and secret value.
    - Update the `app_command` arguments to match your Cortex project and Connections (`-p`, `-d`)

**NOTE**: If your connections do not use Cortex Secrets because the Cortex cluster has [IRSA enabled](https://cognitivescale.github.io/cortex-charts/docs/platforms/aws/aws-irsa), then you may
not be able to run this example without editing the Connection. This is because IRSA provides authentication within the
cluster, and cannot be leveraged when running locally. Try [Running the example as a Skill](#running-as-a-skill).

### Example

**Note**: The cortex backend storage configuration could be set in the `spark-conf.json`, but environment variables are
used below to avoid hardcoding access keys in the source.

The below example command is assuming:
* the Cortex backend is using the `minio` instance packaged in  the [Cortex Charts](https://github.com/CognitiveScale/cortex-charts) with access and secret keys `xxxxx`/`xxxxx`
* the [CustomSecretClient](../local-clients/README.md#secrets) provides the secret used by the Connections (loaded from `CONNECTION_SECRET_VALUE`)

```
# from the parent directory
$ make clean build create-app-image

$ docker run -p 4040:4040 --entrypoint="python" \
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

## Running as a Skill

### Prerequisites
* Ensures the Cortex resources exist, namely the Project, DataSource, and its Connection
* Generate a `CORTEX_TOKEN`
* Update the [spark-conf.json](./src/main/resources/conf/spark-conf.json) file to:
    - use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint (e.g. `https://api.<domain>/fabric/v4/graphql`) and removing the Local Catalog implementation (`spark.cortex.catalog.impl`).
    - remove the Local Secret Client implementation (`spark.cortex.clients.secrets.impl`)
    - Update the `app_command` arguments to match your Cortex project and DataSource names (`-p`, `-d`)

To run this example in Spark local mode against a Cortex Cluster with access to the Catalog and Secrets, you will need to
update the spark configuration file (e.g. `spark-conf.json`) used by the main application to match configuration for
this example.

Refer to the [instructions for running the Skill Template](../README.md#skill-template) in the top level README for
deploying and invoking the skill.

### Example

```json
// spark-conf.json
{
  "pyspark": {
    "pyspark_bin": "bin/spark-submit",
    "app_command": [
      "datasource-refresh",
      "-p",
      "testi-69257",
      "-d",
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
        "spark.sql.streaming.metricsEnabled": "true",
        "spark.cortex.client.phoenix.url": "http://cortex-api.cortex.svc.cluster.local:8080/fabric/v4/graphql",
        "spark.cortex.client.secrets.url": "http://cortex-accounts.cortex.svc.cluster.local:5000",
        "spark.cortex.catalog.impl": "com.c12e.cortex.profiles.catalog.CortexRemoteCatalog",
        "spark.executor.cores": 1,
        "spark.executor.instances": 2,
        "spark.executor.memory": "4g",
        "spark.driver.memory": "2g",
        "spark.kubernetes.driverEnv.STORAGE_TYPE": "s3",
        "spark.kubernetes.driverEnv.S3_ENDPOINT": "http://cortex-minio.cortex.svc.cluster.local:9000",
        "spark.kubernetes.driverEnv.S3_SSL_ENABLED": "false",
        "spark.kubernetes.driverEnv.AWS_ACCESS_KEY_ID": "****",
        "spark.kubernetes.driverEnv.AWS_SECRET_KEY": "****",
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
        "spark.executor.processTreeMetrics.enabled": "true",
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
* The `--master` and `--deploy-mode` have been set to run the Spark job in the Cortex (Kubernetes) Cluster.
* The Phoenix Client URL and Secret Client URL are referring to services in Kubernetes Cluster
