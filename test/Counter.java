// Counter.java - Tests object fields, method calls, this
class Counter {
    public static void main(String[] a) {
        System.out.println(new MyCounter().countUp(5));
        System.out.println(new MyCounter().countDown(10));
    }
}

class MyCounter {
    int value;

    public int countUp(int n) {
        int i;
        value = 0;
        i = 0;
        while (i < n) {
            value = value + 1;
            i = i + 1;
        }
        return value;
    }

    public int countDown(int n) {
        int i;
        value = n;
        i = 0;
        while (i < n) {
            value = value - 1;
            i = i + 1;
        }
        return value;
    }
}
