import java.util.*;

/**
 * Tree-walking interpreter for MiniJava.
 * Executes the AST directly - no bytecode generation needed.
 */
public class Interpreter {

    // ── Runtime value representation ─────────────────────────────────────────
    static class MJObject {
        String className;
        Map<String, Object> fields = new LinkedHashMap<>();
        MJObject(String className) { this.className = className; }
        public String toString() { return "<" + className + " object>"; }
    }

    // ── Symbol table for classes ──────────────────────────────────────────────
    private final Map<String, AST.ClassDecl>  classes  = new LinkedHashMap<>();
    private final Map<String, AST.MethodDecl> methodMap = new LinkedHashMap<>(); // "Class.method"

    public void run(AST.Program prog) {
        // Register all classes
        for (AST.ClassDecl cd : prog.classes) {
            classes.put(cd.name, cd);
            for (AST.MethodDecl md : cd.methods)
                methodMap.put(cd.name + "." + md.name, md);
        }
        // Execute main
        execStmt(prog.mainClass.body, new LinkedHashMap<>(), null);
    }

    // ── Statement execution ───────────────────────────────────────────────────
    private void execStmt(AST.Statement s, Map<String, Object> env, MJObject self) {
        if (s instanceof AST.BlockStmt) {
            for (AST.Statement child : ((AST.BlockStmt) s).stmts)
                execStmt(child, env, self);

        } else if (s instanceof AST.IfStmt) {
            AST.IfStmt is = (AST.IfStmt) s;
            boolean cond = (Boolean) eval(is.cond, env, self);
            if (cond) execStmt(is.thenBranch, env, self);
            else if (is.elseBranch != null) execStmt(is.elseBranch, env, self);

        } else if (s instanceof AST.WhileStmt) {
            AST.WhileStmt ws = (AST.WhileStmt) s;
            while ((Boolean) eval(ws.cond, env, self))
                execStmt(ws.body, env, self);

        } else if (s instanceof AST.PrintStmt) {
            Object val = eval(((AST.PrintStmt) s).expr, env, self);
            System.out.println(val);

        } else if (s instanceof AST.AssignStmt) {
            AST.AssignStmt as = (AST.AssignStmt) s;
            Object val = eval(as.expr, env, self);
            if (env.containsKey(as.var)) {
                env.put(as.var, val);
            } else if (self != null && self.fields.containsKey(as.var)) {
                self.fields.put(as.var, val);
            } else {
                env.put(as.var, val);
            }

        } else if (s instanceof AST.ArrayAssignStmt) {
            AST.ArrayAssignStmt aas = (AST.ArrayAssignStmt) s;
            int[] arr = getArray(aas.array, env, self, s.line);
            int   idx = (Integer) eval(aas.index, env, self);
            int   val = (Integer) eval(aas.value, env, self);
            checkBounds(arr, idx, s.line);
            arr[idx] = val;
        }
    }

    // ── Expression evaluation ─────────────────────────────────────────────────
    private Object eval(AST.Expr e, Map<String, Object> env, MJObject self) {

        if (e instanceof AST.IntLit)  return ((AST.IntLit)  e).value;
        if (e instanceof AST.BoolLit) return ((AST.BoolLit) e).value;

        if (e instanceof AST.ThisExpr) {
            if (self == null)
                throw new CompilerException("'this' used outside object context at line " + e.line);
            return self;
        }

        if (e instanceof AST.IdentExpr) {
            String name = ((AST.IdentExpr) e).name;
            if (env.containsKey(name))  return env.get(name);
            if (self != null && self.fields.containsKey(name)) return self.fields.get(name);
            throw new CompilerException("Undefined variable '" + name + "' at line " + e.line);
        }

        if (e instanceof AST.NewObject) {
            String cls = ((AST.NewObject) e).className;
            AST.ClassDecl cd = classes.get(cls);
            if (cd == null) throw new CompilerException("Unknown class '" + cls + "'");
            MJObject obj = new MJObject(cls);
            // Initialize fields to defaults
            initFields(obj, cd);
            return obj;
        }

        if (e instanceof AST.NewIntArray) {
            int size = (Integer) eval(((AST.NewIntArray) e).size, env, self);
            if (size < 0) throw new CompilerException("Negative array size at line " + e.line);
            return new int[size];
        }

        if (e instanceof AST.ArrayAccess) {
            AST.ArrayAccess aa = (AST.ArrayAccess) e;
            Object arrObj = eval(aa.array, env, self);
            if (!(arrObj instanceof int[]))
                throw new CompilerException("Array access on non-array at line " + e.line);
            int[] arr = (int[]) arrObj;
            int   idx = (Integer) eval(aa.index, env, self);
            checkBounds(arr, idx, e.line);
            return arr[idx];
        }

        if (e instanceof AST.ArrayLength) {
            Object arrObj = eval(((AST.ArrayLength) e).array, env, self);
            if (!(arrObj instanceof int[]))
                throw new CompilerException("'.length' on non-array at line " + e.line);
            return ((int[]) arrObj).length;
        }

        if (e instanceof AST.NotExpr) {
            return !((Boolean) eval(((AST.NotExpr) e).expr, env, self));
        }

        if (e instanceof AST.BinOp) {
            return evalBinOp((AST.BinOp) e, env, self);
        }

        if (e instanceof AST.MethodCall) {
            return evalMethodCall((AST.MethodCall) e, env, self);
        }

        throw new CompilerException("Unknown expression node at line " + e.line);
    }

