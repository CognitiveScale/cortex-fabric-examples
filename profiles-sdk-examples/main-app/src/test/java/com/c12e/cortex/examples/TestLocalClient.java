package com.c12e.cortex.examples;

import com.c12e.cortex.examples.local.CustomSecretsClient;
import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.examples.local.CatalogExample;
import com.c12e.cortex.phoenix.Connection;
import com.c12e.cortex.phoenix.LocalCatalog;
import com.c12e.cortex.profiles.CortexSession;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SetEnvironmentVariable(key = CustomSecretsClient.CONNECTION_SECRET_ENV, value = "secret-value")
@SetEnvironmentVariable(key = CustomSecretsClient.STREAMING_SECRET_ENV, value = "streaming-secret-value")
public class TestLocalClient {

    public static final int EXPECTED_NUM_CONNECTIONS = 8;

    @Test
    public void testUseCortexCatalog() {
        var example = new SessionExample();
        var catalogExample = new CatalogExample();
        CortexSession session = example.getCortexSession();
        assertTrue(session.catalog() instanceof LocalCatalog);
        assertEquals(EXPECTED_NUM_CONNECTIONS, catalogExample.listConnectionsInCatalog(session).size());
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
