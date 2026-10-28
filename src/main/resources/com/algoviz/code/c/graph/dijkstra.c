int dijkstra(Grid *g, int start, int target) {
  for (int v = 0; v < g->cells; v++) dist[v] = INT_MAX;  //@0
  dist[start] = 0;  //@0
  pqPush(&pq, 0, start);  //@0
  while (!pqEmpty(&pq)) {  //@1
    int node = pqPopMin(&pq).node;  //@2
    if (settled[node]) continue;  //@3
    settled[node] = 1;  //@4
    if (node == target) return buildPath(target);  //@5
    for (int d = 0; d < 4; d++) {  //@6
      int nb = neighbour(g, node, d);  //@6
      if (nb < 0 || isWall(g, nb)) continue;  //@6
      int nd = dist[node] + cost(g, nb);  //@7
      if (nd < dist[nb]) {  //@7
        dist[nb] = nd;  //@8
        parent[nb] = node;  //@8
        pqPush(&pq, nd, nb);  //@8
      }
    }
  }
  return NO_PATH;  //@9
}
