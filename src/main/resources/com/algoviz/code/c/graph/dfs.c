typedef struct { int node, parent; } Entry;

int dfs(Grid *g, int start, int target) {
  Entry stack[MAXE];
  int top = 0;
  stack[top++] = (Entry){start, -1};  //@0
  while (top > 0) {  //@1
    Entry e = stack[--top];  //@2
    if (visited[e.node]) continue;  //@3
    visited[e.node] = 1;  //@4
    parent[e.node] = e.parent;  //@4
    if (e.node == target) return buildPath(target);  //@5
    /* left, down, right, up */
    for (int d = 0; d < 4; d++) {  //@6
      int nb = neighbour(g, e.node, d);  //@6
      if (nb >= 0 && !visited[nb] && !isWall(g, nb))  //@7
        stack[top++] = (Entry){nb, e.node};  //@7
    }
  }
  return NO_PATH;  //@8
}
