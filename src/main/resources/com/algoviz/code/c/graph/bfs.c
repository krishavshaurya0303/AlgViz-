int bfs(Grid *g, int start, int target) {
  int queue[MAXV], head = 0, tail = 0;
  queue[tail++] = start;  //@0
  discovered[start] = 1;  //@0
  while (head < tail) {  //@1
    int node = queue[head++];  //@2
    if (node == target) return buildPath(target);  //@3
    /* up, right, down, left */
    for (int d = 0; d < 4; d++) {  //@4
      int nb = neighbour(g, node, d);  //@4
      if (nb >= 0 && !discovered[nb] && !isWall(g, nb)) {  //@5
        discovered[nb] = 1;  //@6
        parent[nb] = node;  //@6
        queue[tail++] = nb;  //@6
      }
    }
  }
  return NO_PATH;  //@7
}
