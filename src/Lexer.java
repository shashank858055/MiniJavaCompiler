import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String src;
    private int pos;
    private int line;
    private int col;

    public Lexer(String src) {
        this.src  = src;
        this.pos  = 0;
        this.line = 1;
        this.col  = 1;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < src.length()) {
            skipWhitespaceAndComments();
            if (pos >= src.length()) break;
            tokens.add(nextToken());
        }
        tokens.add(new Token(TokenType.EOF, "", line, col));
        return tokens;
    }

    private void skipWhitespaceAndComments() {
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (Character.isWhitespace(c)) {
                advance();
            } else if (pos + 1 < src.length() && c == '/' && src.charAt(pos+1) == '/') {
                while (pos < src.length() && src.charAt(pos) != '\n') advance();
            } else if (pos + 1 < src.length() && c == '/' && src.charAt(pos+1) == '*') {
                advance(); advance();
                while (pos + 1 < src.length() &&
                       !(src.charAt(pos) == '*' && src.charAt(pos+1) == '/')) advance();
                if (pos + 1 < src.length()) { advance(); advance(); }
            } else {
                break;
            }
        }
    }

    private char advance() {
        char c = src.charAt(pos++);
        if (c == '\n') { line++; col = 1; } else col++;
        return c;
    }

    private char peek() { return pos < src.length() ? src.charAt(pos) : '\0'; }

    private Token nextToken() {
        int sl = line, sc = col;
        char c = advance();

        switch (c) {
            case '(': return tok(TokenType.LPAREN,    "(", sl, sc);
            case ')': return tok(TokenType.RPAREN,    ")", sl, sc);
            case '{': return tok(TokenType.LBRACE,    "{", sl, sc);
            case '}': return tok(TokenType.RBRACE,    "}", sl, sc);
            case '[': return tok(TokenType.LBRACKET,  "[", sl, sc);
            case ']': return tok(TokenType.RBRACKET,  "]", sl, sc);
            case ';': return tok(TokenType.SEMICOLON, ";", sl, sc);
            case ',': return tok(TokenType.COMMA,     ",", sl, sc);
            case '.': return tok(TokenType.DOT,       ".", sl, sc);
            case '+': return tok(TokenType.PLUS,      "+", sl, sc);
            case '-': return tok(TokenType.MINUS,     "-", sl, sc);
            case '*': return tok(TokenType.TIMES,     "*", sl, sc);
            case '/': return tok(TokenType.DIV,       "/", sl, sc);
            case '!': return tok(TokenType.NOT,       "!", sl, sc);
            case '<': return tok(TokenType.LESS_THAN, "<", sl, sc);
            case '=':
                if (peek() == '=') { advance(); return tok(TokenType.EQUALS, "==", sl, sc); }
                return tok(TokenType.ASSIGN, "=", sl, sc);
            case '&':
                if (peek() == '&') { advance(); return tok(TokenType.AND, "&&", sl, sc); }
                throw new CompilerException("Unexpected '&' at line " + sl);
        }

        if (Character.isDigit(c)) {
            StringBuilder sb = new StringBuilder().append(c);
            while (pos < src.length() && Character.isDigit(peek())) sb.append(advance());
            return tok(TokenType.INTEGER_LITERAL, sb.toString(), sl, sc);
        }

        if (Character.isLetter(c) || c == '_') {
            StringBuilder sb = new StringBuilder().append(c);
            while (pos < src.length() && (Character.isLetterOrDigit(peek()) || peek() == '_'))
                sb.append(advance());
            String word = sb.toString();
            // Special: System.out.println as single token
            if (word.equals("System") && src.startsWith(".out.println", pos)) {
                for (int i = 0; i < ".out.println".length(); i++) advance();
                return tok(TokenType.SYSTEM_OUT_PRINTLN, "System.out.println", sl, sc);
            }
            return keyword(word, sl, sc);
        }

        throw new CompilerException("Lexer error at line " + sl + ", col " + sc
                                    + ": unexpected character '" + c + "'");
    }

    private Token tok(TokenType t, String v, int l, int c) { return new Token(t, v, l, c); }

    private Token keyword(String word, int line, int col) {
        switch (word) {
            case "class":   return tok(TokenType.CLASS,   word, line, col);
            case "public":  return tok(TokenType.PUBLIC,  word, line, col);
            case "static":  return tok(TokenType.STATIC,  word, line, col);
            case "void":    return tok(TokenType.VOID,    word, line, col);
            case "main":    return tok(TokenType.MAIN,    word, line, col);
            case "String":  return tok(TokenType.STRING,  word, line, col);
            case "extends": return tok(TokenType.EXTENDS, word, line, col);
            case "return":  return tok(TokenType.RETURN,  word, line, col);
            case "if":      return tok(TokenType.IF,      word, line, col);
            case "else":    return tok(TokenType.ELSE,    word, line, col);
            case "while":   return tok(TokenType.WHILE,   word, line, col);
            case "int":     return tok(TokenType.INT,     word, line, col);
            case "boolean": return tok(TokenType.BOOLEAN, word, line, col);
            case "new":     return tok(TokenType.NEW,     word, line, col);
            case "this":    return tok(TokenType.THIS,    word, line, col);
            case "length":  return tok(TokenType.LENGTH,  word, line, col);
            case "true":    return tok(TokenType.TRUE,    word, line, col);
            case "false":   return tok(TokenType.FALSE,   word, line, col);
            default:        return tok(TokenType.IDENTIFIER, word, line, col);
        }
    }
}
