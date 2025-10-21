package org.rifaii.dbrng.generator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rifaii.dbrng.db.object.Column;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.rifaii.dbrng.generator.generators.StringGenerator.generateString;

public class Generator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS'Z'");
    private static final Logger LOG  = LogManager.getLogger(Generator.class);

    private Generator() {
    }

    public static CsvRowIterator generate(List<Column> columnDetails, int rowsNum) {
        final List<Supplier<String>> PLAN = new ArrayList<>();
        var random = new Random(System.currentTimeMillis());

        for (Column column : columnDetails) {
            //In case of foreign key, use custom generator
            if (column.getGenerator() != null) {
                PLAN.add(column.getGenerator());
                continue;
            }

            int maxNumericColumnSize = (int) Math.pow(10, column.columnSize);

            if (column.sequential) {
                PrimitiveIterator.OfInt iteratorClone = IntStream.range(1, rowsNum + 1).iterator();
                column.setGenerator(() -> iteratorClone.next().toString());
            }


            PrimitiveIterator.OfInt iterator = IntStream.range(1, rowsNum + 1).iterator();

            switch (column.columnType) {
                case TEXT -> {
                    int maxStringSize = Math.max(column.columnSize, 5);
                    PLAN.add(() -> generateString(maxStringSize));
                }
                case TIMESTAMP -> {
                    String formattedDate = DATE_FORMATTER.format(LocalDateTime.now());
                    PLAN.add(() -> formattedDate);
                }
                case NUMERIC -> PLAN.add(
                        column.sequential
                                ? () -> iterator.next().toString()
                                : () -> String.valueOf(random.nextInt(maxNumericColumnSize))
                );
                default -> PLAN.add(() -> "");
            }
        }

        return new CsvRowIterator(rowsNum, PLAN);
    }
}
