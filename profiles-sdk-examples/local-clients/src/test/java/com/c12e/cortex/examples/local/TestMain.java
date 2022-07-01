package com.c12e.cortex.examples.local;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.phoenix.Connection;
import com.c12e.cortex.phoenix.LocalCatalog;
import com.c12e.cortex.profiles.CortexSession;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SetEnvironmentVariable(key = "MY_ENVIRONMENT_VARIABLE", value = "secret-value")
public class TestMain {

    public static final int EXPECTED_NUM_CONNECTIONS = 4;

    @Test
    public void testUseCortexCatalog() {
        var example = new SessionExample();
        CortexSession session = example.getCortexSessionFromExplicitOptions();
        assertTrue(session.catalog() instanceof LocalCatalog);
        assertEquals(example.useCortexCatalog(session).size(), EXPECTED_NUM_CONNECTIONS);
    }

    @Test
    public void testUseSecretClient() {
        var example = new SessionExample();
        var secrets = new CustomSecretsClient();
        CortexSession session = example.getCortexSessionFromExplicitOptions();
        Connection connWithSecret = session.catalog().getConnection("local", "member-flu-risk-file");
        Map<String, String> params = connWithSecret.getParamMap(secrets);
        assertEquals(params.get("secretParam"), "secret-value");
    }
}
