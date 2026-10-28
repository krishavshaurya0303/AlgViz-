void selectionSort(int a[], int n) {
  for (int i = 0; i < n - 1; i++) {  //@0
    int min = i;  //@1
    for (int j = i + 1; j < n; j++) {  //@2
      if (a[j] < a[min])  //@3
        min = j;  //@4
    }
    if (min != i) {  //@5
      int t = a[i];  //@5
      a[i] = a[min];  //@5
      a[min] = t;  //@5
    }
    /* a[i] is now in its final place */  //@6
  }
}
