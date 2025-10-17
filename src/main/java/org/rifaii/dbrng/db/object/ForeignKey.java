package org.rifaii.dbrng.db.object;

public class ForeignKey {

    public String tableSchema;
    public String constraintName;
    public String tableName;
    public String columnName;
    public String foreignTableSchema;
    public String foreignTableName;
    public String foreignColumnName;

    public ForeignKey(String tableSchema,
                      String constraintName,
                      String tableName,
                      String columnName,
                      String foreignTableSchema,
                      String foreignTableName,
                      String foreignColumnName) {
        this.tableSchema = tableSchema;
        this.constraintName = constraintName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.foreignTableSchema = foreignTableSchema;
        this.foreignTableName = foreignTableName;
        this.foreignColumnName = foreignColumnName;
    }
}
