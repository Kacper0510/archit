parser grammar ArchitParser;
options {
    tokenVocab = ArchitLexer;
}

// Initial rule
program: statement*;

// Statements
statement
    : functionDecl
    | functionCall SEMICOLON
    | functionCallNoBrackets SEMICOLON
    | varDecl
    | assignStat
    | ifStat
    | whileStat
    | repeatStat
    | breakStat
    | continueStat
    | returnStat
    | scopeStat;

returnStat: RETURN (expr | functionCallNoBrackets)? SEMICOLON;
scopeStat: CURLY_OPEN statement* CURLY_CLOSE;

// Variables
symbol: (PARENT)* ID;
varDecl
    : VAR symbol COLON type ASSIGN (expr | functionCallNoBrackets) SEMICOLON;
assignStat
    : symbol op = (ASSIGN | SPECIAL_ASSIGN) (expr | functionCallNoBrackets) SEMICOLON;

// Types
type: primitive = TYPE | listType | mapType | enumType;

listType: SQUARE_OPEN type SQUARE_CLOSE;
mapType: MAP type ARROW type MAP;
enumType: ANGLE_OPEN ID (COMMA ID)* ANGLE_CLOSE;

// If
ifStat: IF (expr | functionCallNoBrackets) scopeStat elseStat?;
elseStat: ELSE (scopeStat | ifStat);

// Expressions
expr
    : NUMBER
    | REAL
    | LOGIC
    | STRING
    | interpolation
    | materialExpr
    | listExpr
    | mapExpr
    | enumExpr
    | symbol
    | op = BRACE_OPEN expr BRACE_CLOSE
    | expr op = SQUARE_OPEN expr SQUARE_CLOSE
    | op = (MINUS | NOT) expr
    | expr op = POWER expr
    | expr op = MULTIPLY_OPS expr
    | expr op = (MINUS | PLUS) expr
    | expr op = (ANGLE_CLOSE | ANGLE_OPEN | COMPARE) expr
    | expr op = AND expr
    | expr op = OR expr
    | functionCall;

listExpr: SQUARE_OPEN (expr (COMMA expr)*)? SQUARE_CLOSE | TAG materialExpr;
mapExpr: MAP (expr ARROW expr (COMMA expr ARROW expr)*)? MAP;
enumExpr: ENUM ID;
materialExpr: ID? COLON ID;

// Loops
whileStat: WHILE (expr | functionCallNoBrackets) scopeStat;
repeatStat: REPEAT (expr | functionCallNoBrackets) scopeStat;
breakStat: BREAK SEMICOLON;
continueStat: CONTINUE SEMICOLON;

// Function calls
functionCall: ID BRACE_OPEN (expr (COMMA expr)*)? BRACE_CLOSE;
functionCallNoBrackets: ID (expr (COMMA expr)*)?;

// Function declarations
functionDecl
    : FUNCTION ID BRACE_OPEN functionParams? BRACE_CLOSE (COLON type)? scopeStat;
nativeDecl
    : NATIVE ID BRACE_OPEN functionParams? BRACE_CLOSE (COLON type)? SEMICOLON;

// Function parameter list
functionParams: functionParam (COMMA functionParam)*;

// Single function parameter: name and type
functionParam: symbol COLON type;

// Interpolation
interpolation
    : INTER_START (INTER_CONTENT | INTER_ESCAPE | INTER_BRACE expr CURLY_CLOSE)* INTER_END;