package org.rifaii.dbrng;

import org.rifaii.dbrng.db.object.Column;

import java.util.ArrayList;
import java.util.List;

public class Static {

    public static List<Column> getColumns() {
        List<Column> columns = new ArrayList<>();

        columns.add(new Column() {{
            columnName = "created_at";
            columnType = "timestamp with time zone";
            columnSize = 47;
            isNullable = false;
        }});

        columns.add(new Column() {{
            columnName = "groceries_receipt_id";
            columnType = "numeric";
            columnSize = 0;
            isNullable = true;
        }});

        columns.add(new Column() {{
            columnName = "id";
            columnType = "numeric";
            columnSize = 0;
            isNullable = false;
        }});

        columns.add(new Column() {{
            columnName = "modified_at";
            columnType = "timestamp with time zone";
            columnSize = 47;
            isNullable = false;
        }});

        columns.add(new Column() {{
            columnName = "name";
            columnType = "character varying";
            columnSize = 255;
            isNullable = false;
        }});

        columns.add(new Column() {{
            columnName = "pre_sale_price";
            columnType = "numeric";
            columnSize = 116;
            isNullable = true;
        }});

        columns.add(new Column() {{
            columnName = "price";
            columnType = "numeric";
            columnSize = 116;
            isNullable = false;
        }});

        columns.add(new Column() {{
            columnName = "price_per_piece";
            columnType = "numeric";
            columnSize = 116;
            isNullable = false;
        }});

        columns.add(new Column() {{
            columnName = "quantity";
            columnType = "numeric";
            columnSize = 110;
            isNullable = false;
        }});

        return columns;
    }

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
        WHERE
            c.table_schema = '%s'
        AND table_name NOT IN ('databasechangelog', 'databasechangeloglock')
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
