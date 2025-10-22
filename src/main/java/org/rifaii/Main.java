package org.rifaii;

import org.rifaii.dbrng.Populator;

public class Main {

    public static void main(String[] args) {
        int rowsNum = 1000;
        String connectionUrl = "jdbc:postgresql://%s:%s/%s?user=%s&password=%s&ssl=false"
                .formatted("localhost", "5432", "dbrng_demo", "postgres", "postgres");
        Populator.populate(connectionUrl, rowsNum);
    }

}