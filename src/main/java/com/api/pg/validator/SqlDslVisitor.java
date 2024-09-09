package com.example.dsl;

import generated.SqlDslBaseVisitor;
import generated.SqlDslParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SqlDslVisitor extends SqlDslBaseVisitor<String> {

    private List<Object> parameters = new ArrayList<>();

    @Override
    public String visitQuery(SqlDslParser.QueryContext ctx) {
        StringBuilder sqlQuery = new StringBuilder();
        sqlQuery.append("SELECT ");
        sqlQuery.append(visit(ctx.selectList()));  // Visit the select list (columns)
        sqlQuery.append(" FROM ");
        sqlQuery.append(visit(ctx.tableName()));   // Visit the FROM clause (table)

        // Handle JOIN clauses
        if (ctx.joinClause() != null) {
            ctx.joinClause().forEach(join -> {
                sqlQuery.append(" JOIN ");
                sqlQuery.append(visit(join));
            });
        }

        // Handle WHERE clauses
        if (ctx.whereClause() != null) {
            sqlQuery.append(" WHERE ");
            sqlQuery.append(visit(ctx.whereClause()));
        }

        return sqlQuery.toString();
    }

    @Override
    public String visitSelectList(SqlDslParser.SelectListContext ctx) {
        return ctx.column().stream()
                .map(this::visit)
                .collect(Collectors.joining(", "));
    }

    @Override
    public String visitColumn(SqlDslParser.ColumnContext ctx) {
        if (ctx.jsonbColumn() != null) {
            return visit(ctx.jsonbColumn());  // Visit JSONB column
        } else if (ctx.ID().size() == 2) {
            return ctx.ID(0).getText() + "." + ctx.ID(1).getText();  // Handle column with table prefix
        }
        return ctx.ID(0).getText();  // Simple column
    }

    @Override
    public String visitJsonbColumn(SqlDslParser.JsonbColumnContext ctx) {
        StringBuilder column = new StringBuilder();
        if (ctx.ID().size() == 2) {
            column.append(ctx.ID(0).getText()).append(".").append(ctx.ID(1).getText());
        } else {
            column.append(ctx.ID(0).getText());
        }

        column.append(visit(ctx.jsonbAccess()));  // Append JSONB access operators and keys
        return column.toString();
    }

    @Override
    public String visitJsonbAccess(SqlDslParser.JsonbAccessContext ctx) {
        String operator = ctx.getChild(0).getText();  // '->' or '->>'
        String key = ctx.STRING().getText();
        StringBuilder jsonbAccess = new StringBuilder(operator)
                .append(" '")
                .append(key.replace("'", "''"))  // Escaping single quotes in the key
                .append("'");

        if (ctx.jsonbAccess() != null) {
            jsonbAccess.append(visit(ctx.jsonbAccess()));  // Recursively visit nested JSONB accesses
        }

        return jsonbAccess.toString();
    }

    @Override
    public String visitJoinClause(SqlDslParser.JoinClauseContext ctx) {
        return visit(ctx.tableName()) + " ON " + visit(ctx.condition());
    }

    @Override
    public String visitCondition(SqlDslParser.ConditionContext ctx) {
        parameters.add(visit(ctx.value()));  // Add value to the parameter list
        return visit(ctx.column()) + " " + ctx.OPERATOR().getText() + " ?";
    }

    @Override
    public String visitJsonbValue(SqlDslParser.JsonbValueContext ctx) {
        StringBuilder jsonValue = new StringBuilder("{");
        ctx.pair().forEach(pair -> {
            jsonValue.append(visit(pair)).append(",");
        });
        jsonValue.setLength(jsonValue.length() - 1);  // Remove the last comma
        jsonValue.append("}");
        parameters.add(jsonValue.toString());  // Add JSONB object to parameters
        return jsonValue.toString();
    }

    @Override
    public String visitWhereClause(SqlDslParser.WhereClauseContext ctx) {
        return ctx.condition().stream()
                .map(this::visit)
                .collect(Collectors.joining(" AND "));
    }

    public List<Object> getParameters() {
        return parameters;
    }
}
