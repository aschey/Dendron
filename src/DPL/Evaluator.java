package DPL;

import static DPL.TokenType.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.io.File;
import java.util.Scanner;
import java.util.function.BiFunction;

/**
 * Created by aschey on 10/19/16.
 */
public class Evaluator {
    private Environment e;
    public static void main(String[] args) throws ReturnEncounteredException {
        //GraphWriter.quickGraph(func, "func");
        Evaluator eval = new Evaluator();
        //eval.evaluate("func.dpl");

    }

    public Evaluator() {
        this.e = new Environment();
    }

    private boolean isTrue(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme result = this.eval(pt, env);
        return result.bool;
    }

    Lexeme addBultins() {
        Lexeme env = this.e.createEnv();
        // General functions
        this.addBuiltin("println", this::evalPrintln, env);
        this.addBuiltin("print", this::evalPrint, env);
        this.addBuiltin("input", this::evalInput, env);
        this.addBuiltin("getInt", this::evalGetInt, env);
        this.addBuiltin("str", this::evalStr, env);
        this.addBuiltin("eq", this::evalEq, env);
        this.addBuiltin("inspect", this::evalInspect, env);

        // Array/string functions
        this.addBuiltin("length", this::evalLength, env);
        this.addBuiltin("append", this::evalAppend, env);
        this.addBuiltin("insert", this::evalInsert, env);
        this.addBuiltin("remove", this::evalRemove, env);
        this.addBuiltin("removeAt", this::evalRemoveAt, env);
        return env;
    }

    private void addBuiltin(String name, BiFunction<Lexeme, Lexeme, Lexeme> evaluator, Lexeme env) {
        Lexeme tag = new Lexeme(BUILTIN);
        tag.eval = evaluator;
        Lexeme funcName = new Lexeme(VARIABLE, name);
        this.e.insert(funcName, tag, env);
    }

    Lexeme evaluate(String input, InputType inputType, Lexeme env) throws ReturnEncounteredException {
        Recognizer r = new Recognizer(input, inputType);
        Lexeme parseTree = r.recognize();
        this.eval(parseTree, env);
        return env;
    }

    private Lexeme eval(Lexeme tree, Lexeme env) throws ReturnEncounteredException {
        if (tree == null) {
            return null;
        }

        if (tree.negative) {
            return this.evalNegative(tree, env);
        }

        return this.evalWithoutNegative(tree, env);
    }

