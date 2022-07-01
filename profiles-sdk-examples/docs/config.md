# Configuration Options

Cortex configuration properties can be specified in the [Spark Configurations](https://spark.apache.org/docs/latest/index.html).

## Recommended Configuration options

TODO: general spark config guidance

### Local Development

When working locally it is useful to set:
* `spark.cortex.catalog.impl` to the Local Catalog implementation
* `spark.cortex.catalog.local.dir` to the Local Catalog Directory
* `spark.cortex.clients.secrets.impl` to the local Secrets Client Implementation
* `spark.cortex.proxy.impl` to a Guice MethodInterceptor implementation
* `spark.cortex.storage.storageType` - Set this to `"file"` to use a local directory to mock Cortex Backend Storage
* `spark.cortex.storage.file.baseDir` - Base directory when working with a local directory

* `spark.ui.enabled`
* `spark.ui.prometheus.enabled`
* `spark.sql.streaming.metricsEnabled`

Example usage of the above configuration options can be found [../local-clients](../local-clients/README.md).

### Using a remote Catalog

When running outside the cluster, set:
* `spark.cortex.client.phoenix.url`
* `spark.cortex.client.phoenix.token`

### Cortex Backend Storage 
* Set `spark.cortex.storage.storageType` to the storage type to either: `s3`, `gcs`, `file`
* Set the corresponding options for the storage type. Refer to below options prefixed with: `spark.cortex.storage.<storage_type>*`
* Configure bucket names Cortex backend storage (can be a local path for `file` based storage):
  - `spark.cortex.storage.bucket.managedContent`
  - `spark.cortex.storage.bucket.profiles`
  - `spark.cortex.storage.bucket.amp`

The required spark Config properties depend on the DCIs configuration, see: 
* https://cognitivescale.github.io/cortex-charts/docs/infrastructure/minio
* https://cognitivescale.github.io/cortex-charts/docs/installation

## Cortex Config options

Below is a full table of options (examples are defaults), **not all of these need be set**

Cortex Config Options:

| Config value                                  | Description (Javadoc)                                                                                                                        | Environment             | Example                         | Supported |
|-----------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|-------------------------|---------------------------------|-----------|
| spark.cortex.catalog.impl                     | Cortex Catalog implementation                                                                                                                |                         |                                 | true      |
| spark.cortex.client.secrets.impl              | Cortex Secrets Client implementation                                                                                                         |                         |                                 | true      |
| spark.cortex.proxy.impl                       | MethodInterceptor implementation to intercept method calls                                                                                   |                         |                                 | true      |
| spark.cortex.reader.impl                      | CortexSparkReader implementation                                                                                                             |                         |                                 | false     |
| spark.cortex.writer.impl                      | CortexSparkWriter implementation                                                                                                             |                         |                                 | false     |
| spark.cortex.client.impl                      | CortexVersionStore implementation                                                                                                            |                         |                                 | false     |
| spark.cortex.writer.connection.impl           | CortexConnectionWriter implementation                                                                                                        |                         |                                 | false     |
| spark.cortex.writer.connection.validator.impl | CortexValidator implementation used for writing connections                                                                                  |                         |                                 | false     |
| spark.cortex.writer.dataSource.impl           | CortexDataSourceWriter implementation                                                                                                        |                         |                                 | false     |
| spark.cortex.writer.dataSource.validator.impl | CortexValidator implementation used for writing DataSources                                                                                  |                         |                                 | false     |
| spark.cortex.reader.connection.impl           | CortexConnectionReader implementation                                                                                                        |                         |                                 | false     |
| spark.cortex.reader.connection.validator.impl | CortexValidator implementation used for reading Connections                                                                                  |                         |                                 | false     |
| spark.cortex.client.phoenix.impl              | ???????                                                                                                                                      |                         |                                 | false     |
| spark.cortex.storage.remote.impl              | PhoenixRemoteStorage implementation                                                                                                          |                         |                                 | false     |
| spark.cortex.phoenix.token                    | Phoenix Token Key Path.                                                                                                                      |                         |                                 | true      |
| spark.cortex.client.phoenix.url               | Phoenix Client URL Key Path.                                                                                                                 |                         |                                 | true      |
| spark.cortex.client.secrets.url               | Cortex Secrets Client URL.                                                                                                                   |                         |                                 | true      |
| spark.cortex.catalog.local.dir                | Local Catalog directory path, only applicable if using a local Cortex catalog instance, @see com.c12e.cortex.phoenix.LocalCatalog.           |                         |                                 | true      |
| spark.cortex.storage.storageType              | Cortex Backend Storage type, one of: s3, gcs, file                                                                                           | STORAGE_TYPE            | file                            | true      |
| spark.cortex.storage.file.baseDir             | Directory for file based backend storage                                                                                                     | FILE_BASE_DIR           | ./build/test-data/              | true      |
| spark.cortex.storage.s3.assumeIam             | Whether IAM authentication is enabled for S3 based Cortex backend storage                                                                    | ASSUME_AWS_IAM          | false                           | true      |
| spark.cortex.storage.s3.accessKey             | AWS access key for S3 based Cortex backend storage                                                                                           | AWS_ACCESS_KEY_ID       | *****                           | true      |
| spark.cortex.storage.s3.secretKey             | AWS secret key for S3 based Cortex backend storage                                                                                           | AWS_SECRET_KEY          | *****                           | true      |
| spark.cortex.storage.s3.endpoint              | S3 Endpoint for S3 based Cortex backend storage                                                                                              | S3_ENDPOINT             | http://localhost:9000           | true      |
| spark.cortex.storage.s3.region                | AWS region S3 based Cortex backend storage                                                                                                   | AWS_REGION              | aws-global                      | true      |
| spark.cortex.storage.s3.sslEnabled            | Whether SSL is enabled for S3 backed Cortex backend storage                                                                                  | S3_SSL_ENABLED          | false                           | true      |
| spark.cortex.storage.s3.pathstyle             | Whether S3 path style is used or not                                                                                                         | S3_PATH_STYLE_ACCESS    | true                            | true      |
| spark.cortex.storage.bucket.managedContent    | Name of bucket for Cortex Managed Content                                                                                                    | CONTENT_BUCKET          | cortex-content                  | true      |
| spark.cortex.storage.bucket.profiles          | Name of bucket for Cortex Profiles                                                                                                           | PROFILES_BUCKET         | cortex-profiles                 | true      |
| spark.cortex.storage.bucket.amp               | Name of bucket for AMP (AI-Mission Planning)                                                                                                 | AMP_BUCKET              | cortex-amp                      | true      |
| spark.cortex.storage.gcs.storageRoot          | Name of bucket for AMP (AI-Mission Planning)                                                                                                 | CONTENT_BUCKET          | https://storage.googleapis.com/ | true      |
| spark.cortex.storage.gcs.servicePath          | Service path for GCS based Cortex backend storage                                                                                            | GCS_SERVICE_PATH        | storage/v1/                     | true      |
| spark.cortex.storage.gcs.authType             | Authentication type for GCS based Cortex backend storage, possible values: SERVICE_ACCOUNT_JSON_KEYFILE, COMPUTE_ENGINE, APPLICATION_DEFAULT | GCS_AUTH_TYPE           | SERVICE_ACCOUNT_JSON_KEYFILE    | true      |
| spark.cortex.storage.gcs.serviceAccountKey    | GCS Service Account (JSON String) for GCS backed Cortex backend storage                                                                      | GCS_SERVICE_ACCOUNT_KEY |                                 | true      |

