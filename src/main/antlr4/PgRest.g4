grammar PgRest;

query : select_clause from_clause join_clause* where_clause? order_by_clause? limit_clause? offset_clause? EOF ;

select_clause : SELECT (DISTINCT)? select_elements ;
select_elements : select_element (',' select_element)* ;
select_element : STAR | jsonb_field | column_name ;

from_clause : FROM table_reference ;

join_clause : join_type JOIN table_reference ON condition ;

join_type : INNER | LEFT | RIGHT ;

where_clause : WHERE condition ;

order_by_clause : ORDER BY column_name (ASC | DESC)? ;

limit_clause : LIMIT INTEGER ;

offset_clause : OFFSET INTEGER ;

condition : expression (AND expression | OR expression)* ;
expression : column_name comparison_operator value ;

jsonb_field : column_name '->>' STRING ;

column_name : IDENTIFIER ('.' IDENTIFIER)? ;

table_reference : IDENTIFIER (AS? IDENTIFIER)? ;

value : STRING | INTEGER ;

comparison_operator : '=' | '<>' | '>' | '<' | '>=' | '<=' | 'LIKE' ;

IDENTIFIER : [a-zA-Z_][a-zA-Z0-9_]* ;
STRING : '\'' .*? '\'' ;
INTEGER : [0-9]+ ;

WS : [ \t\r\n]+ -> skip ;

// Define lexer rules for the string literals
SELECT : 'SELECT' ;
FROM   : 'FROM' ;
JOIN   : 'JOIN' ;
ON     : 'ON' ;
WHERE  : 'WHERE' ;
ORDER  : 'ORDER' ;
BY     : 'BY' ;
LIMIT  : 'LIMIT' ;
OFFSET : 'OFFSET' ;
INNER  : 'INNER' ;
LEFT   : 'LEFT' ;
RIGHT  : 'RIGHT' ;
ASC    : 'ASC' ;
DESC   : 'DESC' ;
DISTINCT : 'DISTINCT' ;
STAR   : '*' ;
