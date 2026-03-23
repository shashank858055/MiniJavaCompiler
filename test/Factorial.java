// Factorial.java - Tests recursion and if/else
class Factorial {
    public static void main(String[] a) {
        System.out.println(new Fac().compute(10));
    }
}

class Fac {
    public int compute(int n) {
        int result;
        if (n < 1)
            result = 1;
        else
            result = n * (this.compute(n - 1));
        return result;
    }
}
