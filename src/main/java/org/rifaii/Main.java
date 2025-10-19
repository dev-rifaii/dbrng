package org.rifaii;

import org.rifaii.dbrng.db.object.Column;
import org.rifaii.dbrng.db.object.ForeignKey;
import org.rifaii.dbrng.db.object.Table;
import org.rifaii.dbrng.generator.CsvRowIterator;
import org.rifaii.dbrng.generator.Generator;
import org.rifaii.dbrng.db.Db;
import org.rifaii.dbrng.db.object.DbIntrospection;
import org.rifaii.dbrng.generator.LaggingIterator;

import java.time.LocalTime;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) {
        int rowsNum = 50;
        Db db = new Db("postgres", "postgres", "localhost", "5432", "tracker_db", "public");
        DbIntrospection dbIntrospection = db.buildPlan();
        boolean connectionEstablished = db.isValidConnection();
        if (!connectionEstablished) {
            System.out.println("Could not establish connection to database, exiting.");
            System.exit(1);
        }

        var fullGenerationStart = LocalTime.now();

        dbIntrospection.getSuggestedInsertOrder()
            .forEach(table -> {
                var start = LocalTime.now();
                table.getColumns().stream().filter(column -> column.foreignKey != null).forEach(columnWithForeignKey -> {
                    ForeignKey fk =  columnWithForeignKey.foreignKey;

                    Table referencedTable = dbIntrospection.getTables().stream().filter(tb -> tb.tableName.equals(fk.foreignTableName))
                            .findFirst()
                            .orElseThrow(RuntimeException::new);

                    Column referencedColumn = referencedTable.getColumns().stream()
                            .filter(c -> c.columnName.equals(fk.foreignColumnName))
                            .findFirst()
                            .orElseThrow(RuntimeException::new);

                    Supplier<String> referencedColumnGenerator = referencedColumn.getGenerator();

                    LaggingIterator laggingIterator = new LaggingIterator(referencedColumnGenerator);
                    columnWithForeignKey.setGenerator(laggingIterator::next);
                });
                CsvRowIterator generate = Generator.generate(table.getColumns(), rowsNum);
                db.copy(table, generate);
                var end = LocalTime.now();
                System.out.printf("=====%s=====%n", table.tableName);
                System.out.println("Started at " + start);
                System.out.println("Ended at " + end);
            });

        var fullGenerationEnd = LocalTime.now();

        System.out.println("Started at " + fullGenerationStart);
        System.out.println("Ended at " + fullGenerationEnd);

    }

}