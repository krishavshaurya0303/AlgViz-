Node *insert(Node *root, int v) {
  if (root == NULL) return newNode(v);  //@0
  Node *cur = root;  //@1
  for (;;) {  //@2
    if (v == cur->value) return root;  /* duplicate */  //@3
    if (v < cur->value) {  //@4
      if (cur->left == NULL) {  //@5
        cur->left = newNode(v);  //@5
        return root;  //@5
      }
      cur = cur->left;  //@6
    } else {  //@7
      if (cur->right == NULL) {  //@8
        cur->right = newNode(v);  //@8
        return root;  //@8
      }
      cur = cur->right;  //@9
    }
  }
}
