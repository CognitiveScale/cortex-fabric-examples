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
     * Local Secrets Client including CData Keys from the "shared" project.
     */
    public static class CDataSecretClient extends LocalSecretClient {
        private static LocalSecrets localSecrets = new LocalSecrets();
        static {{
            // Load the 'cdata-oem_key' and 'cdata-prdct-checksum' secret in the "shared" project.
            // These Secrets are assumed to have the following names in this project by default.
            requireEnvExists(List.of(CDATA_KEY_ENV, CDATA_CHECKSUM_ENV));
            localSecrets.setSecretsForProject("shared", Map.of(
                    "cdata-oem-key", System.getenv(CDATA_KEY_ENV),
                    "cdata-prdct-checksum", System.getenv(CDATA_CHECKSUM_ENV)
            ));


            localSecrets.setSecretsForProject("local", Map.of(
                    // The 'csv-props' Secret has a JSON String with parameters for the CData CSV Driver.
                    // All parameters separated, but could optionally be combined into a single 'url' param, ex: "jdbc:csv:GenerateSchemaFiles=OnStart;URI=file://./src/main/resources/data/members_100_v14.csv;Location=./build/tmp;"
                    "csv-props", "{\"GenerateSchemaFiles\":\"OnStart\",\" URI\":\"file://./src/main/resources/data/members_100_v14.csv\",\"Location\":\"./build/tmp/work\"}",

                    // The 'bigquery-props' Secret has a JSON String with parameters for the CData BigQuery JDBC Driver.
                    // Requires setting 'BIGQUERY_PROJECT' and 'BIGQUERY_CREDS_FILE' environment variable.
                    "bigquery-props",  String.format(
                            "{\"AuthScheme\":\"OAuthJWT\",\"InitiateOAuth\":\"GETANDREFRESH\",\"OAuthJWTCertType\":\"GOOGLEJSON\",\"OAuthJWTCert\":\"%s\",\"ProjectId\":\"%s\",\"DatasetId\":\"covid19_weathersource_co\"}",
                            System.getenv().getOrDefault("BIGQUERY_CREDS_FILE", ""),
                            System.getenv().getOrDefault("BIGQUERY_PROJECT", "")),

                    // The 's3-props' Secret has a JSON String with parameters for the CData S3 JDBC Driver.
                    "s3-props", String.format("{\"AWSAccessKey\":\"%s\",\"AWSSecretKey\":\"%s\"}",
                            System.getenv().getOrDefault("S3_ACCESS_KEY", ""),
                            System.getenv().getOrDefault("S3_SECRET_KEY", ""))
            ));
        }}

        public static void requireEnvExists(List<String> env) {
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
        }
    }

    @Override
    public void run() {
        SessionExample sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSessionWithOverrides(Map.of(
                CortexSession.SECRETS_CLIENT_KEY, CDataSecretClient.class.getName()
        ));
        readConnection(cortexSession, project, source, sink);
    }

    public void readConnection(CortexSession cortexSession, String project, String source, String sink) {
        // Read CData connection
        Dataset<Row> ds = cortexSession.read().connection(project, source).load();

        //TODO: Perform some type of transform

        // Write to a connection
        cortexSession.write().connection(ds, project, sink).mode(SaveMode.Overwrite).save();
    }
}
