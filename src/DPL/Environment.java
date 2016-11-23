package DPL;

import static DPL.TokenType.*;

/**
 * Environment
 * A collection of methods for manipulating environments
 */

class Environment {
    private static boolean varsEqual(Lexeme a, Lexeme b) {
        return a.getVal().equals(b.getVal());
    }

    static Lexeme extendEnv(Lexeme env, Lexeme variables, Lexeme values) {
        return Lexeme.cons(ENV, makeTable(variables, values), env);
    }

    static Lexeme createEnv() {
        return extendEnv(null, null, null);
    }

    private static Lexeme makeTable(Lexeme variables, Lexeme values) {
        return Lexeme.cons(TABLE, variables, values);
    }

    static Lexeme lookupEnv(Lexeme variable, Lexeme env) {
        while (env != null) {
            Lexeme table = env.left;
            Lexeme vars = table.left;
            Lexeme vals = table.right;
            while (vars != null) {
                if (varsEqual(variable, vars.left)) {
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
        return null;
    }

    static void updateEnv(Lexeme variable, Lexeme value, Lexeme env) {
        while (env != null) {
            Lexeme table = env.left;
            Lexeme vars = table.left;
            Lexeme vals = table.right;
            while (vars != null) {
                if (varsEqual(variable, vars.left)) {
                    vals.left = value;
                    return;
                }
                vars = vars.right;
                vals = vals.right;
            }
            env = env.right;
        }
    }

    static Lexeme insert(Lexeme variable, Lexeme value, Lexeme env) {
        Lexeme table = env.left;
        table.left = Lexeme.cons(GLUE, variable, table.left);
        table.right = Lexeme.cons(GLUE, value, table.right);
        return value;
    }

    static Lexeme insertList(Lexeme variables, Lexeme values, Lexeme env) {
        while (variables != null) {
            insert(variables.left, values.left, env);
            variables = variables.right;
            values = values.right;
        }
        return values;
    }

    // Used for array and string methods
    // Get the first variable in the environment
    // This will be the variable that the method is invoked on
    static Lexeme getCallingVal(Lexeme env) {
        return env.left.right.left;
    }
}
