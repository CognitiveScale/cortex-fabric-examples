package com.c12e.cortex.examples.local;

import java.util.Map;
import com.c12e.cortex.profiles.client.LocalSecretClient;

/**
 * Custom implementation of a {@code CortexSecretsClient} for providing secrets for local use.
 */
public class CustomSecretsClient extends LocalSecretClient {
    // stores a map of secret name (key) and values for each project
    private static final LocalSecretClient.LocalSecrets localSecrets = new LocalSecretClient.LocalSecrets();

    static {{
        localSecrets.setSecretsForProject("local", Map.of(
                // load 'secret' from environment variables to avoid harcoding (the env variable choice is arbitrary)
                "secret", System.getenv().getOrDefault("CONNECTION_SECRET_VALUE", "")
        ));
    }}

    public CustomSecretsClient() {
        super(localSecrets);
    }
}
