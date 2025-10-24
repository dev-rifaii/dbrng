package org.rifaii.dbrng.generator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rifaii.dbrng.db.object.Column;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.rifaii.dbrng.generator.generators.StringGenerator.generateString;

public class Generator {

    private static final DateTimeFormatter DTIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS'Z'");
    private static final Logger LOG = LogManager.getLogger(Generator.class);

    private Generator() {}

    public static CsvRowIterator generate(List<Column> columnDetails, int rowsNum) {
        final List<Supplier<String>> PLAN = new ArrayList<>();

        for (Column column : columnDetails) {
            //Use custom generator if present
            if (column.customGenerator != null) {
                LOG.debug("Using custom generator for column {}", column.columnName);
                PLAN.add(column.customGenerator);
                continue;
            } else {
                //If it's a foreign key then custom generator (usually clone of primary key generator) is required
                if (column.foreignKey != null)
                    throw new RuntimeException("Foreign key requires custom generator");
            }

            //If primary key
            if (column.sequential) {
                final PrimitiveIterator.OfInt iterator = IntStream.range(1, rowsNum + 1).iterator();

                PLAN.add(() -> iterator.next().toString());
                LOG.debug("Column {} is sequential, cloning interator",  column.columnName);

                //Assign cloned iterator that can be used to generate foreign key
                column.generatorIteratorSupplier = () -> IntStream.range(1, rowsNum + 1).iterator();
                continue;
            }

            switch (column.columnType) {
                case TEXT -> {
                    final int maxStringSize = Math.min(column.columnSize, 5);
                    PLAN.add(() -> generateString(maxStringSize));
                }
                case TIMESTAMP -> {
                    final String formattedDate = DTIME_FORMATTER.format(LocalDateTime.now());
                    PLAN.add(() -> formattedDate);
                }
                case NUMERIC -> {
                    final var random = new Random(System.currentTimeMillis());
                    final int scale = (int) Math.pow(10, column.columnSize);
                    PLAN.add(() -> String.valueOf(random.nextInt(scale)));
                }
                case DATE -> {
                    final LocalDate now = LocalDate.now();
                    PLAN.add(now::toString);
                }
                case JSON -> {
                    final String emptyJson = "{}";
                    PLAN.add(() -> emptyJson);
                }
                case UUID -> PLAN.add(() -> UUID.randomUUID().toString());
                case BOOLEAN -> {
                    final Random BOOL_RANDOM = new Random();
                    PLAN.add(() -> String.valueOf(BOOL_RANDOM.nextBoolean()));
                }
                case DATERANGE -> {
                    LocalDate start = LocalDate.now().minusYears(1);
                    LocalDate now = LocalDate.now();
                    String range = "\"[%s,%s)\"".formatted(start, now);
                    PLAN.add(() -> range);
                }
                case NUMERIC_RANGE -> {
                    final int scale = (int) Math.pow(10, column.columnSize);
                    final int min = 1;
                    String range = "\"[%d,%d)\"".formatted(min, scale);
                    PLAN.add(() -> range);
                }
                case ARRAY -> PLAN.add(() -> "{}");
                case BYTEA -> PLAN.add(() -> "a");
                default -> PLAN.add(() -> "");
            }
        }

        return new CsvRowIterator(rowsNum, PLAN);
    }
}
