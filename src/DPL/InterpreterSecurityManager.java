package DPL;

/**
 * InterpreterSecurityManager
 * Allows the interactive interpreter to run without exiting on error
 */
class InterpreterSecurityManager extends SecurityManager {
    @Override
    public void checkExit(int status) {
        throw new SecurityException();
    }
}
