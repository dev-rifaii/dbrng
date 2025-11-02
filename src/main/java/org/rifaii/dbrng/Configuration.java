package org.rifaii.dbrng;

import org.rifaii.dbrng.db.object.Table;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class Configuration {

    private final String connectionUrl;
    private final String schema;
    private final int numberOfRows;
    private final Map<String, ColumnConfiguration> customColumnConfigurations = new HashMap<>();

    //example:
    //"jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres",
    public Configuration(String connectionUrl, int numberOfRows) {
        this(connectionUrl, "public", numberOfRows);
    }

    /**
     * Full column reference is schema.table.column
     */
    public Configuration with(String fullColumnReference, Supplier<String> generator) {
        customColumnConfigurations.put(
                fullColumnReference,
                new ColumnConfiguration(fullColumnReference, generator)
        );
        return this;
    }

    public Optional<ColumnConfiguration> findForColumn(String fullColumnReference) {
        return Optional.ofNullable(customColumnConfigurations.get(fullColumnReference));
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
        return new Configuration(connectionUrl, numberOfRows) {
        };
    }

    public record ColumnConfiguration(
            String fullColumnReference,
            Supplier<String> generator
    ){}
}
