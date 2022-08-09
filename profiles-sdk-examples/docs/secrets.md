# Secrets

When packaged in an Agent, the Profiles SDK has access to Cortex Secrets in the cluster. However,
Secrets are not exposed to applications running outside the Cortex cluster. Although this may seem like a limitation, the
Profiles SDK includes a local mock-client for providing Secrets.

This mock-client may be useful when:
* When running an application locally (outside the cluster) that uses a Connection or Data Source defined in a remote Cortex Cluster (with Secrets).
* When running an application locally (outside the cluster) that uses a Connection or Data Source in a remote storage (S3/GCS/Azure).

Base Interface: `com.c12e.cortex.profiles.client.CortexSecretsClient`

Implementation: `com.c12e.cortex.profiles.client.LocalSecretsClient`

### Configuration Options

To use a local Secret client you must:
- Subclass the `LocalSecretsClient` with an implementation that contains Secrets for your Connections/Data Sources
- Specify the Secret client implementation to use in Spark configuration property. Set `spark.cortex.client.secrets.impl` to the corresponding class path 

<!-- It is possible to bind a Secret client using an explicit Guice binding, but that is pulling the covers back too much (requires Guice knowledge) -->
For example, you would set `spark.cortex.client.secrets.impl` to `com.example.app.CustomSecretsClient` for the following custom implementation:
```java
public class CustomSecretsClient extends LocalSecretClient {
    // LocalSecrets stores a map of Secret key and values for each project
    private static final LocalSecretClient.LocalSecrets localSecrets = new LocalSecretClient.LocalSecrets();
    static {{
        localSecrets.setSecretsForProject("local", Map.of(
                // Load Secret from environment variables to avoid hardcoding
                "secret", System.getenv().getOrDefault("MY_ENVIRONMENT_VARIABLE", "default"),
                "plaintext-secret", "****"
        ));
    }}
    public CustomSecretsClient() {
        super(localSecrets);
    }
}
```
(See [./config.md](./config.md#local-development) for more information on configuration options.)

## Resources
* [Manage Cortex Secrets](https://cognitivescale.github.io/cortex-fabric/docs/administration/secrets)
