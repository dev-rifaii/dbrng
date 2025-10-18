package org.rifaii;

import org.rifaii.dbrng.CsvRowIterator;
import org.rifaii.dbrng.Generator;
import org.rifaii.dbrng.db.Db;
import org.rifaii.dbrng.db.object.DbIntrospection;

import java.time.LocalTime;

public class Main {

    public static void main(String[] args) {
        int rowsNum = 1_000;
        Db db = new Db("postgres", "postgres", "localhost", "5432", "tracker_db", "public");
        DbIntrospection dbIntrospection = db.buildPlan();
        boolean connectionEstablished = db.isValidConnection();
        if (!connectionEstablished) {
            System.out.println("Could not establish connection to database, exiting.");
            System.exit(1);
        }

        var fullGenerationStart = LocalTime.now();

        dbIntrospection.getSuggestedInsertOrder()
            .stream()
            .forEach(table -> {
                var start = LocalTime.now();
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