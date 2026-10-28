void deleteAt(int index) {
  if (index == 0) { deleteHead(); return; }  //@0
  Node *cur = head;  //@1
  for (int k = 0; k < index - 1; k++)  //@2
    cur = cur->next;  //@2
  Node *removed = cur->next;  //@3
  cur->next = removed->next;  //@4
  free(removed);  //@4
}
