package DPL;

/**
 * Created by aschey on 11/13/16.
 */
public class Interpreter {
    public static void main(String[] args) throws ReturnEncounteredException {
        new Evaluator().evaluate(args[0]);
    }
}
