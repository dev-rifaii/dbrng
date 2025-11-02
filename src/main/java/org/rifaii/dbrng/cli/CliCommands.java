package org.rifaii.dbrng.cli;

import org.rifaii.dbrng.Configuration;

import java.util.regex.Pattern;

public class CliCommands {

    public static final String USAGE =
            """
                    DBRNG Usage:
                    
                    -h          host     (default=localhost)
                    -p          port     (default=5432)
                    -d          database (default=postgres)
                    -U          username
                    -P          password
                    -c          count of rows to generate
                    """;


    static final Pattern PATTERN = Pattern.compile("(A-Za-z])\\s+(\\S+)");

    public static Configuration parse(String[] args) {
        if (args.length < 6) {
            misuse();
        }

        String host = "localhost";
        String port = "5432";
        String database = "postgres";

        String username = "";
        String password = "";
        int rowsCount = 0;

        for (int i = 0; i < args.length; i += 2) {
            final String flag = args[i];

            switch (flag) {
                case "-U" -> username = args[i + 1];
                case "-P" -> password = args[i + 1];
                case "-d" -> database = args[i + 1];
                case "-h" -> host = args[i + 1];
                case "-p" -> port = args[i + 1];
                case "-c" -> {
                    try {
                        rowsCount = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException e) {
                        System.err.println("Count should be an integer");
                        misuse();
                    }
                }
                default -> {
                    System.err.println("Unknown flag: " + flag);
                    misuse();
                }
            }
        }

        if (username == null || password == null || rowsCount < 1) {
            misuse();
        }

        String connectionUrl = "jdbc:postgresql://%s:%s/%s?user=%s&password=%s"
                .formatted(host, port, database, username, password);

        return new Configuration(connectionUrl, rowsCount) {};
    }

    private static void misuse() {
        System.out.println(USAGE);
        System.exit(1);
    }
}
