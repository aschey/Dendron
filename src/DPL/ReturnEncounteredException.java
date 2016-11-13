package DPL;

/**
 * Created by aschey on 11/11/16.
 */
public class ReturnEncounteredException extends Exception {
    Lexeme retVal;
    ReturnEncounteredException(Lexeme retVal) {
        super("return encountered");
        this.retVal = retVal;
    }
}
