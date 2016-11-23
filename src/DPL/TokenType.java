package DPL;

/**
 * TokenType
 * Different types Lexemes can have
 */

enum TokenType {
    O_BRACKET,
    C_BRACKET,
    COMMA,
    SEMICOLON,
    PLUS,
    MINUS,
    NEGATIVE,
    COLON,
    ASSIGN,
    STAR,
    SLASH,
    CARAT,
    REMAINDER,
    HASH,
    DOT,
    LT,
    GT,
    LEQ,
    GEQ,
    EQ,
    NOT,
    NEQ,
    AND,
    OR,
    VAR,
    DEF,
    IN,
    VARIABLE,
    INTEGER,
    STRING,
    BOOLEAN,
    NULL,
    IF,
    ELSE,
    FOR,
    WHILE,
    RETURN,
    BINARY,
    FUNC_CALL,
    GLUE,
    STATEMENT,
    GROUPING,
    LIST,
    UNKNOWN,
    NONE,
    END_OF_INPUT,
    ENV,
    TABLE,
    CLOSURE,
    LAMBDA,
    ARRAY_DEF,
    ARRAY_ACCESS,
    ARRAY,
    OBJ,
    THIS,
    PROPERTY,
    IMPORT,
    INVOKE,
    BUILTIN
}
