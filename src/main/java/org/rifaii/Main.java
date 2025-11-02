package org.rifaii;

import org.rifaii.dbrng.Configuration;
import org.rifaii.dbrng.Populator;
import org.rifaii.dbrng.cli.CliCommands;

public class Main {

    public static void main(String[] args) {
        Configuration config = CliCommands.parse(args);
        Populator.populate(config);
    }

}