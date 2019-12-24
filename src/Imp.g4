grammar Imp;

prog : com EOF ;

com : IF LPAR exp RPAR THEN LBRACE com RBRACE
        (ELSE LBRACE com RBRACE)?                                       #if
    | IF LPAR exp RPAR THEN LBRACE com RBRACE
        (ELSEIF LPAR exp RPAR LBRACE com RBRACE)*
        (ELSE LBRACE com RBRACE)?                                       # elseif
    | ID ASSIGN exp                                                     # assign
    | SKIPP                                                             # skip
    | com SEMICOLON com                                                 # seq
    | WHILE LPAR exp RPAR LBRACE com RBRACE                             # while
    | FOR LPAR com SEMICOLON exp SEMICOLON com RPAR LBRACE
        com RBRACE                                                      # for
    | DO LBRACE com RBRACE WHILE LPAR exp RPAR                          # dowhile
    | OUT LPAR exp RPAR                                                 # out
    | ND LPAR com COMMA com RPAR                                        # notdeterm
    ;

exp : NAT                                 # nat
    | BOOL                                # bool
    | LPAR exp RPAR                       # parExp
    | <assoc=right> exp POW exp           # pow
    | NOT exp                             # not
    | exp op=(DIV | MUL | MOD) exp        # divMulMod
    | exp op=(PLUS | MINUS) exp           # plusMinus
    | exp op=(LT | LEQ | GEQ | GT) exp    # cmpExp
    | exp op=(EQQ | NEQ) exp              # eqExp
    | exp op=(AND | OR) exp               # logicExp
    | ID                                  # id
    ;

NAT : '0' | [1-9][0-9]* ;
BOOL : 'true' | 'false' ;

PLUS  : '+' ;
MINUS : '-';
MUL   : '*' ;
DIV   : '/' ;
MOD   : 'mod' ;
POW   : '^' ;

AND : '&' ;
OR  : '|' ;

EQQ : '==' ;
NEQ : '!=' ;
LEQ : '<=' ;
GEQ : '>=' ;
LT  : '<' ;
GT  : '>' ;
NOT : '!' ;

IF     : 'if' ;
THEN   : 'then' ;
ELSE   : 'else' ;
ELSEIF : 'elseif' ;
WHILE  : 'while' ;
FOR    : 'for' ;
DO     : 'do' ;
SKIPP  : 'skip' ;
ASSIGN : '=' ;
OUT    : 'out' ;
ND     : 'nd';

LPAR      : '(' ;
RPAR      : ')';
LBRACE    : '{' ;
RBRACE    : '}' ;
SEMICOLON : ';' ;
COMMA     : ',' ;

ID : [a-z]+ ;

WS : [ \t\r\n]+ -> skip ;
