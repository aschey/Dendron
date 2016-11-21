package DPL;

/**
 * Created by aschey on 11/10/16.
 */

import static DPL.TokenType.*;

public class Environment {
    private boolean varsEqual(Lexeme a, Lexeme b) {
        return a.getVal().equals(b.getVal());
    }
    Lexeme extendEnv(Lexeme env, Lexeme variables, Lexeme values) {
        return Lexeme.cons(ENV, this.makeTable(variables, values), env);
    }

    Lexeme createEnv() {
        return extendEnv(null, null, null);
    }

    Lexeme makeTable(Lexeme variables, Lexeme values) {
        return Lexeme.cons(TABLE, variables, values);
    }

    Lexeme lookupEnv(Lexeme variable, Lexeme env) {
        while (env != null) {
            Lexeme table = env.left;
            Lexeme vars = table.left;
            Lexeme vals = table.right;
            while (vars != null) {
                if (this.varsEqual(variable, vars.left)) {
                    return vals.left;
                }
                vars = vars.right;
                vals = vals.right;
            }
            env = env.right;
        }
        try {
            throw new Exception();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        //Helpers.exitWithError("variable " + variable.getVal() + " not found");
        return null;
    }

    void updateEnv(Lexeme variable, Lexeme value, Lexeme env) {
        //GraphWriter.quickGraph(env, "env");
        while (env != null) {
            Lexeme table = env.left;
            Lexeme vars = table.left;
            Lexeme vals = table.right;
            while (vars != null) {
                if (this.varsEqual(variable, vars.left)) {
                    vals.left = value;
                    return;
                }
                vars = vars.right;
                vals = vals.right;
            }
            env = env.right;
        }
    }

    Lexeme insert(Lexeme variable, Lexeme value, Lexeme env) {
        Lexeme table = env.left;
        table.left = Lexeme.cons(GLUE, variable, table.left);
        table.right = Lexeme.cons(GLUE, value, table.right);
        return value;
    }

    Lexeme insertList(Lexeme variables, Lexeme values, Lexeme env) {
        while (variables != null) {
            this.insert(variables.left, values.left, env);
            variables = variables.right;
            values = values.right;
        }
        return values;
    }

    Lexeme getStartVal(Lexeme env) {
        return env.left.right.left;
    }
}
