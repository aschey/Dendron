/**
 * Created by aschey on 9/24/16.
 */
package DPL;

class Lexeme {
    public TokenType type;
    public String str;
    public Integer integer;
    public Double real;
    public Lexeme left;
    public Lexeme right;

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

    public String getVal() {
        if (this.str != null) {
            return "\"" + this.str + "\"";
        }
        else if (this.integer != null) {
            return this.integer.toString();
        }
        else if (this.real != null) {
            return this.real.toString();
        }
        else {
            return this.type.toString();
        }
    }
}
