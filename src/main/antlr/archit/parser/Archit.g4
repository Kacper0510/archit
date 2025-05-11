grammar Archit;

// Initial rule
program: statement*;

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
scopeStat: '{' statement* '}';

// Variables
symbol: ID;
varDecl: 'var' symbol ':' type '=' (expr | functionCallNoBrackets) ';';
assignStat
    : symbol op = ('=' | '+=' | '-=' | '*=' | '/=' | '^=' | '%=') (
        expr
        | functionCallNoBrackets
    ) ';';

// Types
type
    : primitive = ('number' | 'real' | 'logic' | 'string' | 'material')
    | listType
    | mapType
    | enumType;

listType: '[' type ']';
mapType: '|' type '->' type '|';
enumType: '<' ID (',' ID)* '>';

// If
ifStat: 'if' (expr | functionCallNoBrackets) scopeStat elseIfStat* elseStat?;
elseIfStat: 'else if' (expr | functionCallNoBrackets) scopeStat;
elseStat: 'else' scopeStat;

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
    | symbol
    | op = '(' expr ')'
    | expr op = '[' expr ']'
    | op = ('-' | 'not') expr
    | expr op = '^' expr
    | expr op = ('*' | '/' | '%') expr
    | expr op = ('+' | '-') expr
    | expr op = ('==' | '!=' | '>' | '>=' | '<' | '<=') expr
    | expr op = 'and' expr
    | expr op = 'or' expr
    | functionCall;

listExpr: '[' (expr (',' expr)*)? ']' | '#' materialExpr;
mapExpr: '|' (expr '->' expr (',' expr '->' expr)*)? '|';
enumExpr: '$' ID;
materialExpr: ID? ':' ID;

// Loops
whileStat: 'while' (expr | functionCallNoBrackets) scopeStat;
repeatStat: 'repeat' (expr | functionCallNoBrackets) scopeStat;
breakStat: 'break' ';';
continueStat: 'continue' ';';

// Function calls
functionCall: ID '(' (expr (',' expr)*)? ')';
functionCallNoBrackets: ID (expr (',' expr)*)?;

// Function declarations
functionDecl: 'function' ID '(' functionParams? ')' (':' type)? scopeStat;
nativeDecl: 'native' ID '(' functionParams? ')' (':' type)? ';';

// Function parameter list
functionParams: functionParam (',' functionParam)*;

// Single function parameter: name and type
functionParam: symbol ':' type;

// Comments
COMMENT: '/*' .*? '*/' -> channel(HIDDEN);
LINE_COMMENT: '//' ~[\r\n]* -> channel(HIDDEN);

// Tokens
STRING: ['"] (~['\\] | '\\' .)* ['"];
NUMBER: [0-9]([0-9_]* [0-9])?;
REAL: [0-9]([0-9_]* [0-9])? '.' [0-9]([0-9_]* [0-9])? ( [eE] [-+]? [0-9]+)?;
LOGIC: 'true' | 'false';
ID: [a-zA-Z_][a-zA-Z0-9_]*;
WS: [ \t\r\n]+ -> skip;