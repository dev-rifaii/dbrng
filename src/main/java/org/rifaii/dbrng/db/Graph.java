package org.rifaii.dbrng.db;

import java.util.*;
import java.util.stream.Collectors;

public class Graph<T> {

    private final Map<T, Set<T>> GRAPH = new HashMap<>();

    public void addEdge(T from, T to) {
        GRAPH.computeIfAbsent(from, k -> new HashSet<>()).add(to);
        GRAPH.computeIfAbsent(to, k -> new HashSet<>());
    }

    public void addNode(T node) {
        GRAPH.computeIfAbsent(node, k -> new HashSet<>());
    }

    //https://en.wikipedia.org/wiki/Topological_sorting
    public Queue<T> inTopologicalOrder() {
        Map<T, Integer> inDegrees = getInDegree();

        Queue<T> queue = new ArrayDeque<>();
        inDegrees.forEach((node, inDegree) -> {
            if (inDegree == 0) {
                queue.add(node);
            }
        });

        Queue<T> order = new LinkedList<>();

        while (!queue.isEmpty()) {
            T node = queue.poll();
            order.add(node);
            Set<T> dependents = GRAPH.get(node);

            for (T m : dependents) {
                inDegrees.put(m, inDegrees.getOrDefault(m, 0) - 1);
                if (inDegrees.get(m) <= 1) {
                    queue.add(m);
                }
            }
        }
        return order;
    }

    private Map<T, Integer> getInDegree() {
        Map<T, Integer> inDegree = new HashMap<>();
        inDegree.putAll(GRAPH.keySet().stream().collect(Collectors.toMap(k -> k, k -> 0)));

        Set<T> dependents = GRAPH.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());

        dependents.forEach(dependent -> GRAPH.keySet().forEach(node -> {
            if (GRAPH.get(node).contains(dependent))
                inDegree.put(dependent, inDegree.getOrDefault(node, 0) + 1);
        }));

        return inDegree;
    }
}
