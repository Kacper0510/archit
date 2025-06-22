# archit
### Autorzy: Emil Wajda, Dawid Węcirz, Kacper Wojciuch

---
---

<br><br>

# Dokumentacja techniczna

## Wykorzystanie [Antlr4](https://www.antlr.org/)

Gramatyka języka została zdefiniowana w folderze [`src/main/antlr`](https://github.com/Kacper0510/archit/tree/master/src/main/antlr/archit/parser).

Jest to gramatyka podzielona, która składa się z osobnego leksera (`ArchitLexer.g4`) i parsera (`ArchitParser.g4`). Ten podział jest uzasadniony koniecznością wykorzystania wielu trybów leksera, które są używane specjalnie do interpolacji ciągów znaków zapisanych pomiędzy cudzysłowami.

Dla ułatwienia, przytaczamy całą gramatykę języka:

### `ArchitLexer.g4`
```antlr
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
```

Lekser jest w miarę typowy, zdefiniowane są w nim słowa kluczowe, operatory, znaki interpunkcyjne oraz reguły dla komentarzy i białych znaków.

Warto zwrócić uwagę na tryb interpolacji, który pozwala na przetwarzanie ciągów znaków z dynamicznie wplecionymi wartościami (`expr`).

### `ArchitParser.g4`
```antlr
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
```

Parser rozpoczyna pracę od reguły `program`, która składa się z instrukcji (`statement`) - dozwolony jest pusty program.

Instrukcje mogą być różnego rodzaju, w tym deklaracje funkcji, wywołania funkcji (bez lub z nawiasami), deklaracje zmiennych, przypisania, instrukcje warunkowe i pętle.

Niektóre instrukcje zawierają bloki kodu (`scopeStat`), które są otoczone nawiasami klamrowymi `{}`, z kolei część instrukcji zawiera wyrażenia (`expr`), które mogą być różnego rodzaju (liczby, ciągi znaków, zmienne, typy specjalne dla języka, operacje matematyczne i logiczne, wywołania funkcji oraz listy i mapy).

Warto zwrócić uwagę na regułę `interpolation`, która obsługuje interpolację ciągów znaków, pozwalając na dynamiczne wstawianie wartości do tekstu, oraz na regułę `nativeDecl`, która pozwala na deklarację funkcji natywnych, czyli takich, które są implementowane bezpośrednio w Javie i mogą być wywoływane z poziomu skryptu.

## Budowa projektu z użyciem [Gradle](https://gradle.org/)

Projekt budowany jest przy użyciu Gradle - najpopularniejszego narzędzia do automatyzacji budowy projektów w Javie. Jest to wymuszone m.in. przez konieczność budowania modyfikacji do Minecrafta, która wymaga specjalnych zależności i konfiguracji.

W pliku `build.gradle` znajdują się wszystkie niezbędne zależności, konfiguracje i zadania do budowy projektu. Oto kluczowe szczegóły:
- **Zależności**: Projekt korzysta z wielu bibliotek, w tym Antlr4 do parsowania języka, Fabric API do integracji z Minecraftem oraz bibliotek do obsługi kolorów ANSI w terminalu i eksportu modeli 3D.
- **Zadania**: Zdefiniowane są zadania do budowy projektu, uruchamiania klienta Minecrafta lub wersji terminalowej - dostarczają je różne pluginy Gradle'a. Utworzono również własne zadania automatyzujące kopiowanie przykładów przy uruchamianiu gry oraz poprawiania struktury plików generowanych przez Antlr4, aby różne IDE mogły poprawnie rozpoznać wszelkie wygenerowane klasy.
- **Shadow JAR**: Projekt korzysta z własnej implementacji podobnej do Maven Shade lub tzw. "fat JAR", która pozwala na spakowanie wszystkich zależności do jednego pliku JAR. Jest to przydatne w przypadku uruchamiania programu jako samodzielnej aplikacji, gdzie wszystkie zależności muszą być zawarte w jednym pliku. Jednocześnie, ten sam plik JAR może być używany jako mod do Minecrafta, gdzie zależności są dostarczane przez Fabric API.

## Przebiegi interpretera

## Implementacje tablic symboli i funkcji

## Ciekawsze aspekty implementacji

## Dokumentacja klas
