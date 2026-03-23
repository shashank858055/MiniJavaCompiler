import java.util.List;

/** All AST node types for MiniJava */
public class AST {

    // ── Program ──────────────────────────────────────────────────────────────
    public static class Program {
        public final MainClass mainClass;
        public final List<ClassDecl> classes;
        public Program(MainClass mc, List<ClassDecl> cls) {
            this.mainClass = mc; this.classes = cls;
        }
    }

    // ── Class declarations ────────────────────────────────────────────────────
    public static class MainClass {
        public final String name;
        public final Statement body;
        public final int line;
        public MainClass(String name, Statement body, int line) {
            this.name = name; this.body = body; this.line = line;
        }
    }

    public static class ClassDecl {
        public final String name;
        public final String superClass;   // null if no extends
        public final List<VarDecl> fields;
        public final List<MethodDecl> methods;
        public final int line;
        public ClassDecl(String name, String superClass,
                         List<VarDecl> fields, List<MethodDecl> methods, int line) {
            this.name = name; this.superClass = superClass;
            this.fields = fields; this.methods = methods; this.line = line;
        }
    }

    public static class MethodDecl {
        public final Type returnType;
        public final String name;
        public final List<Param> params;
        public final List<VarDecl> locals;
        public final List<Statement> body;
        public final Expr returnExpr;
        public final int line;
        public MethodDecl(Type rt, String name, List<Param> params,
                          List<VarDecl> locals, List<Statement> body,
                          Expr ret, int line) {
            this.returnType = rt; this.name = name; this.params = params;
            this.locals = locals; this.body = body; this.returnExpr = ret;
            this.line = line;
        }
    }

    public static class VarDecl {
        public final Type type;
        public final String name;
        public final int line;
        public VarDecl(Type type, String name, int line) {
            this.type = type; this.name = name; this.line = line;
        }
    }

    public static class Param {
        public final Type type;
        public final String name;
        public Param(Type type, String name) { this.type = type; this.name = name; }
    }

    // ── Types ─────────────────────────────────────────────────────────────────
    public static abstract class Type {}
    public static class IntType     extends Type { public String toString() { return "int"; } }
    public static class BoolType    extends Type { public String toString() { return "boolean"; } }
    public static class IntArrType  extends Type { public String toString() { return "int[]"; } }
    public static class ClassType   extends Type {
        public final String name;
        public ClassType(String name) { this.name = name; }
        public String toString() { return name; }
    }

    // ── Statements ────────────────────────────────────────────────────────────
    public static abstract class Statement { public int line; }

    public static class BlockStmt extends Statement {
        public final List<Statement> stmts;
        public BlockStmt(List<Statement> stmts, int line) {
            this.stmts = stmts; this.line = line;
        }
    }

    public static class IfStmt extends Statement {
        public final Expr cond;
        public final Statement thenBranch, elseBranch;
        public IfStmt(Expr cond, Statement t, Statement e, int line) {
            this.cond = cond; this.thenBranch = t; this.elseBranch = e; this.line = line;
        }
    }

    public static class WhileStmt extends Statement {
        public final Expr cond;
        public final Statement body;
        public WhileStmt(Expr cond, Statement body, int line) {
            this.cond = cond; this.body = body; this.line = line;
        }
    }

    public static class PrintStmt extends Statement {
        public final Expr expr;
        public PrintStmt(Expr expr, int line) { this.expr = expr; this.line = line; }
    }

    public static class AssignStmt extends Statement {
        public final String var;
        public final Expr expr;
        public AssignStmt(String var, Expr expr, int line) {
            this.var = var; this.expr = expr; this.line = line;
        }
    }

    public static class ArrayAssignStmt extends Statement {
        public final String array;
        public final Expr index, value;
        public ArrayAssignStmt(String array, Expr index, Expr value, int line) {
            this.array = array; this.index = index; this.value = value; this.line = line;
        }
    }

    // ── Expressions ──────────────────────────────────────────────────────────
    public static abstract class Expr { public int line; }

    public static class BinOp extends Expr {
        public enum Op { ADD, SUB, MUL, DIV, AND, LT, EQ }
        public final Op op;
        public final Expr left, right;
        public BinOp(Op op, Expr left, Expr right, int line) {
            this.op = op; this.left = left; this.right = right; this.line = line;
        }
    }

    public static class NotExpr extends Expr {
        public final Expr expr;
        public NotExpr(Expr expr, int line) { this.expr = expr; this.line = line; }
    }

    public static class IntLit extends Expr {
        public final int value;
        public IntLit(int value, int line) { this.value = value; this.line = line; }
    }

    public static class BoolLit extends Expr {
        public final boolean value;
        public BoolLit(boolean value, int line) { this.value = value; this.line = line; }
    }

    public static class IdentExpr extends Expr {
        public final String name;
        public IdentExpr(String name, int line) { this.name = name; this.line = line; }
    }

    public static class ThisExpr extends Expr {
        public ThisExpr(int line) { this.line = line; }
    }

    public static class NewIntArray extends Expr {
        public final Expr size;
        public NewIntArray(Expr size, int line) { this.size = size; this.line = line; }
    }

    public static class NewObject extends Expr {
        public final String className;
        public NewObject(String className, int line) { this.className = className; this.line = line; }
    }

    public static class ArrayLength extends Expr {
        public final Expr array;
        public ArrayLength(Expr array, int line) { this.array = array; this.line = line; }
    }

    public static class ArrayAccess extends Expr {
        public final Expr array, index;
        public ArrayAccess(Expr array, Expr index, int line) {
            this.array = array; this.index = index; this.line = line;
        }
    }

    public static class MethodCall extends Expr {
        public final Expr object;
        public final String method;
        public final List<Expr> args;
        public MethodCall(Expr object, String method, List<Expr> args, int line) {
            this.object = object; this.method = method; this.args = args; this.line = line;
        }
    }
}
