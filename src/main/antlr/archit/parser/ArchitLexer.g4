lexer grammar ArchitLexer;

// Keywords and operators
LOGIC: 'true' | 'false';
TYPE: 'number' | 'real' | 'logic' | 'string' | 'material';
SPECIAL_ASSIGN: '+=' | '-=' | '*=' | '/=' | '^=' | '%=';
MINUS: '-';
PLUS: '+';
NOT: 'not';
COMPARE: '==' | '!=' | '<=' | '>=';
MULTIPLY_OPS: [*/%];
RETURN: 'return';
BREAK: 'break';
CONTINUE: 'continue';
VAR: 'var';
FUNCTION: 'function';
NATIVE: 'native';
IF: 'if';
ELSE: 'else';
AND: 'and';
OR: 'or';
WHILE: 'while';
REPEAT: 'repeat';

// Punctuation
SEMICOLON: ';';
COLON: ':';
COMMA: ',';
BRACE_OPEN: '(';
BRACE_CLOSE: ')';
SQUARE_OPEN: '[';
SQUARE_CLOSE: ']';
ANGLE_OPEN: '<';
ANGLE_CLOSE: '>';
CURLY_OPEN: '{' -> pushMode(DEFAULT_MODE);
CURLY_CLOSE: '}' -> popMode;
ARROW: '->';
MAP: '|';
ASSIGN: '=';
POWER: '^';
TAG: '#';
ENUM: '$';
PARENT: '~';

// Comments
COMMENT: '/*' .*? '*/' -> channel(HIDDEN);
LINE_COMMENT: '//' ~[\r\n]* -> channel(HIDDEN);

// Compound rules
STRING: '\'' (~['\\] | '\\\'' | '\\\\')* '\'';
NUMBER: [0-9]([0-9_]* [0-9])?;
REAL: [0-9]([0-9_]* [0-9])? '.' [0-9]([0-9_]* [0-9])? ( [eE] [-+]? [0-9]+)?;
ID: [a-zA-Z_][a-zA-Z0-9_]*;
WS: [ \t\r\n]+ -> skip;

// Interpolation
INTER_START: '"' -> pushMode(INTERPOLATION_MODE);

mode INTERPOLATION_MODE;

INTER_CONTENT: ~["\\{}]+;
INTER_ESCAPE: '\\' ["\\{}];
INTER_BRACE: '{' -> pushMode(DEFAULT_MODE);
INTER_END: '"' -> popMode;