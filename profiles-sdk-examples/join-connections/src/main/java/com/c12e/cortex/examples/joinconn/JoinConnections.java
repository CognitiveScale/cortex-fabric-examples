package com.c12e.cortex.examples.joinconn;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.profiles.CortexSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Example CLI application that performs an inner join on two Cortex Connections and writes the result to a third
 * Connection. All connections are assumed to exist in the Cortex Catalog.
 */
@Command(name = "join-connections", description = "Example Joining Connections", mixinStandardHelpOptions = true)
public class JoinConnections implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @Option(names = {"-l", "--left-conn"}, description = "Left Join Side Connection Name", required = true)
    String leftConnection;

    @Option(names = {"-r", "--right-conn"}, description = "Right Join Side Connection Name", required = true)
    String rightConnection;

    @Option(names = {"-w", "--write-conn"}, description = "Write Connection Name", required = true)
    String writeConnection;

    @Option(names = { "-c", "--column" }, description = "Name of Column to join Connections on", required = true)
    String joinColumn;

    @Override
    public void run() {
        // create a Cortex Session and perform the join
        SessionExample example = new SessionExample();
        CortexSession cortexSession = example.getCortexSession();
        joinConnections(cortexSession, project, leftConnection, rightConnection, writeConnection, joinColumn);
    }

    public Dataset<Row> joinConnections(CortexSession cortexSession,
                                String project,
                                String leftConnection,
                                String rightConnection,
                                String writeConnection,
                                String joinColumn) {
        // load the two Connections
        Dataset<Row> leftConn = cortexSession.read().connection(project, leftConnection).load();
        Dataset<Row> rightConn = cortexSession.read().connection(project, rightConnection).load();

        // perform an inner join
        Dataset<Row> ds = leftConn.join(rightConn, joinColumn);

        // write to a third connection as a data sink. The 'OverWrite' mode will cause any existing content in the
        // connection to be replaced
        cortexSession.write()
                .connection(ds, project, writeConnection)
                .mode(SaveMode.Overwrite)
                .save();
        return ds;
    }
}


