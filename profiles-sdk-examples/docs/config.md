# Configuration Options

Cortex configuration properties can be specified in the [Spark Configurations](https://spark.apache.org/docs/latest/index.html).

Configuration options are specific to your use case and may depend on:
- How/where you intend to run your application
- What resources are available in the target environment
- What your use case is

<!-- TODO: General Spark configuration guidance (Spark UI, Debugging, metrics, fs performance)
## Spark Configuration Guidance
-->

### Local Development

When working locally it is useful to set:
* `spark.cortex.catalog.impl` to the local Catalog implementation.
* `spark.cortex.catalog.local.dir` to the local Catalog directory.
* `spark.cortex.client.secrets.impl` to the local Secrets Client implementation.
* `spark.cortex.proxy.impl` to a Guice `MethodInterceptor` implementation.
* `spark.cortex.client.storage.impl` to `"com.c12e.cortex.profiles.client.LocalRemoteStorageClient"` to use a local directory to mock Cortex backend storage.
* `spark.cortex.storage.file.baseDir` to the base directory when working with a local directory.

Example usage of the above configuration options can be found at [../local-clients](../local-clients/README.md).

### Use a Remote Catalog

When running outside the cluster, set:
* `spark.cortex.client.phoenix.url`
* `spark.cortex.client.phoenix.token`

### Cortex Backend Storage 

* Set `spark.cortex.client.storage.impl` to set the client implementation you are  using.
* If using the local remote storage client implementation, then set `spark.cortex.storage.storageType` to `file` and
  update `spark.cortex.storage.file.baseDir` to the base directory in the local filesystem. You can optionally configure
  the bucket names to refer to a specific file path:
  - `spark.cortex.storage.bucket.managedContent`
  - `spark.cortex.storage.bucket.profiles`
  - `spark.cortex.storage.bucket.amp`
  * If using the default (in-cluster) remote storage client implementation, then configuration options for remote storage
  are provided by the cluster and **need not be set**. Configuration options prefixed with `spark.cortex.storage`
  can override values provided by the client implementation.

The hierarchy for loading Storage Configuration options (least to most priority)
* Values provided by the storage client implementation
* Spark configuration properties
* Environment variables (see table below)
* Programmatic configuration overrides

**Example**: Assume a user has a Spark configuration file with the following options, and no configuration options are set programmatically:
```json
{
  "spark.cortex.client.storage.impl": "com.c12e.cortex.profiles.client.InternalRemoteStorageClient",
  "spark.cortex.client.phoenix.url": "http://cortex-api.cortex.svc.cluster.local:8080",
  "spark.cortex.storage.storageType": "file",

  "spark.cortex.storage.bucket.managedContent": "custom-mc-bucket",
  "spark.cortex.storage.bucket.profiles": "custom-profiles-bucket",
  "spark.cortex.storage.bucket.amp": "custom-amp-bucket",

  "spark.kubernetes.driverEnv.STORAGE_TYPE": "s3",
  "spark.kubernetes.driverEnv.S3_ENDPOINT": "https://minio.example.com",
  "spark.kubernetes.driverEnv.S3_SSL_ENABLED": "false",
  "spark.kubernetes.driverEnv.AWS_ACCESS_KEY_ID": "xxxx",
  "spark.kubernetes.driverEnv.AWS_SECRET_KEY": "xxxx"
}
```

The in-cluster [remote storage client](backendstorage.md#remote-storage-client) is used, but instead of using the
clusters remote storage, the SDK uses `s3` compatible storage hosted at `"https://minio.example.com"` with the
given access keys. This is because the S3 related environment variables are overriding the option set via configuration
properties (`spark.cortex.storage.storageType`), as well as the values provided by the client implementation.

## Cortex Config options

Below is a table of configuration options. Examples are defaults.

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
| spark.cortex.client.phoenix.impl              |                                                                                                                                              |                         |                                 | false     |
| spark.cortex.storage.remote.impl              | PhoenixRemoteStorage implementation                                                                                                          |                         |                                 | false     |
| spark.cortex.phoenix.token                    | Phoenix Token Key Path.                                                                                                                      |                         |                                 | true      |
| spark.cortex.client.phoenix.url               | Phoenix Client URL Key Path.                                                                                                                 |                         |                                 | true      |
| spark.cortex.client.secrets.url               | Cortex Secrets Client URL.                                                                                                                   |                         |                                 | true      |
| spark.cortex.catalog.local.dir                | Local Catalog directory path, only applicable if using a local Cortex catalog instance, @see com.c12e.cortex.phoenix.LocalCatalog.           |                         |                                 | true      |
| spark.cortex.client.storage.impl              | CortexRemoteStorageClient implementation used for accessing remote storage when running in a Cortex Cluster                                  |                         |                                 | true      |
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

