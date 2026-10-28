void levelOrder(Node *root) {
  Node *queue[MAX_NODES];
  int head = 0, tail = 0;
  if (root != NULL) queue[tail++] = root;  //@0
  while (head < tail) {  //@1
    Node *node = queue[head++];  //@2
    visit(node);  //@2
    if (node->left)  queue[tail++] = node->left;  //@3
    if (node->right) queue[tail++] = node->right;  //@3
  }
}
