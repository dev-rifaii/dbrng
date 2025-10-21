package org.rifaii.dbrng.generator;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * Passing the generator used for a primary key
 * and using the same value a few times before moving to next
 * one in order to have more than 1 row for that key.
 */
public class LaggingIterator implements Iterator<String> {

    int lag;
    int currentCount = 1;

    Supplier<String> generator;
    String currentValue;

    public LaggingIterator(Supplier<String> generator) {
        this(10, generator);
    }

    public LaggingIterator(int lag, Supplier<String> generator) {
        this.lag = lag;
        this.generator = generator;
        currentValue = generator.get();
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public String next() {
        if (currentCount % lag == 0) {
            currentValue = generator.get();
        }

        currentCount++;

        return currentValue;
    }
}
