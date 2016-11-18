package DPL;
import java.util.Scanner;

/**
 * Created by aschey on 11/13/16.
 */
public class Interpreter {
    public static void main(String[] args) throws ReturnEncounteredException {
        Lexeme env = new Environment().createEnv();
        if (args.length > 0) {
            new Evaluator().evaluate(args[0], InputType.FILE, env);
        }
        else {
            // When running the interactive interpreter, don't let System.exit() kill the program
            // Instead, display the error to the user and continue
            InterpreterSecurityManager manager = new InterpreterSecurityManager();
            System.setSecurityManager(manager);

            Scanner scan = new Scanner(System.in);
            Evaluator eval = new Evaluator();

            while (scan.hasNext()) {
                try {
                    env = eval.evaluate(scan.nextLine(), InputType.STDIN, env);
                }
                catch (SecurityException ex) {
                    if (ex.getMessage() != null) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
        }
    }
}
