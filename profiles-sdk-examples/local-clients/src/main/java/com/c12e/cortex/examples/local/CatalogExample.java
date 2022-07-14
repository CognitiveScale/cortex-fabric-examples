package com.c12e.cortex.examples.local;

import com.c12e.cortex.phoenix.Catalog;
import com.c12e.cortex.phoenix.Connection;
import com.c12e.cortex.phoenix.DataSource;
import com.c12e.cortex.profiles.CortexSession;

import java.util.ArrayList;
import java.util.List;

public class CatalogExample {
    public List<Connection> listConnectionsInCatalog(CortexSession cortexSession) {
        // list the connections in a project
        Catalog catalog = cortexSession.catalog();
        List<Connection> connections = new ArrayList<>();
        catalog.listConnections("local").forEach(connections::add);
        return connections;
    }

    public void recreateDataSource(CortexSession cortexSession) {
        // get, delete and re-create a data source in the Cortex Catalog
        Catalog catalog = cortexSession.catalog();
        DataSource ds = catalog.getDataSource("project", "data-source-name");
        catalog.deleteDataSource("project", "data-source-name");
        catalog.createDataSource(ds);
    }
}
