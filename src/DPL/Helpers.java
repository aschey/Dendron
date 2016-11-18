package DPL;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import static DPL.TokenType.*;

/**
 * Created by aschey on 10/18/16.
 */
public class Helpers {

    static ArrayList<String> keywords = new ArrayList<>(Arrays.asList("if", "else", "for", "while", "var", "def", "true",
        "false", "return", "lambda", "obj", "this", "null", "and", "or", "import", "in"));

    static HashMap<Character, TokenType> symbols = mapInitialize('[', O_BRACKET, ']', C_BRACKET,',', COMMA, ';', SEMICOLON, '*', STAR, '+',
    PLUS, '-', MINUS, ':', COLON, '<', LT, '>', GT, '!', NOT, '=', ASSIGN, '/', SLASH, '^', CARAT, '%', REMAINDER, '.', DOT, '#', HASH);

    //private static HashMap<TokenType, String> binaryOpMappings = mapInitialize(
    //    LT, "<", GT, ">", LEQ, "<=", GEQ, ">=", EQ, "==", NEQ, "!=", PLUS, "+", MINUS, "-", STAR, "*", CARAT, '^', SLASH, "/", REMAINDER, '%', AND, "and", OR, "or"
    //);

    //static TokenType[] binaryOperators = binaryOpMappings.keySet().toArray(new TokenType[0]);
    static TokenType[] binaryOperators = { LT, GT, LEQ, GEQ, EQ, NEQ, PLUS, MINUS, STAR, CARAT, SLASH, REMAINDER, AND, OR };

    static TokenType[] mathOperators = new TokenType[] { PLUS, MINUS, STAR, SLASH, CARAT, REMAINDER };

    static TokenType[] intsRequired = new TokenType[] { MINUS, STAR, SLASH, CARAT, REMAINDER };

    static TokenType[] unaries = new TokenType[] { INTEGER, STRING, BOOLEAN, O_BRACKET, MINUS, LAMBDA, DOT, NULL, NOT, OBJ, VARIABLE, HASH };

    static <T1, T2> HashMap<T1, T2> mapInitialize(Object... args) {
        HashMap<T1, T2> result = new HashMap<>();
        for (int i = 0; i < args.length - 1; i += 2) {
            result.put((T1) args[i], (T2) args[i + 1]);
        }

        return result;
    }

    static void exitWithError(String error, int errorCode) {
        System.out.println("Error: " + error);
        System.exit(errorCode);
    }

    static void exitIfNotStringOrArray(String funcName) {
        exitWithError(funcName + " can only be called on strings and arrays");
    }

    static void exitWithError(String error) {
        exitWithError(error, 1);
    }

    static TokenType stringToTokenType(String tokenString) {
        return TokenType.valueOf(tokenString.toUpperCase());
    }

    static Lexeme listIndex(Lexeme list, int index) {
        for (int i = 0; i < index; i++) {
            list = list.right;
        }
        return list.left;
    }

    static Lexeme optionalListIndex(Lexeme list, int index) {
        try {
            return listIndex(list, index);
        }
        catch (NullPointerException ex) {
            return null;
        }
    }

    static int listLength(Lexeme list) {
        int length = 0;
        while (list != null) {
            list = list.right;
            length++;
        }

        return length;
    }

    static boolean contains(TokenType[] search, Lexeme val) {
        return Arrays.stream(search).anyMatch(t -> t.equals(val.type));
    }

    static Lexeme getFunction(Lexeme closure) {
        if (closure.right.type.equals(LAMBDA)) {
            return closure.right;
        }
        else {
            return closure.right.right;
        }
    }

    static Object getValWithDefault(Lexeme pt, String defaultVal) {
        try {
            return pt.getVal();
        }
        catch (NullPointerException ex) {
            return defaultVal;
        }
    }

    static Object getPrintValWithDefault(Lexeme pt, String defaultVal) {
        Object val = getValWithDefault(pt, defaultVal);
        if (val == null) {
            return null;
        }
        if (val.getClass() == ArrayList.class) {
            String retVal = "[ ";
            ArrayList<Lexeme> arrayVal = (ArrayList<Lexeme>)val;
            for (Lexeme l : arrayVal) {
                if (l.getVal().getClass() == ArrayList.class) {
                    retVal += (getPrintValWithDefault(l, defaultVal) + " ");
                }
                else {
                    retVal += (l.getVal() + " ");
                }
            }
            retVal += "]";
            return retVal;
        }
        return val;
    }

    static TokenType getTypeIfExists(Lexeme pt) {
        try {
            return pt.type;
        }
        catch (NullPointerException ex) {
            return null;
        }
    }

    static String removeStringRange(String str, int start, int end) {
        return str.substring(0, start) + str.substring(end, str.length());
    }

    static String insertStringRange(String str, String newStr, int start) {
        return str.substring(0, start) + newStr + str.substring(start, str.length());
    }
}
