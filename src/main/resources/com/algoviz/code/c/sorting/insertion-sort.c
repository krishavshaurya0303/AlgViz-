void insertionSort(int a[], int n) {
  for (int i = 1; i < n; i++) {  //@0
    int key = a[i];  //@1
    int j = i - 1;  //@2
    while (j >= 0 && a[j] > key) {  //@3
      a[j + 1] = a[j];  /* shift right */  //@4
      j--;  //@5
    }
    a[j + 1] = key;  //@6
  }
}
