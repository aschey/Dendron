package DPL;
import java.util.Scanner;

/**
 * Interpreter
 * Interfaces with user input
 */
public class Interpreter {
    public static void main(String[] args) throws ReturnEncounteredException {
        Evaluator eval = new Evaluator();
        Lexeme env = eval.addBultins();

        if (args.length > 0) {
            eval.evaluate(args[0], InputType.FILE, env);
        }
        else {
            // When running the interactive interpreter, don't let System.exit() kill the program
            // Instead, display the error to the user and continue
            InterpreterSecurityManager manager = new InterpreterSecurityManager();
            System.setSecurityManager(manager);

            Scanner scan = new Scanner(System.in);

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
