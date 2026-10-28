Node *delete(Node *root, int v) {
  Node *parent = NULL, *cur = root;  //@0
  while (cur != NULL && cur->value != v) {  //@0
    parent = cur;  //@0
    cur = v < cur->value ? cur->left : cur->right;  //@0
  }
  if (cur == NULL) return root;  /* not found */  //@1
  if (cur->left == NULL || cur->right == NULL) {  //@2,3
    Node *child = cur->left ? cur->left : cur->right;  //@2,3
    if (parent == NULL) root = child;  //@2,3
    else if (parent->left == cur) parent->left = child;  //@2,3
    else parent->right = child;  //@2,3
    free(cur);  //@2,3
    return root;
  }
  Node *sp = cur, *succ = cur->right;  //@4
  while (succ->left != NULL) {  //@5
    sp = succ;  //@5
    succ = succ->left;  //@5
  }
  cur->value = succ->value;  //@6
  if (sp == cur) sp->right = succ->right;  //@7
  else sp->left = succ->right;  //@7
  free(succ);  //@7
  return root;
}
