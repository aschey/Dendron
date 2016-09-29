package DPL;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.HashMap;
import java.util.function.Predicate;

import static DPL.TokenType.*;

/**
 * Created by aschey on 9/24/16.
 */
public class Lexer {
    private PushbackInputStream reader;
    private boolean lineIsComment;
    private HashMap<String, DPL.TokenType> keywords;
    private HashMap<Character, DPL.TokenType> symbols;

    public static void main(String[] args) {
        Lexer lexer = new Lexer("test1.dpl");
        Lexeme token = lexer.lex();
        while (token.type != END_OF_INPUT) {
            System.out.println(token.type + " " + token.str + " " + token.integer);
            token = lexer.lex();
        }

    }

    public Lexer(String filename) {
        try {
            this.reader = new PushbackInputStream(new FileInputStream(filename));
        }
        catch (FileNotFoundException ex) {
            System.out.println("File not found");
        }
        this.lineIsComment = false;

        this.keywords = new HashMap<>();
        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("for", FOR);
        keywords.put("while", WHILE);
        keywords.put("var", VAR);
        keywords.put("def", DEF);

        this.symbols = new HashMap<>();
        symbols.put('[', O_BRACKET);
        symbols.put(']', C_BRACKET);
        symbols.put(',', COMMA);
        symbols.put(';', SEMICOLON);
        symbols.put('*', STAR);
        symbols.put('+', PLUS);
        symbols.put('-', MINUS);
        symbols.put(':', COLON);
        symbols.put('<', LT);
        symbols.put('>', GT);
        symbols.put('=', ASSIGN);
        symbols.put('/', SLASH);
    }

    private char read() {
        int readVal = -1;
        try {
            readVal = this.reader.read();
        }
        catch (IOException ex) {
            System.out.println("Error reading file");
            System.exit(1);
        }
        return (char)readVal;
    }

    private void putBack(char ch) {
        try {
            this.reader.unread(ch);
        }
        catch (IOException ex) {
            System.out.println("Error putting character back into file");
            System.exit(1);
        }
    }

    private boolean isEndOfFile(char ch) {
        return ch == (char)-1;
    }

    private String getTokenWithPredicate(Predicate<Character> predicate) {
        char ch;
        String token = "";

        ch = this.read();

        while(predicate.test(ch)) {
            token += ch;
            ch = this.read();
        }
        if (!this.isEndOfFile(ch)) {
            this.putBack(ch);
        }

        return token;
    }

    private void checkForComment(char ch) {
        if (ch == '`') {
            this.lineIsComment = true;
        }
        else if (this.isEndOfFile(ch) || ch == '\n') {
            this.lineIsComment = false;
        }
    }

    private boolean isComment() {
        return this.lineIsComment;
    }

    private void skipWhitespace() {
        // Get a token containing all leading whitespace
        // We don't need to do anything with it, so don't store it in a variable
        this.getTokenWithPredicate((Character ch) -> {
            this.checkForComment(ch);
            return (this.isComment() || Character.isWhitespace(ch));
        });
    }

    private Lexeme lexDigit() {
        String token = this.getTokenWithPredicate(Character::isDigit);
        return new Lexeme(INTEGER, Integer.parseInt(token));
    }

    private Lexeme lexString() {
        String token = this.getTokenWithPredicate((Character ch) -> ch != '\'');
        // Get rid of the string terminator that got added back onto the stream
        this.read();
        return new Lexeme(STRING, token);
    }

    private Lexeme lexVariableOrKeyword() {
        String token = this.getTokenWithPredicate((Character ch) -> Character.isAlphabetic(ch) || Character.isDigit(ch));
        if (!this.keywords.containsKey(token)) {
            return new Lexeme(VARIABLE, token);
        }

        return new Lexeme(this.keywords.get(token));
    }

    private Lexeme lexCompoundSymbol(Character ch) {
        char next = this.read();
        if (next == '=') {
            switch(ch) {
                case '>': return new Lexeme(GEQ);
                case '<': return new Lexeme(LEQ);
                case '=': return new Lexeme(EQ);
                case '!': return new Lexeme(NEQ);
            }
        }
        this.putBack(next);
        return new Lexeme(NONE);
    }

    public Lexeme lex() {
        this.skipWhitespace();

        char ch = this.read();

        if (this.isEndOfFile(ch)) {
            return new Lexeme(END_OF_INPUT);
        }

        if (this.symbols.containsKey(ch)) {
            if (ch == '>' || ch == '<' || ch == '=') {
                Lexeme symbol = this.lexCompoundSymbol(ch);
                if (symbol.type != NONE) {
                    return symbol;
                }
            }
            return new Lexeme(this.symbols.get(ch));
        }

        if (Character.isDigit(ch)) {
            this.putBack(ch);
            return lexDigit();
        }
        else if (Character.isAlphabetic(ch)) {
            this.putBack(ch);
            return lexVariableOrKeyword();
        }
        else if (ch == '\'') {
            return lexString();
        }

        return new Lexeme(UNKNOWN);
    }
}