int search(int v) {
  Node *cur = head;  //@0
  int index = 0;  //@0
  while (cur != NULL) {  //@1
    if (cur->value == v) return index;  //@2
    cur = cur->next;  //@3
    index++;  //@3
  }
  return -1;  /* not found */  //@4
}
