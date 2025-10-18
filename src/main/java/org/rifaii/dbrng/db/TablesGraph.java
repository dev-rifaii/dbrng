package org.rifaii.dbrng.db;

import org.rifaii.dbrng.db.object.Table;

import java.util.ArrayList;
import java.util.List;

public class TablesGraph {

    List<Node<Table>> nodes = new ArrayList<>();

    public void addNode(Table from, Table to) {
        nodes.stream().filter(it -> it.data.equals(from))
                .findFirst();
    }

    public static class Node<T> {
        T data;
        List<Node<T>> children;
    }
}
