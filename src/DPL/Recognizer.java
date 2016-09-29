/**
 * Created by aschey on 9/24/16.
 */
package DPL;
import static DPL.TokenType.*;

public class Recognizer {
    private Lexeme currentLexeme;
    private Lexer lexer;
    private TokenType[] binaryOperators = new TokenType[] { LT, GT, LEQ, GEQ, EQ, NEQ, PLUS, MINUS, STAR, SLASH };

    public static void main(String[] args) {
        Recognizer recognizer = new Recognizer();
        recognizer.functionDef();
    }

    public Recognizer() {
        this.lexer = new Lexer("test1.dpl");
        this.currentLexeme = this.lexer.lex();
    }

    private boolean checkMultiple(TokenType[] types) {
        for (TokenType type: types) {
            if (this.check(type)) {
                return true;
            }
        }
        return false;
    }

    private void matchMultiple(TokenType[] types) {
        for (TokenType type: types) {
            if (this.check(type)) {
                this.match(type);
                return;
            }
        }
        System.out.println("Syntax error");
        System.exit(1);
    }

    private boolean check(TokenType type) {
        return this.currentLexeme.type.equals(type);
    }

    private void advance() {
        this.currentLexeme = this.lexer.lex();
    }

    private void match(TokenType type) {
        this.matchNoAdvance(type);
        this.advance();
    }

    private void matchNoAdvance(TokenType type) {
        System.out.println(type + " " + this.currentLexeme.type + " " + this.currentLexeme.str);
        if (!this.check(type)) {
            System.out.println("Syntax error: Got " + this.currentLexeme.type + " expected " + type);
            System.exit(1);
        }
    }

    private void var() {
        this.match(VAR);
        this.match(VARIABLE);
        if (this.check(ASSIGN)) {
            this.match(ASSIGN);
            this.expression();
        }
        this.match(SEMICOLON);
    }

    private void functionDef() {
        this.match(DEF);
        this.match(VARIABLE);
        this.match(O_BRACKET);
        this.optParamList();
        this.match(C_BRACKET);
        this.block();
    }

    private void unary() {
        if (check(INTEGER)) {
            match(INTEGER);
        }
        else if (this.check(STRING)) {
            this.match(STRING);
        }
        else if (this.check(MINUS)) {
            this.match(MINUS);
            this.unary();
        }
        else if (this.check(O_BRACKET)) {
            this.match(O_BRACKET);
            this.expression();
            this.match(C_BRACKET);
        }
        else {
            this.varExpression();
        }
    }

    private void expression() {
        this.unary();
        if (this.binaryOperatorPending()) {
            this.binaryOperator();
            this.expression();
        }
    }

    private void conditionalOrLoopHeader() {
        this.match(O_BRACKET);
        this.expression();
        this.match(C_BRACKET);
    }

    private void ifStatement() {
        this.match(IF);
        this.conditionalOrLoopHeader();
        this.block();
        this.optElse();
    }

    private void block() {
        this.match(O_BRACKET);
        if (this.statementPending()) {
            this.statements();
        }
        this.match(C_BRACKET);
    }

    private void statements() {
        this.statement();
        if (this.statementPending()) {
            this.statements();
        }
    }

    private void statement() {
        if (this.ifStatementPending()) {
            this.ifStatement();
        }
        else if (this.whileLoopPending()) {
            this.whileLoop();
        }
        else if (this.forLoopPending()) {
            this.forLoop();
        }
        else if (this.varPending()) {
            this.var();
        }
        else {
            this.expression();
            this.match(SEMICOLON);
        }
    }

    private void whileLoop() {
        this.match(WHILE);
        this.conditionalOrLoopHeader();
        this.block();
    }

    private void forLoop() {
        this.match(FOR);
        this.match(O_BRACKET);
        this.list();
        this.match(C_BRACKET);
        this.block();
    }

    private void optElse() {
        if (this.check(ELSE)) {
            this.match(ELSE);
            if (this.ifStatementPending()) {
                this.ifStatement();
            }
            else {
                this.block();
            }
        }
    }

    private void binaryOperator() {
        this.matchMultiple(this.binaryOperators);
    }

//    private void binaryExpr() {
//        unary();
//        binaryOperator();
//        unary();
//    }

    private void varExpression() {
        this.match(VARIABLE);
        if (this.check(O_BRACKET)) {
            this.match(O_BRACKET);
            this.optParamList();
            this.match(C_BRACKET);
        }
    }

    private void optParamList() {
        if (this.listPending()) {
            this.list();
        }
    }

    private void list() {
        if (unaryPending()) {
            this.unary();
            this.list();
        }
    }

    private boolean binaryOperatorPending() {
        return this.checkMultiple(this.binaryOperators);
    }

    private boolean expressionPending() {
        return this.unaryPending();
    }

    private boolean unaryPending() {
        return this.checkMultiple(new TokenType[] {INTEGER, STRING, O_BRACKET, MINUS }) || this.varExpressionPending();
    }

//    private boolean binaryExprPending() {
//        return this.unaryPending();
//    }

    private boolean varExpressionPending() {
        return this.check(VARIABLE);
    }

    private boolean listPending() {
        return this.unaryPending();
    }

    private boolean ifStatementPending() {
        return this.check(IF);
    }

    private boolean whileLoopPending() {
        return this.check(WHILE);
    }

    private boolean forLoopPending() {
        return this.check(FOR);
    }

    private boolean statementPending() {
        return this.expressionPending() || this.ifStatementPending() || this.whileLoopPending() || this.forLoopPending() || this.varPending();
    }

    private boolean varPending() {
        return this.check(VAR);
    }
}
