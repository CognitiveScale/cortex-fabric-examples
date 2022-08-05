# Cortex Backend Storage

When packaged in an Agent, the Profiles SDK has access to the Cortex Clusters remote storage
for [Managed Content](https://cognitivescale.github.io/cortex-fabric/docs/manage-data/managed-content),
[Cortex Profile](https://cognitivescale.github.io/cortex-fabric/docs/introduction/intro-fabric-profiles),
and [Cortex Campaigns](https://cognitivescale.github.io/cortex-fabric/docs/introduction/intro-fabric-campaigns) data.

However, the remote storage is not immediately available when running outside the Cortex Cluster
unless [configuration properties](config.md#cortex-backend-storage) are set to authenticate against the remote storage.
Although this may seem like a limitation during development, the Profiles SDK includes a local client for using your 
local filesystem instead of Cortex's remote storage.

Base Interface: `com.c12e.cortex.profiles.client.CortexRemoteStorageClient`

## Remote Storage Client

By default, the Profiles SDK (`CortexSession`) utilizes a remote storage client to get
the [bucket configuration](../docs/config.md#cortex-backend-storage) for the Cortex Fabric cluster. This is such that
deployed Agents containing your application are able to access Managed Content and Profile data.

**This implementation will not work when running outside the cluster**. <!-- TODO: Show error when using it outside the cluster? -->

Implementation: `com.c12e.cortex.profiles.client.InternalRemoteStorageClient` <!-- TODO: Link to javadoc -->

### Configuration Properties

The remote client implementation requires setting:
* The Cortex API endpoint to be specified (`https://api.<dci-base-domain>`) - `spark.cortex.client.phoenix.url`.
* A Cortex API Token - `spark.cortex.client.phoenix.token`.

The Spark-submit configuration options should include:
```json
{
  "spark.cortex.client.storage.impl": "com.c12e.cortex.profiles.client.InternalRemoteStorageClient",
  "spark.cortex.client.phoenix.url": "http://cortex-api.cortex.svc.cluster.local:8080"
}
```

**NOTE**: The Cortex API token referenced above is NOT set in the configuration file.

Additional [configuration properties](./config.md#cortex-backend-storage) can be set to override which remote storage is
used by the SDK. When packaged in an Agent, no additional configuration options need to be set.

## Local Storage Client

The Profiles SDK supports configuring a local filesystem as the backend Cortex Storage. This is useful when working
outside the cluster, but may require additional setup because there is no starting data for Managed Content, Profiles,
or Campaigns.

Implementation: `com.c12e.cortex.profiles.client.LocalRemoteStorageClient` <!-- TODO: Link to javadoc -->

### Configuration Properties

The local client implementation requires setting the base directory for the file implementation - `spark.cortex.storage.file.baseDir`.

The Spark-submit [configuration options](./config.md#cortex-backend-storage) should look similar to:
```json
{
  "spark.cortex.client.storage.impl": "com.c12e.cortex.profiles.client.LocalRemoteStorageClient",
  "spark.cortex.storage.file.baseDir": "/tmp/local-data",
  "spark.cortex.storage.bucket.managedContent": "cortex-content",
  "spark.cortex.storage.bucket.profiles": "cortex-profiles",
  "spark.cortex.storage.bucket.amp": "cortex-amp"
}
```

Note that the base directory can be configured and that the storage buckets are optional. The above example
configures the Profiles SDK to use the local filepaths:
- `/tmp/local-data/cortex-content`
- `/tmp/local-data/cortex-profiles`
- `/tmp/local-data/cortex-amp`