import java.util.*;

/**
 * Type checker / elaborator for MiniJava.
 * Builds a symbol table and verifies type correctness.
 */
public class TypeChecker {

    // Class info: fields and methods
    private final Map<String, ClassInfo> classTable = new LinkedHashMap<>();

    static class ClassInfo {
        String superClass;
        Map<String, AST.Type> fields   = new LinkedHashMap<>();
        Map<String, MethodInfo> methods = new LinkedHashMap<>();
    }

    static class MethodInfo {
        AST.Type returnType;
        List<AST.Param> params;
        Map<String, AST.Type> locals = new LinkedHashMap<>();
    }

    // ── First pass: build class table ────────────────────────────────────────
    public void check(AST.Program prog) {
        buildClassTable(prog);
        checkMainClass(prog.mainClass);
        for (AST.ClassDecl cd : prog.classes) checkClass(cd);
    }

    private void buildClassTable(AST.Program prog) {
        for (AST.ClassDecl cd : prog.classes) {
            if (classTable.containsKey(cd.name))
                error("Duplicate class: " + cd.name, cd.line);
            ClassInfo ci = new ClassInfo();
            ci.superClass = cd.superClass;
            for (AST.VarDecl vd : cd.fields) ci.fields.put(vd.name, vd.type);
            for (AST.MethodDecl md : cd.methods) {
                MethodInfo mi = new MethodInfo();
                mi.returnType = md.returnType;
                mi.params = md.params;
                for (AST.VarDecl vd : md.locals) mi.locals.put(vd.name, vd.type);
                ci.methods.put(md.name, mi);
            }
            classTable.put(cd.name, ci);
        }
    }

    // ── Check main class body ─────────────────────────────────────────────────
    private void checkMainClass(AST.MainClass mc) {
        checkStmt(mc.body, null, null);
    }

    // ── Check class ───────────────────────────────────────────────────────────
    private void checkClass(AST.ClassDecl cd) {
        ClassInfo ci = classTable.get(cd.name);
        for (AST.MethodDecl md : cd.methods) {
            checkMethod(md, ci);
        }
    }

    private void checkMethod(AST.MethodDecl md, ClassInfo ci) {
        MethodInfo mi = ci.methods.get(md.name);
        // Build combined scope: params + locals + fields
        Map<String, AST.Type> scope = new LinkedHashMap<>();
        scope.putAll(ci.fields);
        for (AST.Param p : md.params)  scope.put(p.name, p.type);
        for (AST.VarDecl v : md.locals) scope.put(v.name, v.type);

        for (AST.Statement s : md.body) checkStmt(s, scope, mi);
        AST.Type retT = typeOfExpr(md.returnExpr, scope, mi);
        if (!typesMatch(retT, md.returnType))
            error("Return type mismatch in method '" + md.name + "': expected "
                  + md.returnType + " got " + retT, md.line);
    }

    // ── Statement type checking ───────────────────────────────────────────────
    private void checkStmt(AST.Statement s, Map<String, AST.Type> scope, MethodInfo mi) {
        if (s instanceof AST.BlockStmt) {
            for (AST.Statement child : ((AST.BlockStmt) s).stmts) checkStmt(child, scope, mi);
        } else if (s instanceof AST.IfStmt) {
            AST.IfStmt is = (AST.IfStmt) s;
            AST.Type ct = typeOfExpr(is.cond, scope, mi);
            if (!(ct instanceof AST.BoolType))
                error("If condition must be boolean at line " + s.line, s.line);
            checkStmt(is.thenBranch, scope, mi);
            if (is.elseBranch != null) checkStmt(is.elseBranch, scope, mi);
        } else if (s instanceof AST.WhileStmt) {
            AST.WhileStmt ws = (AST.WhileStmt) s;
            AST.Type ct = typeOfExpr(ws.cond, scope, mi);
            if (!(ct instanceof AST.BoolType))
                error("While condition must be boolean at line " + s.line, s.line);
            checkStmt(ws.body, scope, mi);
        } else if (s instanceof AST.PrintStmt) {
            typeOfExpr(((AST.PrintStmt) s).expr, scope, mi);
        } else if (s instanceof AST.AssignStmt) {
            AST.AssignStmt as = (AST.AssignStmt) s;
            AST.Type declared = lookupVar(as.var, scope, s.line);
            AST.Type actual   = typeOfExpr(as.expr, scope, mi);
            if (!typesMatch(actual, declared))
                error("Type mismatch in assignment to '" + as.var + "' at line " + s.line, s.line);
        } else if (s instanceof AST.ArrayAssignStmt) {
            AST.ArrayAssignStmt aas = (AST.ArrayAssignStmt) s;
            AST.Type arrType = lookupVar(aas.array, scope, s.line);
            if (!(arrType instanceof AST.IntArrType))
                error("'" + aas.array + "' is not an int[] at line " + s.line, s.line);
            if (!(typeOfExpr(aas.index, scope, mi) instanceof AST.IntType))
                error("Array index must be int at line " + s.line, s.line);
            if (!(typeOfExpr(aas.value, scope, mi) instanceof AST.IntType))
                error("Array value must be int at line " + s.line, s.line);
        }
    }

