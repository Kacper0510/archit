grammar Archit;

// Reguła startowa
program: statement+ ;

// Możliwe statementy
statement
    : printStat
    | moveStat
    | placeStat
    | varDecl
    | assignStat       
    | ifStat
    | whileStat         
    | repeatStat        
    | breakStat         
    | continueStat      
    ;

// Deklaracje i przypisywanie zmiennych
varDecl: 'var' ID ':' type '=' expr ';' ;
assignStat: ID '=' expr ';' ;


type: 'number' | 'real' | 'logic' | 'string' | 'material' | listType | mapType | enumType;

listType: '[' type ']';
mapType: '|' type '->' type '|';
enumType: '<' ID (',' ID)* '>';

// If
ifStat: 'if' expr '{' statement+ '}' ('else if' expr '{' statement+ '}')* ('else' '{' statement+ '}')?;

// Wyrażenia
expr
    : NUMBER
    | REAL
    | LOGIC
    | STRING
    | MATERIAL
    | listExpr
    | mapExpr
    | enumExpr
    | ID
    | '(' expr ')'
    | expr ('+' | '-' | '*' | '/' | '^' | '%') expr
    | expr ('==' | '!=' | '>' | '>=' | '<' | '<=') expr
    | expr ('and' | 'or') expr
    | 'not' expr
    ;

listExpr: '[' (expr (',' expr)*)? ']';
mapExpr: '|' (expr '->' expr (',' expr '->' expr)*)? '|';
enumExpr: '$' ID;

// Statystyki
printStat: 'print' (STRING | '(' STRING ')') ';' ;
moveStat: 'move' ( '$' ID | '(' '$' ID ')' ) ';' ;
placeStat: 'place' ( 'minecraft:' ID | ':' ID | '(' 'minecraft:' ID ')' | '(' ':' ID ')' ) ';' ;
whileStat: 'while' expr '{' statement+ '}' ;
repeatStat: 'repeat' expr '{' statement+ '}' ;
breakStat: 'break' ';' ;
continueStat: 'continue' ';' ;


// Tokeny
STRING: '\'' (~['\\] | '\\' .)* '\'' ;
NUMBER: [0-9]([0-9_]*[0-9])? ;
REAL: [0-9]([0-9_]*[0-9])? '.' [0-9]([0-9_]*[0-9])? ( [eE] [-+]? [0-9]+ )? ;
LOGIC: 'true' | 'false' ;
MATERIAL: ('minecraft:' ID | ':' ID);
ID: [a-zA-Z_][a-zA-Z0-9_]* ;
WS: [ \t\r\n]+ -> skip ;
