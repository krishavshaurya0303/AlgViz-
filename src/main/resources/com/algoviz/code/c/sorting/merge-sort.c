void mergeSort(int a[], int tmp[], int lo, int hi) {  //@0
  if (lo >= hi) return;  //@1
  int mid = (lo + hi) / 2;  //@2
  mergeSort(a, tmp, lo, mid);  //@3
  mergeSort(a, tmp, mid + 1, hi);  //@4
  merge(a, tmp, lo, mid, hi);  //@5
}

void merge(int a[], int tmp[], int lo, int mid, int hi) {
  for (int x = lo; x <= hi; x++) tmp[x] = a[x];  //@6
  int i = lo, j = mid + 1, k = lo;  //@7
  while (i <= mid && j <= hi) {  //@8
    if (tmp[i] <= tmp[j]) a[k++] = tmp[i++];  //@9
    else                  a[k++] = tmp[j++];  //@10
  }
  while (i <= mid) a[k++] = tmp[i++];  //@11
  while (j <= hi)  a[k++] = tmp[j++];  //@11
}
