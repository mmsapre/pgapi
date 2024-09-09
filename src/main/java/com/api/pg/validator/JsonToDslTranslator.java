package com.example.dsl;

import com.example.validation.QueryValidator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class JsonToDslTranslator {

    private final QueryValidator queryValidator;

    public JsonToDslTranslator(QueryValidator queryValidator) {
        this.queryValidator = queryValidator;
    }

    /**
     * Translates the Elasticsearch-style JSON query request to a DSL query string.
     * Handles JOINs, JSONB validation, and single table queries.
     * @param queryRequest The incoming Elasticsearch-style query request.
     * @return The corresponding DSL query string.
     * @throws IllegalArgumentException If the query is invalid.
     */
    public String translateToDsl(Map<String, Object> queryRequest) throws IllegalArgumentException {
        // Validate the query before translating it
        if (!queryValidator.validateQuery(queryRequest)) {
            throw new IllegalArgumentException("Invalid table, alias, or columns in the request.");
        }

        // Extract the source columns (_source) from the request
        List<String> selectColumns = (List<String>) queryRequest.get("_source");
        String select = String.join(", ", selectColumns);

        // Extract the table name or join tables
        String from = handleFromClause(queryRequest);

        // Start building the DSL query
        StringBuilder queryString = new StringBuilder();
        queryString.append("SELECT ").append(select).append(" FROM ").append(from);

        // Add WHERE clause if provided in the query
        if (queryRequest.containsKey("query")) {
            Map<String, Object> queryClause = (Map<String, Object>) queryRequest.get("query");
            queryString.append(" WHERE ").append(handleQueryClause(queryClause));
        }

        // Add LIMIT and OFFSET for pagination
        if (queryRequest.containsKey("size")) {
            int limit = (int) queryRequest.get("size");
            queryString.append(" LIMIT ").append(limit);
        }

        if (queryRequest.containsKey("from")) {
            int offset = (int) queryRequest.get("from");
            queryString.append(" OFFSET ").append(offset);
        }

        // Return the final DSL query string
        return queryString.toString();
    }

    // Handle the FROM clause for single and JOIN queries
    private String handleFromClause(Map<String, Object> queryRequest) {
        if (queryRequest.containsKey("joins")) {
            // Handling JOIN queries
            List<Map<String, Object>> joins = (List<Map<String, Object>>) queryRequest.get("joins");
            StringBuilder joinClause = new StringBuilder((String) queryRequest.get("from"));

            for (Map<String, Object> join : joins) {
                String joinType = (String) join.get("type"); // e.g., INNER, LEFT
                String joinTable = (String) join.get("table");
                String joinCondition = (String) join.get("on");

                joinClause.append(" ").append(joinType).append(" JOIN ").append(joinTable)
                        .append(" ON ").append(joinCondition);
            }

            return joinClause.toString();
        } else {
            // Single table queries
            return (String) queryRequest.get("from");
        }
    }

    // Handle the Elasticsearch-style WHERE clause
    private String handleQueryClause(Map<String, Object> queryClause) {
        Map<String, Object> boolClause = (Map<String, Object>) queryClause.get("bool");

        StringBuilder whereString = new StringBuilder();
        if (boolClause.containsKey("must")) {
            List<Map<String, Object>> mustClauses = (List<Map<String, Object>>) boolClause.get("must");
            whereString.append(handleMustClause(mustClauses));
        }

        return whereString.toString();
    }

    // Handle the MUST clause (logical AND) for multiple conditions
    private String handleMustClause(List<Map<String, Object>> mustClauses) {
        StringBuilder mustString = new StringBuilder();

        for (int i = 0; i < mustClauses.size(); i++) {
            Map<String, Object> condition = mustClauses.get(i);
            if (condition.containsKey("match")) {
                Map<String, Object> matchClause = (Map<String, Object>) condition.get("match");
                for (Map.Entry<String, Object> entry : matchClause.entrySet()) {
                    String column = entry.getKey();
                    Object value = entry.getValue();
                    mustString.append(column).append(" = '").append(value).append("'");
                }
            } else if (condition.containsKey("range")) {
                Map<String, Object> rangeClause = (Map<String, Object>) condition.get("range");
                for (Map.Entry<String, Object> entry : rangeClause.entrySet()) {
                    String column = entry.getKey();
                    Map<String, Object> rangeValues = (Map<String, Object>) entry.getValue();
                    for (Map.Entry<String, Object> range : rangeValues.entrySet()) {
                        String operator = convertRangeOperator(range.getKey());
                        Object value = range.getValue();
                        mustString.append(column).append(" ").append(operator).append(" ").append(value);
                    }
                }
            }

            // Add AND between conditions if there are multiple must clauses
            if (i < mustClauses.size() - 1) {
                mustString.append(" AND ");
            }
        }

        return mustString.toString();
    }

    // Convert Elasticsearch range operators to SQL operators
    private String convertRangeOperator(String esOperator) {
        switch (esOperator) {
            case "gte": return ">=";
            case "lte": return "<=";
            case "gt": return ">";
            case "lt": return "<";
            default: throw new IllegalArgumentException("Invalid range operator: " + esOperator);
        }
    }
}
