package org.rifaii.dbrng.generator;

import org.rifaii.dbrng.db.object.Column;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Generator {

    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS'Z'");
    private static final byte[] ALPHANUMERICS = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    private Generator() {
    }

    private static byte[] generateString(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = ALPHANUMERICS[RANDOM.nextInt(ALPHANUMERICS.length)];
        }
        return bytes;
    }

    public static CsvRowIterator generate(List<Column> columnDetails, int rowsNum) {
        List<Supplier<byte[]>> plan = new ArrayList<>();
        var random = new Random(System.currentTimeMillis());
        byte[] formattedDate = DATE_FORMATTER.format(LocalDateTime.now()).getBytes();


        for (Column c : columnDetails) {
            PrimitiveIterator.OfInt iterator = IntStream.range(1, rowsNum + 1).iterator();
            int maxNumericColumnSize = (int) Math.pow(10, c.columnSize);

            //If table is a foreign table
            if (c.foreignKey != null) {
                Supplier<byte[]> fkGenerator = c.getGenerator();
                if (fkGenerator == null) {
                    throw new IllegalStateException("Foreign key requires custom generator");
                }

                plan.add(fkGenerator);

                continue;
            }

            if (c.isPrimaryKey) {
                byte[] cloneBuf = new byte[4];
                PrimitiveIterator.OfInt iteratorClone = IntStream.range(1, rowsNum + 1).iterator();
                c.setGenerator(() -> {
                    int value = iteratorClone.next();
                    cloneBuf[0] = (byte) (value >>> 24);
                    cloneBuf[1] = (byte) (value >>> 16);
                    cloneBuf[2] = (byte) (value >>> 8);
                    cloneBuf[3] = (byte) value;
                    return cloneBuf;
//                    return BigInteger.valueOf(value).toByteArray();
                });
            }

            byte[] intBuffer1 = new byte[4];

            switch (c.columnType) {
                case TEXT -> {
                    int maxStringSize = Math.max(c.columnSize, 5);
                    plan.add(() -> generateString(maxStringSize));
                }
                case TIMESTAMP -> plan.add(() -> formattedDate);
                case NUMERIC -> plan.add(
                        c.isPrimaryKey
                                ? () -> {
                            int value = iterator.nextInt();
                            intBuffer1[0] = (byte) (value >>> 24);
                            intBuffer1[1] = (byte) (value >>> 16);
                            intBuffer1[2] = (byte) (value >>> 8);
                            intBuffer1[3] = (byte) value;
                            return intBuffer1;
//                            return BigInteger.valueOf(value).toByteArray();
                        }
                                : () -> {
                            int value = random.nextInt(maxNumericColumnSize);
                            intBuffer1[0] = (byte) (value >>> 24);
                            intBuffer1[1] = (byte) (value >>> 16);
                            intBuffer1[2] = (byte) (value >>> 8);
                            intBuffer1[3] = (byte) value;
                            return intBuffer1;
//                            return BigInteger.valueOf(value).toByteArray();
                        }
                );
                default -> plan.add(() -> new byte[0]);
            }
        }

        return new CsvRowIterator(rowsNum, plan);
    }
}
