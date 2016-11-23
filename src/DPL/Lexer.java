package DPL;

import java.io.*;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

import static DPL.TokenType.*;

/**
 * Created by aschey on 9/24/16.
 */
public class Lexer {
    private PushbackInputStream reader;
    private boolean lineIsComment;
    private int oBracketCount;
    private int cBracketCount;

    public Lexer(String input, InputType inputType) {
        // Set the amount of tokens this program can put back onto the input stream
        final int BUFFER_SIZE = 1000;
        try {
            switch (inputType) {
                case FILE:
                    this.reader = new PushbackInputStream(new FileInputStream(input), BUFFER_SIZE);
                    break;
                case STDIN:
                    this.reader = new PushbackInputStream(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), BUFFER_SIZE);
            }
        }
        catch (FileNotFoundException ex) {
            System.out.println("File not found");
            System.exit(1);
        }
        this.lineIsComment = false;
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

    private void putBackMultiple(String s) {
        for (int i = s.length() - 1; i >= 0; i--) {
            this.putBack(s.charAt(i));
        }
    }

    private boolean isEndOfFile(char ch) {
        return ch == (char)(-1);
    }

    private String getTokenWithPredicate(Predicate<Character> predicate) {
        char ch;
        String token = "";

        ch = this.read();

        while (predicate.test(ch) && !this.isEndOfFile(ch)) {
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

    private boolean funcBodyComplete(Character ch) {
        if (this.isEndOfFile(ch)) {
            return false;
        }
        if (ch == '[') {
            this.oBracketCount++;
        }
        else if (ch == ']') {
            this.cBracketCount++;
        }
        if (this.oBracketCount == this.cBracketCount) {
            this.oBracketCount = 0;
            this.cBracketCount = 0;
            return false;
        }
        return true;
    }

    private Lexeme lexVariableOrKeyword() {
        String token = this.getTokenWithPredicate((Character ch) -> Character.isAlphabetic(ch) || Character.isDigit(ch) || ch == '_');
        if (token.equals("inspect")) {
            String inspectVal = this.getTokenWithPredicate(this::funcBodyComplete);
            Lexeme inspectLexeme = new Lexeme(VARIABLE, token);
            // Get rid of "["
            inspectLexeme.inspectVal = inspectVal.substring(1);
            // Put the value back so it can be lexed
            this.putBackMultiple(inspectVal);
            return inspectLexeme;
        }
        if (!Helpers.keywords.contains(token)) {
            return new Lexeme(VARIABLE, token);
        }
        else if (token.equals("true") || token.equals("false")) {
            return new Lexeme(BOOLEAN, Boolean.parseBoolean(token));
        }

        return new Lexeme(Helpers.stringToTokenType(token));
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

        boolean negative = false;

        if (this.isEndOfFile(ch)) {
            return new Lexeme(END_OF_INPUT);
        }

        if (ch == '-') {
            char next = this.read();
            if (!Character.isWhitespace(next)) {
               negative = true;
                ch = next;
            }
            else {
                this.putBack(next);
            }
        }

        if (Helpers.symbols.containsKey(ch)) {
            if (ch == '>' || ch == '<' || ch == '=' || ch == '!') {
                Lexeme symbol = this.lexCompoundSymbol(ch);
                if (symbol.type != NONE) {
                    return symbol;
                }
            }

            return new Lexeme(Helpers.symbols.get(ch));
        }

        if (Character.isDigit(ch)) {
            this.putBack(ch);
            Lexeme result = this.lexDigit();
            result.negative = negative;
            return result;
        }
        else if (Character.isAlphabetic(ch) || ch == '_') {
            this.putBack(ch);
            Lexeme result = this.lexVariableOrKeyword();
            result.negative = negative;
            return result;
        }
        else if (ch == '\'') {
            return this.lexString();
        }

        return new Lexeme(UNKNOWN);
    }
}