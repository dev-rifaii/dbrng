package org.rifaii.dbrng.db;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.rifaii.dbrng.CsvIteratorInputStream;
import org.rifaii.dbrng.CsvRowIterator;
import org.rifaii.dbrng.Static;
import org.rifaii.dbrng.db.object.Column;
import org.rifaii.dbrng.db.object.DbIntrospection;
import org.rifaii.dbrng.db.object.Table;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Db {

    private final String username;
    private final String password;
    private final String host;
    private final String port;
    private final String database;
    private final String schema;

    private final String connectionUrl;

    public Db(String username, String password, String host, String port, String database, String schema) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.database = database;
        this.schema = schema;

        connectionUrl = "jdbc:postgresql://%s:%s/%s?user=%s&password=%s&ssl=false"
            .formatted(host, port, database, username, password);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionUrl);
    }

    public DbIntrospection introspectSchema() {
        try (Connection connection = getConnection()) {
            ResultSet resultSet = connection.prepareStatement(Static.QUERY_SCHEMA_INTROSPECT.formatted(schema))
                .executeQuery();
            Map<String, String> primaryKeys = getPrimaryKeys();

            Map<String, Table> tables = new HashMap<>();

            while (resultSet.next()) {
                String tableName =  resultSet.getString("table_name");
                String columnName =  resultSet.getString("column_name");
                boolean isNullable =  resultSet.getBoolean("nullable");
                String columnDefault =  resultSet.getString("column_default");
                String type =  resultSet.getString("data_type");
                int columnSize =  resultSet.getInt("character_maximum_length");
                int numericPrecision =  resultSet.getInt("numeric_precision");

                var column = new Column();
                column.columnName = columnName;
                column.isNullable = isNullable;
                column.columnType = type;
                column.columnSize = columnSize > 0 ? columnSize : 5;
                column.isPrimaryKey = primaryKeys.containsKey(tableName) && primaryKeys.get(tableName).equals(columnName);

                tables.computeIfAbsent(tableName, k -> new Table(tableName))
                    .addColumn(column);
            }

            return new DbIntrospection(tables.values());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void copy(Table table, CsvRowIterator iterator) {
        try (Connection conn = getConnection()) {
            long rowsInserted = new CopyManager((BaseConnection) conn)
                .copyIn(
                    "COPY %s FROM STDIN (FORMAT csv)".formatted(table.tableName),
                    new CsvIteratorInputStream(iterator)
                );
            System.out.printf("%d row(s) inserted%n", rowsInserted);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isValidConnection() {
        try (Connection connection = getConnection()) {
            connection.prepareStatement("SELECT 1").executeQuery();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private Map<String, String> getPrimaryKeys() {
      try (Connection connection = getConnection()) {
        ResultSet resultSet = connection.prepareStatement(Static.QUERY_PRIMARY_KEYS.formatted(schema))
            .executeQuery();

        Map<String, String> tables = new HashMap<>();

        while (resultSet.next()) {
          String tableName =  resultSet.getString("table_name");
          String columnName =  resultSet.getString("column_name");

          tables.put(tableName, columnName);
        }

        return tables;
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

}
