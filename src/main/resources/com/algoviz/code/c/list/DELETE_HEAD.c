void deleteHead(void) {
  if (head == NULL) return;  /* empty list */  //@0
  Node *removed = head;  //@1
  head = head->next;  //@2
  free(removed);  //@2
}
