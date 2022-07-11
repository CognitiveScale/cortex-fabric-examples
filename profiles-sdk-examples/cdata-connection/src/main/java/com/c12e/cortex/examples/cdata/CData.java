package com.c12e.cortex.examples.cdata;

import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.client.LocalSecretClient;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.c12e.cortex.examples.local.SessionExample;

/**
 * Example CLI application using a CData Connection and setting up a custom secret client.
 */
@Command(name = "cdata", description = "Example CData Connection", mixinStandardHelpOptions = true)
public class CData implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @Option(names = {"-i", "--input"}, description = "Connection to read from", required = true)
    String source;

    @Option(names = {"-o", "--output"}, description = "Connection to write to", required = true)
    String sink;

    /**
     * Environment variable name for loading cdata key
     */
    private static final String CDATA_KEY_ENV = "CDATA_OEM_KEY";

    /**
     * Environment variable name for loading cdata checksum
     */
    private static final String CDATA_CHECKSUM_ENV = "CDATA_PRODUCT_CHECKSUM";

    /**
     * Custom Secrets Client.
     */
    public static class CDataSecretClient extends LocalSecretClient {
        private static LocalSecrets localSecrets = new LocalSecrets();
        static {{
            // load 'oem_key' secret in the "local" project
            localSecrets.setSecretsForProject("local", Map.of(
                    "oem_key", System.getenv(CDATA_KEY_ENV)
                    //"secret", System.getenv().getOrDefault("CONNECTION_SECRET_VALUE", "")
            ));
        }}

        public void requireEnvExists(List<String> env) {
            var envNotExist = env.stream().map(e -> System.getenv(e) == null).collect(Collectors.toList());
            var missingEnv = IntStream.range(0, env.size())
                    .filter(i -> envNotExist.get(i))
                    .mapToObj(i -> env.get(i))
                    .collect(Collectors.toList());

            if (!missingEnv.isEmpty()) {
                System.err.println(
                        String.format("Missing environment variable(s) '%s' for local secrets client",
                                String.join(", ", missingEnv)));
                System.exit(2);
            }
        }

        public CDataSecretClient() {
            super(localSecrets);
            requireEnvExists(List.of(CDATA_KEY_ENV, CDATA_CHECKSUM_ENV));
            System.setProperty("product_checksum", System.getenv(CDATA_CHECKSUM_ENV)); // TODO(LA): Why do we need this?
        }
    }

    @Override
    public void run() {
        SessionExample sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSessionWithOverrides(Map.of(
                //CortexSession.SECRETS_CLIENT_KEY, CDataSecretClient.class.getCanonicalName())
                CortexSession.SECRETS_CLIENT_KEY, "com.c12e.cortex.examples.cdata.CData$CDataSecretClient"
        ));
        readConnection(cortexSession, project, source, sink);
    }

    public void readConnection(CortexSession cortexSession, String project, String source, String sink) {
        // read CData connection
        Dataset<Row> ds = cortexSession.read().connection(project, source).load();

        //TODO Perform some type of transform

        // write to a connection
        cortexSession.write().connection(ds, project, sink).mode(SaveMode.Overwrite).save();

        //TODO add CDATA jar to build dependencies

        //TODO whatever is easiest...salesforce?
    }

}
