package com.api.pg;
import com.api.query.PgRestListener;
import com.api.query.PgRestParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
public class PostgresSQLCustomListener implements PgRestListener {

    private QueryConfig queryConfig;
    private String tableName;

    public PostgresSQLCustomListener(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    @Override
    public void enterFrom_clause(PgRestParser.From_clauseContext ctx) {
        tableName = ctx.table_reference().IDENTIFIER(0).getText();
        if (!queryConfig.isTableAllowed(tableName)) {
            throw new IllegalArgumentException("Table " + tableName + " is not allowed.");
        }
    }

    // Triggered when entering a SELECT element, validates both regular columns and JSONB fields
    @Override
    public void enterSelect_element(PgRestParser.Select_elementContext ctx) {
        String field = ctx.getText();
        if (field.contains("->>")) {
            if (!queryConfig.isJsonbFieldAllowed(tableName, field)) {
                throw new IllegalArgumentException("JSONB field " + field + " is not allowed for table " + tableName);
            }
        } else {
            if (!queryConfig.isColumnAllowed(tableName, field)) {
                throw new IllegalArgumentException("Column " + field + " is not allowed for table " + tableName);
            }
        }
    }

    // Triggered when entering a JOIN clause, checks if the joining table is allowed
    @Override
    public void enterJoin_clause(PgRestParser.Join_clauseContext ctx) {
        String joinTable = ctx.table_reference().IDENTIFIER(0).getText();
        if (!queryConfig.isTableAllowed(joinTable)) {
            throw new IllegalArgumentException("Joining table " + joinTable + " is not allowed.");
        }
    }

    // Triggered when entering a WHERE clause, can be customized for validation
    @Override
    public void enterWhere_clause(PgRestParser.Where_clauseContext ctx) {
        // You can add custom logic for WHERE clause validation here if needed
    }

    // Triggered when entering the ORDER BY clause, can be customized for validation
    @Override
    public void enterOrder_by_clause(PgRestParser.Order_by_clauseContext ctx) {
        // You can add custom logic for ORDER BY clause validation here if needed
    }

    // Triggered when entering the LIMIT clause, can be customized for validation
    @Override
    public void enterLimit_clause(PgRestParser.Limit_clauseContext ctx) {
        // You can add custom logic for LIMIT clause validation here if needed
    }

    // Triggered when entering the OFFSET clause, can be customized for validation
    @Override
    public void enterOffset_clause(PgRestParser.Offset_clauseContext ctx) {
        // You can add custom logic for OFFSET clause validation here if needed
    }

    // The following are overrides of other methods in the base listener, providing empty implementations

    @Override
    public void enterQuery(PgRestParser.QueryContext ctx) {
        // Default implementation for enterQuery
    }

    @Override
    public void exitQuery(PgRestParser.QueryContext ctx) {
        // Default implementation for exitQuery
    }

    @Override
    public void enterSelect_clause(PgRestParser.Select_clauseContext ctx) {

    }

    @Override
    public void exitSelect_clause(PgRestParser.Select_clauseContext ctx) {

    }

    @Override
    public void enterSelect_elements(PgRestParser.Select_elementsContext ctx) {

    }

    @Override
    public void exitSelect_elements(PgRestParser.Select_elementsContext ctx) {

    }

    @Override
    public void exitFrom_clause(PgRestParser.From_clauseContext ctx) {
        // Default implementation for exitFrom_clause
    }

    @Override
    public void exitSelect_element(PgRestParser.Select_elementContext ctx) {
        // Default implementation for exitSelect_element
    }

    @Override
    public void exitJoin_clause(PgRestParser.Join_clauseContext ctx) {
        // Default implementation for exitJoin_clause
    }

    @Override
    public void enterJoin_type(PgRestParser.Join_typeContext ctx) {

    }

    @Override
    public void exitJoin_type(PgRestParser.Join_typeContext ctx) {

    }

    @Override
    public void exitWhere_clause(PgRestParser.Where_clauseContext ctx) {
        // Default implementation for exitWhere_clause
    }

    @Override
    public void exitOrder_by_clause(PgRestParser.Order_by_clauseContext ctx) {
        // Default implementation for exitOrder_by_clause
    }

    @Override
    public void exitLimit_clause(PgRestParser.Limit_clauseContext ctx) {
        // Default implementation for exitLimit_clause
    }

    @Override
    public void exitOffset_clause(PgRestParser.Offset_clauseContext ctx) {
        // Default implementation for exitOffset_clause
    }

    @Override
    public void enterCondition(PgRestParser.ConditionContext ctx) {

    }

    @Override
    public void exitCondition(PgRestParser.ConditionContext ctx) {

    }

    @Override
    public void enterExpression(PgRestParser.ExpressionContext ctx) {

    }

    @Override
    public void exitExpression(PgRestParser.ExpressionContext ctx) {

    }

    @Override
    public void enterJsonb_field(PgRestParser.Jsonb_fieldContext ctx) {

    }

    @Override
    public void exitJsonb_field(PgRestParser.Jsonb_fieldContext ctx) {

    }

    @Override
    public void enterColumn_name(PgRestParser.Column_nameContext ctx) {

    }

    @Override
    public void exitColumn_name(PgRestParser.Column_nameContext ctx) {

    }

    @Override
    public void enterTable_reference(PgRestParser.Table_referenceContext ctx) {

    }

    @Override
    public void exitTable_reference(PgRestParser.Table_referenceContext ctx) {

    }

    @Override
    public void enterValue(PgRestParser.ValueContext ctx) {

    }

    @Override
    public void exitValue(PgRestParser.ValueContext ctx) {

    }

    @Override
    public void enterComparison_operator(PgRestParser.Comparison_operatorContext ctx) {

    }

    @Override
    public void exitComparison_operator(PgRestParser.Comparison_operatorContext ctx) {

    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {

    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {

    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {

    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {

    }
}
