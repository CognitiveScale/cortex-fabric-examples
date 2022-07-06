package com.c12e.cortex.examples.datasource;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.phoenix.DataSource;
import io.delta.tables.DeltaTable;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Sample CLI application that writes the underlying Connection data to an existing Cortex DataSource.
 * (Refreshes the DataSource).
 */
@Command(name = "datasource-refresh", description = "Example DataSource Read and Write", mixinStandardHelpOptions = true)
public class DataSourceRW implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Cortex Project to use", required = true)
    String project;

    @Option(names = {"-d", "--data-source"}, description = "DataSource Name", required = true)
    String dataSourceName;

    @Override
    public void run() {
        var sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSession();
        refreshDataSource(cortexSession, project, dataSourceName);
    }

    public Dataset<Row> refreshDataSource(CortexSession cortexSession, String project, String dataSourceName) {
        // get the DataSource and read its corresponding Connection
        DataSource dataSource = cortexSession.catalog().getDataSource(project, dataSourceName);
        String connectionName =  dataSource.getConnection().getName();
        Dataset<Row> connectionData = cortexSession.read().connection(project, connectionName).load();

        // write to the DataSource in 'Overwrite' mode to replace any existing content in the DataSource
        cortexSession.write()
                .dataSource(connectionData, project, dataSourceName)
                .mode(SaveMode.Overwrite)
                .save();

        // read from the DataSource (returns a DeltaTable)
        DeltaTable deltaTable = cortexSession.read().dataSource(project, dataSourceName).load();
        return deltaTable.df();
    }
}
