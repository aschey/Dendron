/**
 * Created by aschey on 9/24/16.
 */
package DPL;
import java.util.Arrays;
import java.util.HashMap;

import static DPL.TokenType.*;

public class Recognizer {
    private Lexeme currentLexeme;
    private Lexer lexer;

    private static boolean toPrint = false;
    private static boolean toTraverse = false;

    private static void printMethod(String name) {
        if (toPrint) {
            System.out.println(name);
        }
    }

    public static void main(String[] args) {
        Recognizer recognizer = new Recognizer("func.dpl");
        Lexeme l = recognizer.statements();
        //recognizer.traverse(l);
        //GraphWriter.quickGraph(l, "func");
        //System.out.println(l.right.left.right.left.type);
        //recognizer.prettyPrint(l);
    }

    public Recognizer(String filename) {
        this.lexer = new Lexer(filename);
        this.currentLexeme = this.lexer.lex();
    }

    Lexeme recognize() {
        return this.statements();
    }

    private void traverse(Lexeme tree) {
        if (!toTraverse) {
            return;
        }
        _traverse(tree, "root");
        System.out.println();
    }

    private void traverse(Lexeme tree, boolean override) {
        if (!toTraverse && !override) {
            return;
        }
        _traverse(tree, "root");
        System.out.println();
    }

    private void _traverse(Lexeme tree, String dir) {
        if (tree != null) {
            System.out.println(tree.getVal() + " " + dir);
            _traverse(tree.left, "left");
            _traverse(tree.right, "right");
        }
    }

    private void printToken(Object token) {
        System.out.print(token + " ");
    }

    private void prettyPrint(Lexeme tree) {
        if (tree == null) {
            return;
        }
        if (Helpers.contains(Helpers.binaryOperators, tree)) {
            this.printToken(Helpers.binaryOpMappings.get(tree.type));
            return;
        }

        if (Helpers.contains(new TokenType[] { FOR, WHILE, IF }, tree)) {
            printToken(tree.type.toString().toLowerCase());
            printToken("[");
            this.prettyPrint(tree.left);
            System.out.println("]");
            System.out.println("[");
            this.prettyPrint(tree.right);
            System.out.println("]");
            return;
        }
        switch(tree.type) {
            case INTEGER: {
                this.printToken(tree.integer);
                break;
            }
            case VARIABLE: {
                this.printToken(tree.str);
                break;
            }
            case FOR: {
                this.printToken("for");
            }
            case BOOLEAN: {
                this.printToken(tree.str);
                break;
            }
            case NULL: {
                this.printToken("null");
            }
            case RETURN: {
                this.printToken("return");
                this.prettyPrint(tree.right);
                this.printToken(";");
                System.out.println();
                break;
            }
            case STRING: {
                this.printToken("\"" + tree.str + "\"");
                break;
            }
            case NEGATIVE: {
                System.out.print("-");
                this.prettyPrint(tree.right);
                break;
            }
            case VAR: {
                this.printToken("var");
                this.prettyPrint(tree.left);
                if (tree.right != null) {
                    this.printToken("=");
                    this.prettyPrint(tree.right);
                }
                break;
            }
            case LAMBDA: {
                this.printToken("lambda");
                this.printToken("[");
                this.prettyPrint(tree.left);
                System.out.println("]");
                System.out.println("[ ");
                this.prettyPrint(tree.right);
                this.printToken("]");
                break;
            }
            case DEF: {
                this.printToken("def");
                this.prettyPrint(tree.left);
                this.printToken("[");
                this.prettyPrint(tree.right.left);
                this.printToken("]");
                System.out.println();
                System.out.println("[");
                this.prettyPrint(tree.right.right);
                System.out.println("]");
                break;
            }
            case FUNC_CALL: {
                this.prettyPrint(tree.left);
                this.printToken("[");
                this.prettyPrint(tree.right);
                this.printToken("]");
                break;
            }
            case ASSIGN: {
                this.prettyPrint(tree.left);
                this.printToken("=");
                this.prettyPrint(tree.right);
                System.out.println(";");
                break;
            }

            case BINARY: {
                this.prettyPrint(tree.right.left);
                this.prettyPrint(tree.left);
                this.prettyPrint(tree.right.right);
                break;
            }

            case ARRAY_DEF: {
                this.printToken(".");
                this.printToken("[");
                this.prettyPrint(tree.right);
                this.printToken("]");
                break;
            }

            case ARRAY_ACCESS: {
                this.printToken(".");
                this.prettyPrint(tree.left);
                this.printToken("[");
                this.prettyPrint(tree.right);
                this.printToken("]");
                break;
            }

            case STATEMENT: {
                if (tree.left.type == IF) {
                    this.printToken(tree.left.type);
                    this.printToken("[");
                    this.prettyPrint(tree.left.left);
                    System.out.println("]");
                    System.out.println("[");
                    this.prettyPrint(tree.left.right.left);
                    System.out.println("]");
                    if (tree.left.type == IF && tree.left.right.right != null && tree.left.right.right.type == IF) {
                        this.printToken("ELSE");
                    }
                    this.prettyPrint(tree.left.right.right);
                }
                else {
                    this.prettyPrint(tree.left);
                }
                if (tree.left.type == VAR || tree.left.type == FUNC_CALL) {
                    System.out.println(";");
                }
                this.prettyPrint(tree.right);
                break;
            }
            default: {
                this.prettyPrint(tree.left);
                this.prettyPrint(tree.right);
            }
        }
    }

