package org.rifaii.dbrng.generator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvRowIterator implements Iterator<byte[]> {

    int limit;
    List<Supplier<byte[]>> suppliers;
    private static final byte NL = (byte) '\n';
    private static final byte COMMA = (byte) ',';

    public CsvRowIterator(int limit, List<Supplier<byte[]>> suppliers) {
        this.limit = limit;
        this.suppliers = suppliers;
    }

    @Override
    public boolean hasNext() {
        return limit > 0;
    }

    @Override
    public byte[] next() {
        limit = limit - 1;

        List<byte[]> generatedValues = suppliers.stream().map(Supplier::get).toList();

        int rowBytesLength = generatedValues.stream().map(it -> it.length).reduce(Integer::sum).orElseThrow();
        byte[] rowBytes = new byte[rowBytesLength + 1 + (generatedValues.size() - 1)]; // 1 for NL and values size for commas

        int pos = 0;
        for (int i = 0; i < generatedValues.size(); i++) {
            byte[] b = generatedValues.get(i);
            System.arraycopy(b, 0, rowBytes, pos, b.length);
            pos += b.length;

            if (i < generatedValues.size() - 1) {
                rowBytes[pos++] = COMMA;
            }
        }
        rowBytes[pos] = NL;

        System.out.println("------------");
        for (int i = 0; i < rowBytesLength; i++) {
            System.out.print(rowBytes[i]);
        }
        System.out.println();
        return rowBytes;
    }
}
