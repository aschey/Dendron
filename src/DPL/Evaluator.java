package DPL;
import java.util.HashMap;

import static DPL.TokenType.*;

/**
 * Created by aschey on 10/19/16.
 */
public class Evaluator {
    private Environment e;
    public static void main(String[] args) {
        Recognizer r = new Recognizer("func.dpl");
        Lexeme func = r.recognize();
        //GraphWriter.quickGraph(func, "func");
        Evaluator eval = new Evaluator();
        eval.eval(func, new Environment().createEnv());
        //e.evalProgram(func, e.createEnv());
        //g.createGraph();
        //g.showGraph();
        //Lexeme l = e.createEnv();
        //e.insert(new Lexeme(VARIABLE, "a"), new Lexeme(INTEGER, 1), l);
        //e.insert(new Lexeme(VARIABLE, "b"), new Lexeme(INTEGER, 2), l);
        //GraphWriter g = new GraphWriter(l, "envTest");
        //g.createGraph();
        //g.showGraph();
    }

    public Evaluator() {
        this.e = new Environment();
    }

    private boolean isTrue(Lexeme pt, Lexeme env) {
        return this.eval(pt.left, env).type.equals(TRUE);
    }

    Lexeme eval (Lexeme tree, Lexeme env) {
        if (tree == null) {
            return null;
        }
        switch (tree.type) {
            case INTEGER: return tree;
            case STRING: return tree;
            case DEF: return this.evalFuncDef(tree, env);
            case VAR: return this.evalVarDef(tree, env);
            case VAR_EXPR: return this.evalVarExpr(tree, env);
            case VARIABLE: return this.e.lookupEnv(tree, env);
            case LIST: return this.evalList(tree, env);
            case RETURN: return this.evalReturn(tree, env);
            case STATEMENT: return this.evalStatement(tree, env);
            case GROUPING: this.eval(tree.right, env);
            default:
                Helpers.exitWithError("Type not found: " + tree.type);
        }
        return null;
    }

    Lexeme evalStatement(Lexeme pt, Lexeme env) {
        Lexeme val = null;
        while (pt != null) {
            val = this.eval(pt.left, env);
            pt = pt.right;
        }
        return val;
    }

    Lexeme evalReturn(Lexeme pt, Lexeme env) {
        Lexeme result = this.eval(pt.right, env);
        return result;
    }

    Lexeme evalBlock(Lexeme pt, Lexeme env) {
        Lexeme result = null;
        while (pt != null) {
            result = this.eval(pt.left, env);
            pt = pt.right;
        }
        return result;
    }

    Lexeme evalVarDef(Lexeme pt, Lexeme env) {
        Lexeme varName = pt.left;
        Lexeme varVal = pt.right;
        Lexeme init = this.eval(varVal, env);
        this.e.insert(varName, init, env);
        return null;
    }

    Lexeme evalVarExpr(Lexeme pt, Lexeme env) {
        if (pt.right == null) {
            return this.e.lookupEnv(pt.left, env);
        }

        return this.evalCall(pt, env);
    }

    Lexeme evalList(Lexeme pt, Lexeme env) {
        Lexeme result = new Lexeme(LIST);
        Lexeme resultPtr = result;
        while (pt != null) {
            resultPtr.left = this.eval(pt.left, env);
            pt = pt.right;
            if (pt != null) {
                resultPtr.right = new Lexeme(LIST);
                resultPtr = resultPtr.right;
            }
        }

        return result;
    }

    Lexeme evalMathExpr(Lexeme pt, Lexeme env) {
        Lexeme addend = this.eval(pt.right.left, env);
        Lexeme augend = this.eval(pt.right.right, env);
        Integer result = null;
        if (!addend.type.equals(INTEGER) && !augend.type.equals(INTEGER)) {
            Helpers.exitWithError(String.format("Invalid types for operator %s: %s and %s", pt.left.type, addend.type, augend.type));
        }
        switch (pt.left.type) {
            case PLUS:
                result = addend.integer + augend.integer;
                break;
            case MINUS:
                result = addend.integer - augend.integer;
                break;
            case STAR:
                result = addend.integer * augend.integer;
                break;
            case SLASH:
                result = addend.integer / augend.integer;
                break;
            default:
                Helpers.exitWithError("Operator " + pt.left.type + " is not a valid mathematical operator");
        }

        return new Lexeme(INTEGER, result);
    }

    Lexeme evalWhile(Lexeme pt, Lexeme env) {
        Lexeme result = new Lexeme(FALSE);
        while (this.isTrue(pt.left, env)) {
            result = this.eval(pt.right, env);
        }

        return result;
    }

    Lexeme evalFuncDef(Lexeme pt, Lexeme env) {
        Lexeme closure = Lexeme.cons(CLOSURE, env, pt);
        // Insert function name
        //GraphWriter.quickGraph(closure, "closure");
        this.e.insert(pt.left, closure, env);
        return null;
    }

    Lexeme evalCall(Lexeme pt, Lexeme env) {
        Lexeme closure = this.eval(pt.left, env);
        //GraphWriter.quickGraph(closure, "closure");
        Lexeme args = pt.right;
        Lexeme params = Helpers.varExprsToVarList(closure.right.right.left);
        Lexeme eargs = this.eval(args, env);
        Lexeme denv = closure.left;
        Lexeme xenv = e.extendEnv(denv, params, eargs);
        Lexeme result = this.eval(closure.right.right.right, xenv);
        return result;
    }
}
