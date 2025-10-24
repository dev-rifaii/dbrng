package org.rifaii.dbrng;

import org.rifaii.dbrng.db.object.Table;

import java.sql.Connection;

public abstract class Configuration {

    private final String connectionUrl;
    private final String schema;
    private final int numberOfRows;

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

    public static Configuration of(String connectionUrl, int numberOfRows) {
        return new Configuration(connectionUrl, numberOfRows) {};
    }
}
