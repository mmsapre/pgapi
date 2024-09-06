package com.api.pg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    @Autowired
    private QueryTranslator queryTranslator;

    @Autowired
    private QueryExecutionService queryExecutionService;

    @PostMapping("/execute")
    public ResponseEntity<?> executeQuery(@RequestBody Map<String, Object> request) {
        try {
            // Translate and validate query
            String query = queryTranslator.translateAndValidateQuery(request);

            // Execute the query
            List<Map<String, Object>> result = queryExecutionService.executeQuery(query);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
