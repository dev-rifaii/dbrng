package org.rifaii.dbrng.db.object;

import java.util.ArrayList;
import java.util.List;

public class Table {

    public final String tableName;
    private List<Column> columns = new ArrayList<>();

    public Table(String tableName) {
        this.tableName = tableName;
    }

    public void addColumn(Column column) {
        columns.add(column);
    }

    public List<Column> getColumns() {
        return columns;
    }
}
