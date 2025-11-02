package org.rifaii;

import org.rifaii.dbrng.Configuration;
import org.rifaii.dbrng.Populator;
import org.rifaii.dbrng.cli.CliCommands;
import org.rifaii.dbrng.generator.generators.StringGenerator;

public class Main {

    public static void main(String[] args) {
        Configuration config = new Configuration(
                "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres",
                20
        )
                .with(
                        "public.customer.name",
                        () -> "myCustomGenerator-%s".formatted(StringGenerator.generateString(4))
                )
                .with(
                        "public.customer.email",
                        () -> "%s@gmail.com".formatted(StringGenerator.generateString(4))
                );
        Populator.populate(config);
//        Configuration config = CliCommands.parse(args);
//        Populator.populate(config);
    }

}