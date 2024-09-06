package com.api.pg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.List;

@Component
public class QueryConfig {

    // Load allowed tables and their columns and jsonb fields
    @Value("#{${query.allowed-tables}}")
    private Map<String, Map<String, List<String>>> allowedTables;

    public Map<String, Map<String, List<String>>> getAllowedTables() {
        return allowedTables;
    }

    public boolean isTableAllowed(String tableName) {
        return allowedTables.containsKey(tableName);
    }

    public boolean isColumnAllowed(String tableName, String columnName) {
        List<String> allowedColumns = allowedTables.get(tableName).get("columns");
        return allowedColumns != null && allowedColumns.contains(columnName);
    }

    public boolean isJsonbFieldAllowed(String tableName, String jsonbField) {
        List<String> allowedJsonbFields = allowedTables.get(tableName).get("jsonb");
        return allowedJsonbFields != null && allowedJsonbFields.contains(jsonbField);
    }
}
