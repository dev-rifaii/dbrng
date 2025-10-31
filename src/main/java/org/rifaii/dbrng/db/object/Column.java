package org.rifaii.dbrng.db.object;

import org.rifaii.dbrng.db.ColumnType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    public Supplier<String> customGenerator;
    public Supplier<Iterator<?>> generatorIteratorSupplier;
    public boolean sequential = false;
    public List<Constraint> constraints = new ArrayList<>();

    public boolean isUnique() {
        return constraints.stream()
                .map(it -> it.constraintType)
                .anyMatch(Column.Constraint.Type.UNIQUE::equals);
    }

    public static class Constraint {

        public String constraintName;
        public Type constraintType;
        public String columnName;
        public String rawCheckClause;

        public static class Check {
            public String biggerThanExclusive;
            public String smallerThanExclusive;
            public String notEqualsTo;
            public List<String> allowedValues;
        }

        public Constraint(String constraintName, Type constraintType, String columnName, String rawCheckClause) {
            this.constraintName = constraintName;
            this.constraintType = constraintType;
            this.columnName = columnName;
            this.rawCheckClause = rawCheckClause;
        }

        public enum Type {
            CHECK,
            UNIQUE,
            FOREIGN_KEY,
            PRIMARY_KEY,
        }
    }
}
