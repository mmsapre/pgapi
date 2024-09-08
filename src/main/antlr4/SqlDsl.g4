
grammar SqlDsl;

query: SELECT selectList FROM tableName (JOIN joinClause)* whereClause?;

selectList: column (',' column)*;
column: jsonbColumn | ID ('.' ID)?;

jsonbColumn: ID ('.' ID)? jsonbAccess;
jsonbAccess: ('->' | '->>') STRING (jsonbAccess)?;

tableName: ID;
joinClause: tableName ON condition;
condition: column OPERATOR value;

whereClause: WHERE condition (AND condition)*;

value: STRING | NUMBER | jsonbValue;

jsonbValue: '{' pair (',' pair)* '}';
pair: STRING ':' value;

SELECT: 'SELECT';
FROM: 'FROM';
JOIN: 'JOIN';
ON: 'ON';
WHERE: 'WHERE';
AND: 'AND';
OPERATOR: '=' | '>' | '<' | '>=' | '<=' | '<>' | '@>' | 'LIKE';
ID: [a-zA-Z_][a-zA-Z_0-9]*;
STRING: ''' .*? ''';
NUMBER: [0-9]+;

WS: [ 	
]+ -> skip;
