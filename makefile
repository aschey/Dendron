all: TokenType.class Lexeme.class Helpers.class InputType.class Lexer.class Recognizer.class ReturnEncounteredException.class Environment.class Evaluator.class InterpreterSecurityManager.class Interpreter.class

TokenType.class: TokenType.java
	javac -d . -classpath . TokenType.java

Lexeme.class: Lexeme.java
	javac -d . -classpath . Lexeme.java
	
Helpers.class: Helpers.java
	javac -d . -classpath . Helpers.java
	
InputType.class: InputType.java
	javac -d . -classpath . InputType.java
	
Lexer.class: Lexer.java
	javac -d . -classpath . Lexer.java
	
Recognizer.class: Recognizer.java
	javac -d . -classpath . Recognizer.java
	
ReturnEncounteredException.class: ReturnEncounteredException.java
	javac -d . -classpath . ReturnEncounteredException.java
	
Environment.class: Environment.java
	javac -d . -classpath . Environment.java
	
Evaluator.class: Evaluator.java
	javac -d . -classpath . Evaluator.java

Interpreter.class: Interpreter.java
	javac -d . -classpath . Interpreter.java
	
InterpreterSecurityManager.class: InterpreterSecurityManager.java
	javac -d . -classpath . InterpreterSecurityManager.java

clean:
	rm DPL/*

# Examples
error1: examples/error1.den
	cat examples/error1.den

error1x: examples/error1.den
	dpl examples/error1.den

error2: examples/error2.den
	cat examples/error2.den

error2x: examples/error2.den
	dpl examples/error2.den

error3: examples/error3.den
	cat examples/error3.den

error3x: examples/error3.den
	dpl examples/error3.den
	
arrays: examples/arrays.den
	cat examples/arrays.den

arraysx: examples/arrays.den
	dpl examples/arrays.den
	
conditionals: examples/conditionals.den
	cat examples/conditionals.den

conditionalsx: examples/conditionals.den
	dpl examples/conditionals.den
	
recursion: examples/recursion.den
	cat examples/recursion.den

recursionx: examples/recursion.den
	dpl examples/recursion.den
	
iteration: examples/iteration.den
	cat examples/iteration.den

iterationx: examples/iteration.den
	dpl examples/iteration.den

functions: examples/functions.den
	cat examples/functions.den

functionsx: examples/functions.den
	dpl examples/functions.den
	
dictionary: examples/dictionary.den
	cat examples/dictionary.den

dictionaryx: examples/dictionary.den
	dpl examples/dictionary.den

problem: examples/rpn.den
	cat examples/rpn.den

problemx: examples/rpn.den
	dpl examples/rpn.den
