package com.jio.jcpaura.controller;

import com.jio.jcpaura.service.OllamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ollama")
public class OllamaController {

    @Autowired
    private OllamaService ollamaService;

    /**
     * Generate response using Ollama Qwen2.5-coder model
     * POST /api/ollama/generate
     *
     * Request body:
     * {
     *   "systemPrompt": "You are a helpful coding assistant",
     *   "userPrompt": "How do I sort a list in Python?"
     * }
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateResponse(@RequestBody GenerateRequest request) {
        try {
            // Validate input
            if (request.getSystemPrompt() == null || request.getSystemPrompt().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse(
                        "systemPrompt cannot be empty"
                ));
            }
            if (request.getUserPrompt() == null || request.getUserPrompt().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse(
                        "userPrompt cannot be empty"
                ));
            }

            // Generate response
            String response = ollamaService.generateResponse(
                    request.getSystemPrompt(),
                    request.getUserPrompt()
            );

            // Return success response
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("response", response);
            result.put("systemPrompt", request.getSystemPrompt());
            result.put("userPrompt", request.getUserPrompt());
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    createErrorResponse(e.getMessage())
            );
        }
    }

    /**
     * Generate response with custom parameters
     * POST /api/ollama/generate-advanced
     *
     * Request body:
     * {
     *   "systemPrompt": "You are a helpful coding assistant",
     *   "userPrompt": "How do I sort a list in Python?",
     *   "temperature": 0.7,
     *   "topP": 0.9,
     *   "topK": 40
     * }
     */
    @PostMapping("/generate-advanced")
    public ResponseEntity<Map<String, Object>> generateResponseAdvanced(
            @RequestBody GenerateAdvancedRequest request) {
        try {
            // Validate input
            if (request.getSystemPrompt() == null || request.getSystemPrompt().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse(
                        "systemPrompt cannot be empty"
                ));
            }
            if (request.getUserPrompt() == null || request.getUserPrompt().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse(
                        "userPrompt cannot be empty"
                ));
            }

            // Validate parameters
            if (request.getTemperature() != null &&
                    (request.getTemperature() < 0.0 || request.getTemperature() > 2.0)) {
                return ResponseEntity.badRequest().body(createErrorResponse(
                        "temperature must be between 0.0 and 2.0"
                ));
            }
            if (request.getTopP() != null &&
                    (request.getTopP() < 0.0 || request.getTopP() > 1.0)) {
                return ResponseEntity.badRequest().body(createErrorResponse(
                        "topP must be between 0.0 and 1.0"
                ));
            }

            // Generate response
            String response = ollamaService.generateResponseWithParams(
                    request.getSystemPrompt(),
                    request.getUserPrompt(),
                    request.getTemperature(),
                    request.getTopP(),
                    request.getTopK()
            );

            // Return success response
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("response", response);
            result.put("systemPrompt", request.getSystemPrompt());
            result.put("userPrompt", request.getUserPrompt());
            result.put("parameters", new HashMap<String, Object>() {{
                put("temperature", request.getTemperature() != null ? request.getTemperature() : 0.7);
                put("topP", request.getTopP() != null ? request.getTopP() : 0.9);
                put("topK", request.getTopK() != null ? request.getTopK() : 40);
            }});
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    createErrorResponse(e.getMessage())
            );
        }
    }

    /**
     * Health check - verify Ollama service is running
     * GET /api/ollama/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        boolean isAvailable = ollamaService.isServiceAvailable();
        Map<String, Object> result = new HashMap<>();
        result.put("success", isAvailable);
        result.put("status", isAvailable ? "Ollama service is running" : "Ollama service is not available");
        result.put("timestamp", System.currentTimeMillis());

        return isAvailable ? ResponseEntity.ok(result) :
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
    }

    /**
     * Get list of available models
     * GET /api/ollama/models
     */
    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> getModels() {
        try {
            String models = ollamaService.getAvailableModels();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("models", models);
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    createErrorResponse(e.getMessage())
            );
        }
    }

    /**
     * Helper method to create error response
     */
    private Map<String, Object> createErrorResponse(String error) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", error);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }

    /**
     * Request class for generate endpoint
     */
    public static class GenerateRequest {
        private String systemPrompt;
        private String userPrompt;

        public GenerateRequest() {
        }

        public GenerateRequest(String systemPrompt, String userPrompt) {
            this.systemPrompt = systemPrompt;
            this.userPrompt = userPrompt;
        }

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }

        public String getUserPrompt() {
            return userPrompt;
        }

        public void setUserPrompt(String userPrompt) {
            this.userPrompt = userPrompt;
        }
    }

    /**
     * Request class for generate-advanced endpoint
     */
    public static class GenerateAdvancedRequest {
        private String systemPrompt;
        private String userPrompt;
        private Double temperature;
        private Double topP;
        private Integer topK;

        public GenerateAdvancedRequest() {
        }

        public GenerateAdvancedRequest(String systemPrompt, String userPrompt,
                                      Double temperature, Double topP, Integer topK) {
            this.systemPrompt = systemPrompt;
            this.userPrompt = userPrompt;
            this.temperature = temperature;
            this.topP = topP;
            this.topK = topK;
        }

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }

        public String getUserPrompt() {
            return userPrompt;
        }

        public void setUserPrompt(String userPrompt) {
            this.userPrompt = userPrompt;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getTopP() {
            return topP;
        }

        public void setTopP(Double topP) {
            this.topP = topP;
        }

        public Integer getTopK() {
            return topK;
        }

        public void setTopK(Integer topK) {
            this.topK = topK;
        }
    }
}

