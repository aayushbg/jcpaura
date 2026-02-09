package com.jio.jcpaura.controller;

import com.jio.jcpaura.entity.AuraGeneralMetrics;
import com.jio.jcpaura.service.AuraGeneralMetricsService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aura-metrics")
public class AuraGeneralMetricsController {

    @Autowired
    private AuraGeneralMetricsService metricsService;

    /**
     * Execute a JSON-based query or aggregation pipeline
     * POST /api/aura-metrics/query
     *
     * Request body can be:
     * 1. Simple query: {"type": "query", "data": "{ \"circle\": \"NORTH\" }"}
     * 2. Aggregation pipeline: {"type": "aggregation", "data": "[{\"$match\": {\"circle\": \"NORTH\"}}, {\"$group\": {\"_id\": \"$service_type\", \"count\": {\"$sum\": 1}}}]"}
     */
    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> executeQuery(@RequestBody QueryRequest request) {
        try {
            Map<String, Object> response = new HashMap<>();

            if ("aggregation".equalsIgnoreCase(request.getType())) {
                // Execute aggregation pipeline
                List<Document> results = metricsService.executeAggregation(request.getData());
                response.put("success", true);
                response.put("type", "aggregation");
                response.put("data", results);
                response.put("count", results.size());
            } else {
                // Execute simple query
                List<AuraGeneralMetrics> results = metricsService.executeQuery(request.getData());
                response.put("success", true);
                response.put("type", "query");
                response.put("data", results);
                response.put("count", results.size());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleError(e);
        }
    }

    /**
     * Get all metrics
     * GET /api/aura-metrics/all
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllMetrics() {
        try {
            List<AuraGeneralMetrics> results = metricsService.getAllMetrics();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results);
            response.put("count", results.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleError(e);
        }
    }

    /**
     * Get metric by ID
     * GET /api/aura-metrics/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMetricsById(@PathVariable String id) {
        try {
            var result = metricsService.getMetricsById(id);
            Map<String, Object> response = new HashMap<>();

            if (result.isPresent()) {
                response.put("success", true);
                response.put("data", result.get());
            } else {
                response.put("success", false);
                response.put("message", "Metrics not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleError(e);
        }
    }

    /**
     * Create new metrics
     * POST /api/aura-metrics/create
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createMetrics(@RequestBody AuraGeneralMetrics metrics) {
        try {
            AuraGeneralMetrics saved = metricsService.saveMetrics(metrics);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Metrics created successfully");
            response.put("data", saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return handleError(e);
        }
    }

    /**
     * Update existing metrics
     * PUT /api/aura-metrics/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable String id,
                                                            @RequestBody AuraGeneralMetrics metrics) {
        try {
            metrics.setId(id);
            AuraGeneralMetrics updated = metricsService.saveMetrics(metrics);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Metrics updated successfully");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleError(e);
        }
    }

    /**
     * Delete metric by ID
     * DELETE /api/aura-metrics/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMetrics(@PathVariable String id) {
        try {
            metricsService.deleteMetrics(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Metrics deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleError(e);
        }
    }

    /**
     * Handle errors and return standardized error response
     */
    private ResponseEntity<Map<String, Object>> handleError(Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", e.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Inner class to handle query/aggregation requests
     */
    public static class QueryRequest {
        private String type; // "query" or "aggregation"
        private String data; // JSON string for query or pipeline

        public QueryRequest() {
        }

        public QueryRequest(String type, String data) {
            this.type = type;
            this.data = data;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}

