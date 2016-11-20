/**
 * Created by aschey on 9/24/16.
 */
package DPL;

import static DPL.TokenType.*;
import java.util.ArrayList;

public class Recognizer {
    private Lexeme currentLexeme;
    private Lexer lexer;

    public static void main(String[] args) {
        //Recognizer recognizer = new Recognizer("func.dpl");
        //Lexeme l = recognizer.statements();
        //recognizer.traverse(l);
        //GraphWriter.quickGraph(l, "func");
        //System.out.println(l.right.left.right.left.type);
        //recognizer.prettyPrint(l);
    }

    public Recognizer(String input, InputType inputType) {
        this.lexer = new Lexer(input, inputType);
        this.currentLexeme = this.lexer.lex();
    }

    Lexeme recognize() {
        return this.statements();
    }

    private boolean checkMultiple(ArrayList<TokenType> types) {
        return types.contains(this.currentLexeme.type);
    }

    private Lexeme matchMultiple(ArrayList<TokenType> types) {
        if (types.contains(this.currentLexeme.type)) {
            return this.advance();
        }
//        for (TokenType type : types) {
//            if (this.check(type)) {
//                return this.match(type);
//            }
//        }
        System.out.println("Syntax error");
        System.exit(1);
        return null;
    }

    private boolean check(TokenType type) {
        return this.currentLexeme.type == type;
    }

    private Lexeme advance() {
        //System.out.println(this.currentLexeme.type + " " + this.currentLexeme.getVal());
        Lexeme current = this.currentLexeme;
        this.currentLexeme = this.lexer.lex();
        return current;
    }

    private Lexeme match(TokenType type) {
        this.matchNoAdvance(type);
        return this.advance();
    }

