// Inheritance.java - Tests class extends and method override
class Inheritance {
    public static void main(String[] a) {
        System.out.println(new Dog().speak());
        System.out.println(new Cat().speak());
        System.out.println(new Dog().legs());
    }
}

class Animal {
    public int legs() {
        return 4;
    }
    public int speak() {
        return 0;
    }
}

class Dog extends Animal {
    public int speak() {
        return 1;
    }
}

class Cat extends Animal {
    public int speak() {
        return 2;
    }
}
