/**
 * Created by aschey on 9/24/16.
 */
package DPL;

import java.util.ArrayList;

class Lexeme {
    TokenType type;
    String str;
    Integer integer;
    Boolean bool;
    ArrayList<Lexeme> array;
    Lexeme left;
    Lexeme right;

    Lexeme(TokenType type) {
        this.type = type;
    }

    Lexeme(TokenType type, String str) {
        this.type = type;
        this.str = str;
    }

    Lexeme(TokenType type, Integer integer) {
        this.type = type;
        this.integer = integer;
    }

    Lexeme(TokenType type, Boolean bool) {
        this.type = type;
        this.bool = bool;
    }

    Lexeme(TokenType type, ArrayList<Lexeme> array) {
        this.type = type;
        this.array = array;
    }

    Lexeme(TokenType type, Object obj) {
        this.type = type;
        if (obj.getClass().equals(Integer.class)) {
            this.integer = (Integer)obj;
        }
        else if (obj.getClass().equals(Boolean.class)) {
            this.bool = (Boolean)obj;
        }
        else {
            this.str = obj.toString();
        }
    }

    Lexeme(TokenType type, Lexeme left, Lexeme right) {
        this.type = type;
        this.left = left;
        this.right = right;
    }

    Object getVal() {
        if (this.str != null) {
            return "\"" + this.str + "\"";
        }
        else if (this.integer != null) {
            return this.integer;
        }
        else if (this.bool != null) {
            return this.bool;
        }
        else if (this.array != null) {
            return this.array;
        }
        else {
            return this.type;
        }
    }

    static Lexeme cons(TokenType val, Lexeme left, Lexeme right) {
        return new Lexeme(val, left, right);
    }
}
