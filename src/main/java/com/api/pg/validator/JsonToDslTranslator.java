
package com.api.pg.validator;

import java.util.List;
import java.util.Map;

public class JsonToDslTranslator {

    private final DslValidator dslValidator;

    // Constructor to inject the validator to check allowed tables and columns
    public JsonToDslTranslator(DslValidator dslValidator) {
        this.dslValidator = dslValidator;
    }

    public String translate(Map<String, Object> queryRequest) {
        StringBuilder sql = new StringBuilder("SELECT ");

        // Handle SELECT fields
        List<String> selectFields = (List<String>) queryRequest.get("select");
        sql.append(String.join(", ", selectFields));

        // Handle FROM table
        String fromTable = (String) queryRequest.get("from");
        sql.append(" FROM ").append(fromTable);

        // Validate selected fields and table
        dslValidator.validateTableAndColumns(fromTable, selectFields);

        // Handle JOINs if any
        if (queryRequest.containsKey("join")) {
            List<Map<String, Object>> joinTables = (List<Map<String, Object>>) queryRequest.get("join");
            for (Map<String, Object> join : joinTables) {
                String tableToJoin = (String) join.get("table");
                Map<String, String> onCondition = (Map<String, String>) join.get("on");

                sql.append(" JOIN ").append(tableToJoin).append(" ON ");
                onCondition.forEach((left, right) -> sql.append(left).append(" = ").append(right).append(" "));
            }
        }

        // Handle WHERE conditions
        if (queryRequest.containsKey("where")) {
            sql.append(" WHERE ");
            Map<String, Object> whereConditions = (Map<String, Object>) queryRequest.get("where");
            whereConditions.forEach((field, condition) -> {
                if (condition instanceof Map) {
                    Map<String, Object> conditionMap = (Map<String, Object>) condition;
                    conditionMap.forEach((operator, value) -> {
                        switch (operator) {
                            case "eq": sql.append(field).append(" = ").append(value); break;
                            case "gt": sql.append(field).append(" > ").append(value); break;
                            case "lt": sql.append(field).append(" < ").append(value); break;
                            case "like": sql.append(field).append(" LIKE '").append(value).append("'"); break;
                            case "contains": sql.append(field).append(" @> '").append(value).append("'"); break;
                        }
                    });
                }
            });
        }

        // Handle pagination (limit and offset)
        if (queryRequest.containsKey("limit")) {
            sql.append(" LIMIT ").append(queryRequest.get("limit"));
        }
        if (queryRequest.containsKey("offset")) {
            sql.append(" OFFSET ").append(queryRequest.get("offset"));
        }

        return sql.toString();
    }
}
