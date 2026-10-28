void swap(int *x, int *y) { int t = *x; *x = *y; *y = t; }

void quickSort(int a[], int lo, int hi) {  //@0
  if (lo >= hi) return;  //@1
  int p = partition(a, lo, hi);  //@2
  quickSort(a, lo, p - 1);  //@3
  quickSort(a, p + 1, hi);  //@4
}

int partition(int a[], int lo, int hi) {
  int pivot = a[hi];  //@5
  int i = lo;  //@6
  for (int j = lo; j < hi; j++) {  //@7
    if (a[j] <= pivot) {  //@8
      if (i != j) swap(&a[i], &a[j]);  //@9
      i++;  //@10
    }
  }
  if (i != hi) swap(&a[i], &a[hi]);  //@11
  return i;  //@12
}
