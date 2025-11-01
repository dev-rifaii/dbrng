package org.rifaii.dbrng.db.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Table {

    public final String schemaName;
    public final String tableName;
    private List<Column> columns = new ArrayList<>();
    private List<ForeignKey> foreignKeys = new ArrayList<>();

    public Table(String schemaName, String tableName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public Table addColumn(Column column) {
        columns.add(column);
        return this;
    }

    public void setForeignKeys(List<ForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public boolean hasForeignKeys() {
        return !foreignKeys.isEmpty();
    }

    public List<Column> getColumns() {
        return columns;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return Objects.equals(schemaName, table.schemaName) && Objects.equals(tableName, table.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemaName, tableName);
    }
}
