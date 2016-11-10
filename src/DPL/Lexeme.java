/**
 * Created by aschey on 9/24/16.
 */
package DPL;

class Lexeme {
    TokenType type;
    String str;
    Integer integer;
    Double real;
    Lexeme left;
    Lexeme right;

    Lexeme(TokenType type) {
        this.type = type;
    }

    Lexeme(TokenType type, String str) {
        this.type = type;
        this.str = str;
    }

    Lexeme(TokenType type, int integer) {
        this.type = type;
        this.integer = integer;
    }

    Lexeme(TokenType type, Lexeme left, Lexeme right) {
        this.type = type;
        this.left = left;
        this.right = right;
    }

    String getVal() {
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

    static Lexeme cons(TokenType val, Lexeme left, Lexeme right) {
        return new Lexeme(val, left, right);
    }

    //Lexeme car() {
    //    return this.left;
    //}

    //Lexeme cdr() {
    //    return this.right;
    //}

    //void setCar(Lexeme l) {
    //    this.left = l;
    //}

    //void setCdr(Lexeme l) {
     //   this.right = l;
    //}
}
