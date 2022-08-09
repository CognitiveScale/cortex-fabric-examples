package com.c12e.cortex.examples;

import com.c12e.cortex.examples.local.CustomSecretsClient;
import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.examples.local.CatalogExample;
import com.c12e.cortex.phoenix.Connection;
import com.c12e.cortex.phoenix.LocalCatalog;
import com.c12e.cortex.profiles.CortexSession;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SetEnvironmentVariable(key = CustomSecretsClient.CONNECTION_SECRET_ENV, value = "secret-value")
@SetEnvironmentVariable(key = CustomSecretsClient.STREAMING_SECRET_ENV, value = "streaming-secret-value")
public class TestLocalClient {

    public static Set<String> EXPECTED_CONNECTIONS = Set.of(
            "member-base-file", "member-feedback-file", "member-flu-risk-file", "member-joined-file");

    @Test
    public void testUseCortexCatalog() {
        var example = new SessionExample();
        var catalogExample = new CatalogExample();
        CortexSession session = example.getCortexSession();
        List<Connection> connections = catalogExample.listConnectionsInCatalog(session);
        assertTrue(session.catalog() instanceof LocalCatalog);
        for (var connectionName : EXPECTED_CONNECTIONS) {
            assertTrue(connections.stream().anyMatch(c -> connectionName.equals(c.getName())),
                    String.format("Connection '%s' not found in the LocalCatalog", connectionName));
        }
    }

    @Test
    public void testUseSecretClient() {
        var example = new SessionExample();
        var secrets = new CustomSecretsClient();
        CortexSession session = example.getCortexSession();

        // expect plaintext secret when reading a connection (provided by custom client)
        Connection connWithSecret = session.catalog().getConnection("local", "member-flu-risk-file");
        Map<String, String> params = connWithSecret.getParamMap(secrets);
        assertEquals("secret-value", params.get("secretParam"));
    }
}
