grammar Archit;

//Reguła startowa
program: statement+ ;

//Możliwe statementy
statement
    : printStat
    | moveStat
    | placeStat
    ;

printStat: 'print' (STRING | '(' STRING ')') ';' ;
moveStat: 'move' '$' ID ';' ;
placeStat: 'place' ( 'minecraft:' ID | ':' ID ) ';' ;

//Tokeny
STRING: '\'' (~['\\] | '\\' .)* '\'' ;
ID: [a-zA-Z_][a-zA-Z0-9_]* ;
WS: [ \t\r\n]+ -> skip ;
