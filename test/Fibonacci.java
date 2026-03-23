// Fibonacci.java - Tests recursion with two base cases
class Fibonacci {
    public static void main(String[] a) {
        System.out.println(new Fib().compute(0));
        System.out.println(new Fib().compute(1));
        System.out.println(new Fib().compute(10));
    }
}

class Fib {
    public int compute(int n) {
        int result;
        if (n < 2)
            result = n;
        else
            result = this.compute(n - 1) + this.compute(n - 2);
        return result;
    }
}
