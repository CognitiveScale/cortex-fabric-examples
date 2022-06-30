# Secrets

When packaged in an Agent the Profiles SDK (`CortexSession`) will have access to Cortex Secrets in the cluster. However,
secrets are not exposed to application running outside the Cortex cluster. Although this may seem like a limitation, the
Profiles SDK includes a local mock client for providing secrets.

This mock client may be useful when:
* when running an application locally (outside the cluster) that uses a Connection or DataSource defined in a remote Cortex Cluster (with secrets)
* when running an application locally (outside the cluster) that uses a Connection or DataSource in a remote storage (S3/GCS/Azure)

Base Interface: `com.c12e.cortex.profiles.client.CortexSecretsClient`

Implementation: `com.c12e.cortex.profiles.client.LocalSecretsClient`

### Configuration Options

To use a local Secret client you will need to:
- Subclass the `LocalSecretsClient` with an implementation that contains secrets for your Connections/DataSources
- Specify the secret client implementation to use in Spark configuration property. Set `spark.cortex.clients.secrets.impl` to the corresponding class path 

<!-- It is possible to bind a Secret client using an explicit Guice binding, but that is pulling the covers back too much (requires Guice knowledge) -->
For example, you would set `spark.cortex.clients.secrets.impl` to `com.example.app.CustomSecretsClient` for the following custom implementation:
```java
package com.example.app;

public class CustomSecretsClient extends LocalSecretsClient {
    // LocalSecrets stores a map of secret key and values for each project
    private LocalSecrets localSecrets = new LocalSecretsClient.LocalSecrets();
    public CustomSecretsClient() {
        super(localSecrets);
        localSecrets.setSecretsForProject("project", Map.of(
                "secret-key", "secret-value",
                "secret-from-env", System.getenv("MY_ENVIRONMENT_VARIABLE") // load secret from environment variables
        ));
    }
}
```
See [./config.md](./config.md#local-development) for more information on configuration options.

## Resources
* [Manage Cortex Secrets](https://cognitivescale.github.io/cortex-fabric/docs/administration/secrets)