    // ── Expression type inference ─────────────────────────────────────────────
    private AST.Type typeOfExpr(AST.Expr e, Map<String, AST.Type> scope, MethodInfo mi) {
        if (e instanceof AST.IntLit)  return new AST.IntType();
        if (e instanceof AST.BoolLit) return new AST.BoolType();
        if (e instanceof AST.ThisExpr) {
            // Find the class this method belongs to
            if (mi != null) {
                for (Map.Entry<String, ClassInfo> entry : classTable.entrySet())
                    if (entry.getValue().methods.containsValue(mi))
                        return new AST.ClassType(entry.getKey());
            }
            return new AST.ClassType("?");
        }
        if (e instanceof AST.IdentExpr) {
            AST.IdentExpr ie = (AST.IdentExpr) e;
            if (scope == null) error("Undeclared variable '" + ie.name + "' at line " + e.line, e.line);
            return lookupVar(ie.name, scope, e.line);
        }
        if (e instanceof AST.NewIntArray) {
            if (!(typeOfExpr(((AST.NewIntArray)e).size, scope, mi) instanceof AST.IntType))
                error("Array size must be int at line " + e.line, e.line);
            return new AST.IntArrType();
        }
        if (e instanceof AST.NewObject) {
            String cls = ((AST.NewObject) e).className;
            if (!classTable.containsKey(cls))
                error("Unknown class '" + cls + "' at line " + e.line, e.line);
            return new AST.ClassType(cls);
        }
        if (e instanceof AST.ArrayLength) {
            AST.Type t = typeOfExpr(((AST.ArrayLength)e).array, scope, mi);
            if (!(t instanceof AST.IntArrType))
                error("'.length' applied to non-array at line " + e.line, e.line);
            return new AST.IntType();
        }
        if (e instanceof AST.ArrayAccess) {
            AST.ArrayAccess aa = (AST.ArrayAccess) e;
            AST.Type arrT = typeOfExpr(aa.array, scope, mi);
            if (!(arrT instanceof AST.IntArrType))
                error("Array access on non-array at line " + e.line, e.line);
            if (!(typeOfExpr(aa.index, scope, mi) instanceof AST.IntType))
                error("Array index must be int at line " + e.line, e.line);
            return new AST.IntType();
        }
        if (e instanceof AST.NotExpr) {
            AST.Type t = typeOfExpr(((AST.NotExpr)e).expr, scope, mi);
            if (!(t instanceof AST.BoolType))
                error("'!' applied to non-boolean at line " + e.line, e.line);
            return new AST.BoolType();
        }
        if (e instanceof AST.BinOp) {
            AST.BinOp bo = (AST.BinOp) e;
            AST.Type l = typeOfExpr(bo.left,  scope, mi);
            AST.Type r = typeOfExpr(bo.right, scope, mi);
            switch (bo.op) {
                case ADD: case SUB: case MUL: case DIV:
                    if (!(l instanceof AST.IntType) || !(r instanceof AST.IntType))
                        error("Arithmetic requires int operands at line " + e.line, e.line);
                    return new AST.IntType();
                case LT:
                    if (!(l instanceof AST.IntType) || !(r instanceof AST.IntType))
                        error("'<' requires int operands at line " + e.line, e.line);
                    return new AST.BoolType();
                case EQ:
                    return new AST.BoolType();
                case AND:
                    if (!(l instanceof AST.BoolType) || !(r instanceof AST.BoolType))
                        error("'&&' requires boolean operands at line " + e.line, e.line);
                    return new AST.BoolType();
            }
        }
        if (e instanceof AST.MethodCall) {
            AST.MethodCall mc = (AST.MethodCall) e;
            AST.Type objType = typeOfExpr(mc.object, scope, mi);
            if (!(objType instanceof AST.ClassType))
                error("Method call on non-object at line " + e.line, e.line);
            String className = ((AST.ClassType) objType).name;
            MethodInfo mInfo = lookupMethod(className, mc.method, e.line);
            if (mInfo.params.size() != mc.args.size())
                error("Wrong number of arguments in call to '" + mc.method
                      + "' at line " + e.line, e.line);
            return mInfo.returnType;
        }
        throw new CompilerException("Unknown expression type at line " + e.line);
    }

    // ── Lookup helpers ────────────────────────────────────────────────────────
    private AST.Type lookupVar(String name, Map<String, AST.Type> scope, int line) {
        if (scope != null && scope.containsKey(name)) return scope.get(name);
        error("Undeclared variable '" + name + "' at line " + line, line);
        return null;
    }

    private MethodInfo lookupMethod(String className, String methodName, int line) {
        ClassInfo ci = classTable.get(className);
        if (ci == null)
            error("Unknown class '" + className + "' at line " + line, line);
        if (ci.methods.containsKey(methodName))
            return ci.methods.get(methodName);
        // Check superclass chain
        if (ci.superClass != null)
            return lookupMethod(ci.superClass, methodName, line);
        error("Method '" + methodName + "' not found in class '" + className + "' at line " + line, line);
        return null;
    }

    private boolean typesMatch(AST.Type actual, AST.Type expected) {
        if (actual == null || expected == null) return false;
        if (actual.getClass() == expected.getClass()) {
            if (actual instanceof AST.ClassType)
                return ((AST.ClassType)actual).name.equals(((AST.ClassType)expected).name)
                       || isSubtype((AST.ClassType)actual, (AST.ClassType)expected);
            return true;
        }
        return false;
    }

    private boolean isSubtype(AST.ClassType sub, AST.ClassType sup) {
        String cur = sub.name;
        while (cur != null) {
            if (cur.equals(sup.name)) return true;
            ClassInfo ci = classTable.get(cur);
            cur = (ci != null) ? ci.superClass : null;
        }
        return false;
    }

    private void error(String msg, int line) {
        throw new CompilerException("Type error at line " + line + ": " + msg);
    }
}
