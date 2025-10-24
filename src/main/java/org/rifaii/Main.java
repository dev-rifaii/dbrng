package org.rifaii;

import org.rifaii.dbrng.DbRngDbConfiguration;
import org.rifaii.dbrng.Populator;

public class Main {

    public static void main(String[] args) {
        Populator.populate(new DbRngDbConfiguration());
    }

}