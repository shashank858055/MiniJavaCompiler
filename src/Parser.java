import java.util.*;

/**
 * Recursive-descent parser for MiniJava.
 */
public class Parser {
    private final List<Token> tokens;
    private int pos;

    public Parser(List<Token> tokens) { this.tokens = tokens; this.pos = 0; }

    private Token peek()      { return tokens.get(pos); }
    private Token peek(int k) { return tokens.get(Math.min(pos+k, tokens.size()-1)); }
    private Token consume()   { return tokens.get(pos++); }
    private int   curLine()   { return peek().line; }

    private Token expect(TokenType t) {
        Token tok = consume();
        if (tok.type != t)
            throw new CompilerException("Parse error at line " + tok.line
                + ": expected " + t + " but got " + tok.type + " (\"" + tok.value + "\")");
        return tok;
    }

    private boolean check(TokenType t)     { return peek().type == t; }
    private boolean match(TokenType t)     { if (check(t)) { consume(); return true; } return false; }

    // ── Top level ─────────────────────────────────────────────────────────────
    public AST.Program parse() {
        AST.MainClass mc = parseMainClass();
        List<AST.ClassDecl> classes = new ArrayList<>();
        while (!check(TokenType.EOF)) classes.add(parseClassDecl());
        return new AST.Program(mc, classes);
    }

