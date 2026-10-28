void preorder(Node *node) {  //@0
  if (node == NULL) return;  //@1
  visit(node);  //@2
  preorder(node->left);  //@3
  preorder(node->right);  //@4
}
