package org.rifaii.dbrng.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.rifaii.dbrng.Configuration;
import org.rifaii.dbrng.datastructure.Graph;
import org.rifaii.dbrng.db.object.Column;
import org.rifaii.dbrng.db.object.DbIntrospection;
import org.rifaii.dbrng.db.object.ForeignKey;
import org.rifaii.dbrng.db.object.Table;
import org.rifaii.dbrng.generator.CsvRowIterator;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Db implements AutoCloseable {

    private static final Logger LOG = LogManager.getLogger(Db.class);
    private final String schema;
    private final String connectionUrl;
    private final Configuration configuration;

    public Db(Configuration configuration) {
        this.schema = configuration.getSchema();
        this.connectionUrl = configuration.getConnectionUrl();
        this.configuration = configuration;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionUrl);
    }

    public DbIntrospection buildPlan() {
        LOG.info("Figuring out dependency graph");
        DbIntrospection dbIntrospection = introspectSchema();
        Collection<Table> allTables = dbIntrospection.getTables();

        Graph<Table> graph = new Graph<>();

        allTables.forEach(potentiallyDependentTable -> {
            graph.addNode(potentiallyDependentTable);
            potentiallyDependentTable.getForeignKeys().forEach(fk -> {
                Table foreignTable = allTables.stream().filter(tb -> tb.tableName.equals(fk.foreignTableName)).findFirst().orElseThrow();
                graph.addEdge(foreignTable, potentiallyDependentTable);
            });
        });

        LOG.info("Sorting tables in topological order");
        Queue<Table> topologicalOrder = graph.inTopologicalOrder();
        dbIntrospection.setSuggestedInsertOrder(topologicalOrder);
        LOG.info("Finished building plan");
        return dbIntrospection;
    }

    private DbIntrospection introspectSchema() {
        LOG.info("Starting introspection for schema [{}]", schema);
        Map<String, List<ForeignKey>> foreignKeys = getForeignKeys();
        try (Connection connection = getConnection()) {
            ResultSet resultSet = connection.prepareStatement(Queries.QUERY_SCHEMA_INTROSPECT.formatted(schema)).executeQuery();
            Map<String, List<String>> primaryKeys = getPrimaryKeys();

            Map<String, Table> tables = new HashMap<>();

            while (resultSet.next()) {
                String tableName = resultSet.getString("table_name");
                String columnName = resultSet.getString("column_name");
                boolean isNullable = resultSet.getBoolean("nullable");
                String columnDefault = resultSet.getString("column_default");
                String type = resultSet.getString("data_type");
                int columnSize = resultSet.getInt("character_maximum_length");
                int numericPrecision = resultSet.getInt("numeric_precision");

                List<ForeignKey> tableForeignKeys = foreignKeys.getOrDefault(tableName, new ArrayList<>());

                var column = new Column();
                column.columnName = columnName;
                column.isNullable = isNullable;
                column.columnType = switch (type) {
                    case "CHARACTER VARYING", "TEXT" -> ColumnType.TEXT;
                    case "TIMESTAMP WITH TIME ZONE", "TIMESTAMP WITHOUT TIME ZONE" -> ColumnType.TIMESTAMP;
                    case "NUMERIC", "BIGINT", "INTEGER", "SMALLINT" -> ColumnType.NUMERIC;
                    case "BOOLEAN" -> ColumnType.BOOLEAN;
                    case "DATE" -> ColumnType.DATE;
                    case "BYTEA" -> ColumnType.BYTEA;
                    case "UUID" -> ColumnType.UUID;
                    case "DATERANGE" -> ColumnType.DATERANGE;
                    case "ARRAY" -> ColumnType.ARRAY;
                    case "TSTZRANGE" -> ColumnType.TIMESTAMP_RANGE;
                    case "NUMRANGE", "INT4RANGE" -> ColumnType.NUMERIC_RANGE;
                    case "JSONB" -> ColumnType.JSON;
                    case "CHARACTER" -> ColumnType.CHARACTER;
                    case "INET" -> ColumnType.INET;
                    default -> throw new IllegalStateException("Unexpected value: " + type);
                };
                if (type.equals("SMALLINT")) {
                    column.columnSize = 1;
                } else {
                    column.columnSize = columnSize > 0 ? columnSize : 5;
                }
                column.isPrimaryKey = primaryKeys.containsKey(tableName) && primaryKeys.get(tableName).contains(columnName);
                if (column.isPrimaryKey) {
                    column.sequential = true;
                }
                column.foreignKey = tableForeignKeys.stream()
                        .filter(fk -> fk.columnName.equals(columnName)).findFirst().orElse(null);

                tables.computeIfAbsent(tableName, k -> new Table(tableName))
                        .addColumn(column)
                        .setForeignKeys(tableForeignKeys);
            }

            tables.keySet().forEach(this::truncateTable);

            LOG.info("Finished introspection for schema [{}]", schema);
            return new DbIntrospection(tables.values());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void truncateTable(String tableName) {
        try (Connection connection = getConnection()) {
            LOG.info("Truncating table [{}]", tableName);
            PreparedStatement preparedStatement = connection.prepareStatement("TRUNCATE TABLE %s.%s CASCADE;".formatted(schema, tableName));
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void copy(Table table, CsvRowIterator iterator) {
        LOG.info("Creating a new connection to COPY into table [{}]", table.tableName);
        try (Connection conn = getConnection()) {
            exec(conn, "ALTER SYSTEM SET max_wal_size='4096MB'");
            exec(conn, "ALTER SYSTEM SET maintenance_work_mem='256MB'");
            exec(conn, "ALTER SYSTEM SET archive_mode='off'");
            exec(conn, "SELECT pg_reload_conf()");

            boolean continueCopying = configuration.runPreCopy(conn, table);

            if (!continueCopying) {
                return;
            }

            LOG.info("[START] Copying data into table [{}]", table.tableName);
            long rowsInserted = new CopyManager((BaseConnection) conn)
                    .copyIn(
                            "COPY %s.%s FROM STDIN (FORMAT csv, QUOTE '\"')".formatted(schema, table.tableName),
                            new CsvIteratorInputStream(iterator)
                    );
            LOG.info("[FINISH] Copying data into table [{}]", table.tableName);
            LOG.info("{} row(s) inserted to [{}]", rowsInserted, table.tableName);
        } catch (SQLException | IOException e) {
            LOG.error("Failed to copy data into table [{}]", table.tableName, e);
            throw new RuntimeException(e);
        }
    }

    public boolean isValidConnection() {
        try (Connection connection = getConnection()) {
            connection.prepareStatement("SELECT 1")
                    .executeQuery();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private Map<String, List<ForeignKey>> getForeignKeys() {
        Map<String, List<ForeignKey>> foreignKeys = new HashMap<>();

        try (Connection connection = getConnection()) {
            ResultSet resultSet = connection
                    .prepareStatement(Queries.QUERY_FOREIGN_KEYS.formatted(schema))
                    .executeQuery();

            while (resultSet.next()) {
                String tableSchema = resultSet.getString("table_schema");
                String constraintName = resultSet.getString("constraint_name");
                String tableName = resultSet.getString("table_name");
                String columnName = resultSet.getString("column_name");
                String foreignTableSchema = resultSet.getString("foreign_table_schema");
                String foreignTableName = resultSet.getString("foreign_table_name");
                String foreignColumnName = resultSet.getString("foreign_column_name");

                ForeignKey fk = new ForeignKey(
                        tableSchema,
                        constraintName,
                        tableName,
                        columnName,
                        foreignTableSchema,
                        foreignTableName,
                        foreignColumnName
                );

                foreignKeys.computeIfAbsent(tableName, k -> new ArrayList<>())
                        .add(fk);

            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch foreign keys for schema: " + schema, e);
        }

        return foreignKeys;
    }

    private void exec(Connection connection, String query) {
        try {
            Statement statement = connection.createStatement();
            statement.execute(query);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, List<String>> getPrimaryKeys() {
        try (Connection connection = getConnection()) {
            ResultSet resultSet = connection.prepareStatement(Queries.QUERY_PRIMARY_KEYS.formatted(schema))
                    .executeQuery();

            Map<String, List<String>> tables = new HashMap<>();

            while (resultSet.next()) {
                String tableName = resultSet.getString("table_name");
                String columnName = resultSet.getString("column_name");

                tables.computeIfAbsent(tableName, k -> new ArrayList<>()).add(columnName);
            }

            return tables;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            Connection conn = getConnection();
            exec(conn, "VACUUM ANALYZE;");
            LOG.debug("Vacuum analyzed");
        } catch (SQLException e) {
            LOG.error("Failed to close", e);
            throw new RuntimeException(e);
        }
    }
}
