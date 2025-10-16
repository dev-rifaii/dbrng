package org.rifaii;

import org.rifaii.dbrng.CsvRowIterator;
import org.rifaii.dbrng.Generator;
import org.rifaii.dbrng.Static;
import org.rifaii.dbrng.db.Db;
import org.rifaii.dbrng.db.object.Column;
import org.rifaii.dbrng.db.object.DbIntrospection;
import org.rifaii.dbrng.db.object.Table;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        Db db = new Db("postgres", "postgres", "localhost", "5432", "tracker_db", "public");
        DbIntrospection dbIntrospection = db.introspectSchema();
        boolean connectionEstablished = db.isValidConnection();
        if (!connectionEstablished) {
            System.out.println("Could not establish connection to database, exiting.");
            System.exit(1);
        }

        Set<String> it = new HashSet<>();

        var fullGenerationStart = LocalTime.now();

        dbIntrospection.getTables()
            .forEach(table -> {
                var start = LocalTime.now();
                CsvRowIterator generate = Generator.generate(table.getColumns(), 10);
//                while (generate.hasNext()) {
//                    String next = generate.next();
//                    it.add(next);
//                }
                db.copy(table, generate);
                var end = LocalTime.now();
                System.out.printf("=====%s=====%n", table.tableName);
                System.out.println("Started at " + start);
                System.out.println("Ended at " + end);
            });

        var fullGenerationEnd = LocalTime.now();

        System.out.println("Generated " + it.size() + " columns in database.");
        System.out.println("Started at " + fullGenerationStart);
        System.out.println("Ended at " + fullGenerationEnd);

    }

}