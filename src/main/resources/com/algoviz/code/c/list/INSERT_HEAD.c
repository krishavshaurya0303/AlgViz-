void insertHead(int v) {
  Node *node = newNode(v);  //@0
  node->next = head;  //@1
  head = node;  //@2
}
