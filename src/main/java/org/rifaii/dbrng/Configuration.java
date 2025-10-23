package org.rifaii.dbrng;

import org.rifaii.dbrng.db.object.Table;

import java.sql.Connection;

public abstract class Configuration {

    private String connectionUrl;
    private String schema;
    private int numberOfRows;

    public Configuration(String connectionUrl, int numberOfRows) {
        this(connectionUrl, "public", numberOfRows);
    }

    public Configuration(String connectionUrl, String schema, int numberOfRows) {
        this.connectionUrl = connectionUrl;
        this.schema = schema;
        this.numberOfRows = numberOfRows;
    }


    public boolean runPreCopy(Connection connection, Table targetTable) {
        return true;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public String getSchema() {
        return schema;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }
}
