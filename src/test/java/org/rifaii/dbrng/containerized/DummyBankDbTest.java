package org.rifaii.dbrng.containerized;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.rifaii.dbrng.Configuration;
import org.rifaii.dbrng.Populator;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;

class DummyBankDbTest {

    private static final PostgreSQLContainer<?> CONTAINER;

    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String DB = "dummy_bank";
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
        final String setupScript = Util.readFile("setup/dummy-bank.sql");
        Util.execScript(JDBC_URL, setupScript);
    }

    @Disabled("requires CHECK constraint support")
    @Order(1)
    @Test
    void test() {
        int rowsCount = 50;
        Populator.populate(Configuration.of(JDBC_URL, rowsCount));
        Util.assertCountMatches(JDBC_URL, rowsCount);
    }
}
