package com.c12e.cortex.examples.local;

import com.c12e.cortex.phoenix.Catalog;
import com.c12e.cortex.phoenix.Connection;
import com.c12e.cortex.phoenix.DataSource;
import com.c12e.cortex.phoenix.LocalCatalog;
import com.c12e.cortex.profiles.CortexSession;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SessionExample {

    protected SparkSession getSparkSession() {
        // create the spark session by loading the Spark Configuration file
        SparkConf sparkConf = new SparkConf();
        return SparkSession.builder()
                .config(sparkConf)
                .getOrCreate();
    }

    protected CortexSession getCortexSession(SparkSession session) {
        SparkSession sparkSession = getSparkSession();
        return CortexSession.newSession(sparkSession);
    }

    public CortexSession getCortexSessionFromExplicitOptions() {
        Map<String, String> options = Map.of(
                // specify a local catalog implementation with the directory pointing
                CortexSession.CATALOG_KEY, LocalCatalog.class.getCanonicalName(),
                CortexSession.LOCAL_CATALOG_DIR_KEY,  "src/main/resources/spec",

                // specify the local secret client implementation
                CortexSession.SECRETS_CLIENT_KEY, CustomSecretsClient.class.getCanonicalName()
        );
        SparkConf sparkConf = new SparkConf();
        SparkSession sparkSession = SparkSession.builder()
                .master("local[*]")
                .config(sparkConf)
                .getOrCreate();
        return CortexSession.newSession(sparkSession, options);
    }

    public List<Connection> useCortexCatalog(CortexSession cortexSession) {
        Catalog catalog = cortexSession.catalog();

        // get, delete and re-create a data source in the Cortex Catalog
        //DataSource ds = catalog.getDataSource("project", "data-source-name");
        //catalog.deleteDataSource("project", "data-source-name");
        //catalog.saveDataSource(ds);
        //catalog.saveDataSource(ds);

        // list the connections in a project
        List<Connection> connections = new ArrayList<>();
        catalog.listConnections("project").forEach(connections::add);
        return connections;
    }
}
