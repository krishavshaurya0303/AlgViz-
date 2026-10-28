Node *search(Node *root, int v) {
  Node *cur = root;  //@0
  while (cur != NULL) {  //@1
    if (v == cur->value) return cur;  //@2
    if (v < cur->value) cur = cur->left;  //@3
    else cur = cur->right;  //@4
  }
  return NULL;  /* not found */  //@5
}
