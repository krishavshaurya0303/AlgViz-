void clear(void) {
  while (head != NULL) {  //@0
    Node *next = head->next;  //@0
    free(head);  //@0
    head = next;  //@0
  }
}
