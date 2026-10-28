void inorder(Node *node) {  //@0
  if (node == NULL) return;  //@1
  inorder(node->left);  //@2
  visit(node);  //@3
  inorder(node->right);  //@4
}
