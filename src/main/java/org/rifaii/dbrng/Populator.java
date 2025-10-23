package org.rifaii.dbrng;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rifaii.dbrng.db.Db;
import org.rifaii.dbrng.db.object.Column;
import org.rifaii.dbrng.db.object.DbIntrospection;
import org.rifaii.dbrng.db.object.ForeignKey;
import org.rifaii.dbrng.db.object.Table;
import org.rifaii.dbrng.generator.CsvRowIterator;
import org.rifaii.dbrng.generator.Generator;
import org.rifaii.dbrng.generator.LaggingIterator;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Populator {

    private static final Logger LOG = LogManager.getLogger(Populator.class);

    public static void populate(String connectionUrl, int rowsNum) {
        populate(connectionUrl, "public", rowsNum);
    }
    //500k takes 23 seconds for 5 tables
    public static void populate(String connectionUrl, String schema, int rowsNum) {
        final Db db = new Db(connectionUrl, schema);
        LOG.info("Populating database with {} rows per table", rowsNum);
        final DbIntrospection dbIntrospection = db.buildPlan();
        final boolean connectionEstablished = db.isValidConnection();
        if (!connectionEstablished) {
            LOG.info("Could not establish connection to database, exiting.");
            System.exit(1);
        }

        final var fullGenerationStart = LocalTime.now();

        final Collection<Table> allTables = dbIntrospection.getTables();
        final Collection<Table> tablesWithoutFk = allTables.stream().filter(table -> !table.hasForeignKeys()).toList();

        if (allTables.isEmpty()) {
            LOG.error("No tables found in database, exiting gracefully");
            System.exit(0);
        }

        try (final ExecutorService executor = Executors.newFixedThreadPool(tablesWithoutFk.size())) {
            final Collection<Runnable> copyCommands = tablesWithoutFk.stream()
                    .map(table -> (Runnable) () -> {
                        LOG.info("Start populating table {}", table.tableName);
                        copyTable(db, table, rowsNum);
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
            copyCommands.forEach(executor::submit);
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            LOG.error("Interrupted while populating tables", e);
            throw new RuntimeException(e);
        }


        final Queue<Table> suggestedInsertOrder = dbIntrospection.getSuggestedInsertOrder();
        while (!suggestedInsertOrder.isEmpty()) {
            Table table = suggestedInsertOrder.poll();

            if (tablesWithoutFk.contains(table)) {
                continue;
            }

            copyTable(db, table, rowsNum, dbIntrospection);
        }

        var fullGenerationEnd = LocalTime.now();


        Duration duration = Duration.between(fullGenerationStart, fullGenerationEnd);
        LOG.info("Successfully populated {} tables with {} rows", allTables.size(), rowsNum * allTables.size());
        LOG.info("Populating database started at {} and ended at {}", fullGenerationStart, fullGenerationEnd);
        LOG.info("Time spent in seconds: {}", duration.getSeconds());
        db.close();
    }

    private static void copyTable(Db db, Table table, int rowsNum) {
        try {
            CsvRowIterator generate = Generator.generate(table.getColumns(), rowsNum);
            db.copy(table, generate);
            System.out.printf("=====%s=====%n", table.tableName);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private static void copyTable(Db db, Table table, int rowsNum, DbIntrospection dbIntrospection) {
        table.getColumns().stream().filter(column -> column.foreignKey != null).forEach(columnWithForeignKey -> {
            ForeignKey fk = columnWithForeignKey.foreignKey;


            Table referencedTable = dbIntrospection.getTables().stream().filter(tb -> tb.tableName.equals(fk.foreignTableName))
                    .findFirst()
                    .orElseThrow(RuntimeException::new);

            Column referencedColumn = referencedTable.getColumns().stream()
                    .filter(c -> c.columnName.equals(fk.foreignColumnName))
                    .findFirst()
                    .orElseThrow(RuntimeException::new);

            Supplier<String> referencedColumnGenerator = referencedColumn.generator;

            LaggingIterator laggingIterator = new LaggingIterator(referencedColumnGenerator);
            columnWithForeignKey.generator = laggingIterator::next;
        });
        CsvRowIterator generate = Generator.generate(table.getColumns(), rowsNum);
        db.copy(table, generate);
    }
}
