void reverse(void) {
  Node *prev = NULL, *cur = head;  //@0
  while (cur != NULL) {  //@1
    Node *next = cur->next;  /* save */  //@2
    cur->next = prev;  /* flip */  //@3
    prev = cur;  //@4
    cur = next;  //@5
  }
  head = prev;  //@6
}