    private boolean checkMultiple(TokenType[] types) {
        for (TokenType type : types) {
            if (this.check(type)) {
                return true;
            }
        }
        return false;
    }

    private Lexeme matchMultiple(TokenType[] types) {
        for (TokenType type : types) {
            if (this.check(type)) {
                return this.match(type);
            }
        }
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
            System.out.println("Syntax error: Got " + this.currentLexeme.type + " expected " + type);
            System.exit(1);
        }
    }

    private Lexeme var() {
        printMethod("var");

        Lexeme tree = this.match(VAR);
        tree.left = this.match(VARIABLE);
        if (this.check(ASSIGN)) {
            this.advance();
            tree.right = this.expression();
        }
        this.match(SEMICOLON);
        traverse(tree);
        return tree;
    }

    private Lexeme functionDef() {
        printMethod("functionDef");

        Lexeme tree = this.match(DEF);
        tree.left = this.match(VARIABLE);
        tree.right = new Lexeme(GLUE);
        this.match(O_BRACKET);
        tree.right.left = this.optParamList();
        this.match(C_BRACKET);
        tree.right.right = this.block();
        traverse(tree);
        return tree;
    }

    private Lexeme unary() {
        printMethod("unary");
        Lexeme tree;
        if (this.checkMultiple(new TokenType[] {INTEGER, STRING, BOOLEAN, NULL})) {
            Lexeme l = this.advance();
            traverse(l);
            return l;
        }

        if (this.check(MINUS)) {
            tree = new Lexeme(NEGATIVE);
            this.advance();
            tree.right = this.unary();
            traverse(tree);
            return tree;
        }
        else if (this.check(NOT)) {
            tree = new Lexeme(NOT);
            this.advance();
            tree.right = this.unary();
            traverse(tree);
            return tree;
        }
        else if (this.check(O_BRACKET)) {
            this.advance();
            tree = new Lexeme(GROUPING);
            tree.right = this.expression();
            this.match(C_BRACKET);
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
        traverse(tree);
        return tree;
    }

    private Lexeme expression() {
        printMethod("expression");
        Lexeme unary = this.unary();
        if (this.binaryOperatorPending()) {
            Lexeme tree = new Lexeme(BINARY);
            tree.left = this.binaryOperator();
            tree.right = new Lexeme(GLUE);
            tree.right.left = unary;
            tree.right.right = this.expression();
            traverse(tree);
            return tree;
        }
        traverse(unary);
        return unary;
    }

    private Lexeme conditionalOrLoopHeader() {
        printMethod("conditionalOrLoopHeader");
        this.match(O_BRACKET);
        Lexeme tree = this.expression();
        this.match(C_BRACKET);
        traverse(tree);
        return tree;
    }

    private Lexeme ifStatement() {
        printMethod("ifStatement");
        Lexeme tree = this.match(IF);
        tree.left = this.conditionalOrLoopHeader();
        tree.right = new Lexeme(GLUE);
        tree.right.left = this.block();
        tree.right.right = this.optElse();
        traverse(tree);
        return tree;
    }

    private Lexeme returnVal() {
        printMethod("returnVal");
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
        printMethod("block");
        Lexeme tree = null;
        this.match(O_BRACKET);
        if (this.statementPending()) {
            tree = this.statements();
        }
        this.match(C_BRACKET);
        traverse(tree);
        return tree;
    }

    private Lexeme statements() {
        printMethod("statements");
        Lexeme tree = new Lexeme(STATEMENT);
        tree.left = this.statement();

        if (this.statementPending()) {
            tree.right = this.statements();
        }
        traverse(tree);
        return tree;
    }

    private Lexeme statement() {
        printMethod("statement");
        if (this.ifStatementPending()) {
            Lexeme l = this.ifStatement();
            traverse(l);
            return l;
        }
        if (this.whileLoopPending()) {
            Lexeme l = this.whileLoop();
            traverse(l);
            return l;
        }
        if (this.forLoopPending()) {
            Lexeme l = this.forLoop();
            traverse(l);
            return l;
        }
        if (this.varPending()) {
            Lexeme l = this.var();
            traverse(l);
            return l;
        }

        if (this.functionDefPending()) {
            Lexeme l = this.functionDef();
            traverse(l);
            return l;
        }

        if (this.returnPending()) {
            Lexeme l = this.returnVal();
            traverse(l);
            return l;
        }

        Lexeme tree = this.expression();
        this.match(SEMICOLON);
        traverse(tree);
        return tree;
    }

    private Lexeme whileLoop() {
        printMethod("whileLoop");
        Lexeme tree = this.match(WHILE);
        tree.left = this.conditionalOrLoopHeader();
        tree.right = this.block();

        traverse(tree);
        return tree;
    }

    private Lexeme forLoop() {
        printMethod("forLoop");
        Lexeme tree = this.match(FOR);
        this.match(O_BRACKET);
        tree.left = this.list();
        this.match(C_BRACKET);
        tree.right = this.block();
        traverse(tree);
        return tree;
    }

    private Lexeme optElse() {
        printMethod("optElse");
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
        traverse(tree);
        return tree;
    }

    private Lexeme binaryOperator() {
        printMethod("binaryOperator");
        Lexeme l = this.matchMultiple(Helpers.binaryOperators);
        traverse(l);
        return l;
    }

    private Lexeme varExpression() {
        printMethod("varExpression");
        Lexeme var = this.match(VARIABLE);
        Lexeme tree;
        if (this.check(O_BRACKET)) {
            tree = new Lexeme(FUNC_CALL);
            this.advance();
            tree.left = var;
            tree.right = this.optParamList();
            this.match(C_BRACKET);
            if (this.check(DOT)) {
                this.advance();
                Lexeme newTree = new Lexeme(PROPERTY);
                newTree.left = tree;
                newTree.right = this.varExpression();

                return newTree;
            }

            return tree;
        }
        else if (this.check(ASSIGN)) {
            tree = this.advance();
            tree.left = var;
            tree.right = this.expression();
        }
        else if (this.check(DOT)) {
            this.advance();
            tree = new Lexeme(PROPERTY);
            tree.left = var;
            tree.right = this.varExpression();
        }
        else {
            tree = var;
        }

        traverse(tree);
        return tree;
    }

    private Lexeme optParamList() {
        printMethod("optParamList");
        Lexeme tree = null;
        if (this.listPending()) {
            tree = this.list();
        }
        traverse(tree);
        return tree;
    }

    private Lexeme list() {
        printMethod("list");
        Lexeme tree = null;
        if (this.expressionPending()) {
            tree = new Lexeme(LIST);
            tree.left = this.expression();
            tree.right = this.list();
        }
        traverse(tree);
        return tree;
    }

    private Lexeme lambda() {
        printMethod("lambda");
        Lexeme tree = this.match(LAMBDA);
        this.match(O_BRACKET);
        tree.left = this.optParamList();
        this.match(C_BRACKET);
        tree.right = this.block();
        return tree;
    }

    private Lexeme array() {
        this.match(DOT);
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

    private boolean binaryOperatorPending() {
        return this.checkMultiple(Helpers.binaryOperators);
    }

    private boolean expressionPending() {
        return this.unaryPending();
    }

    private boolean unaryPending() {
        return this.checkMultiple(new TokenType[] {INTEGER, STRING, BOOLEAN, O_BRACKET, MINUS, LAMBDA, DOT, NULL, NOT, OBJ }) || this.varExpressionPending();
    }

    private boolean varExpressionPending() {
        return this.check(VARIABLE);
    }

    private boolean arrayPending() { return this.check(DOT); }

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

    private boolean statementPending() {
        return this.expressionPending() || this.ifStatementPending() || this.whileLoopPending() || this.forLoopPending() ||
            this.varPending() || this.functionDefPending() || this.returnPending() || this.objPending();
    }

    private boolean varPending() {
        return this.check(VAR);
    }

    private boolean objPending() { return this.check(OBJ); }

    private boolean functionDefPending() {
        return this.check(DEF);
    }
}

