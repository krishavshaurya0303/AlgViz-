void deleteTail(void) {
  if (head == NULL) return;  /* empty list */  //@0
  if (head->next == NULL) {  //@1
    free(head);  //@1
    head = NULL;  //@1
    return;  //@1
  }
  Node *cur = head;  //@2
  while (cur->next->next != NULL) cur = cur->next;  //@3
  free(cur->next);  //@4
  cur->next = NULL;  //@4
}
