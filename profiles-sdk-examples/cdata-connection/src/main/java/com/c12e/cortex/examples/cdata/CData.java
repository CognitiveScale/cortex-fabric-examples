package com.c12e.cortex.examples.cdata;

import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.client.LocalSecretClient;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import java.util.Map;

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
     * Custom Secrets Client.
     */
    public static class CDataSecretClient extends LocalSecretClient {
        private static LocalSecrets localSecrets = new LocalSecrets();
        static {{
            // load 'oem_key' secret in the "local" project
            localSecrets.setSecretsForProject("local", Map.of(
                    "oem_key", System.getenv(CDATA_KEY_ENV)
            ));
        }}

        public CDataSecretClient() {
            super(localSecrets);
            if (System.getenv(CDATA_KEY_ENV) == null) {
                System.err.println(String.format("Missing environment variable '%s' for local secrets client", CDATA_KEY_ENV));
                System.exit(2);
            }
            // TODO(LA): Why do we need this?
            System.setProperty("product_checksum", System.getenv("CDATA_PRODUCT_CHECKSUM"));
        }

    }

    @Override
    public void run() {
        SessionExample sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSession();
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
