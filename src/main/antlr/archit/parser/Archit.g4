grammar Archit;

// Initial rule
program: statement+;

// Statements
statement
    : functionDecl
    | functionCall ';'
    | functionCallNoBrackets ';'
    | varDecl
    | assignStat
    | ifStat
    | whileStat
    | repeatStat
    | breakStat
    | continueStat
    | returnStat
    | scopeStat;

returnStat: 'return' expr ';';
scopeStat: '{' statement+ '}';

// Variables
varDecl: 'var' ID ':' type '=' (expr | functionCallNoBrackets) ';';
assignStat: ID '=' (expr | functionCallNoBrackets) ';';

type
    : 'number'
    | 'real'
    | 'logic'
    | 'string'
    | 'material'
    | listType
    | mapType
    | enumType;

listType: '[' type ']';
mapType: '|' type '->' type '|';
enumType: '<' ID (',' ID)* '>';

// If
ifStat
    : 'if' (expr | functionCallNoBrackets) '{' statement+ '}' (
        'else if' (expr | functionCallNoBrackets) '{' statement+ '}'
    )* ('else' '{' statement+ '}')?;

// Expressions
expr
    : NUMBER
    | REAL
    | LOGIC
    | STRING
    | materialExpr
    | listExpr
    | mapExpr
    | enumExpr
    | ID
    | '(' expr ')'
    | expr ('+' | '-' | '*' | '/' | '^' | '%') expr
    | expr ('==' | '!=' | '>' | '>=' | '<' | '<=') expr
    | expr ('and' | 'or') expr
    | 'not' expr
    | expr '[' expr ']'
    | functionCall;

listExpr: '[' (expr (',' expr)*)? ']' | '#' materialExpr;
mapExpr: '|' (expr '->' expr (',' expr '->' expr)*)? '|';
enumExpr: '$' ID;
materialExpr: ID ':' ID | ':' ID;

// Loops
whileStat: 'while' (expr | functionCallNoBrackets) '{' statement+ '}';
repeatStat: 'repeat' (expr | functionCallNoBrackets) '{' statement+ '}';
breakStat: 'break' ';';
continueStat: 'continue' ';';

// Function calls
functionCall: ID '(' (expr (',' expr)*)? ')';
functionCallNoBrackets: ID (expr (',' expr)*)?;

// Function declarations
functionDecl
    : 'function' ID '(' functionParams? ')' ':' type '{' statement* '}'
    ;

// Function parameter list
functionParams
    : functionParam (',' functionParam)*
    ;

// Single function parameter: name and type
functionParam
    : ID ':' type
    ;

// Comments
COMMENT: '/*' .*? '*/' -> channel(HIDDEN);

LINE_COMMENT: '//' ~[\r\n]* -> channel(HIDDEN);


// Tokens
STRING: '\'' (~['\\] | '\\' .)* '\'';
NUMBER: [0-9]([0-9_]* [0-9])?;
REAL: [0-9]([0-9_]* [0-9])? '.' [0-9]([0-9_]* [0-9])? ( [eE] [-+]? [0-9]+)?;
LOGIC: 'true' | 'false';
ID: [a-zA-Z_][a-zA-Z0-9_]*;
WS: [ \t\r\n]+ -> skip;