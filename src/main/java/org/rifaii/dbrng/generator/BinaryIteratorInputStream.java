package org.rifaii.dbrng.generator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class BinaryIteratorInputStream extends InputStream {
    private enum State { HEADER, ROWS, TRAILER, END }
    private State state = State.HEADER;
    private final Iterator<byte[]> rowIterator;

    // Buffers for header, current row, and trailer
    private static final byte[] BINARY_HEADER = {
            // Signature: "PGCOPY\n\377\r\n\0"
            (byte) 'P', (byte) 'G', (byte) 'C', (byte) 'O', (byte) 'P', (byte) 'Y', (byte) '\n',
            (byte) 0xFF, (byte) '\r', (byte) '\n', (byte) 0x00,
            // Flags: 0x00000000 (No OID, no header extensions)
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            // Header Extension Length: 0x00000000
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };

    private static final byte[] BINARY_TRAILER = {
            (byte) 0xFF, (byte) 0xFF // -1 (2-byte short, Big-Endian)
    };
    private byte[] currentBuffer = new byte[0];
    private int position = 0;

    public BinaryIteratorInputStream(Iterator<byte[]> rowIterator) {
        this.rowIterator = rowIterator;
    }

    @Override
    public int read() throws IOException {
        while (true) {
            if (position < currentBuffer.length) {
                // Read from current buffer (header, row, or trailer)
                return currentBuffer[position++] & 0xFF;
            }

            // Move to the next state/buffer
            switch (state) {
                case HEADER:
                    currentBuffer = BINARY_HEADER;
                    position = 0;
                    state = State.ROWS;
                    break;
                case ROWS:
                    if (rowIterator.hasNext()) {
                        // The iterator must now return the full binary row (Step 2)
                        currentBuffer = rowIterator.next();
                        position = 0;
                    } else {
                        // All rows read, move to trailer
                        state = State.TRAILER;
                    }
                    break;
                case TRAILER:
                    currentBuffer = BINARY_TRAILER;
                    position = 0;
                    state = State.END;
                    break;
                case END:
                    return -1; // End of stream
            }
        }
    }
}
