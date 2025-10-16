package org.rifaii.dbrng;

import org.rifaii.dbrng.db.object.Column;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class Generator {

    private static final Random random = new Random();

    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS'Z'");


    static char[] alphanumerics = {
        'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
        '0','1','2','3','4','5','6','7','8','9'
    };

    static char[] generateString(int length) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = alphanumerics[random.nextInt(alphanumerics.length)];
        }
        return chars;
    }

    public static CsvRowIterator generate(List<Column> columnDetails, int rowsNum) {
        List<Supplier<String>> plan = new ArrayList<>();
        var random = new Random();
        String formattedDate = formatter.format(LocalDateTime.now());

        columnDetails.forEach(c -> {
            switch (c.columnType) {
                case "CHARACTER VARYING", "TEXT" -> plan.add(() -> new String(generateString(c.columnSize)));
                case "TIMESTAMP WITH TIME ZONE" -> plan.add(() -> formattedDate);
                case "NUMERIC", "BIGINT" -> plan.add(() -> String.valueOf(random.nextInt((int) Math.pow(10, c.columnSize))));
                default -> plan.add(() -> "");
            }
        });

        return new CsvRowIterator(rowsNum, plan);
    }

}
