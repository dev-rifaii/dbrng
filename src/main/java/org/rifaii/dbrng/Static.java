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
            c.table_schema = 'public'
        AND table_name NOT IN ('databasechangelog', 'databasechangeloglock')
        ORDER BY c.table_name, c.ordinal_position;
        """;
}
