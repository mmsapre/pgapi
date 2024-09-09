package com.api.pg.validator;

package com.example.validation;

import com.example.config.TableConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

@Component
public class QueryValidator {

    private final TableConfig tableConfig;

    public QueryValidator(TableConfig tableConfig) {
        this.tableConfig = tableConfig;
    }

    /**
     * Validates the table/alias and columns in the query request.
     * Handles columns that are prefixed with multiple table aliases.
     * @param queryRequest The incoming query request containing "from" (table/alias) and "_source" (columns).
     * @return true if the query is valid, false otherwise.
     */
    public boolean validateQuery(Map<String, Object> queryRequest) {
        String fromTableOrAlias = (String) queryRequest.get("from");
        List<Map<String, Object>> joins = (List<Map<String, Object>>) queryRequest.get("joins");
        List<String> requestedColumns = (List<String>) queryRequest.get("_source");

        // Create a map to track alias -> real table name
        Map<String, String> aliasToTableMap = buildAliasToTableMap(fromTableOrAlias, joins);

        // Validate columns based on their alias and the corresponding real table
        for (String column : requestedColumns) {
            String[] columnParts = column.split("\\."); // Split alias and column name (e.g., "ef.flt_name")
            if (columnParts.length != 2) {
                // Invalid column format, should be in form "alias.column"
                return false;
            }
            String alias = columnParts[0];      // Extract alias (e.g., "ef")
            String columnName = columnParts[1]; // Extract column name (e.g., "flt_name")

            // Get the real table corresponding to the alias
            String realTable = aliasToTableMap.get(alias);
            if (realTable == null) {
                // Invalid alias
                return false;
            }

            // Validate the column name for the real table
            TableConfig.TableInfo tableInfo = tableConfig.getName().get(realTable);
            if (tableInfo == null || !Set.of(tableInfo.getColumns()).contains(columnName)) {
                // Invalid column for the table
                return false;
            }
        }

        return true;
    }

    /**
     * Builds a map of alias -> real table name from the "from" and "joins" clauses.
     * @param fromTableOrAlias The "from" clause containing the base table and its alias.
     * @param joins The list of joins containing tables and their aliases.
     * @return A map where the key is the alias and the value is the corresponding real table name.
     */
    private Map<String, String> buildAliasToTableMap(String fromTableOrAlias, List<Map<String, Object>> joins) {
        Map<String, String> aliasToTableMap = new HashMap<>();

        // Handle the "from" table (e.g., "ent_flt ef")
        String[] fromParts = fromTableOrAlias.split("\\s+"); // Split "table alias"
        String fromTable = fromParts[0];   // Real table name (e.g., "ent_flt")
        String fromAlias = fromParts.length > 1 ? fromParts[1] : fromTable; // Alias or table if no alias provided
        aliasToTableMap.put(fromAlias, fromTable);

        // Handle the joins (e.g., "flt_type ft", "flt_details fd")
        if (joins != null) {
            for (Map<String, Object> join : joins) {
                String joinTable = (String) join.get("table"); // Get the joined table (e.g., "flt_type ft")
                String[] joinParts = joinTable.split("\\s+");   // Split "table alias"
                String realJoinTable = joinParts[0];            // Real table name (e.g., "flt_type")
                String joinAlias = joinParts.length > 1 ? joinParts[1] : realJoinTable; // Alias or table if no alias
                aliasToTableMap.put(joinAlias, realJoinTable);
            }
        }

        return aliasToTableMap;
    }
}
