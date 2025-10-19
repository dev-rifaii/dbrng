package org.rifaii.dbrng.generator;

import org.rifaii.dbrng.db.object.Column;

import java.util.List;

public class DataProvider {

    static final List<Column> columns = List.of(
            new Column() {{
                columnName = "id";
                columnType = "BIGINT";
                columnSize = 20;
                isNullable = false;
                isPrimaryKey = true;
                foreignKey = null;
            }},
            new Column() {{
                columnName = "username";
                columnType = "CHARACTER VARYING";
                columnSize = 100;
                isNullable = false;
                isPrimaryKey = false;
                foreignKey = null;
            }},
            new Column() {{
                columnName = "email";
                columnType = "TEXT";
                columnSize = 0;
                isNullable = true;
                isPrimaryKey = false;
                foreignKey = null;
            }},
            new Column() {{
                columnName = "age";
                columnType = "NUMERIC";
                columnSize = 3;
                isNullable = true;
                isPrimaryKey = false;
                foreignKey = null;
            }},
            new Column() {{
                columnName = "balance";
                columnType = "NUMERIC";
                columnSize = 10;
                isNullable = true;
                isPrimaryKey = false;
                foreignKey = null;
            }},
            new Column() {{
                columnName = "description";
                columnType = "TEXT";
                columnSize = 0;
                isNullable = true;
                isPrimaryKey = false;
                foreignKey = null;
            }}
    );
}
