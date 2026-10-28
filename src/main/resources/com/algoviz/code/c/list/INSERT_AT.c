void insertAt(int index, int v) {
  if (index == 0) { insertHead(v); return; }  //@0
  Node *cur = head;  //@1
  for (int k = 0; k < index - 1; k++)  //@2
    cur = cur->next;  //@2
  Node *node = newNode(v);  //@3
  node->next = cur->next;  //@4
  cur->next = node;  //@5
}
