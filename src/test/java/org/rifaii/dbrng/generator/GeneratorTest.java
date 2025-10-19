package org.rifaii.dbrng.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.rifaii.dbrng.db.object.Column;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorTest {

    @Timeout(value = 1800, unit = TimeUnit.MILLISECONDS)
    @Test
    void generate() {
        List<Column> columns = DataProvider.columns;
        CsvRowIterator iterator = Generator.generate(columns, 1_000_000);

        while (iterator.hasNext()) {
            iterator.next();
        }

        assertFalse(iterator.hasNext());
    }
}