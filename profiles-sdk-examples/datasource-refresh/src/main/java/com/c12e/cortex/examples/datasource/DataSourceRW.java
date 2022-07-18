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
 * Sample CLI application that writes the underlying Connection data to an existing Cortex Data Source.
 * Refreshes the Data Source.
 */
@Command(name = "datasource-refresh", description = "Example Data Source Refresh", mixinStandardHelpOptions = true)
public class DataSourceRW implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Cortex Project to use", required = true)
    String project;

    @Option(names = {"-d", "--data-source"}, description = "Data Source Name", required = true)
    String dataSourceName;

    @Override
    public void run() {
        var sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSession();
        refreshDataSource(cortexSession, project, dataSourceName);
    }

    public Dataset<Row> refreshDataSource(CortexSession cortexSession, String project, String dataSourceName) {
        // Get the Data Source and read its corresponding Connection.
        DataSource dataSource = cortexSession.catalog().getDataSource(project, dataSourceName);
        String connectionName =  dataSource.getConnection().getName();
        Dataset<Row> connectionData = cortexSession.read().connection(project, connectionName).load();

        // Write to the Data Source in 'Overwrite' mode to replace any existing content in the Data Source.
        cortexSession.write()
                .dataSource(connectionData, project, dataSourceName)
                .mode(SaveMode.Overwrite)
                .save();

        // Read from the Data Source (returns a DeltaTable).
        DeltaTable deltaTable = cortexSession.read().dataSource(project, dataSourceName).load();
        return deltaTable.df();
    }
}
