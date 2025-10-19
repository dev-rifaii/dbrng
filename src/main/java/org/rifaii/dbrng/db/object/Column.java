package org.rifaii.dbrng.db.object;

import org.rifaii.dbrng.db.ColumnType;

import java.util.function.Supplier;

public class Column {

    public String columnName;
    public ColumnType columnType;
    public int columnSize;
    public boolean isNullable;
    public boolean isPrimaryKey;
    public ForeignKey foreignKey;
    private Config config;

    public void setGenerator(Supplier<String> generator) {
        config = new Config(generator);
    }

    public Supplier<String> getGenerator() {
        if (config.generator == null) {
            throw new RuntimeException("No generator exists for %s".formatted(columnName));
        }
        return this.config.generator;
    }

    public static class Config {
        public Supplier<String> generator;

        public Config(Supplier<String> generator) {
            this.generator = generator;
        }
    }

}
