package org.rifaii.dbrng.db;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphTest {

    // Suppose we have dependencies like:
    // A -> B -> C
    // (A depends on B, B depends on C)
    // Then topological order should be: [C, B, A]
    @Test
    void inTopologicalOrderTest() {
        Graph<String> graph = new Graph<>();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");

        String order = String.join(",", graph.inTopologicalOrder());

        assertEquals("A,B,C", order);
    }

}