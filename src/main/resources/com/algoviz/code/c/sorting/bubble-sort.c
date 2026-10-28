void bubbleSort(int a[], int n) {
  for (int pass = 0; pass < n - 1; pass++) {  //@0
    int swapped = 0;  //@1
    for (int j = 0; j < n - pass - 1; j++) {  //@2
      if (a[j] > a[j + 1]) {  //@3
        int t = a[j];  //@4
        a[j] = a[j + 1];  //@4
        a[j + 1] = t;  //@4
        swapped = 1;  //@5
      }
    }
    /* a[n - pass - 1] is now final */  //@6
    if (!swapped) break;  //@7
  }
}
