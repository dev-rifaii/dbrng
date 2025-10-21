package org.rifaii.dbrng.generator;

import java.util.Iterator;
import java.util.function.Supplier;

public class LaggingIterator implements Iterator<byte[]> {

    int lag;
    int currentCount = 1;

    Supplier<byte[]> generator;
    byte[] currentValue;

    public LaggingIterator(Supplier<byte[]> generator) {
        this(10, generator);
    }

    public LaggingIterator(int lag, Supplier<byte[]> generator) {
        this.lag = lag;
        this.generator = generator;
        currentValue = generator.get();
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public byte[] next() {
        if (currentCount % lag == 0) {
            currentValue = generator.get();
        }

        currentCount++;

        return currentValue;
    }
}
