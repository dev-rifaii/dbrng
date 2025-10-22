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
            if (column.generator != null) {
                LOG.debug("Using custom generator for column {}", column.columnName);
                PLAN.add(column.generator);
                continue;
            } else {
                //If it's a foreign key then custom generator (usually clone of primary key generator) is required
                if (column.foreignKey != null)
                    throw new RuntimeException("Foreign key requires custom generator");
            }

            //If primary key
            if (column.sequential) {
                PrimitiveIterator.OfInt iterator = IntStream.range(1, rowsNum + 1).iterator();

                PLAN.add(() -> iterator.next().toString());
                LOG.debug("Column {} is sequential, cloning interator",  column.columnName);

                //Assign cloned iterator that can be used by a foreign key
                //TODO: We might have an issue if 1 primary key has 2 foreign keys
                PrimitiveIterator.OfInt iteratorClone = IntStream.range(1, rowsNum + 1).iterator();
                column.generator = () -> iteratorClone.next().toString();
                continue;
            }

            switch (column.columnType) {
                case TEXT -> {
                    int maxStringSize = Math.max(column.columnSize, 5);
                    PLAN.add(() -> generateString(maxStringSize));
                }
                case TIMESTAMP -> {
                    String formattedDate = DTIME_FORMATTER.format(LocalDateTime.now());
                    PLAN.add(() -> formattedDate);
                }
                case NUMERIC -> {
                    var random = new Random(System.currentTimeMillis());
                    int scale = (int) Math.pow(10, column.columnSize);
                    PLAN.add(() -> String.valueOf(random.nextInt(scale)));
                }
                case DATE -> {
                    LocalDate now = LocalDate.now();
                    PLAN.add(now::toString);
                }
                case JSON -> {
                    String emptyJson = "{}";
                    PLAN.add(() -> emptyJson);
                }
                default -> PLAN.add(() -> "");
            }
        }

        return new CsvRowIterator(rowsNum, PLAN);
    }
}
