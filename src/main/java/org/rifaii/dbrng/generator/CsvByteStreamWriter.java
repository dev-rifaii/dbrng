package org.rifaii.dbrng.generator;

import org.postgresql.util.ByteStreamWriter;

import java.io.IOException;

public class CsvByteStreamWriter implements ByteStreamWriter {



    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public void writeTo(ByteStreamTarget target) throws IOException {
    }
}
