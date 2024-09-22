
package com.api.pg.validator;

import com.api.query.PgRestBaseVisitor;
import com.api.query.PgRestParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SqlDslVisitor extends PgRestBaseVisitor<String> {

    private List<Object> parameters = new ArrayList<>();
    private boolean isCountQuery = false;  // Flag to control count query behavior

    public SqlDslVisitor(boolean isCountQuery) {
        this.isCountQuery = isCountQuery;
    }
    @Override
    public String visitQuery(PgRestParser.QueryContext ctx) {
        StringBuilder query = new StringBuilder();

        // Process SELECT clause (handle COUNT query if the flag is set)
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

        // Process ORDER BY clause (if present and not a count query)
        if (!isCountQuery && ctx.orderClause() != null) {
            query.append(visit(ctx.orderClause())).append(" ");
        }

        // Process LIMIT and OFFSET clauses (not needed in count queries)
        if (!isCountQuery) {
            if (ctx.limitClause() != null) {
                query.append(visit(ctx.limitClause())).append(" ");
            }
            if (ctx.offsetClause() != null) {
                query.append(visit(ctx.offsetClause())).append(" ");
            }
        }

        // Add semicolon at the end of the query if present
        if (ctx.SEMICOLON() != null) {
            query.append(";");
        }

        return query.toString().trim();
    }

    // Visit the SELECT clause with support for COUNT query
    @Override
    public String visitSelectClause(PgRestParser.SelectClauseContext ctx) {
        if (isCountQuery) {
            return "SELECT COUNT(*)";
        } else {
            return "SELECT " + visit(ctx.selectList());
        }
    }

    // Visit the FROM clause considering table name with alias and optional schema
    @Override
    public String visitFromClause(PgRestParser.FromClauseContext ctx) {
        return "FROM " + visit(ctx.tableName());
    }

    // Visit the table name, including optional schema and alias if present
    @Override
    public String visitTableName(PgRestParser.TableNameContext ctx) {
        StringBuilder tableName = new StringBuilder();

        if (ctx.ID().size() == 3) {
            tableName.append(ctx.ID(0).getText()).append("."); // Schema name
            tableName.append(ctx.ID(1).getText()); // Table name
            tableName.append(" ").append(ctx.ID(2).getText()); // Table alias
        } else if (ctx.ID().size() == 2) {
            tableName.append(ctx.ID(0).getText()); // Table name
            tableName.append(" ").append(ctx.ID(1).getText()); // Table alias
        } else {
            tableName.append(ctx.ID(0).getText()); // Table name only
        }

        return tableName.toString();
    }

    // Visit the JOIN clause, ensuring the correct join type is included
    @Override
    public String visitJoinClause(PgRestParser.JoinClauseContext ctx) {
        String joinType = visit(ctx.joinType());    // JOIN or INNER JOIN
        String tableName = visit(ctx.tableName());  // Table to join
        String condition = visit(ctx.condition());  // ON condition

        return joinType + " " + tableName + " ON " + condition;
    }

    // Visit join types (JOIN or INNER JOIN)
    @Override
    public String visitJoinType(PgRestParser.JoinTypeContext ctx) {
        // Check if the join type is "INNER JOIN" and handle it properly
        if (ctx.getText().equals("INNERJOIN")) {
            return "INNER JOIN";  // Add space between "INNER" and "JOIN"
        }
        return ctx.getText();  // Return the exact join type (JOIN, INNER JOIN, etc.)
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

    // Visit the LIMIT clause (skip if it's a count query)
    @Override
    public String visitLimitClause(PgRestParser.LimitClauseContext ctx) {
        return !isCountQuery ? "LIMIT " + ctx.NUMBER().getText() : "";
    }

    // Visit the OFFSET clause (skip if it's a count query)
    @Override
    public String visitOffsetClause(PgRestParser.OffsetClauseContext ctx) {
        return !isCountQuery ? "OFFSET " + ctx.NUMBER().getText() : "";
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

        // Apply alias only in SELECT statements
        if (ctx.ID() != null && ctx.getParent() instanceof PgRestParser.SelectListContext) {
            columnStr += " AS " + ctx.ID().getText();  // Handle alias
        }

        return columnStr;
    }

    // Visit a regular column (table.column or just column)
    @Override
    public String visitRegularColumn(PgRestParser.RegularColumnContext ctx) {
        if (ctx.ID().size() == 2) {
            return ctx.ID(0).getText() + "." + ctx.ID(1).getText();  // e.g., t1.age
        } else {
            return ctx.ID(0).getText();
        }
    }

    // Visit a condition (for JOIN or WHERE clauses), handles both sides of the condition
    @Override
    public String visitCondition(PgRestParser.ConditionContext ctx) {
        return visit(ctx.column(0)) + " " + ctx.OPERATOR().getText() + " " + visit(ctx.column(1));
    }

    @Override
    public String visitSelectList(PgRestParser.SelectListContext ctx) {
        // Collect all columns into a comma-separated list
        StringBuilder selectList = new StringBuilder();
        for (int i = 0; i < ctx.column().size(); i++) {
            selectList.append(visit(ctx.column(i)));
            if (i < ctx.column().size() - 1) {
                selectList.append(", "); // Append a comma between columns
            }
        }
        return selectList.toString();
    }
    public List<Object> getParameters() {
        return parameters;
    }
}
