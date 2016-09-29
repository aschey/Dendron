/**
 * Created by aschey on 9/24/16.
 */
package DPL;

class Lexeme {
    public TokenType type;
    public String str;
    public int integer;
    public double real;

    public Lexeme(TokenType type) {
        this.type = type;
    }

    public Lexeme(TokenType type, String str) {
        this.type = type;
        this.str = str;
    }

    public Lexeme(TokenType type, int integer) {
        this.type = type;
        this.integer = integer;
    }
}
