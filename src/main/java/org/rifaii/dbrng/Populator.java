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

    //500k takes 23 seconds for 5 tables
    public static void populate(String connectionUrl, int rowsNum) {
        Db db = new Db(connectionUrl);
        LOG.info("Populating database with {} rows per table", rowsNum);
        DbIntrospection dbIntrospection = db.buildPlan();
        boolean connectionEstablished = db.isValidConnection();
        if (!connectionEstablished) {
            LOG.info("Could not establish connection to database, exiting.");
            System.exit(1);
        }

        var fullGenerationStart = LocalTime.now();

        Collection<Table> allTables = dbIntrospection.getTables();
        Collection<Table> tablesWithoutFk = allTables.stream().filter(table -> !table.hasForeignKeys()).toList();

        try (ExecutorService executor = Executors.newFixedThreadPool(tablesWithoutFk.size())) {
            Collection<Runnable> copyCommands = tablesWithoutFk.stream()
                    .map(table -> (Runnable) () -> {
                        LOG.info("Start populating table {}", table.tableName);
                        copyTable(db, table, rowsNum);
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
            copyCommands.forEach(executor::submit);
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        Queue<Table> suggestedInsertOrder = dbIntrospection.getSuggestedInsertOrder();
        while (!suggestedInsertOrder.isEmpty()) {
            Table table = suggestedInsertOrder.poll();

            if (tablesWithoutFk.contains(table)) {
                continue;
            }

            copyTable(db, table, rowsNum, dbIntrospection);
        }

        var fullGenerationEnd = LocalTime.now();

        LOG.info("Started at {}", fullGenerationStart);
        LOG.info("Ended at {}", fullGenerationEnd);
        LOG.info("Successfully populated {} tables with {} rows", allTables.size(), rowsNum * allTables.size());
    }

    private static void copyTable(Db db, Table table, int rowsNum) {
        var start = LocalTime.now();
        CsvRowIterator generate = Generator.generate(table.getColumns(), rowsNum);
        db.copy(table, generate);
        var end = LocalTime.now();
        System.out.printf("=====%s=====%n", table.tableName);
        LOG.info("Started at {}", start);
        LOG.info("Ended at {}", end);
    }

    private static void copyTable(Db db, Table table, int rowsNum, DbIntrospection dbIntrospection) {
        var start = LocalTime.now();
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
        var end = LocalTime.now();
        System.out.printf("=====%s=====%n", table.tableName);
        LOG.info("Started at {}", start);
        LOG.info("Ended at {}", end);
    }
}
