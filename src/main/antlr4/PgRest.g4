grammar PgRest;

// Main query structure, includes SELECT, FROM, JOIN, WHERE, LIMIT, OFFSET, etc.
query: selectClause fromClause (joinClause)* whereClause? orderClause? limitClause? offsetClause? SEMICOLON? EOF;

// SELECT clause, handles selecting columns (with aliases)
selectClause: SELECT selectList;

// FROM clause, specifies the base table and optional alias
fromClause: FROM tableName;

// JOIN clause with a condition (can have multiple JOINs)
joinClause: JOIN tableName ON condition;

// WHERE clause, allows multiple conditions joined by AND
whereClause: WHERE condition (AND condition)*;

// ORDER BY clause
// ORDER BY clause (optional)
orderClause: ORDER BY column (ASC | DESC)?;

// LIMIT clause (optional)
limitClause: LIMIT NUMBER;

// OFFSET clause (optional)
offsetClause: OFFSET NUMBER;

// List of columns in the SELECT clause, columns can have aliases
selectList: column (',' column)*;

// A column can either be a regular column, a JSONB column, or a column with an alias
column: (jsonbColumn | regularColumn) (AS? ID)?;

// Regular column (e.g., table.column or just column)
regularColumn: ID ('.' ID)?;

// JSONB column with access operators (->, ->>, @>, #>>), allowing nested access
jsonbColumn: regularColumn jsonbAccess;

// JSONB access (e.g., -> 'key' or ->> 'key')
jsonbAccess: ('->' | '->>' | '@>' | '#>>') STRING (jsonbAccess)?;

// Table name with optional alias
tableName: ID (ID)?;  // The second ID represents the table alias

// Condition for WHERE and JOIN clauses, allowing the right-hand side to be a value or a column
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

// Handle semicolon at the end of a query
SEMICOLON: ';';

// Skip whitespace
WS: [ \t\r\n]+ -> skip;
