package org.rifaii.dbrng.db.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;

public class DbIntrospection {

   private final Collection<Table> tables;
   private Queue<Table> suggestedInsertOrder;

    public DbIntrospection(Collection<Table> tables) {
        this.tables = tables;
    }

    public void addTable(Table table) {
       tables.add(table);
   }

   public Collection<Table> getTables() {
       return this.tables;
   }

   public Queue<Table> getSuggestedInsertOrder() {
        return this.suggestedInsertOrder;
   }

   public void setSuggestedInsertOrder(Queue<Table> suggestedInsertOrder) {
        this.suggestedInsertOrder = suggestedInsertOrder;
   }
}