    private Object evalBinOp(AST.BinOp bo, Map<String, Object> env, MJObject self) {
        // Short-circuit AND
        if (bo.op == AST.BinOp.Op.AND) {
            boolean l = (Boolean) eval(bo.left, env, self);
            if (!l) return false;
            return (Boolean) eval(bo.right, env, self);
        }

        Object left  = eval(bo.left,  env, self);
        Object right = eval(bo.right, env, self);

        switch (bo.op) {
            case ADD: return (Integer) left + (Integer) right;
            case SUB: return (Integer) left - (Integer) right;
            case MUL: return (Integer) left * (Integer) right;
            case DIV:
                if ((Integer) right == 0)
                    throw new CompilerException("Division by zero at line " + bo.line);
                return (Integer) left / (Integer) right;
            case LT:  return (Integer) left < (Integer) right;
            case EQ:  return left.equals(right);
            default:  throw new CompilerException("Unknown operator at line " + bo.line);
        }
    }

    private Object evalMethodCall(AST.MethodCall mc, Map<String, Object> env, MJObject self) {
        Object objVal = eval(mc.object, env, self);
        if (!(objVal instanceof MJObject))
            throw new CompilerException("Method call on non-object at line " + mc.line);

        MJObject receiver = (MJObject) objVal;

        // Resolve method through class hierarchy
        AST.MethodDecl method = resolveMethod(receiver.className, mc.method, mc.line);

        // Build new environment for the method call
        Map<String, Object> callEnv = new LinkedHashMap<>();

        // Bind parameters
        List<AST.Param> params = method.params;
        if (params.size() != mc.args.size())
            throw new CompilerException("Wrong arg count for '" + mc.method
                                        + "' at line " + mc.line);
        for (int i = 0; i < params.size(); i++)
            callEnv.put(params.get(i).name, eval(mc.args.get(i), env, self));

        // Initialize locals to defaults
        for (AST.VarDecl vd : method.locals)
            callEnv.put(vd.name, defaultValue(vd.type));

        // Execute body
        for (AST.Statement s : method.body)
            execStmt(s, callEnv, receiver);

        // Evaluate return expression
        return eval(method.returnExpr, callEnv, receiver);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private AST.MethodDecl resolveMethod(String className, String methodName, int line) {
        String key = className + "." + methodName;
        if (methodMap.containsKey(key)) return methodMap.get(key);
        // Walk superclass chain
        AST.ClassDecl cd = classes.get(className);
        if (cd != null && cd.superClass != null)
            return resolveMethod(cd.superClass, methodName, line);
        throw new CompilerException("Method '" + methodName
                                    + "' not found in class '" + className
                                    + "' at line " + line);
    }

    private void initFields(MJObject obj, AST.ClassDecl cd) {
        // Init superclass fields first
        if (cd.superClass != null) {
            AST.ClassDecl superCd = classes.get(cd.superClass);
            if (superCd != null) initFields(obj, superCd);
        }
        for (AST.VarDecl vd : cd.fields)
            obj.fields.put(vd.name, defaultValue(vd.type));
    }

    private Object defaultValue(AST.Type t) {
        if (t instanceof AST.IntType)    return 0;
        if (t instanceof AST.BoolType)   return false;
        if (t instanceof AST.IntArrType) return new int[0];
        return null; // class type
    }

    private int[] getArray(String name, Map<String, Object> env, MJObject self, int line) {
        Object val = null;
        if (env.containsKey(name)) val = env.get(name);
        else if (self != null && self.fields.containsKey(name)) val = self.fields.get(name);
        if (!(val instanceof int[]))
            throw new CompilerException("'" + name + "' is not an int[] at line " + line);
        return (int[]) val;
    }

    private void checkBounds(int[] arr, int idx, int line) {
        if (idx < 0 || idx >= arr.length)
            throw new CompilerException("Array index " + idx + " out of bounds (length="
                                        + arr.length + ") at line " + line);
    }
}
