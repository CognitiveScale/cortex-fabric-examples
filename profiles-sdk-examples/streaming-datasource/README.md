# Streaming Connections

This example is contains a CLI application for refreshing a DataSource via streaming. This builds off
the [Local Clients](../local-clients/README.md) example for its setup but uses a different set of Connections and DataSource for this example defined in .

See [StreamDataSource.java](./src/main/java/com/c12e/cortex/examples/streaming/StreamingDataSource.java) for the full source.

## Prerequisite:

**NOTE:** Streaming is only supported for S3 File Stream and GCS File Stream Connection types. See, [Connection Types](https://cognitivescale.github.io/cortex-fabric/docs/reference-guides/connection-types).
This example will assume an S3 File Stream is being used, but you can update Connection definition and secrets accordingly.

* Upload data to S3 that can be used for the connection. You can optionally use the [member dataset](../main-app/src/main/resources/data/members_100_v14.csv) used in local examples.
* Update the `member-base-s3-stream` [S3 File Stream](https://cognitivescale.github.io/cortex-fabric/docs/reference-guides/connection-types#s3-file-stream-connections)
  Connection parameters with your data. Specifically, `uri`, `streamReadDir`, `s3Endpoint`, `publicKey`, and `secretKey`
  (this should be a [secret](https://cognitivescale.github.io/cortex-fabric/docs/administration/secrets),
* Update the [CustomSecretsClient](../local-clients/README.md#secrets) to load the Secret. For example, supposing the Connection's `secretKey` is `#SECURE.streaming-secret` and the key will be loaded from the `STREAMING_SECRET_KEY`:
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
        if (System.getenv(STREAMING_SECRET_ENV) == null) { // missing secret key
            throw new RuntimeException(String.format("Missing environment variable '%s' for local secrets client", STREAMING_SECRET_ENV));
        }
    }
}
```

## Running Locally

To run this example locally with local Cortex clients (from the parent directory):
```
$ make clean build

$ ./gradlew main-app:run --args="ds-streaming -p local -d member-base-s3-stream
```

This will write teh `member-base-s3-stream` DataSource to a loal file. **NOTE**: Because this is a running with a local Catalog
and local filesystem as the Cortex backend, the result of the DataSource will be written to a local file an does not exist
prior to running the above.

The resulting DataSource is saved as a DeltaTable in `../main-app/build/test-data/cortex-prof`