
package com.api.pg.validator;

import com.api.query.PgRestBaseVisitor;
import com.api.query.PgRestParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SqlDslVisitor extends PgRestBaseVisitor<String> {

    private List<Object> parameters = new ArrayList<>();
    @Override
    public String visitQuery(PgRestParser.QueryContext ctx) {
        StringBuilder query = new StringBuilder();

        // Process SELECT clause
        query.append(visit(ctx.selectClause())).append(" ");

        // Process FROM clause
        query.append(visit(ctx.fromClause())).append(" ");

        // Process JOIN clauses (if any)
        for (PgRestParser.JoinClauseContext joinCtx : ctx.joinClause()) {
            query.append(visit(joinCtx)).append(" ");
        }

        // Process WHERE clause (if present)
        if (ctx.whereClause() != null) {
            query.append(visit(ctx.whereClause())).append(" ");
        }

        // Process ORDER BY clause (if present)
        if (ctx.orderClause() != null) {
            query.append(visit(ctx.orderClause())).append(" ");
        }

        // Process LIMIT clause (if present)
        if (ctx.limitClause() != null) {
            query.append(visit(ctx.limitClause())).append(" ");
        }

        // Process OFFSET clause (if present)
        if (ctx.offsetClause() != null) {
            query.append(visit(ctx.offsetClause())).append(" ");
        }

        // Add semicolon at the end of the query if present
        if (ctx.SEMICOLON() != null) {
            query.append(";");
        }

        return query.toString().trim();
    }

    // Visit the SELECT clause
    @Override
    public String visitSelectClause(PgRestParser.SelectClauseContext ctx) {
        return "SELECT " + visit(ctx.selectList());
    }

    // Visit the FROM clause considering table name with alias
    @Override
    public String visitFromClause(PgRestParser.FromClauseContext ctx) {
        return "FROM " + visit(ctx.tableName());
    }

    // Visit the table name, including alias if present
    @Override
    public String visitTableName(PgRestParser.TableNameContext ctx) {
        StringBuilder tableName = new StringBuilder();

        // The table name is the first ID
        tableName.append(ctx.ID(0).getText());

        // If an alias is present, it is the second ID
        if (ctx.ID().size() > 1) {
            tableName.append(" ").append(ctx.ID(1).getText());  // Add alias after table name
        }

        return tableName.toString();
    }

    // Visit the JOIN clause, including condition
    @Override
    public String visitJoinClause(PgRestParser.JoinClauseContext ctx) {
        return "JOIN " + visit(ctx.tableName()) + " ON " + visit(ctx.condition());
    }

    // Visit the WHERE clause
    @Override
    public String visitWhereClause(PgRestParser.WhereClauseContext ctx) {
        StringBuilder whereClause = new StringBuilder("WHERE ");
        whereClause.append(visit(ctx.condition(0)));

        for (int i = 1; i < ctx.condition().size(); i++) {
            whereClause.append(" AND ").append(visit(ctx.condition(i)));
        }

        return whereClause.toString();
    }

    // Visit the LIMIT clause
    @Override
    public String visitLimitClause(PgRestParser.LimitClauseContext ctx) {
        return "LIMIT " + ctx.NUMBER().getText();
    }

    // Visit the OFFSET clause
    @Override
    public String visitOffsetClause(PgRestParser.OffsetClauseContext ctx) {
        return "OFFSET " + ctx.NUMBER().getText();
    }

    // Visit a column (regular or JSONB column) with optional alias
    @Override
    public String visitColumn(PgRestParser.ColumnContext ctx) {
        String columnStr;
        if (ctx.regularColumn() != null) {
            columnStr = visit(ctx.regularColumn());
        } else {
            columnStr = visit(ctx.jsonbColumn());
        }

        if (ctx.ID() != null) {
            columnStr += " AS " + ctx.ID().getText();  // Handle alias
        }

        return columnStr;
    }

    // Visit a regular column (table.column or just column)
    @Override
    public String visitRegularColumn(PgRestParser.RegularColumnContext ctx) {
        // Ensure we capture table alias and column name
        if (ctx.ID().size() == 2) {
            return ctx.ID(0).getText() + "." + ctx.ID(1).getText();  // e.g., t1.age
        } else {
            return ctx.ID(0).getText();
        }
    }

    // Visit a condition (e.g., column = value), which can now be a column or literal value
    @Override
    public String visitCondition(PgRestParser.ConditionContext ctx) {
        // Visit the left-hand column and right-hand value/column in the condition
        return visit(ctx.column(0)) + " " + ctx.OPERATOR().getText() + " " + visit(ctx.getChild(2));
    }

    // Visit value (could be STRING, NUMBER, BOOLEAN, ID, or JSONB)
    @Override
    public String visitValue(PgRestParser.ValueContext ctx) {
        if (ctx.STRING() != null) {
            return ctx.STRING().getText();
        } else if (ctx.NUMBER() != null) {
            return ctx.NUMBER().getText();
        } else if (ctx.BOOLEAN() != null) {
            return ctx.BOOLEAN().getText();
        } else if (ctx.ID() != null) {
            return ctx.ID().getText();  // Handle table identifiers like 't2'
        } else if (ctx.jsonbValue() != null) {
            return visit(ctx.jsonbValue());
        }
        return "";
    }

    // Visit a JSONB value (key-value pairs)
    @Override
    public String visitJsonbValue(PgRestParser.JsonbValueContext ctx) {
        StringBuilder jsonbValue = new StringBuilder("{");
        for (int i = 0; i < ctx.pair().size(); i++) {
            jsonbValue.append(visit(ctx.pair(i)));
            if (i < ctx.pair().size() - 1) {
                jsonbValue.append(", ");
            }
        }
        jsonbValue.append("}");
        return jsonbValue.toString();
    }

    // Visit key-value pairs in JSONB
    @Override
    public String visitPair(PgRestParser.PairContext ctx) {
        return ctx.STRING().getText() + ": " + visit(ctx.value());
    }

    public List<Object> getParameters() {
        return parameters;
    }
}
