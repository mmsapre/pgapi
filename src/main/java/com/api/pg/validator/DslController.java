
package com.api.pg.validator;

import com.example.dsl.DslQueryExecutor;
import com.example.dsl.JsonToDslTranslator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dsl")
public class DslController {

    private final DataSource dataSource;
    private final JsonToDslTranslator jsonToDslTranslator;

    // Inject the translator into the controller
    public DslController(DataSource dataSource, JsonToDslTranslator jsonToDslTranslator) {
        this.dataSource = dataSource;
        this.jsonToDslTranslator = jsonToDslTranslator;
    }

    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> executeElasticSearchStyleQuery(@RequestBody Map<String, Object> queryRequest) {
        try (Connection connection = dataSource.getConnection()) {
            DslQueryExecutor executor = new DslQueryExecutor(connection);

            // Use the translator to convert the JSON request to SQL
            String sql = jsonToDslTranslator.translate(queryRequest);

            // Execute query
            ResultSet resultSet = executor.executeQuery(sql, null);  // Assuming no parameters for now
            Map<String, Object> result = processResultSet(resultSet);
            
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "SQL error occurred"));
        }
    }

    // Helper method to process the ResultSet and return it as a JSON structure
    private Map<String, Object> processResultSet(ResultSet resultSet) throws SQLException {
        // Logic to process the ResultSet and convert it into a JSON-like structure
        // ...
    }
}
