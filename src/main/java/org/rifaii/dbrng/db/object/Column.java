package org.rifaii.dbrng.db.object;

import org.rifaii.dbrng.db.ColumnType;

import java.util.function.Supplier;

public class Column {

    public String columnName;
    public ColumnType columnType;
    /*
     * For VARCHAR it's max char count
     * For NUMERIC it's precision
     */
    public int columnSize;
    public boolean isNullable;
    public boolean isPrimaryKey;
    public ForeignKey foreignKey;
    public Supplier<String> generator;
    public boolean sequential = false;

}
