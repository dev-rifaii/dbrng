package org.rifaii;

import org.rifaii.dbrng.Populator;

public class Main {

    public static void main(String[] args) {
        int rowsNum = 500_000;
        Populator.populate(rowsNum);
    }

}