    private Lexeme evalWithoutNegative(Lexeme tree, Lexeme env) throws ReturnEncounteredException {
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

    private Lexeme evalNegative(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme result = this.evalWithoutNegative(pt, env);
        if (result == null || result.type != INTEGER) {
            Helpers.exitWithError("Unary negative does not resolve to integer");
            return null;
        }
        int val = -1 * result.integer;
        return new Lexeme(INTEGER, val);
    }

    private Lexeme evalNot(Lexeme pt, Lexeme env) throws ReturnEncounteredException{
        boolean result = this.eval(pt.right, env).bool;
        return new Lexeme(BOOLEAN, !result);
    }

    private Lexeme evalIf(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        if (this.isTrue(pt.left, env)) {
            return this.eval(pt.right.left, env);
        }
        else {
            return this.eval(pt.right.right, env);
        }
    }

    private Lexeme evalStatement(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme val = null;
        while (pt != null) {
            val = this.eval(pt.left, env);
            pt = pt.right;
        }
        return val;
    }

    private Lexeme evalReturn(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme result = this.eval(pt.right, env);
        if (result != null && result.type == THIS) {
            result = env;
        }
        throw new ReturnEncounteredException(result);
    }

    private Lexeme evalAssign(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        // Update variable value
        Lexeme value = this.eval(pt.right, env);
        this.e.updateEnv(pt.left, value, env);
        return null;
    }

    private Lexeme evalVariable(Lexeme pt, Lexeme env) {
        return this.e.lookupEnv(pt, env);
    }

    private Lexeme evalList(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
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

    private Lexeme evalBinaryExpr(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme operator = pt.left;
        Lexeme first = this.eval(pt.right.left, env);

        // Short-circuiting
        if (operator.type == AND && !first.bool) {
            return new Lexeme(BOOLEAN, false);
        }
        if (operator.type == OR && first.bool) {
            return new Lexeme(BOOLEAN, true);
        }

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
                return null;
            }
        }

        TokenType returnType;
        if (Helpers.mathOperators.contains(operator.type)) {
            if (first.type == INTEGER) {
                returnType = INTEGER;
            }
            else {
                returnType = STRING;
            }
        }
        else {
            returnType = BOOLEAN;
        }

        boolean intsRequired = Helpers.intsRequired.contains(operator);

        // Ignore special case: string + other value
        if (!(operator.type == PLUS && first.type == STRING)) {
            if (intsRequired && (first.type != INTEGER && second.type != INTEGER) || first.type != second.type) {
                Helpers.exitWithError(String.format("Invalid types for operator %s: %s and %s", pt.left.type, first.type, second.type));
            }
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
                if (first.type == INTEGER) {
                    result = first.integer + second.integer;
                }
                else if (second.type == INTEGER) {
                    result = first.str + second.integer;
                }
                else if (second.type == BOOLEAN) {
                    result = first.str + second.bool;
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
            case CARAT:
                result = (int)Math.pow(first.integer, second.integer);
                break;
            case REMAINDER:
                result = first.integer % second.integer;
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

    private Lexeme evalWhile(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme result = new Lexeme(BOOLEAN, false);
        while (this.isTrue(pt.left, env)) {
            result = this.eval(pt.right, env);
        }

        return result;
    }

    private Lexeme evalFor(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        if (pt.left.type == IN) {
            return this.evalForEach(pt, env);
        }
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
                loopStep = this.eval(Helpers.listIndex(loopArgs, 3), env).integer;
            case 3:
                loopStartLexeme = this.eval(Helpers.listIndex(loopArgs, 1), env);
                loopStart = loopStartLexeme.integer;
                loopEnd = this.eval(Helpers.listIndex(loopArgs, 2), env).integer;
                break;
            case 2:
                loopEnd = this.eval(Helpers.listIndex(loopArgs, 1), env).integer;
                break;
        }

        // Create the new loop variable
        this.e.insert(loopVar, loopStartLexeme, env);
        Lexeme result = new Lexeme(BOOLEAN, false);

        for (int i = loopStart; i < loopEnd; i += loopStep) {
            result = this.eval(pt.right, env);
            // Update the loop variable after each iteration
            this.e.updateEnv(loopVar, new Lexeme(INTEGER, i + loopStep), env);
        }

        return result;
    }

    private Lexeme evalForEach(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme loopVar = pt.left.left;
        Lexeme loopObj = pt.left.right;
        Lexeme eLoopObj = this.eval(loopObj, env);
        this.e.insert(loopVar, null, env);
        Lexeme result = null;
        for (Lexeme l : eLoopObj.array) {
            this.e.updateEnv(loopVar, l, env);
            result = this.eval(pt.right, env);
        }

        return result;
    }

    private Lexeme evalLambda(Lexeme pt, Lexeme env) {
        return Lexeme.cons(CLOSURE, env, pt);
    }

    private Lexeme evalArrayDef(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme arrayVals = pt.right;
        ArrayList<Lexeme> array = new ArrayList<>();
        while (arrayVals != null) {
            array.add(this.eval(arrayVals.left, env));
            arrayVals = arrayVals.right;
        }
        return new Lexeme(ARRAY, array);
    }

    private Lexeme evalArrayAccess(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme arrayLexeme = this.e.lookupEnv(pt.left, env);
        int arrayIndex = this.eval(pt.right, env).integer;
        return arrayLexeme.array.get(arrayIndex);
    }

    private Lexeme evalFuncDef(Lexeme pt, Lexeme env) {
        Lexeme closure = Lexeme.cons(CLOSURE, env, pt);
        // Insert function name
        this.e.insert(pt.left, closure, env);
        return null;
    }

    private Lexeme evalVarDef(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme varName = pt.left;
        Lexeme varVal = pt.right;
        Lexeme init = this.eval(varVal, env);
        this.e.insert(varName, init, env);
        return null;
    }

    private Lexeme evalObj(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme xenv = e.extendEnv(env, null, null);
        try {
            Lexeme result = this.eval(pt.right, xenv);
            return result;
        }
        catch (ReturnEncounteredException ex) {
            return ex.retVal;
        }
    }

    private Lexeme evalProperty(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme obj = this.eval(pt.left, env);

        if (obj == null) {
            if (pt.left.type == FUNC_CALL) {
                Helpers.exitWithError(pt.left.left.str + " is null");
            }
            else {
                Helpers.exitWithError(pt.left.str + " is null");
            }
            return null;
        }

        // Create a new environment for string and array method calls
        if (obj.type != ENV) {
            Lexeme newEnv = this.e.extendEnv(null, null, env);
            Lexeme vars = Helpers.getVars(pt.right);
            Lexeme vals = this.eval(vars, env);
            this.e.insertList(vars, vals, newEnv);
            this.e.insert(pt.left, obj, newEnv);
            obj = newEnv;
        }

        if (obj.type != ENV && obj.type != ARRAY && obj.type != STRING) {
            Helpers.exitWithError("attempting to retrieve property on variable of type " + obj.type);
        }
        if (pt.right.type == ASSIGN) {
            // Evaluate the right hand side of the assignment in the calling environment
            Lexeme value = this.eval(pt.right.right, env);
            // Update the value in the object's environment
            this.e.updateEnv(pt.right.left, value, obj);
            return null;
        }
        if (pt.right.type == FUNC_CALL) {
            Lexeme eargs = this.eval(pt.right.right, env);
            return this.evalFuncCall(pt.right, eargs, obj);
        }

        return this.eval(pt.right, obj);
    }

    private Lexeme evalPrintln(Lexeme eargs, Lexeme env) {
        Lexeme printVal = Helpers.optionalListIndex(eargs, 0);
        if (eargs == null) {
            System.out.println();
        }
        else {
            System.out.println(Helpers.getPrintValWithDefault(printVal, null));
        }
        return null;
    }

    private Lexeme evalPrint(Lexeme eargs, Lexeme env) {
        Lexeme printVal = Helpers.optionalListIndex(eargs, 0);
        if (eargs == null) {
            System.out.print("");
        }
        else {
            System.out.print(Helpers.getPrintValWithDefault(printVal, null));
        }
        return null;
    }

    private Lexeme evalStr(Lexeme eargs, Lexeme env) {
        Lexeme strVal = Helpers.listIndex(eargs, 0);
        return new Lexeme(STRING, strVal.getVal().toString());
    }

    private Lexeme evalLength(Lexeme eargs, Lexeme env) {
        Lexeme obj = this.e.getStartVal(env);
        switch (obj.type) {
            case ARRAY:
                return new Lexeme(INTEGER, obj.array.size());
            case STRING:
                return new Lexeme(INTEGER, obj.str.length());
        }
        Helpers.exitIfNotStringOrArray("length");
        return null;
    }

    private Lexeme evalAppend(Lexeme eargs, Lexeme env) {
        Lexeme obj = this.e.getStartVal(env);
        Lexeme valToAdd = Helpers.listIndex(eargs, 0);
        switch (obj.type) {
            case ARRAY:
                obj.array.add(valToAdd);
                return null;
            case STRING:
                return new Lexeme(STRING, obj.str.concat(valToAdd.str));
        }
        Helpers.exitIfNotStringOrArray("append");
        return null;
    }

    private Lexeme evalInsert(Lexeme eargs, Lexeme env) {
        Lexeme obj = this.e.getStartVal(env);
        Lexeme valToAdd = Helpers.listIndex(eargs, 0);
        int index = Helpers.listIndex(eargs, 1).integer;
        switch (obj.type) {
            case ARRAY:
                obj.array.add(index, valToAdd);
                return null;
            case STRING:
                String newString = Helpers.insertStringRange(obj.str, valToAdd.str, index);
                return new Lexeme (STRING, newString);
        }
        Helpers.exitIfNotStringOrArray("insert");
        return null;
    }

    private Lexeme evalRemove(Lexeme eargs, Lexeme env) {
        Lexeme obj = this.e.getStartVal(env);
        Lexeme valToRemove = Helpers.listIndex(eargs, 0);
        switch (obj.type) {
            case ARRAY:
                for (Lexeme l : obj.array) {
                    if (l.getVal().equals(valToRemove.getVal())) {
                        obj.array.remove(l);
                        return new Lexeme(BOOLEAN, true);
                    }
                }
                return new Lexeme(BOOLEAN, false);
            case STRING:
                int removeLength = valToRemove.str.length();
                for (int i = 0; i <= obj.str.length() - removeLength; i++) {
                    if (obj.str.substring(i, i + removeLength).equals(valToRemove.str)) {
                        String newString = Helpers.removeStringRange(obj.str, i, i + removeLength);
                        return new Lexeme(STRING, newString);
                    }
                }
                return obj;
        }
        Helpers.exitIfNotStringOrArray("remove");
        return null;
    }

    private Lexeme evalRemoveAt(Lexeme eargs, Lexeme env) {
        Lexeme obj = this.e.getStartVal(env);
        Lexeme startRemove = Helpers.listIndex(eargs, 0);
        Lexeme endRemove = Helpers.optionalListIndex(eargs, 1);
        int start = startRemove.integer;
        int end = start + 1;
        if (endRemove != null) {
            end = endRemove.integer;
        }
        switch (obj.type) {
            case ARRAY:
                for (int i = end - 1; i >= start; i--) {
                    obj.array.remove(i);
                }
                return null;
            case STRING:
                String newString = Helpers.removeStringRange(obj.str, start, end);
                return new Lexeme(STRING, newString);
        }
        Helpers.exitIfNotStringOrArray("removeAt");
        return null;
    }

    private Lexeme evalInput(Lexeme eargs, Lexeme env) {
        Scanner scan;
        String inputFile = Helpers.listIndex(eargs, 0).str;
        ArrayList<Lexeme> array = new ArrayList<>();
        try {
            if (inputFile.equals("stdin")) {
                scan = new Scanner(System.in);
            } else {
                scan = new Scanner(new File(inputFile));
            }
            while (scan.hasNext()) {
                array.add(new Lexeme(STRING, scan.next()));
            }
        }
        catch (FileNotFoundException ex) {
            Helpers.exitWithError(ex.getMessage());
        }

        return new Lexeme(ARRAY, array);
    }

    private Lexeme evalGetInt(Lexeme eargs, Lexeme env) {
        String val = Helpers.listIndex(eargs, 0).str;
        try {
            return new Lexeme(INTEGER, Integer.parseInt(val));
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }

    private Lexeme evalEq(Lexeme eargs, Lexeme env) {
        Lexeme first = Helpers.listIndex(eargs, 0);
        Lexeme second = Helpers.listIndex(eargs, 1);
        if (first == null) {
            return new Lexeme(BOOLEAN, second == null);
        }
        if (first.type != ENV) {
            return new Lexeme(BOOLEAN, first.getVal() == second.getVal());
        }
        return new Lexeme(BOOLEAN, first.equals(second));
    }

    private Lexeme evalInspect(Lexeme eargs, Lexeme env) {
        Lexeme inspectVal = Helpers.listIndex(eargs, 0);
        Lexeme printVal = Helpers.optionalListIndex(eargs, 1);
        System.out.println(inspectVal.str + " is " + Helpers.getPrintValWithDefault(printVal, null));
        return null;
    }

    private Lexeme evalFuncCall(Lexeme pt, Lexeme env) throws ReturnEncounteredException {
        Lexeme args = pt.right;
        Lexeme eargs = this.eval(args, env);
        if (pt.left.str != null && pt.left.str.equals("inspect")) {
            eargs = Helpers.listCons(new Lexeme(STRING, pt.left.inspectVal), eargs);
        }
        return this.evalFuncCall(pt, eargs, env);
    }

    private Lexeme evalFuncCall(Lexeme pt, Lexeme eargs, Lexeme env) throws ReturnEncounteredException {
        Lexeme closure = this.eval(pt.left, env);
        if (closure == null) {
            Helpers.exitWithError(pt.left.str + " is null");
            return null;
        }
        return this.evalClosure(closure, eargs, env);
    }

    private Lexeme evalClosure(Lexeme closure, Lexeme eargs, Lexeme env) {
        if (closure.type == BUILTIN) {
            return closure.eval.apply(eargs, env);
        }
        Lexeme function = Helpers.getFunction(closure);
        Lexeme params = function.left;
        Lexeme denv = closure.left;
        Lexeme xenv = e.extendEnv(denv, params, eargs);
        // Eval function code
        try {
            return this.eval(function.right, xenv);
        }
        catch (ReturnEncounteredException ex) {
            return ex.retVal;
        }
    }
}
