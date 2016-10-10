/**
 * Created by aschey on 9/24/16.
 */
package DPL;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.io.PrintWriter;

import static DPL.TokenType.*;

public class Recognizer {
    private Lexeme currentLexeme;
    private Lexer lexer;
    private TokenType[] binaryOperators = new TokenType[] { LT, GT, LEQ, GEQ, EQ, NEQ, PLUS, MINUS, STAR, SLASH };

    private static boolean toPrint = false;
    private static boolean toTraverse = false;

    private static void printMethod(String name) {
        if (toPrint) {
            System.out.println(name);
        }
    }

    public static void main(String[] args) {
        Recognizer recognizer = new Recognizer();
        Lexeme l = recognizer.statements();
        //recognizer.traverse(l);
        TreeGraphWriter t = new TreeGraphWriter(l);
        t.createGraph();
        //System.out.println(l.right.left.right.left.type);
        //recognizer.prettyPrint(l);
    }

    public Recognizer() {
        this.lexer = new Lexer("test1.dpl");
        this.currentLexeme = this.lexer.lex();
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

    private void prettyPrint(Lexeme tree) {
        if (tree == null) {
            return;
        }
        //System.out.println("type = " + tree.type);
        if (Arrays.stream(this.binaryOperators).anyMatch(t -> t.equals(tree.type))) {
            this.prettyPrint(tree.left);
            System.out.print(tree.type);
            this.prettyPrint(tree.right);
            return;
        }
        switch(tree.type) {
            case INTEGER: {
                System.out.print(tree.integer);
                break;
            }
            case VARIABLE: {
                System.out.print(tree.str);
            }
            case STRING: {
                System.out.print("\"" + tree.str + "\"");
            }
            case O_BRACKET: {
                System.out.print("[");
                this.prettyPrint(tree.right);
                System.out.print("]");
            }
            case NEGATIVE: {
                System.out.print("-");
                this.prettyPrint(tree.right);
            }
            case VAR: {
                System.out.print("var ");
                this.prettyPrint(tree.left);
                System.out.print(" = ");
                this.prettyPrint(tree.right);
            }
            case DEF: {
                System.out.print("def ");
                this.prettyPrint(tree.left);
                this.prettyPrint(tree.right);
                System.out.println("[");
                this.prettyPrint(tree.right.left);
                System.out.println("[");
                this.prettyPrint(tree.right.right);
            }
            case GLUE: {
                this.prettyPrint(tree.left);
                this.prettyPrint(tree.right);
            }

            case VAR_EXPR: {
                this.prettyPrint(tree.left);
                if (tree.right != null) {
                    System.out.println("[");
                    this.prettyPrint(tree.right);
                    System.out.println("]");
                }
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
        return this.currentLexeme.type.equals(type);
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
        if (this.checkMultiple(new TokenType[] {INTEGER, STRING})) {
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
        else if (this.check(O_BRACKET)) {
            this.advance();
            tree = new Lexeme(GROUPING);
            tree.right = this.expression();
            this.match(C_BRACKET);
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

    private Lexeme block() {
        printMethod("block");
        Lexeme tree = null;
        this.match(O_BRACKET);
        if (this.statementPending()) {
            tree = this.statements();
        //traverse(tree);
        //System.out.println();
            //System.out.println(tree.getVal());
        }
        this.match(C_BRACKET);
        traverse(tree);
        return tree;
    }

    private Lexeme statements() {
        printMethod("statements");
        Lexeme tree = new Lexeme(STATEMENT);
        tree.left = this.statement();
//        if (tree.right.left != null) {
//            System.out.println(tree.right.left.getVal());
//        }
        //System.out.println(tree.getVal());
        //System.out.println(tree.str);
        if (this.statementPending()) {
            tree.right = this.statements();
//            if (tree.right.right.left != null) {
//                System.out.println(tree.right.right.left.getVal());
//            }
        }
        //if (tree.right.right != null)
            //System.out.println("VAL= " + tree.right.right.getVal());
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
            //System.out.println(l.right.left.getVal());
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
        //traverse(tree.right);
//        if (tree.right.right.left != null) {
//            System.out.println(tree.right.left.getVal());
//        }
        //System.out.println("WHILE=" + tree.right.left.getVal());
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
        Lexeme l = this.matchMultiple(this.binaryOperators);
        traverse(l);
        return l;
    }

//    private void binaryExpr() {
//        unary();
//        binaryOperator();
//        unary();
//    }

    private Lexeme varExpression() {
        printMethod("varExpression");
        Lexeme tree = new Lexeme(VAR_EXPR);
        tree.left = this.match(VARIABLE);
        if (this.check(O_BRACKET)) {
            this.advance();
            tree.right = this.optParamList();
            this.match(C_BRACKET);
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
        return this.expressionPending() || this.ifStatementPending() || this.whileLoopPending() || this.forLoopPending() || this.varPending() || this.functionDefPending();
    }

    private boolean varPending() {
        return this.check(VAR);
    }

    private boolean functionDefPending() {
        return this.check(DEF);
    }
}

class TreeGraphWriter {
    private int nodeNum;
    private int nullNum;
    private PrintWriter graph;
    private ArrayDeque<Lexeme> queue;
    private Lexeme tree;

    public TreeGraphWriter(Lexeme tree) {
        this.nodeNum = 1;
        this.nullNum = 0;
        try {
            this.graph = new PrintWriter("parseTree.dot");
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex);
        }
        this.queue = new ArrayDeque<>();
        this.tree = tree;
    }

    public void createGraph() {
        this.graph.write("digraph {\n");
        this.graph.write("graph [ordering=\"out\"];\n");
        this.queue.addFirst(this.tree);
        this.graph.write("Node0 [label=" + this.tree.getVal() + "];\n");
        int localRootNode = 0;
        while (!this.queue.isEmpty()) {
            Lexeme current = this.queue.removeLast();
            String curNode = "Node" + localRootNode;
            this.writeToGraph(curNode, current.left);
            this.writeToGraph(curNode, current.right);
            localRootNode++;
        }
        this.graph.write("}");
        this.graph.close();
    }

    private void writeToGraph(String curNode, Lexeme directionNode) {
        if (directionNode != null) {
            String nextNode = "Node" + this.nodeNum;
            this.nodeNum++;
            this.graph.write(nextNode + " [label=" + directionNode.getVal() + "];\n");
            this.graph.write(curNode + " -> " + nextNode + ";\n");
            this.queue.addFirst(directionNode);
        }
        else {
            String nullStr = "Null" + this.nullNum;
            this.nullNum++;
            this.graph.write(nullStr + " [shape=point];\n");
            this.graph.write(curNode + " -> " + nullStr + ";\n");
        }
    }
}