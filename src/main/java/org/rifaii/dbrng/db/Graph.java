package org.rifaii.dbrng.db;

import java.util.*;
import java.util.stream.Collectors;

public class Graph<T> {

    Map<T, Set<T>> graph =  new HashMap<>();

    public void addNode(T from, T to) {
        graph.computeIfAbsent(from, k -> new HashSet<>()).add(to);
    }

    //https://en.wikipedia.org/wiki/Topological_sorting
    public List<T> inTopologicalOrder() {
        Map<T, Set<T>> graphClone = new HashMap<>(this.graph);
        List<T> L = new ArrayList<>();
        Set<T> S = getNoDepTs();

        while (!S.isEmpty()) {
            T n = S.iterator().next();
            S.remove(n);
            L.add(n);

            for (T m : graphClone.get(n)) {
                S.remove(m);

                if (hasNoIncomingEdges(graphClone, m)) {
                    L.add(m);
                }
            }
        }

        return L;
    }

    private boolean hasNoIncomingEdges(Map<T, Set<T>> graph, T table) {
        return graph.values().stream().noneMatch(deps -> deps.contains(table));
    }

    private Set<T> getNoDepTs() {
        Set<T> tablesWithDeps = graph.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toSet());

        return graph.keySet()
                .stream()
                .filter(table -> !tablesWithDeps.contains(table))
                .collect(Collectors.toSet());
    }
}
