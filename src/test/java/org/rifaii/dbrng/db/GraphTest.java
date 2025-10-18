package org.rifaii.dbrng.db;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphTest {

    // Suppose we have dependencies like:
    // A -> B -> C
    // (A depends on B, B depends on C)
    // Then topological order should be: [C, B, A]
    @Test
    void inTopologicalOrderTest() {
        Graph<String> graph = new Graph<>();
        graph.addNode("A", "B");
        graph.addNode("B", "C");

        List<String> order = graph.inTopologicalOrder();

        assertEquals(List.of("C", "B", "A"), order);
    }

}