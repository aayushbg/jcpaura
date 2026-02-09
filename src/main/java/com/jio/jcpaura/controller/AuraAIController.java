package com.jio.jcpaura.controller;

import com.jio.jcpaura.service.AuraAIQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for AI-powered natural language query processing
 * Handles the /message endpoint for processing user queries through the 4-step AI pipeline
 */
@RestController
@RequestMapping("/api/aura-ai")
public class AuraAIController {

    @Autowired
    private AuraAIQueryService aiQueryService;

    /**
     * Process a natural language message and return AI-powered response with data
     * POST /api/aura-ai/message
     *
     * Request body:
     * {
     *   "message": "What is the availability in Karnataka?"
     * }
     *
     * Response body:
     * {
     *   "success": true,
     *   "originalMessage": "What is the availability in Karnataka?",
     *   "entityIdentification": "Entity: auraGeneralMetrics - The user is asking about network performance metrics...",
     *   "mongoQuery": "{ \"circle\": \"Karnataka\" }",
     *   "resultCount": 5,
     *   "queryResults": [...],
     *   "response": "Based on the Karnataka circle metrics, the availability is...",
     *   "timestamp": 1707458688000
     * }
     */
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> processMessage(@RequestBody MessageRequest request) {
        try {
            // Validate input
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse(
                        "Message cannot be empty"
                ));
            }

            // Process the query through the 4-step pipeline
            Map<String, Object> result = aiQueryService.processQuery(request.getMessage());

            // Add timestamp
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    createErrorResponse(e.getMessage())
            );
        }
    }

    /**
     * Health check for AI query service
     * GET /api/aura-ai/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("status", "AI Query Service is running");
        result.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(result);
    }

    /**
     * Get API documentation
     * GET /api/aura-ai/docs
     */
    @GetMapping("/docs")
    public ResponseEntity<Map<String, Object>> getDocumentation() {
        Map<String, Object> docs = new HashMap<>();
        docs.put("endpoint", "/api/aura-ai/message");
        docs.put("method", "POST");
        docs.put("description", "Process natural language queries using AI-powered multi-step query pipeline");
        docs.put("requestBody", new HashMap<String, Object>() {{
            put("message", "The user's natural language query (string)");
        }});
        docs.put("responseBody", new HashMap<String, Object>() {{
            put("success", "Boolean indicating if the query was processed successfully");
            put("originalMessage", "The original user message");
            put("entityIdentification", "AI's identification of which table/entity is being queried");
            put("mongoQuery", "The generated MongoDB query");
            put("resultCount", "Number of results returned");
            put("queryResults", "Array of data returned from the database");
            put("response", "Formatted response from AI based on the data");
            put("timestamp", "Timestamp of the request");
        }});
        docs.put("examples", new HashMap<String, Object>() {{
            put("example1", new HashMap<String, Object>() {{
                put("question", "What is the availability in Karnataka?");
                put("expected", "The system will identify auraGeneralMetrics table, build a MongoDB query, " +
                        "execute it, and return formatted results");
            }});
            put("example2", new HashMap<String, Object>() {{
                put("question", "Show me all sites with error rate above 5%");
                put("expected", "The system will query for sites matching the condition and provide a summary");
            }});
        }});
        return ResponseEntity.ok(docs);
    }

    /**
     * Create error response
     */
    private Map<String, Object> createErrorResponse(String error) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", error);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }

    /**
     * Request class for message endpoint
     */
    public static class MessageRequest {
        private String message;

        public MessageRequest() {
        }

        public MessageRequest(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

