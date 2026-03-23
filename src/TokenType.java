public enum TokenType {
    // Literals
    INTEGER_LITERAL, BOOLEAN_LITERAL, NULL_LITERAL,

    // Identifiers & Keywords
    IDENTIFIER,
    CLASS, PUBLIC, STATIC, VOID, MAIN, STRING,
    EXTENDS, RETURN, IF, ELSE, WHILE,
    INT, BOOLEAN, NEW, THIS, LENGTH,
    TRUE, FALSE,
    SYSTEM_OUT_PRINTLN,

    // Operators
    PLUS, MINUS, TIMES, DIV,
    AND, LESS_THAN,
    NOT,
    ASSIGN,
    EQUALS,

    // Delimiters
    LPAREN, RPAREN,
    LBRACE, RBRACE,
    LBRACKET, RBRACKET,
    SEMICOLON, COMMA, DOT,

    // Special
    EOF
}
