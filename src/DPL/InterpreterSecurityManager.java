package DPL;

/**
 * Created by aschey on 11/18/16.
 */
public class InterpreterSecurityManager extends SecurityManager {
    @Override
    public void checkExit(int status) {
        throw new SecurityException();
    }
}
