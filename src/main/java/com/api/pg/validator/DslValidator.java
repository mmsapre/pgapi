
package com.api.pg.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DslValidator {

    private final Map<String, List<String>> allowedTables;

    // Load allowed tables and columns from application.yml
    public DslValidator(
            @Value("#{${dsl.allowed.tables}}") List<Map<String, Object>> allowedTablesConfig) {
        this.allowedTables = new HashMap<>();
        for (Map<String, Object> tableConfig : allowedTablesConfig) {
            String tableName = (String) tableConfig.get("name");
            List<String> columns = (List<String>) tableConfig.get("columns");
            allowedTables.put(tableName, columns);
        }
    }

    // Validate table and columns
    public void validateTableAndColumns(String tableName, List<String> columns) throws IllegalArgumentException {
        if (!allowedTables.containsKey(tableName)) {
            throw new IllegalArgumentException("Table " + tableName + " is not allowed.");
        }

        List<String> allowedColumns = allowedTables.get(tableName);
        for (String column : columns) {
            if (!allowedColumns.contains(column)) {
                throw new IllegalArgumentException("Column " + column + " is not allowed for table " + tableName);
            }
        }
    }
}
