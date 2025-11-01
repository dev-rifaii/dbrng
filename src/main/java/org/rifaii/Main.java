package org.rifaii;

import org.rifaii.dbrng.Configuration;
import org.rifaii.dbrng.Populator;

public class Main {

    public static void main(String[] args) {
        Populator.populate(new Configuration(
                "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres",
                20
        ) {});
    }

}