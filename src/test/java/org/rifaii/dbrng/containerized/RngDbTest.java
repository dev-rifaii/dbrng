package org.rifaii.dbrng.containerized;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.rifaii.dbrng.Configuration;
import org.rifaii.dbrng.Populator;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;

public class RngDbTest {

    private static final PostgreSQLContainer<?> CONTAINER;

    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String DB = "dbrng_demo";
    private static final String JDBC_URL;

    static {
        CONTAINER = new PostgreSQLContainer<>()
                .withUsername(USER)
                .withPassword(PASSWORD)
                .withDatabaseName(DB);
        CONTAINER.start();
        JDBC_URL = Util.constructUrl(CONTAINER);
    }

    @AfterAll
    public static void tearDown() {
        CONTAINER.stop();
    }

    @BeforeAll
    public static void init() throws IOException {
        final String setupScript = Util.readFile("setup/dbrng.sql");
        Util.execScript(JDBC_URL, setupScript);
    }

    @Order(1)
    @Test
    void test() {
        int rowsCount = 100_000;
        Populator.populate(Configuration.of(JDBC_URL, rowsCount));
        Util.assertCountMatches(JDBC_URL, rowsCount);
    }
}
