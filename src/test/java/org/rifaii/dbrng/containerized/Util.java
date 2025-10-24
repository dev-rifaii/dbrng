package org.rifaii.dbrng.containerized;

import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
}
