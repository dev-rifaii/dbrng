package org.rifaii.dbrng.db;

public class Queries {

    public static final String QUERY_PRIMARY_KEYS =
            """
                    SELECT
                        tc.table_name,
                        kcu.column_name
                    FROM information_schema.table_constraints tc
                             JOIN information_schema.key_column_usage kcu
                                  ON tc.constraint_name = kcu.constraint_name
                    WHERE tc.constraint_type = 'PRIMARY KEY'
                      AND tc.table_schema = '%s';
                    """;

    public static final String QUERY_SCHEMA_INTROSPECT
            = """
            SELECT
                c.table_name,
                c.column_name,
                c.is_nullable = 'YES' AS nullable,
                c.column_default,
                UPPER(c.data_type) AS data_type,
                c.character_maximum_length,
                c.numeric_precision,
                c.numeric_scale
            FROM information_schema.columns c
            JOIN information_schema.tables t
                ON c.table_name = t.table_name
                AND c.table_schema = t.table_schema
            WHERE
                c.table_schema = '%s'
                AND t.table_type = 'BASE TABLE'
                AND c.table_name NOT IN ('databasechangelog', 'databasechangeloglock')
            ORDER BY c.table_name, c.ordinal_position;
            """;

    public static final String QUERY_FOREIGN_KEYS
            = """
            SELECT
                tc.table_schema,
                tc.constraint_name,
                tc.table_name,
                kcu.column_name,
                ccu.table_schema AS foreign_table_schema,
                ccu.table_name AS foreign_table_name,
                ccu.column_name AS foreign_column_name
            FROM information_schema.table_constraints AS tc
            JOIN information_schema.key_column_usage AS kcu
                    ON tc.constraint_name = kcu.constraint_name
                    AND tc.table_schema = kcu.table_schema
            JOIN information_schema.constraint_column_usage AS ccu
                    ON ccu.constraint_name = tc.constraint_name
            WHERE tc.constraint_type = 'FOREIGN KEY'
                AND tc.table_schema='%s';
            """;
}
