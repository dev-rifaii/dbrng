package org.rifaii;

import org.rifaii.dbrng.Populator;

public class Main {

    public static void main(String[] args) {
        Populator.populate(new CardDbConfiguration());
    }

}