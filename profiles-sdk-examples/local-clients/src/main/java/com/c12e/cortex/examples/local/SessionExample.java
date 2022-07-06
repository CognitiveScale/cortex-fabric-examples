package com.c12e.cortex.examples.local;

import com.c12e.cortex.phoenix.LocalCatalog;
import com.c12e.cortex.profiles.CortexSession;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

import java.util.Map;

public class SessionExample {

    protected Map<String, String> getDefaultOptions() {
        return Map.of(
                // Use local catalog implementation
                CortexSession.CATALOG_KEY, LocalCatalog.class.getCanonicalName(),
                CortexSession.LOCAL_CATALOG_DIR_KEY,  "src/main/resources/spec",

                // Use a local secret client implementation
                CortexSession.SECRETS_CLIENT_KEY, CustomSecretsClient.class.getCanonicalName()
        );
    }

    public SparkSession sparkSessionFromConfig() {
        SparkConf sparkConf = new SparkConf();
        return SparkSession.builder()
                .config(sparkConf)
                .getOrCreate();
    }

    public SparkSession sparkSessionWithDefaultOptions() {
        Map<String, String> options = getDefaultOptions();
        SparkConf sparkConf = new SparkConf();
        options.forEach((k, v) -> sparkConf.set(k, v));
        return SparkSession.builder()
                .master("local[*]")
                .config(sparkConf)
                .getOrCreate();
    }

    public CortexSession getCortexSession() {
        // Use the SPARK_HOME env variable as a proxy for whether this is running in-cluster or locally, and
        // whether to load spark submit config file. (Guarding against missing config file & required properties).
        boolean useDefaultOptions = System.getenv("SPARK_HOME") == null;
        if (useDefaultOptions) {
            return CortexSession.newSession(sparkSessionWithDefaultOptions(), getDefaultOptions());
        }
        return CortexSession.newSession(sparkSessionFromConfig());
    }
}
