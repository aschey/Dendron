package DPL;
import java.util.HashMap;

import static DPL.TokenType.*;

/**
 * Created by aschey on 10/19/16.
 */
public class Evaluator {
    public static void main(String[] args) {
        Recognizer r = new Recognizer("func.dpl");
        Lexeme func = r.recognize();
        //GraphWriter g = new GraphWriter(func, "func");
        Evaluator e = new Evaluator();
        //e.evalProgram(func, e.createEnv());
        //g.createGraph();
        //g.showGraph();
        Lexeme l = e.createEnv();
        e.insert(new Lexeme(VARIABLE, "a"), new Lexeme(INTEGER, 1), l);
        e.insert(new Lexeme(VARIABLE, "b"), new Lexeme(INTEGER, 2), l);
        GraphWriter g = new GraphWriter(l, "envTest");
        g.createGraph();
        g.showGraph();
    }

    private boolean isTrue(Lexeme pt, Lexeme env) {
        return this.eval(pt.left, env).type.equals(TRUE);
    }

    Lexeme eval (Lexeme tree, Lexeme env) {
        switch (tree.type) {
            case INTEGER: return tree;
            case STRING: return tree;
            case DEF: return evalFuncDef(tree, env);
            case VARIABLE: return lookupEnv(tree, env);
            case GROUPING: return eval(tree.right, env);
            case GT:

        }
        return null;
    }

    Lexeme evalProgram(Lexeme pt, Lexeme env) {
        while (pt != null) {
            this.eval(pt.left, env);
            pt = pt.right;
        }

        return null;
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
        this.insert(varName, init, env);
        return init;
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
        this.insert(env, pt.left, closure);
        return null;
    }

    Lexeme evalCall(Lexeme pt, Lexeme env) {
        Lexeme closure = this.lookupEnv(env, pt.left);
        Lexeme args = pt.right;
        //Lexeme params = closure.
        return null;
    }

    Lexeme extendEnv(Lexeme env, Lexeme variables, Lexeme values) {
        return Lexeme.cons(ENV, makeTable(variables, values), env);
    }

    Lexeme createEnv() {
        return extendEnv(null, null, null);
    }

    Lexeme makeTable(Lexeme variables, Lexeme values) {
        return Lexeme.cons(TABLE, variables, values);
    }

    Lexeme lookupEnv(Lexeme env, Lexeme variable) {
        while (env != null) {
            Lexeme table = env.left;
            Lexeme vars = table.left;
            Lexeme vals = table.right;
            while (vars != null) {
                if (variable.equals(vars.left)) {
                    return vals.left;
                }
                vars = vars.right;
                vals = vals.right;
            }
        }

        return null;
    }

    Lexeme insert(Lexeme variable, Lexeme value, Lexeme env) {
        Lexeme table = env.left;
        table.left = Lexeme.cons(GLUE, variable, table.left);
        table.right = Lexeme.cons(GLUE, value, table.right);
        return value;
    }
}
