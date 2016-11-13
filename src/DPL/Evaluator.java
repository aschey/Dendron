package DPL;

import static DPL.TokenType.*;
import java.util.ArrayList;

/**
 * Created by aschey on 10/19/16.
 */
public class Evaluator {
    private Environment e;
    public static void main(String[] args) throws ReturnEncounteredException {
        Recognizer r = new Recognizer("dictionary.dpl");
        Lexeme func = r.recognize();
        //GraphWriter.quickGraph(func, "func");
        Evaluator eval = new Evaluator();
        Lexeme result = eval.eval(func, new Environment().createEnv());
        //System.out.println(result.getVal());
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

    private boolean isTrue(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme result = this.eval(pt, env);
        return result.bool;
    }

    //Lexeme evaluate(Lexeme tree, Lexeme env) {

    //}

    Lexeme eval (Lexeme tree, Lexeme env) throws ReturnEncounteredException {
        if (tree == null) {
            return null;
        }
        switch (tree.type) {
            case INTEGER: return tree;
            case STRING: return tree;
            case BOOLEAN: return tree;
            case THIS: return tree;
            case NULL: return null;
            case NEGATIVE: return this.evalNegative(tree, env);
            case NOT: return this.evalNot(tree, env);
            case LAMBDA: return this.evalLambda(tree, env);
            case DEF: return this.evalFuncDef(tree, env);
            case VAR: return this.evalVarDef(tree, env);
            case ASSIGN: return this.evalAssign(tree, env);
            case VARIABLE: return this.evalVariable(tree, env);
            case FUNC_CALL: return this.evalFuncCall(tree, env);
            case LIST: return this.evalList(tree, env);
            case RETURN: return this.evalReturn(tree, env);
            case STATEMENT: return this.evalStatement(tree, env);
            case GROUPING: return this.eval(tree.right, env);
            case WHILE: return this.evalWhile(tree, env);
            case FOR: return this.evalFor(tree, env);
            case IF: return this.evalIf(tree, env);
            case BINARY: return this.evalBinaryExpr(tree, env);
            case ARRAY_DEF: return this.evalArrayDef(tree, env);
            case ARRAY_ACCESS: return this.evalArrayAccess(tree, env);
            case OBJ: return this.evalObj(tree, env);
            case PROPERTY: return this.evalProperty(tree, env);
            default:
                Helpers.exitWithError("Type not found: " + tree.type);
        }
        return null;
    }

    Lexeme evalNegative(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        int val = -1 * this.eval(pt.right, env).integer;
        return new Lexeme(INTEGER, val);
    }

    Lexeme evalNot(Lexeme pt, Lexeme env) throws ReturnEncounteredException{
        boolean result = this.eval(pt.right, env).bool;
        return new Lexeme(BOOLEAN, !result);
    }

    Lexeme evalIf(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        if (this.isTrue(pt.left, env)) {
            return this.eval(pt.right.left, env);
        }
        else {
            return this.eval(pt.right.right, env);
        }
    }

    Lexeme evalStatement(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme val = null;
        while (pt != null) {
            val = this.eval(pt.left, env);
            pt = pt.right;
        }
        return val;
    }

    Lexeme evalReturn(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme result = this.eval(pt.right, env);
        if (result != null && result.type == THIS) {
            result = env;
        }
        throw new ReturnEncounteredException(result);
    }

    Lexeme evalAssign(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        // Update variable value
        Lexeme value = this.eval(pt.right, env);
        this.e.updateEnv(pt.left, value, env);
        return null;
    }

    Lexeme evalVariable(Lexeme pt, Lexeme env) {
        Lexeme result = this.e.lookupEnv(pt, env);
        return result;
    }

    Lexeme evalList(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        if (pt.left == null) {
            return null;
        }
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

    Lexeme evalBinaryExpr(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme operator = pt.left;
        Lexeme first = this.eval(pt.right.left, env);
        Lexeme second = this.eval(pt.right.right, env);
        Object result = null;

        if (first == null || second == null) {
            if (operator.type == EQ) {
                return new Lexeme(BOOLEAN, first == second);
            }
            else if (operator.type == NEQ) {
                return new Lexeme(BOOLEAN, first != second);
            }
            else {
                Helpers.exitWithError(String.format("Invalid types for operator %s: %s and %s",
                    pt.left.type, Helpers.getTypeIfExists(first), Helpers.getTypeIfExists(second)));
            }
        }

        TokenType returnType;
        if (Helpers.contains(Helpers.mathOperators, operator)) {
            returnType = INTEGER;
        }
        else {
            returnType = BOOLEAN;
        }

        boolean intsRequired = Helpers.contains(Helpers.intsRequired, operator);

        if (intsRequired && (first.type != INTEGER && second.type != INTEGER) || first.type != second.type) {
            Helpers.exitWithError(String.format("Invalid types for operator %s: %s and %s", pt.left.type, first.type, second.type));
        }

        boolean isInteger = (first.type == INTEGER);

        switch (operator.type) {
            case AND:
                result = first.bool && second.bool;
                break;
            case OR:
                result = first.bool || second.bool;
                break;
            case PLUS:
                if (isInteger) {
                    result = first.integer + second.integer;
                }
                else {
                    result = first.str + second.str;
                }
                break;
            case MINUS:
                result = first.integer - second.integer;
                break;
            case STAR:
                result = first.integer * second.integer;
                break;
            case SLASH:
                result = first.integer / second.integer;
                break;
            case LT:
                if (isInteger) {
                    result = first.integer < second.integer;
                }
                else {
                    result = first.str.compareTo(second.str) < 0;
                }
                break;
            case GT:
                if (isInteger) {
                    result = first.integer > second.integer;
                }
                else {
                    result = first.str.compareTo(second.str) > 0;
                }
                break;
            case LEQ:
                if (isInteger) {
                    result = first.integer <= second.integer;
                }
                else {
                    result = first.str.compareTo(second.str) <= 0;
                }
                break;
            case GEQ:
                if (isInteger) {
                    result = first.integer >= second.integer;
                }
                else {
                    result = first.str.compareTo(second.str) >= 0;
                }
                break;
            case EQ:
                result = first.getVal().equals(second.getVal());
                break;
            case NEQ:
                result = !first.getVal().equals(second.getVal());
                break;
            default:
                Helpers.exitWithError("Operator " + pt.left.type + " is not a valid binary operator");
        }

        return new Lexeme(returnType, result);
    }

    Lexeme evalWhile(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme result = new Lexeme(BOOLEAN, "false");
        while (this.isTrue(pt.left, env)) {
            result = this.eval(pt.right, env);
        }

        return result;
    }

    Lexeme evalFor(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme loopArgs = pt.left;
        // Determine how many args the user supplied
        int length = Helpers.listLength(loopArgs);
        Lexeme loopVar = Helpers.listIndex(loopArgs, 0);
        int loopStart = 0;
        Lexeme loopStartLexeme = new Lexeme(INTEGER, 0);
        int loopStep = 1;
        int loopEnd = 0;
        switch(length) {
            case 4:
                loopStep = Helpers.listIndex(loopArgs, 3).integer;
            case 3:
                loopStartLexeme = Helpers.listIndex(loopArgs, 1);
                loopStart = loopStartLexeme.integer;
                loopEnd = Helpers.listIndex(loopArgs, 2).integer;
                break;
            case 2:
                loopEnd = Helpers.listIndex(loopArgs, 1).integer;
                break;
        }

        // Create the new loop variable
        this.e.insert(loopVar, loopStartLexeme, env);
        Lexeme result = new Lexeme(BOOLEAN, "false");

        for (int i = loopStart; i < loopEnd; i += loopStep) {
            result = this.eval(pt.right, env);
            // Update the loop variable after each iteration
            this.e.updateEnv(loopVar, new Lexeme(INTEGER, i + loopStep), env);
        }

        return result;
    }

    Lexeme evalLambda(Lexeme pt, Lexeme env) {
        return Lexeme.cons(CLOSURE, env, pt);
    }

    Lexeme evalArrayDef(Lexeme pt, Lexeme env) {
        Lexeme arrayVals = pt.right;
        ArrayList<Lexeme> array = new ArrayList<>();
        while (arrayVals != null) {
            array.add(arrayVals.left);
            arrayVals = arrayVals.right;
        }
        return new Lexeme(ARRAY, array);
    }

    Lexeme evalArrayAccess(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme arrayLexeme = this.e.lookupEnv(pt.left, env);
        int arrayIndex = this.eval(pt.right, env).integer;
        return arrayLexeme.array.get(arrayIndex);
    }

    Lexeme evalFuncDef(Lexeme pt, Lexeme env) {
        Lexeme closure = Lexeme.cons(CLOSURE, env, pt);
        // Insert function name
        this.e.insert(pt.left, closure, env);
        return null;
    }

    Lexeme evalVarDef(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme varName = pt.left;
        Lexeme varVal = pt.right;
        Lexeme init = this.eval(varVal, env);
        this.e.insert(varName, init, env);
        return null;
    }

    Lexeme evalObj(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme xenv = e.extendEnv(env, null, null);
        try {
            Lexeme result = this.eval(pt.right, xenv);
            return result;
        }
        catch (ReturnEncounteredException ex) {
            return ex.retVal;
        }
    }

    Lexeme evalProperty(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme obj = this.eval(pt.left, env);
        if (pt.right.type == ASSIGN) {
            // Evaluate the right hand side of the assignment in the calling environment
            Lexeme value = this.eval(pt.right.right, env);
            // Update the value in the object's environment
            this.e.updateEnv(pt.right.left, value, obj);
            return null;
        }
        return this.eval(pt.right, obj);
    }

    Lexeme evalPrintln(Lexeme eargs) {
        Lexeme printVal = Helpers.listIndex(eargs, 0);
        System.out.println(Helpers.getValWithDefault(printVal, null));
        return null;
    }

    Lexeme evalPrint(Lexeme eargs) {
        Lexeme printVal = Helpers.listIndex(eargs, 0);
        System.out.print(Helpers.getValWithDefault(printVal, null));
        return null;
    }

    Lexeme evalLength(Lexeme eargs) {
        Lexeme arrayLexeme = Helpers.listIndex(eargs, 0);
        return new Lexeme(INTEGER, arrayLexeme.array.size());
    }

    Lexeme evalAppend(Lexeme eargs) {
        Lexeme arrayLexeme = Helpers.listIndex(eargs, 0);
        Lexeme valToAdd = Helpers.listIndex(eargs, 1);
        arrayLexeme.array.add(valToAdd);
        return null;
    }

    Lexeme evalFuncCall(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme args = pt.right;
        Lexeme eargs = this.eval(args, env);
        String funcName = pt.left.str;
        switch(funcName) {
            case "println": return this.evalPrintln(eargs);
            case "print": return this.evalPrint(eargs);
            case "length": return this.evalLength(eargs);
            case "append": return this.evalAppend(eargs);
            default: return this.evalUserDefinedFuncCall(pt, eargs, env);
        }
    }

    Lexeme evalUserDefinedFuncCall(Lexeme pt, Lexeme eargs, Lexeme env) throws ReturnEncounteredException {
        Lexeme closure = this.eval(pt.left, env);
        Lexeme function = Helpers.getFunction(closure);
        Lexeme params = function.left;
        Lexeme denv = closure.left;
        Lexeme xenv = e.extendEnv(denv, params, eargs);
        // Eval function code
        try {
            Lexeme result = this.eval(function.right, xenv);
            return result;
        }
        catch (ReturnEncounteredException ex) {
            return ex.retVal;
        }
    }
}
