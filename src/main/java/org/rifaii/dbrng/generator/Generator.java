package org.rifaii.dbrng.generator;

import org.rifaii.dbrng.db.object.Column;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Generator {

    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS'Z'");
    private static final char[] ALPHANUMERICS = {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    private Generator() {}

    static char[] generateString(int length) {
        char[] chars = new char[length];
        for (int i = 0;i < length;i++) {
            chars[i] = ALPHANUMERICS[RANDOM.nextInt(ALPHANUMERICS.length)];
        }
        return chars;
    }

    public static CsvRowIterator generate(List<Column> columnDetails, int rowsNum) {
        List<Supplier<String>> plan = new ArrayList<>();
        var random = new Random(System.currentTimeMillis());
        String formattedDate = DATE_FORMATTER.format(LocalDateTime.now());

        columnDetails.forEach(c -> {
            Queue<Integer> ids = new ArrayDeque<>();
            if (c.isPrimaryKey) {
                IntStream.range(0, rowsNum).forEach(ids::add);
            }
            int maxNumericColumnSize = (int) Math.pow(10, c.columnSize);

            switch (c.columnType) {
                case "CHARACTER VARYING", "TEXT" -> plan.add(() -> new String(generateString(c.columnSize)));
                case "TIMESTAMP WITH TIME ZONE" -> plan.add(() -> formattedDate);
                case "NUMERIC", "BIGINT" -> plan.add(
                    c.isPrimaryKey
                        ? () -> String.valueOf(ids.poll())
                        : () -> String.valueOf(random.nextInt(maxNumericColumnSize))
                );
                default -> plan.add(() -> "");
            }
        });

        return new CsvRowIterator(rowsNum, plan);
    }
}
