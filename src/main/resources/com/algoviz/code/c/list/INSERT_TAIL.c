void insertTail(int v) {
  Node *node = newNode(v);  //@0
  if (head == NULL) { head = node; return; }  //@1
  Node *cur = head;  //@2
  while (cur->next != NULL) cur = cur->next;  //@3
  cur->next = node;  //@4
}
