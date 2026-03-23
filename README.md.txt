# 🔧 MiniJava Compiler & Interpreter

A fully working compiler and interpreter for the **MiniJava** language — built from scratch in pure Java with zero external dependencies.

MiniJava is a clean subset of Java designed for teaching compiler construction. This project implements every classical phase of a real compiler.

---

## 🚀 Pipeline
```
Source (.java) → Lexer → Parser → AST → Type Checker → Interpreter → Output
```

| Phase | File | Description |
|-------|------|-------------|
| Lexical Analysis | `Lexer.java` | Tokenises source into keywords, identifiers, operators |
| Parsing | `Parser.java` | Recursive-descent parser builds an Abstract Syntax Tree |
| Type Checking | `TypeChecker.java` | Symbol table, type inference, error detection |
| Interpretation | `Interpreter.java` | Tree-walking executor with full OOP support |

---

## ⚡ Quick Start

**Requirements:** Java 8 or higher — no other dependencies.
```bash
# 1. Compile the compiler
javac -d bin src/*.java        # Linux/macOS
# OR on Windows:
javac -d bin src\TokenType.java src\Token.java src\CompilerException.java src\AST.java src\Lexer.java src\Parser.java src\TypeChecker.java src\Interpreter.java src\MiniJava.java

# 2. Run a MiniJava program
java -cp bin MiniJava test/Factorial.java
```

---

## 🧪 Test Programs

| Program | Tests | Expected Output |
|---------|-------|-----------------|
| `Factorial.java` | Recursion, if/else | `3628800` |
| `Fibonacci.java` | Multiple recursion | `0`, `1`, `55` |
| `BubbleSort.java` | Arrays, nested while loops | `1 2 3 4 5` |
| `Inheritance.java` | extends, method override | `1`, `2`, `4` |
| `LinearSearch.java` | Arrays, boolean logic | `1`, `0` |
| `Counter.java` | Object fields, this | `5`, `0` |

---

## 🛠️ Compiler Modes
```bash
java -cp bin MiniJava test/Factorial.java         # Run program
java -cp bin MiniJava --lex   test/Factorial.java # Show tokens
java -cp bin MiniJava --parse test/Factorial.java # Show AST
java -cp bin MiniJava --check test/Factorial.java # Type-check only
java -cp bin MiniJava --help                      # Help
```

---

## 📁 Project Structure
```
MiniJavaCompiler/
├── src/
│   ├── TokenType.java        Token type enum (40+ types)
│   ├── Token.java            Token data class
│   ├── Lexer.java            Hand-written lexical analyser
│   ├── AST.java              All 20+ AST node classes
│   ├── Parser.java           Recursive-descent parser
│   ├── TypeChecker.java      Type checker with symbol table
│   ├── Interpreter.java      Tree-walking interpreter
│   ├── CompilerException.java Error handling
│   └── MiniJava.java         Main entry point
├── test/                     6 MiniJava test programs
├── bin/                      Compiled .class files
├── docs/                     HTML documentation
└── build.sh / build.bat      Build scripts
```

---

## 🌐 MiniJava Language Features

- **Types:** `int`, `boolean`, `int[]`, class references
- **OOP:** Classes, single inheritance (`extends`), method calls, `this`
- **Control Flow:** `if/else`, `while`
- **Operators:** `+` `-` `*` `/` `<` `==` `&&` `!`
- **Arrays:** `new int[n]`, index access, `.length`
- **I/O:** `System.out.println()`

---

## 🧠 Key Concepts Demonstrated

- **Recursive Descent Parsing** — each grammar rule maps to one method
- **Operator Precedence Climbing** — via call chain (AND → EQ → LT → ADD → MUL)
- **Visitor Pattern** — `instanceof` dispatch over AST node hierarchy
- **Symbol Table Design** — two-pass type checking with class/method/scope tables
- **Tree-Walking Interpretation** — fresh environment map per method call (call stack simulation)
- **Inheritance** — superclass chain walk for method and field resolution

---

## 👤 Author

**Shashank Shivaswamy**
Java Full Stack Developer
[GitHub](https://github.com/shashank858055)

---

## 📄 License

MIT License — free to use, modify, and distribute.
```

---

## 🚫 Step 5 — Create a .gitignore File

In your project folder, create a file named `.gitignore` (no extension) with this content:
```
# Compiled class files
bin/*.class
bin/**/*.class

# OS files
.DS_Store
Thumbs.db

# IDE files
.idea/
*.iml
.vscode/
*.class