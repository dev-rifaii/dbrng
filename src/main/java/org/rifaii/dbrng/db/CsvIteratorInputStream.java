package org.rifaii.dbrng.db;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class CsvIteratorInputStream extends InputStream {
    private final Iterator<String> iterator;
    private byte[] currentBuffer = new byte[0];
    private int position = 0;

    public CsvIteratorInputStream(Iterator<String> iterator) {
        this.iterator = iterator;
    }

    @Override
    public int read() {
        if (position >= currentBuffer.length) {
            if (!iterator.hasNext()) return -1;
            String row = iterator.next() + "\n";
            currentBuffer = row.getBytes(StandardCharsets.UTF_8);
            position = 0;
        }
        return currentBuffer[position++] & 0xFF;
    }
}
