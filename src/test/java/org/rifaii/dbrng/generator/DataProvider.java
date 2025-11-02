package org.rifaii.dbrng.generator;

import org.rifaii.dbrng.db.ColumnType;
import org.rifaii.dbrng.db.object.Column;

import java.util.List;

public class DataProvider {

    static final List<Column> columns = List.of(
            new Column() {{
                columnName = "id";
                columnType = ColumnType.NUMERIC;
                columnSize = 20;
                isNullable = false;
                isPrimaryKey = true;
                foreignKey = null;
            }},
            new Column() {{
                columnName = "username";
                columnType = ColumnType.TEXT;
                columnSize = 100;
                isNullable = false;
                isPrimaryKey = false;
                foreignKey = null;
            }},
            new Column() {{
                columnName = "email";
                columnType = ColumnType.TEXT;
                columnSize = 20;
                isNullable = true;
                isPrimaryKey = false;
                foreignKey = null;
            }},
            new Column() {{
                columnName = "age";
                columnType = ColumnType.NUMERIC;
                columnSize = 3;
                isNullable = true;
                isPrimaryKey = false;
                foreignKey = null;
            }},
            new Column() {{
                columnName = "balance";
                columnType = ColumnType.NUMERIC;
                columnSize = 10;
                isNullable = true;
                isPrimaryKey = false;
                foreignKey = null;
            }},
            new Column() {{
                columnName = "description";
                columnType = ColumnType.TEXT;
                columnSize = 20;
                isNullable = true;
                isPrimaryKey = false;
                foreignKey = null;
            }}
    );
}