    private void matchNoAdvance(TokenType type) {
        if (!this.check(type)) {
            try {
                System.out.println("Syntax error: Got " + this.currentLexeme.type + " expected " + type);
                throw new Exception();
            }
            catch(Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
    }

    private Lexeme var() {
        Lexeme tree = this.match(VAR);
        tree.left = this.match(VARIABLE);
        if (this.check(ASSIGN)) {
            this.advance();
            tree.right = this.expression();
        }
        this.match(SEMICOLON);

        return tree;
    }

    private Lexeme functionDef() {
        Lexeme tree = this.match(DEF);
        tree.left = this.match(VARIABLE);
        tree.right = new Lexeme(GLUE);
        this.match(O_BRACKET);
        tree.right.left = this.optParamList();
        this.match(C_BRACKET);
        tree.right.right = this.block();

        return tree;
    }

    private Lexeme unary() {
        Lexeme tree;
        if (this.checkMultiple(Helpers.selfEvaluating)) {
            tree = this.advance();
        }
//        else if (this.check(MINUS)) {
//            tree = new Lexeme(NEGATIVE);
//            this.advance();
//            tree.right = this.unary();
//        }
        else if (this.check(NOT)) {
            tree = new Lexeme(NOT);
            this.advance();
            tree.right = this.unary();
        }
        else if (this.check(O_BRACKET)) {
            this.advance();
            tree = new Lexeme(GROUPING);
            tree.right = this.expression();
            this.match(C_BRACKET);
            if (this.check(DOT)) {
                this.match(DOT);
                this.match(INVOKE);
                Lexeme temp = new Lexeme(FUNC_CALL);
                temp.left = tree;
                this.match(O_BRACKET);
                temp.right = this.optParamList();
                this.match(C_BRACKET);
                tree = temp;
            }
        }
        else if (this.check(LAMBDA)) {
            tree = this.lambda();
        }
        else if (this.arrayPending()) {
            tree = this.array();
        }
        else if (this.objPending()) {
            tree = this.obj();
        }
        else {
            tree = this.varExpression();
        }

        if (this.propertyPending()) {
            tree = this.property(tree);
        }

        return tree;
    }

    private Lexeme expression() {
        Lexeme tree = this.unary();
        //Lexeme unary = this.unary();
        while (this.binaryOperatorPending()) {
            //Lexeme tree = new Lexeme(BINARY);
            Lexeme temp = this.binaryOperator();
            temp.right.left = tree;
            temp.right.right = this.unary();
            tree = temp;
        }
//        if (this.binaryOperatorPending()) {
//            Lexeme tree = new Lexeme(BINARY);
//            tree.left = this.binaryOperator();
//            tree.right = new Lexeme(GLUE);
//            tree.right.left = unary;
//            tree.right.right = this.expression();
//            return tree;
//        }
//        return unary;
        return tree;
    }

    private Lexeme conditionalOrLoopHeader() {
        this.match(O_BRACKET);
        Lexeme tree = this.expression();
        this.match(C_BRACKET);

        return tree;
    }

    private Lexeme ifStatement() {
        Lexeme tree = this.match(IF);
        tree.left = this.conditionalOrLoopHeader();
        tree.right = new Lexeme(GLUE);
        tree.right.left = this.block();
        tree.right.right = this.optElse();

        return tree;
    }

    private Lexeme returnVal() {
        Lexeme tree = this.match(RETURN);
        if (this.check(THIS)) {
            tree.right = this.advance();
        }
        else if (this.expressionPending()) {
            tree.right = this.expression();
        }
        this.match(SEMICOLON);

        return tree;
    }

    private Lexeme block() {
        Lexeme tree = null;
        this.match(O_BRACKET);
        if (this.statementPending()) {
            tree = this.statements();
        }
        this.match(C_BRACKET);

        return tree;
    }

    private Lexeme statements() {
        Lexeme tree = new Lexeme(STATEMENT);
        tree.left = this.statement();

        if (this.statementPending()) {
            tree.right = this.statements();
        }

        return tree;
    }

    private Lexeme statement() {
        if (this.ifStatementPending()) {
            return this.ifStatement();
        }
        if (this.whileLoopPending()) {
            return this.whileLoop();
        }
        if (this.forLoopPending()) {
            return this.forLoop();
        }
        if (this.varPending()) {
            return this.var();
        }

        if (this.functionDefPending()) {
            return this.functionDef();
        }

        if (this.returnPending()) {
            return this.returnVal();
        }

        if (this.importPending()) {
            return this.importFile();
        }

        Lexeme tree = this.expression();
        this.match(SEMICOLON);

        return tree;
    }

    private Lexeme whileLoop() {
        Lexeme tree = this.match(WHILE);
        tree.left = this.conditionalOrLoopHeader();
        tree.right = this.block();

        return tree;
    }

    private Lexeme forLoop() {
        Lexeme tree = this.match(FOR);
        this.match(O_BRACKET);
        tree.left = this.forLoopSig();
        this.match(C_BRACKET);
        tree.right = this.block();

        return tree;
    }

    private Lexeme forLoopSig() {
        Lexeme tree = new Lexeme(LIST);
        tree.left = this.match(VARIABLE);
        if (this.check(IN)) {
            Lexeme temp = this.advance();
            temp.left = tree.left;
            tree = temp;
            tree.right = this.unary();
            return tree;
        }
        tree.right = this.list();

        return tree;
    }

    private Lexeme optElse() {
        Lexeme tree = null;
        if (this.check(ELSE)) {
            this.advance();
            if (this.ifStatementPending()) {
                tree = this.ifStatement();
            }
            else {
                tree = this.block();
            }
        }

        return tree;
    }

    private Lexeme binaryOperator() {
        Lexeme tree = new Lexeme(BINARY);
        tree.left = this.matchMultiple(Helpers.binaryOperators);
        tree.right = new Lexeme(GLUE);
        return tree;
        //return this.matchMultiple(Helpers.binaryOperators);
    }

    private Lexeme varExpression() {
        Lexeme var = this.match(VARIABLE);
        Lexeme tree;
        if (this.check(O_BRACKET)) {
            tree = new Lexeme(FUNC_CALL);
            this.advance();
            tree.left = var;
            tree.right = this.optParamList();
            this.match(C_BRACKET);
            if (this.propertyPending()) {
                tree = this.property(tree);
            }

            return tree;
        }
        else if (this.check(ASSIGN)) {
            tree = this.advance();
            tree.left = var;
            tree.right = this.expression();
        }
        else if (this.propertyPending()) {
            tree = this.property(var);
        }
        else {
            tree = var;
        }

        return tree;
    }

    private Lexeme property(Lexeme obj) {
        this.advance();
        Lexeme tree = new Lexeme(PROPERTY);
        tree.left = obj;
        if (this.arrayPending()) {
            tree.right = this.array();
        }
        else {
            tree.right = this.varExpression();
        }
        return tree;
    }

    private Lexeme optParamList() {
        Lexeme tree = null;
        if (this.listPending()) {
            tree = this.list();
        }

        return tree;
    }

    private Lexeme list() {
        Lexeme tree = null;
        if (this.expressionPending()) {
            tree = new Lexeme(LIST);
            tree.left = this.expression();
            tree.right = this.list();
        }

        return tree;
    }

    private Lexeme lambda() {
        Lexeme tree = this.match(LAMBDA);
        this.match(O_BRACKET);
        tree.left = this.optParamList();
        this.match(C_BRACKET);
        tree.right = this.block();

        return tree;
    }

    private Lexeme array() {
        this.match(HASH);
        Lexeme tree;
        if (this.check(O_BRACKET)) {
            this.advance();
            tree = new Lexeme(ARRAY_DEF);
            tree.right = this.optParamList();
        }
        else {
            tree = new Lexeme(ARRAY_ACCESS);
            tree.left = this.match(VARIABLE);
            this.match(O_BRACKET);
            tree.right = this.expression();
        }
        this.match(C_BRACKET);

        return tree;
    }

    private Lexeme obj() {
        Lexeme tree = this.match(OBJ);
        tree.right = this.block();

        return tree;
    }

    private Lexeme importFile() {
        this.match(IMPORT);
        String filename = this.match(STRING).str;
        Recognizer r = new Recognizer(filename, InputType.FILE);

        return r.recognize();
    }

    private boolean binaryOperatorPending() {
        return this.checkMultiple(Helpers.binaryOperators);
    }

    private boolean expressionPending() {
        return this.unaryPending();
    }

    private boolean unaryPending() {
        return this.checkMultiple(Helpers.unaries);
    }

    private boolean arrayPending() { return this.check(HASH); }

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

    private boolean returnPending() { return this.check(RETURN); }

    private boolean importPending() {
        return this.check(IMPORT);
    }

    private boolean statementPending() {
        return this.expressionPending() || this.ifStatementPending() || this.whileLoopPending() || this.forLoopPending() ||
            this.varPending() || this.functionDefPending() || this.returnPending() || this.objPending() || this.importPending();
    }

    private boolean varPending() {
        return this.check(VAR);
    }

    private boolean objPending() { return this.check(OBJ); }

    private boolean functionDefPending() {
        return this.check(DEF);
    }

    private boolean propertyPending() {
        return this.check(DOT);
    }
}