    // MainClass ::= class id { public static void main(String[] id) { Stmt* } }
    private AST.MainClass parseMainClass() {
        int l = curLine();
        expect(TokenType.CLASS);
        String name = expect(TokenType.IDENTIFIER).value;
        expect(TokenType.LBRACE);
        expect(TokenType.PUBLIC);
        expect(TokenType.STATIC);
        expect(TokenType.VOID);
        expect(TokenType.MAIN);
        expect(TokenType.LPAREN);
        expect(TokenType.STRING);
        expect(TokenType.LBRACKET);
        expect(TokenType.RBRACKET);
        expect(TokenType.IDENTIFIER);
        expect(TokenType.RPAREN);
        expect(TokenType.LBRACE);
        // Parse zero or more statements as a block
        List<AST.Statement> stmts = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !check(TokenType.EOF))
            stmts.add(parseStatement());
        expect(TokenType.RBRACE);
        expect(TokenType.RBRACE);
        AST.BlockStmt block = new AST.BlockStmt(stmts, l);
        return new AST.MainClass(name, block, l);
    }

    // ClassDecl ::= class id [extends id] { VarDecl* MethodDecl* }
    private AST.ClassDecl parseClassDecl() {
        int l = curLine();
        expect(TokenType.CLASS);
        String name = expect(TokenType.IDENTIFIER).value;
        String superClass = null;
        if (match(TokenType.EXTENDS)) superClass = expect(TokenType.IDENTIFIER).value;
        expect(TokenType.LBRACE);
        List<AST.VarDecl>    fields  = new ArrayList<>();
        List<AST.MethodDecl> methods = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            if (check(TokenType.PUBLIC)) methods.add(parseMethodDecl());
            else fields.add(parseVarDecl());
        }
        expect(TokenType.RBRACE);
        return new AST.ClassDecl(name, superClass, fields, methods, l);
    }

    // MethodDecl ::= public Type id ( Params ) { VarDecl* Stmt* return Expr ; }
    private AST.MethodDecl parseMethodDecl() {
        int l = curLine();
        expect(TokenType.PUBLIC);
        AST.Type retType = parseType();
        String name = expect(TokenType.IDENTIFIER).value;
        expect(TokenType.LPAREN);
        List<AST.Param> params = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            params.add(new AST.Param(parseType(), expect(TokenType.IDENTIFIER).value));
            while (match(TokenType.COMMA))
                params.add(new AST.Param(parseType(), expect(TokenType.IDENTIFIER).value));
        }
        expect(TokenType.RPAREN);
        expect(TokenType.LBRACE);
        List<AST.VarDecl>   locals = new ArrayList<>();
        List<AST.Statement> body   = new ArrayList<>();
        // var decls first
        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)
               && !check(TokenType.RETURN) && isVarDeclStart())
            locals.add(parseVarDecl());
        // statements until return
        while (!check(TokenType.RBRACE) && !check(TokenType.EOF) && !check(TokenType.RETURN))
            body.add(parseStatement());
        expect(TokenType.RETURN);
        AST.Expr ret = parseExpr();
        expect(TokenType.SEMICOLON);
        expect(TokenType.RBRACE);
        return new AST.MethodDecl(retType, name, params, locals, body, ret, l);
    }

    private boolean isVarDeclStart() {
        TokenType t = peek().type;
        if (t == TokenType.INT || t == TokenType.BOOLEAN) return true;
        if (t == TokenType.IDENTIFIER && peek(1).type == TokenType.IDENTIFIER) return true;
        return false;
    }

    private AST.VarDecl parseVarDecl() {
        int l = curLine();
        AST.Type type = parseType();
        String name = expect(TokenType.IDENTIFIER).value;
        expect(TokenType.SEMICOLON);
        return new AST.VarDecl(type, name, l);
    }

    private AST.Type parseType() {
        if (check(TokenType.INT)) {
            consume();
            if (match(TokenType.LBRACKET)) { expect(TokenType.RBRACKET); return new AST.IntArrType(); }
            return new AST.IntType();
        }
        if (match(TokenType.BOOLEAN)) return new AST.BoolType();
        return new AST.ClassType(expect(TokenType.IDENTIFIER).value);
    }

    // ── Statements ────────────────────────────────────────────────────────────
    private AST.Statement parseStatement() {
        int l = curLine();

        if (check(TokenType.LBRACE)) {
            consume();
            List<AST.Statement> stmts = new ArrayList<>();
            while (!check(TokenType.RBRACE) && !check(TokenType.EOF))
                stmts.add(parseStatement());
            expect(TokenType.RBRACE);
            return new AST.BlockStmt(stmts, l);
        }

        if (match(TokenType.IF)) {
            expect(TokenType.LPAREN);
            AST.Expr cond = parseExpr();
            expect(TokenType.RPAREN);
            AST.Statement thenB = parseStatement();
            AST.Statement elseB = null;
            if (match(TokenType.ELSE)) elseB = parseStatement();
            return new AST.IfStmt(cond, thenB, elseB, l);
        }

        if (match(TokenType.WHILE)) {
            expect(TokenType.LPAREN);
            AST.Expr cond = parseExpr();
            expect(TokenType.RPAREN);
            return new AST.WhileStmt(cond, parseStatement(), l);
        }

        if (match(TokenType.SYSTEM_OUT_PRINTLN)) {
            expect(TokenType.LPAREN);
            AST.Expr e = parseExpr();
            expect(TokenType.RPAREN);
            expect(TokenType.SEMICOLON);
            return new AST.PrintStmt(e, l);
        }

        if (check(TokenType.IDENTIFIER)) {
            String name = consume().value;
            if (match(TokenType.LBRACKET)) {
                AST.Expr idx = parseExpr();
                expect(TokenType.RBRACKET);
                expect(TokenType.ASSIGN);
                AST.Expr val = parseExpr();
                expect(TokenType.SEMICOLON);
                return new AST.ArrayAssignStmt(name, idx, val, l);
            }
            expect(TokenType.ASSIGN);
            AST.Expr val = parseExpr();
            expect(TokenType.SEMICOLON);
            return new AST.AssignStmt(name, val, l);
        }

        throw new CompilerException("Parse error at line " + l
            + ": unexpected token '" + peek().value + "' in statement");
    }

    // ── Expressions ──────────────────────────────────────────────────────────
    private AST.Expr parseExpr()      { return parseAnd(); }

    private AST.Expr parseAnd() {
        int l = curLine(); AST.Expr left = parseEquality();
        while (check(TokenType.AND)) {
            consume();
            left = new AST.BinOp(AST.BinOp.Op.AND, left, parseEquality(), l);
        }
        return left;
    }

    private AST.Expr parseEquality() {
        int l = curLine(); AST.Expr left = parseComparison();
        while (check(TokenType.EQUALS)) {
            consume();
            left = new AST.BinOp(AST.BinOp.Op.EQ, left, parseComparison(), l);
        }
        return left;
    }

    private AST.Expr parseComparison() {
        int l = curLine(); AST.Expr left = parseAddSub();
        while (check(TokenType.LESS_THAN)) {
            consume();
            left = new AST.BinOp(AST.BinOp.Op.LT, left, parseAddSub(), l);
        }
        return left;
    }

    private AST.Expr parseAddSub() {
        int l = curLine(); AST.Expr left = parseMulDiv();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            AST.BinOp.Op op = consume().type == TokenType.PLUS ? AST.BinOp.Op.ADD : AST.BinOp.Op.SUB;
            left = new AST.BinOp(op, left, parseMulDiv(), l);
        }
        return left;
    }

    private AST.Expr parseMulDiv() {
        int l = curLine(); AST.Expr left = parseUnary();
        while (check(TokenType.TIMES) || check(TokenType.DIV)) {
            AST.BinOp.Op op = consume().type == TokenType.TIMES ? AST.BinOp.Op.MUL : AST.BinOp.Op.DIV;
            left = new AST.BinOp(op, left, parseUnary(), l);
        }
        return left;
    }

    private AST.Expr parseUnary() {
        int l = curLine();
        if (match(TokenType.NOT)) return new AST.NotExpr(parseUnary(), l);
        return parsePostfix();
    }

    private AST.Expr parsePostfix() {
        AST.Expr expr = parsePrimary();
        int l = curLine();
        while (true) {
            if (check(TokenType.LBRACKET)) {
                consume();
                AST.Expr idx = parseExpr();
                expect(TokenType.RBRACKET);
                expr = new AST.ArrayAccess(expr, idx, l);
            } else if (check(TokenType.DOT)) {
                consume();
                if (match(TokenType.LENGTH)) {
                    expr = new AST.ArrayLength(expr, l);
                } else {
                    String method = expect(TokenType.IDENTIFIER).value;
                    expect(TokenType.LPAREN);
                    List<AST.Expr> args = new ArrayList<>();
                    if (!check(TokenType.RPAREN)) {
                        args.add(parseExpr());
                        while (match(TokenType.COMMA)) args.add(parseExpr());
                    }
                    expect(TokenType.RPAREN);
                    expr = new AST.MethodCall(expr, method, args, l);
                }
            } else break;
        }
        return expr;
    }

    private AST.Expr parsePrimary() {
        int l = curLine();
        Token t = peek();

        if (match(TokenType.INTEGER_LITERAL)) return new AST.IntLit(Integer.parseInt(t.value), l);
        if (match(TokenType.TRUE))  return new AST.BoolLit(true,  l);
        if (match(TokenType.FALSE)) return new AST.BoolLit(false, l);
        if (match(TokenType.THIS))  return new AST.ThisExpr(l);

        if (match(TokenType.NEW)) {
            if (check(TokenType.INT)) {
                consume();
                expect(TokenType.LBRACKET);
                AST.Expr size = parseExpr();
                expect(TokenType.RBRACKET);
                return new AST.NewIntArray(size, l);
            }
            String cls = expect(TokenType.IDENTIFIER).value;
            expect(TokenType.LPAREN);
            expect(TokenType.RPAREN);
            return new AST.NewObject(cls, l);
        }

        if (match(TokenType.LPAREN)) {
            AST.Expr e = parseExpr();
            expect(TokenType.RPAREN);
            return e;
        }

        if (check(TokenType.IDENTIFIER)) return new AST.IdentExpr(consume().value, l);

        throw new CompilerException("Parse error at line " + l
            + ": unexpected token '" + t.value + "' in expression");
    }
}
