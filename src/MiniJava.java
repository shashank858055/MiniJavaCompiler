import java.io.*;
import java.util.List;

/**
 * MiniJava Compiler & Interpreter
 * Entry point — orchestrates lexing, parsing, type-checking, and execution.
 *
 * Usage:
 *   java MiniJava <file.java>              -- compile and run
 *   java MiniJava --lex <file.java>        -- show tokens only
 *   java MiniJava --parse <file.java>      -- show AST only
 *   java MiniJava --check <file.java>      -- type-check only
 *   java MiniJava --help                   -- show help
 */
public class MiniJava {

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("--help")) {
            printHelp();
            return;
        }

        String mode = "run";
        String filename = null;

        for (String arg : args) {
            if (arg.equals("--lex"))   { mode = "lex";   }
            else if (arg.equals("--parse")) { mode = "parse"; }
            else if (arg.equals("--check")) { mode = "check"; }
            else if (arg.equals("--run"))   { mode = "run";   }
            else if (!arg.startsWith("--")) { filename = arg; }
        }

        if (filename == null) {
            System.err.println("Error: No input file specified.");
            printHelp();
            System.exit(1);
        }

        // ── Read source file ──────────────────────────────────────────────────
        String source;
        try {
            source = readFile(filename);
        } catch (IOException e) {
            System.err.println("Error: Cannot read file '" + filename + "': " + e.getMessage());
            System.exit(1);
            return;
        }

        try {
            // ── Lex ───────────────────────────────────────────────────────────
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();

            if (mode.equals("lex")) {
                System.out.println("=== Tokens for: " + filename + " ===");
                for (Token t : tokens)
                    System.out.printf("  %-25s  \"%s\"%n", t.type, t.value);
                System.out.println("Total: " + tokens.size() + " tokens");
                return;
            }

            // ── Parse ─────────────────────────────────────────────────────────
            Parser parser = new Parser(tokens);
            AST.Program program = parser.parse();

            if (mode.equals("parse")) {
                System.out.println("=== AST for: " + filename + " ===");
                printAST(program);
                return;
            }

            // ── Type check ────────────────────────────────────────────────────
            TypeChecker checker = new TypeChecker();
            checker.check(program);

            if (mode.equals("check")) {
                System.out.println("Type check passed for: " + filename);
                return;
            }

            // ── Interpret ─────────────────────────────────────────────────────
            Interpreter interp = new Interpreter();
            interp.run(program);

        } catch (CompilerException e) {
            System.err.println("\n[MiniJava Error] " + e.getMessage());
            System.exit(1);
        }
    }

    // ── AST Printer ───────────────────────────────────────────────────────────
    private static void printAST(AST.Program prog) {
        System.out.println("Program");
        System.out.println("  MainClass: " + prog.mainClass.name);
        for (AST.ClassDecl cd : prog.classes) {
            System.out.println("  Class: " + cd.name
                + (cd.superClass != null ? " extends " + cd.superClass : ""));
            for (AST.VarDecl vd : cd.fields)
                System.out.println("    Field: " + vd.type + " " + vd.name);
            for (AST.MethodDecl md : cd.methods) {
                System.out.print("    Method: " + md.returnType + " " + md.name + "(");
                for (int i = 0; i < md.params.size(); i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(md.params.get(i).type + " " + md.params.get(i).name);
                }
                System.out.println(")");
                for (AST.VarDecl vd : md.locals)
                    System.out.println("      Local: " + vd.type + " " + vd.name);
                System.out.println("      Statements: " + md.body.size());
            }
        }
    }

    private static String readFile(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private static void printHelp() {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║         MiniJava Compiler & Interpreter          ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  Usage:                                          ║");
        System.out.println("║    java MiniJava <file.java>      Run program    ║");
        System.out.println("║    java MiniJava --lex   <file>   Show tokens    ║");
        System.out.println("║    java MiniJava --parse <file>   Show AST       ║");
        System.out.println("║    java MiniJava --check <file>   Type-check     ║");
        System.out.println("║    java MiniJava --help           Show this help ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  MiniJava supports:                              ║");
        System.out.println("║    int, boolean, int[]                           ║");
        System.out.println("║    classes, inheritance (extends)                ║");
        System.out.println("║    if/else, while, System.out.println            ║");
        System.out.println("║    +  -  *  /  <  &&  !  ==                      ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
}
