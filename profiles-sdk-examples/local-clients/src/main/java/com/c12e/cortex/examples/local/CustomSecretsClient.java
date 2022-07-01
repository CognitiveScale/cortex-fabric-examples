package com.c12e.cortex.examples.local;

import java.util.Map;
import com.c12e.cortex.profiles.client.LocalSecretClient;

/**
 * Custom implementation of a {@code CortexSecretsClient} for providing secrets for local use.
 */
public class CustomSecretsClient extends LocalSecretClient {
    // stores a map of secret key and values for each project
    private static final LocalSecretClient.LocalSecrets localSecrets = new LocalSecretClient.LocalSecrets();
    static {{
        localSecrets.setSecretsForProject("local", Map.of(
                // load secret from environment variables to avoid harcoding
                "secret", System.getenv().getOrDefault("MY_ENVIRONMENT_VARIABLE", "")
        ));
    }}

    public CustomSecretsClient() {
        super(localSecrets);
    }
}
