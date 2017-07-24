# The Dendron Programming Language

Dendron files have the suffix ".den". Run Dendron files using the command "dpl filename.den".
Additionally, you can run "dpl" by itself to evaluate input from stdin.

NOTE: The java source files require the use of Java 8

## Objective
The objective of the Dendron programming language is to make it as fast to type as possible while 
still allowing whitespace to not matter in terms of formatting. As a result, square brackets have
replaced braces and parenthesis entirely. Also, variables are delimited by whitespace instead of commas. 

## Limitations
Using whitespace instead of commas has some limitations when it comes to negatives.
"1-1" must be written as "1 - 1" and "- 1" must be written as "-1" because unary negatives
are considered a single token as must be formatted as so.

## Comments
Comments are prefaced using a backtick (\`). 
**Ex:** ` This is a comment.

## Strings
Strings are denoted by single quotes (''). 
Ex: `'This is a string'.`

## Integers
Integers behave as they do in any standard language.

## Functions
Functions are declared using the keyword "def".
Ex:
    def a[b] [
        return b;
    ]

## Variables
variables are declared using the keyword "var". 
Ex: `var a = 2;`

## Arrays
Arrays a prefaced using a hash (#).
Declaring an array: `var a = #[1 2 3];`
Accessing an array: `#a[0]`

##For loops
For loops are similar to Python's. They allow for three variants.
Ex 1:
\`Print values from 0 to 9
    for [i 10] [
        println[i];
    ]
Ex 2:
\`Print values from 1 to 9
    for [i 1 10] [
        println[i];
    ]
Ex 3:
Print values from 1 to 9 in increments of 2
    for [i 1 10 2] [
        println[i];
    ]

## Foreach loops
Iterates through items in an array
Ex:
    var a = #[1 2 3];
    for [i in a] [
        println[i];
    ]

## While loops
Standard while loop
Ex:
    var i = 0;
    while [i < 10] [
        i = i + 1;
    ]

## Lambdas
Lambdas create an anonymous function.
Ex:
    var a = lambda[x] [ return x; ];

## Objects
Objects are created by defining a function and returning its environment (keyword "this").
Functions and variables inside of the object can be referenced by using a dot as the delimiter.
Arrays inside an object are still prefaced with a hash.
Objects can be nested.
Ex:
    def a[b c] [
        var d = #[1 2 3];
        def e[] [
            return 0;
        ]

        return this;
    ]

    var f = a[0 0];
    println[f.b];
    println[f.#d];
    println[f.e[]];

## Static objects
Static objects are created using the keyword "obj" and are assigned directly to a variable instead of 
being instantiated by a function call. These are useful for grouping related variables and functions 
that have no state.
Ex:
    var a = obj [
        var b = 2;
        def c[] [
            return b;
        ]
    ]

    println[a.b];
    println[a.c[]];

## Builtin functions
println: Prints to the console on a new line
Ex: `println['a'];`

print: Prints to the console on the same line
Ex: `print['a'];`

input: Retrieves input from a file or stdin and returns an array containing each whitespace-delimited token
Ex1: 
    var a = input['stdin'];
    println[#a[0]];
Ex2:
    var a = input['test.txt'];
    println[#a[0]];

str: Converts its argument into a string
Ex: println[str[1]];

eq: Returns true if two strings have the same value, two integers have the same value, or two objects point to the same value
Ex1: 
    println[eq['a' 'a']]; \`true
Ex2:
    var a = #[1 2];
    var b = #[1 2];
    println[eq[a b]]; \`false
Ex3:
    var a = #[1 2];
    var b = a;
    println[eq[a b]]; (true)
inspect: Same functionality as println but also tells what you're printing
Ex: `inspect[1 + 1]; \`result = "1 + 1 is 2"

## String and Array Methods
The following methodsa are applicable to strings and arrays.
Arrays are mutable, so they will modify the array.
Strings are immutable, so they will return a new string.

Length: Returns the length of the object
Ex: `'aa'.length[];`

Append: Adds an element to the end of the object
Ex: `#[1 2 3].append[4];`

Insert: Inserts an element at the specified index
First param: object to insert
Second param: index at which to insert
Ex: `'aa'.insert['bb' 1];`

Remove: Finds the specified substring or element and removes it from the object
Ex: #[1 2 3].remove[2];

RemoveAt: Removes the elements from the specified index or indexes. If no end index is specified, only the element at the start index will be removed.
First param: start index (inclusive)
Second param (optional): end index (exclusive)
Ex1: `'aaa'.removeAt[1];`
Ex2: `'aaa'.removeAt[1 3];`

## Invoke
The "invoke" function is used to invoke a lambda or a function returned from another function. This is an alternative to storing the pointer in a variable and calling it from there.
Ex1 
Without invoke:
    var a = lambda[a] [ return a; ];
    a[1];
With invoke:
    [lambda[a] [ return a; ]].invoke[1];
Ex2:
    def f[a] [
        return a;
    ]

    def g[] [
        return f;
    ]
Without invoke:
    var d = g[];
    g[1];

With invoke:
    [g[]].invoke[1]];

