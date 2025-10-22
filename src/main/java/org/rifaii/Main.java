package org.rifaii;

import org.rifaii.dbrng.Populator;

public class Main {

    public static void main(String[] args) {
        int rowsNum = 1000;
        Populator.populate(rowsNum);
    }

}