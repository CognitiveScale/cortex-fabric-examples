package com.c12e.cortex.examples.local;

import com.c12e.cortex.phoenix.LocalCatalog;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.intercept.TracingMethodInterceptor;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

import java.util.HashMap;
import java.util.Map;

public class SessionExample {

    protected Map<String, String> getDefaultOptions() {
        var options = new HashMap<>(Map.of(
                // Use local catalog implementation
                CortexSession.CATALOG_KEY, LocalCatalog.class.getCanonicalName(),
                CortexSession.LOCAL_CATALOG_DIR_KEY,  "src/main/resources/spec",

                // Use a local secret client implementation
                CortexSession.SECRETS_CLIENT_KEY, CustomSecretsClient.class.getCanonicalName()
        ));
        options.put("spark.ui.enabled", "true");
        options.put("spark.ui.prometheus.enabled", "true");
        options.put("spark.delta.logStore.gs.impl", "io.delta.storage.GCSLogStore");
        options.put("spark.shuffle.service.enabled", "false");
        options.put("spark.dynamicAllocation.enabled", "false");
        options.put("spark.scheduler.mode", "FAIR");
        options.put("spark.sql.streaming.schemaInference", "true");
        options.put("spark.sql.legacy.timeParserPolicy", "LEGACY");
        options.put("spark.sql.adaptive.enabled", "true");
        options.put("spark.sql.adaptive.coalescePartitions.enabled", "true");
        options.put("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension");
        options.put("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog");
        options.put("spark.hadoop.fs.s3a.imp", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        options.put("spark.hadoop.fs.s3a.fast.upload.buffer", "disk");
        options.put("spark.hadoop.fs.s3a.block.size", "128M");
        options.put("spark.hadoop.fs.s3a.fast.upload", "true");
        options.put("spark.hadoop.fs.s3a.multipart.size", "512M");
        options.put("spark.hadoop.fs.s3a.multipart.threshold", "512M");
        options.put("spark.hadoop.fs.s3a.fast.upload.active.blocks", "2048");
        options.put("spark.hadoop.fs.s3a.committer.threads", "2048");
        options.put("spark.hadoop.fs.s3a.max.total.tasks", "2048");
        options.put("spark.hadoop.fs.s3a.threads.max", "2048");
        options.put("spark.databricks.delta.schema.autoMerge.enabled", "true");
        options.put("spark.databricks.delta.merge.repartitionBeforeWrite.enabled", "true");
        options.put("spark.sql.shuffle.partitions", "10");
        return options;
    }

    public SparkSession sparkSessionFromConfig() {
        SparkConf sparkConf = new SparkConf();
        return SparkSession.builder()
                .config(sparkConf)
                .getOrCreate();
    }

    public SparkSession sparkSessionWithDefaultOptions() {
        return sparkSessionWithOptions(getDefaultOptions());
    }

    public SparkSession sparkSessionWithOptions(Map<String, String> options) {
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

    public CortexSession getCortexSessionWithOverrides(Map<String, String> overrides) {
        // Use the SPARK_HOME env variable as a proxy for whether this is running in-cluster or locally, and
        // whether to load spark submit config file. (Guarding against missing config file & required properties).
        boolean useDefaultOptions = System.getenv("SPARK_HOME") == null;
        if (useDefaultOptions) {
            var options = new HashMap<>(getDefaultOptions());
            options.putAll(overrides);
            return CortexSession.newSession(sparkSessionWithOptions(options), options);
        }
        return CortexSession.newSession(sparkSessionFromConfig());
    }
}
