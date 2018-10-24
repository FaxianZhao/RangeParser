grammar Range;

prog
    : stat ;

stat
    : expr ;

expr
    : NON expr                                     # NonExpr
    | left = expr op = ( AND | OR ) right = expr   # AndOrExpr
    | LOPEN expr ROPEN                             # ParenExpr
    | COMPARABLE                                   # ComparLit
    | signal = (GREATER | LESS | GEUQAL | LEQUAL | EQUAL) COMPARABLE # SelfExpr
    | left = (LOPEN | LCLOSE) COMPARABLE COMMA COMPARABLE right = (ROPEN | RCLOSE)  # BracksExpr;

OR      : 'OR' | 'or' | '|' | '||' ;
AND     : 'AND' | 'and' | '&' | '&&' ;
NON     : '!' | '!=' | '^' ;
LOPEN   : '(' ;
ROPEN   : ')' ;
LCLOSE  : '[' ;
RCLOSE  : ']' ;
COMMA   : ',' ;
GREATER : '>' ;
LESS    : '<' ;
GEUQAL  : '>=' ;
LEQUAL  : '<=' ;
EQUAL   : '=' | '==' ;

// modify this as your wish
COMPARABLE : '-' ? DIGITS;

//CRLF    : '\r'? '\n' | '\r' ;
WS      : [ \r\n\t]+ -> channel(HIDDEN);

fragment DIGITS: '1'..'9' '0'..'9'*;