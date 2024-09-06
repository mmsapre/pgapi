package com.api.pg;
import java.util.Map;
import java.util.List;
public class QueryTranslator {

    private PostgresSQLValidator validator;

    public QueryTranslator(PostgresSQLValidator validator) {
        this.validator = validator;
    }

    public String translateAndValidateQuery(Map<String, Object> request) throws Exception {
        String query = buildQueryFromJson(request);

        // Validate the translated query
        validator.validateQuery(query);

        return query;
    }

    private String buildQueryFromJson(Map<String, Object> request) {
        StringBuilder queryBuilder = new StringBuilder();

        // Extract table name
        String table = (String) request.get("table");
        if (!validator.isTableAllowed(table)) {
            throw new IllegalArgumentException("Table " + table + " is not allowed.");
        }

        // Extract and append fields
        List<String> fields = (List<String>) request.get("fields");
        String fieldString = String.join(", ", fields);
        queryBuilder.append("SELECT ").append(fieldString).append(" FROM ").append(table);

        // Extract and append conditions
        Map<String, Map<String, Object>> conditions = (Map<String, Map<String, Object>>) request.get("conditions");
        if (conditions != null && !conditions.isEmpty()) {
            queryBuilder.append(" WHERE ");
            for (Map.Entry<String, Map<String, Object>> conditionEntry : conditions.entrySet()) {
                String field = conditionEntry.getKey();
                Map<String, Object> conditionDetails = conditionEntry.getValue();
                String operator = (String) conditionDetails.get("operator");
                Object value = conditionDetails.get("value");

                queryBuilder.append(field).append(" ").append(operator).append(" '").append(value).append("' AND ");
            }
            queryBuilder.setLength(queryBuilder.length() - 5);  // Remove trailing ' AND '
        }

        // Extract and append sorting
        Map<String, String> sort = (Map<String, String>) request.get("sort");
        if (sort != null) {
            String sortField = sort.get("field");
            String sortOrder = sort.get("order");
            queryBuilder.append(" ORDER BY ").append(sortField).append(" ").append(sortOrder);
        }

        // Extract and append pagination
        Map<String, Integer> pagination = (Map<String, Integer>) request.get("pagination");
        if (pagination != null) {
            int limit = pagination.getOrDefault("limit", 10);
            int offset = pagination.getOrDefault("offset", 0);
            queryBuilder.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
        }

        return queryBuilder.toString();
    }
}
