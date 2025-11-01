package org.rifaii.dbrng.containerized;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class Util {

    static void execScript(String connectionUrl, String sql) {
        try (Connection connection = getConnection(connectionUrl)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static Connection getConnection(String connectionUrl) throws SQLException {
        return DriverManager.getConnection(connectionUrl);
    }

    static String constructUrl(PostgreSQLContainer container) {
        return "jdbc:postgresql://%s:%s/%s?user=%s&password=%s".formatted(
                container.getHost(),
                container.getFirstMappedPort(),
                container.getDatabaseName(),
                container.getUsername(),
                container.getPassword()
        );
    }

    static String readFile(String filePath) throws IOException {
        try {
            final URL setupScriptUrl = RngDbTest.class.getClassLoader().getResource(filePath);
            return Files.readString(Paths.get(setupScriptUrl.getPath()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void assertCountMatches(String connectionUrl, int rowsCount) {
        try {
            long tablesCount = getTablesCount(connectionUrl);
            long expectedCount = rowsCount * tablesCount;
            long actualCount = totalRowsCount(connectionUrl);

            Assertions.assertEquals(expectedCount, actualCount, "Count doesn't match");
            System.out.printf("Actual count is %d%n", actualCount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static long getTablesCount(String connectionUrl) throws SQLException {
        final String QUERY =
                """
                SELECT COUNT(*) AS table_count
                FROM information_schema.tables
                WHERE table_type = 'BASE TABLE'
                  AND table_schema NOT IN ('pg_catalog', 'information_schema');
                """;
        try (Statement statement = getConnection(connectionUrl).createStatement()) {
            ResultSet resultSet = statement.executeQuery(QUERY);
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                throw new RuntimeException("Failed to get total tables count");
            }
        }
    }

    static long totalRowsCount(String connectionUrl) throws SQLException {
        List<Pair<String, String>> allTables = getAllTables(connectionUrl);
        long total = 0L;
        for (Pair<String, String> table : allTables) {
            try (Statement statement = getConnection(connectionUrl).createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM %s.%s".formatted(table.getLeft(), table.getRight()));
                while (resultSet.next()) {
                    long count = resultSet.getLong(1);

                    total = total + count;
                }

            }
        }

        return total;
    }

    private static List<Pair<String, String>> getAllTables(String connectionUrl) throws SQLException {
        final List<Pair<String, String>> tables = new ArrayList<>();
        final String ALL_TABLES_QUERY
                = """
                SELECT
                    table_schema,
                    table_name
                FROM information_schema.tables
                WHERE
                    table_type = 'BASE TABLE'
                    AND table_schema NOT IN ('pg_catalog', 'information_schema')
                ORDER BY table_schema, table_name;
                """;
        try (Statement statement = getConnection(connectionUrl).createStatement()) {
            ResultSet resultSet = statement.executeQuery(ALL_TABLES_QUERY);
            while (resultSet.next()) {
                String schema = resultSet.getString("table_schema");
                String tableName = resultSet.getString("table_name");

                tables.add(Pair.of(schema, tableName));
            }
        }
        return tables;
    }
}
