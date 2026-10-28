void postorder(Node *node) {  //@0
  if (node == NULL) return;  //@1
  postorder(node->left);  //@2
  postorder(node->right);  //@3
  visit(node);  //@4
}
