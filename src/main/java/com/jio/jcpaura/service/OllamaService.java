package com.jio.jcpaura.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

@Service
public class OllamaService {

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:qwen2.5-coder:3b}")
    private String modelName;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OllamaService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate response from Ollama model
     * @param systemPrompt System prompt/context
     * @param userPrompt User's prompt/question
     * @return Response from the model
     * @throws IOException if API call fails
     */
    public String generateResponse(String systemPrompt, String userPrompt) throws IOException {
        try {
            // Combine system and user prompts
            String combinedPrompt = systemPrompt + "\n\n" + userPrompt;

            // Create request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", modelName);
            requestBody.put("prompt", combinedPrompt);
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.7);
            requestBody.put("top_p", 0.9);
            requestBody.put("top_k", 40);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Make request
            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody),
                    headers
            );

            String response = restTemplate.postForObject(
                    ollamaBaseUrl + "/api/generate",
                    entity,
                    String.class
            );

            // Parse and extract response
            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                return jsonNode.get("response").asText();
            }

            throw new RuntimeException("Empty response from Ollama");

        } catch (Exception e) {
            throw new IOException("Error calling Ollama API: " + e.getMessage(), e);
        }
    }

    /**
     * Generate response with custom parameters
     * @param systemPrompt System prompt/context
     * @param userPrompt User's prompt/question
     * @param temperature Temperature for response generation (0.0 - 2.0)
     * @param topP Top P sampling parameter (0.0 - 1.0)
     * @param topK Top K sampling parameter
     * @return Response from the model
     * @throws IOException if API call fails
     */
    public String generateResponseWithParams(String systemPrompt, String userPrompt,
                                            Double temperature, Double topP, Integer topK) throws IOException {
        try {
            String combinedPrompt = systemPrompt + "\n\n" + userPrompt;

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", modelName);
            requestBody.put("prompt", combinedPrompt);
            requestBody.put("stream", false);

            if (temperature != null) {
                requestBody.put("temperature", temperature);
            }
            if (topP != null) {
                requestBody.put("top_p", topP);
            }
            if (topK != null) {
                requestBody.put("top_k", topK);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody),
                    headers
            );

            String response = restTemplate.postForObject(
                    ollamaBaseUrl + "/api/generate",
                    entity,
                    String.class
            );

            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                return jsonNode.get("response").asText();
            }

            throw new RuntimeException("Empty response from Ollama");

        } catch (Exception e) {
            throw new IOException("Error calling Ollama API: " + e.getMessage(), e);
        }
    }

    /**
     * Check if Ollama service is available
     * @return true if service is reachable
     */
    public boolean isServiceAvailable() {
        try {
            restTemplate.getForObject(ollamaBaseUrl + "/api/tags", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get available models from Ollama
     * @return JSON string with available models
     */
    public String getAvailableModels() {
        try {
            return restTemplate.getForObject(ollamaBaseUrl + "/api/tags", String.class);
        } catch (Exception e) {
            return "{\"error\": \"Unable to fetch models: " + e.getMessage() + "\"}";
        }
    }
}

