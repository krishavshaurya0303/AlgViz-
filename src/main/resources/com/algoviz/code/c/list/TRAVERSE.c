void traverse(void) {
  Node *cur = head;  //@0
  while (cur != NULL) {  //@1
    visit(cur);  //@2
    cur = cur->next;  //@3
  }
}
