package DPL;
import java.util.Scanner;

/**
 * Created by aschey on 11/13/16.
 */
public class Interpreter {
    public static void main(String[] args) throws ReturnEncounteredException {
        Lexeme env = new Environment().createEnv();
        if (args.length > 0) {
            new Evaluator().evaluate(args[0], Helpers.InputType.FILE, env);
        }
        else {
            Scanner scan = new Scanner(System.in);
            Evaluator eval = new Evaluator();
            while (scan.hasNext()) {
                env = eval.evaluate(scan.nextLine(), Helpers.InputType.STDIN, env);
            }
        }
    }
}
