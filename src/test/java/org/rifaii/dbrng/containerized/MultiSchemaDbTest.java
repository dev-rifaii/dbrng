package org.rifaii.dbrng.containerized;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.rifaii.dbrng.Configuration;
import org.rifaii.dbrng.Populator;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;

public class MultiSchemaDbTest {

    private static final PostgreSQLContainer<?> CONTAINER;

    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String DB = "multi_schema_db";
    private static final String JDBC_URL;

    static {
        CONTAINER = new PostgreSQLContainer<>("postgres:18")
                .withUsername(USER)
                .withPassword(PASSWORD)
                .withDatabaseName(DB);
        CONTAINER.start();
        JDBC_URL = Util.constructUrl(CONTAINER);
    }

    @BeforeAll
    static void init() throws IOException {
        final String setupScript = Util.readFile("setup/multi-schema.sql");
        Util.execScript(JDBC_URL, setupScript);
    }

    @Order(1)
    @Test
    void test() {
        int rowsCount = 10;
        Populator.populate(new Configuration(JDBC_URL, "people", rowsCount) {});
        Util.assertCountMatches(JDBC_URL, rowsCount);
    }
}
