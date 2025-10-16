package org.rifaii.dbrng.db.object;

import java.util.ArrayList;
import java.util.Collection;

public class DbIntrospection {

   private final Collection<Table> tables;

    public DbIntrospection() {
        tables = new ArrayList<>();
    }

    public DbIntrospection(Collection<Table> tables) {
        this.tables = tables;
    }

    public void addTable(Table table) {
       tables.add(table);
   }

   public Collection<Table> getTables() {
       return this.tables;
   }

}
