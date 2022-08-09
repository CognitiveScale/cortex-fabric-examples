package com.c12e.cortex.examples.local;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.c12e.cortex.profiles.client.LocalSecretClient;

/**
 * Custom implementation of a {@code CortexSecretsClient} for providing secrets for local use.
 */
public class CustomSecretsClient extends LocalSecretClient {
    public static final String STREAMING_SECRET_ENV = "STREAMING_SECRET_KEY";
    public static final String CONNECTION_SECRET_ENV = "CONNECTION_SECRET_VALUE";

    // Stores a map of Secret key and values per Project.
    private static final LocalSecretClient.LocalSecrets localSecrets = new LocalSecretClient.LocalSecrets();
    static {{
        localSecrets.setSecretsForProject("local", Map.of(
                // Add 'secret-key' to the "local" Project
                "secret-key", "secret-value",

                // Load the Secret value from an environment variable to avoid hardcoding
                "secret-from-env", System.getenv().getOrDefault(CONNECTION_SECRET_ENV, ""),

                // Load 'streaming-secret' from an environment variable (for streaming-connections)
                "streaming-secret", System.getenv().getOrDefault(STREAMING_SECRET_ENV, "")
        ));
    }}

    public CustomSecretsClient() {
        super(localSecrets);
    }

    /**
     * Checks whether the environment variables exist or not.
     * @param env environment variables to check
     * @throws RuntimeException if any of the environment variables is not defined
     */
    public void requireEnvExists(List<String> env) {
        var envNotExist = env.stream().map(e -> System.getenv(e) == null).collect(Collectors.toList());
        var missingEnv = IntStream.range(0, env.size())
                .filter(i -> envNotExist.get(i))
                .mapToObj(i -> env.get(i))
                .collect(Collectors.toList());

        if (!missingEnv.isEmpty()) {
            var error =String.format("Missing environment variable(s) '%s' for CustomSecretsClient.",
                    String.join(", ", missingEnv));
            System.err.println(error);
            throw new RuntimeException(error);
        }
    }
}
