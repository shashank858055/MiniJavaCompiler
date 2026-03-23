// LinearSearch.java - Tests arrays, while loops, boolean logic
// Array is [0, 3, 6, 9, 12, 15, 18, 21, 24, 27]
// Searching for 6 (found=1) and 99 (found=0)
class LinearSearch {
    public static void main(String[] a) {
        System.out.println(new Searcher().search(6));
        System.out.println(new Searcher().search(99));
    }
}

class Searcher {
    public int search(int target) {
        int[] arr;
        int i;
        int found;
        arr = new int[10];
        i = 0;
        while (i < 10) {
            arr[i] = i * 3;
            i = i + 1;
        }
        found = 0;
        i = 0;
        while (i < 10) {
            if (arr[i] == target)
                found = 1;
            i = i + 1;
        }
        return found;
    }
}
