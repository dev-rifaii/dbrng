package org.rifaii.dbrng.generator;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class CsvIteratorInputStream extends InputStream {
    private final Iterator<byte[]> iterator;
    private byte[] currentBuffer = new byte[0];
    private int position = 0;

    public CsvIteratorInputStream(Iterator<byte[]> iterator) {
        this.iterator = iterator;
    }

    @Override
    public int read() {
        if (position >= currentBuffer.length) {
            if (!iterator.hasNext()) return -1;

            currentBuffer = iterator.next();
            position = 0;
        }
        return currentBuffer[position++] & 0xFF;
    }
}
