// BubbleSort.java - Tests arrays and nested while loops
class BubbleSort {
    public static void main(String[] a) {
        System.out.println(new Sorter().run(5));
    }
}

class Sorter {
    public int run(int size) {
        int[] arr;
        int i;
        int j;
        int tmp;
        int n;
        arr = new int[size];
        // Fill with reverse values: 5,4,3,2,1
        i = 0;
        while (i < size) {
            arr[i] = size - i;
            i = i + 1;
        }
        // Bubble sort
        n = size;
        i = 0;
        while (i < n) {
            j = 0;
            while (j < (n - i - 1)) {
                if (arr[j+1] < arr[j]) {
                    tmp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = tmp;
                }
                j = j + 1;
            }
            i = i + 1;
        }
        // Print sorted array
        i = 0;
        while (i < size) {
            System.out.println(arr[i]);
            i = i + 1;
        }
        return 0;
    }
}
