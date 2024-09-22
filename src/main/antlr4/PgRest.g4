grammar PgRest;

// Main query structure
query: selectClause fromClause (joinClause)* whereClause? orderClause? limitClause? offsetClause? SEMICOLON? EOF;

// SELECT clause
selectClause: SELECT selectList;

// FROM clause
fromClause: FROM tableName;

// JOIN clauses, with support for both regular JOIN and INNER JOIN
joinClause: joinType tableName ON condition;

joinType: JOIN | INNER JOIN;

// WHERE clause
whereClause: WHERE condition (AND condition)*;

// ORDER BY clause (optional)
orderClause: ORDER BY column (ASC | DESC)?;

// LIMIT clause (optional)
limitClause: LIMIT NUMBER;

// OFFSET clause (optional)
offsetClause: OFFSET NUMBER;

// List of columns in the SELECT clause
selectList: column (',' column)*;

// A column can either be a regular column or a JSONB column
column: (jsonbColumn | regularColumn) (AS? ID)?;

// Regular column (e.g., table.column or just column)
regularColumn: ID ('.' ID)?;

// JSONB column
jsonbColumn: regularColumn jsonbAccess;

// JSONB access (e.g., -> 'key' or ->> 'key')
jsonbAccess: ('->' | '->>' | '@>' | '#>>') STRING (jsonbAccess)?;

// Table name with optional schema and alias
tableName: (ID '.')? ID (ID)?;  // Optional schema name and table alias

// Condition for WHERE and JOIN clauses
condition: column OPERATOR (column | value);

// Possible values in conditions (including JSONB values and literals)
value: STRING | NUMBER | BOOLEAN | ID | jsonbValue;

// JSONB value as key-value pairs
jsonbValue: '{' pair (',' pair)* '}';

// Key-value pairs for JSONB objects
pair: STRING ':' value;

// SQL keywords
SELECT: 'SELECT';
FROM: 'FROM';
JOIN: 'JOIN';
INNER: 'INNER';
ON: 'ON';
WHERE: 'WHERE';
ORDER: 'ORDER';
BY: 'BY';
LIMIT: 'LIMIT';
OFFSET: 'OFFSET';
ASC: 'ASC';
DESC: 'DESC';
AS: 'AS';

// Operators for conditions and JSONB access
OPERATOR: '=' | '>' | '<' | '>=' | '<=' | '<>' | '@>' | 'LIKE';

// Identifiers for table names, column names, and aliases
ID: [a-zA-Z_][a-zA-Z_0-9_]*;

// String literals
STRING: '\'' ( ~['\\] | '\\' . )* '\'';

// Number literals
NUMBER: [0-9]+;

// Boolean literals
BOOLEAN: 'TRUE' | 'FALSE';

// Semicolon for query end
SEMICOLON: ';';

// Skip whitespace
WS: [ \t\r\n]+ -> skip;
