package org.rifaii.dbrng;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CsvRowIterator implements Iterator<String> {

    int limit;
    List<Supplier<String>> suppliers;

    public CsvRowIterator(int limit, List<Supplier<String>> suppliers) {
        this.limit = limit;
        this.suppliers = suppliers;
    }

    @Override
    public boolean hasNext() {
        return limit > 0;
    }

    @Override
    public String next() {
        limit = limit - 1;
        return suppliers.stream()
            .map(Supplier::get)
            .collect(Collectors.joining(","));
    }
}
