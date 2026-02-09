package com.jio.jcpaura.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for handling AI-powered queries using multi-step prompting with Ollama
 * Step 1: getEntities - Identify which table/entity the query is about
 * Step 2: getQueryBuilder - Build the MongoDB query
 * Step 3: executeQuery - Execute the query
 * Step 4: getResponse - Format and return the response
 */
@Service
public class AuraAIQueryService {

    @Autowired
    private OllamaService ollamaService;

    @Autowired
    private AuraGeneralMetricsService metricsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Process a user message through the 4-step AI query pipeline
     *
     * @param userMessage The user's natural language query
     * @return Map containing the final response and metadata
     */
    public Map<String, Object> processQuery(String userMessage) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Step 1: Identify the entity/table
            String entityIdentification = stepGetEntities(userMessage);
            result.put("entityIdentification", entityIdentification);

            // Step 2: Build the MongoDB query
            String mongoQuery = stepGetQueryBuilder(userMessage, entityIdentification);
            result.put("mongoQuery", mongoQuery);

            // Step 3: Execute the query
            List<?> queryResults = stepExecuteQuery(mongoQuery);
            result.put("queryResults", queryResults);
            result.put("resultCount", queryResults.size());

            // Step 4: Get formatted response from the AI
            String formattedResponse = stepGetResponse(userMessage, entityIdentification, mongoQuery, queryResults);
            result.put("response", formattedResponse);
            result.put("success", true);
            result.put("originalMessage", userMessage);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("originalMessage", userMessage);
        }

        return result;
    }

    /**
     * Step 1: Identify the entity/collection that the query is about
     * Uses system prompt with table schema information
     */
    private String stepGetEntities(String userMessage) {
        String systemPrompt = buildEntityIdentificationSystemPrompt();
        try {
            String response = ollamaService.generateResponse(systemPrompt, userMessage);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error in entity identification step: " + e.getMessage(), e);
        }
    }

    /**
     * Step 2: Build the MongoDB query based on the identified entity
     */
    private String stepGetQueryBuilder(String userMessage, String entityIdentification) {
        String systemPrompt = buildQueryBuilderSystemPrompt();
        String userPrompt = "User query: " + userMessage + "\n\nIdentified entity: " + entityIdentification;

        try {
            String response = ollamaService.generateResponse(systemPrompt, userPrompt);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error in query builder step: " + e.getMessage(), e);
        }
    }

    /**
     * Step 3: Execute the query against MongoDB
     */
    private List<?> stepExecuteQuery(String mongoQuery) {
        try {
            // Try to determine if it's an aggregation or simple query
            String cleanedQuery = mongoQuery.trim();

            // If it starts with '[', it's likely an aggregation pipeline
            if (cleanedQuery.startsWith("[")) {
                List<Document> results = metricsService.executeAggregation(cleanedQuery);
                return results;
            } else {
                // Extract JSON query from potential text response
                String jsonQuery = extractJsonFromResponse(cleanedQuery);
                List<?> results = metricsService.executeQuery(jsonQuery);
                return results;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error executing query: " + e.getMessage(), e);
        }
    }

    /**
     * Step 4: Get a formatted response from the AI using the query results
     */
    private String stepGetResponse(String userMessage, String entityIdentification,
                                   String mongoQuery, List<?> queryResults) {
        String systemPrompt = buildResponseFormattingSystemPrompt();

        // Convert results to JSON string for the prompt
        String resultsJson;
        try {
            resultsJson = objectMapper.writeValueAsString(queryResults);
        } catch (Exception e) {
            resultsJson = queryResults.toString();
        }

        String userPrompt = String.format(
                "Original user question: %s\n\n" +
                "Identified entity: %s\n\n" +
                "Query executed: %s\n\n" +
                "Query results (in JSON format):\n%s\n\n" +
                "Please provide a clear, formatted response to the user's original question based on the retrieved data.",
                userMessage, entityIdentification, mongoQuery, resultsJson
        );

        try {
            return ollamaService.generateResponse(systemPrompt, userPrompt);
        } catch (Exception e) {
            throw new RuntimeException("Error in response formatting step: " + e.getMessage(), e);
        }
    }

    /**
     * Build system prompt for entity identification step
     */
    private String buildEntityIdentificationSystemPrompt() {
        return """
You are an expert database analyst. Your task is to identify which database table/entity a user is querying about.

Available Tables:
1. **auraGeneralMetrics** - Contains network performance metrics
   Fields: _id, active_users (int), availability_pct (double), avg_latency_ms (double), circle (string),
   error_rate_pct (double), health_status (string), kpi_health_score (double), kpi_timestamp (date),
   packet_loss_pct (double), service_type (string), site_id (string), throughput_mbps (double)
   
   Example fields:
   - active_users: Number of active users (0-10000+)
   - availability_pct: Availability percentage (0-100)
   - avg_latency_ms: Average latency in milliseconds
   - circle: Geographic circle/region (e.g., "Karnataka", "North", "South")
   - error_rate_pct: Error rate percentage (0-100)
   - health_status: System health status (GOOD, WARNING, CRITICAL)
   - kpi_health_score: Health score (0-100)
   - kpi_timestamp: Timestamp of the metric
   - packet_loss_pct: Packet loss percentage (0-100)
   - service_type: Type of service (e.g., "Jio5G", "Jio4G")
   - site_id: Site identifier
   - throughput_mbps: Throughput in Mbps

Your response should clearly state which table the user is querying about and why, in a concise format.
For example: "Entity: auraGeneralMetrics - The user is asking about network performance metrics for a specific region."
""";
    }

    /**
     * Build system prompt for query builder step
     */
    private String buildQueryBuilderSystemPrompt() {
        return """
You are an expert MongoDB query builder. Your task is to convert natural language requirements into MongoDB queries.

The auraGeneralMetrics collection has these fields:
- _id: ObjectId
- active_users: Integer
- availability_pct: Double
- avg_latency_ms: Double
- circle: String
- error_rate_pct: Double
- health_status: String
- kpi_health_score: Double
- kpi_timestamp: Date
- packet_loss_pct: Double
- service_type: String
- site_id: String
- throughput_mbps: Double

IMPORTANT RULES:
1. Return ONLY the MongoDB query JSON or aggregation pipeline, no explanation
2. For simple lookups, return a query object like: { "field": "value" }
3. For complex queries with conditions, use MongoDB operators like $gt, $gte, $lt, $lte, $in, $regex, etc.
4. For aggregation operations (grouping, counting, averaging), return an aggregation pipeline as a JSON array
5. Ensure the JSON is valid and can be directly parsed
6. Use field names exactly as they appear in the schema

Examples:
- Find by circle: { "circle": "Karnataka" }
- Find with condition: { "availability_pct": { "$gte": 95 } }
- Complex: { "circle": { "$in": ["North", "South"] }, "health_status": "GOOD" }
- Aggregation: [{ "$match": { "circle": "Karnataka" } }, { "$group": { "_id": "$service_type", "count": { "$sum": 1 } } }]
""";
    }

    /**
     * Build system prompt for response formatting step
     */
    private String buildResponseFormattingSystemPrompt() {
        return """
You are an expert data analyst and communicator. Your task is to convert database query results into a clear, 
user-friendly response that directly answers the user's question.

Guidelines:
1. Be concise but comprehensive
2. Highlight the most important metrics and findings
3. Use proper formatting and language
4. If no results were found, explain this clearly
5. Provide context or insights where relevant
6. Format numbers appropriately (e.g., percentages, decimals)
7. Use bullet points or structured formatting for multiple results
8. Keep the response focused on answering the original question

Your response should be professional and clear, suitable for displaying to a user.
""";
    }

    /**
     * Extract JSON from a response that might contain additional text
     */
    private String extractJsonFromResponse(String response) {
        String cleaned = response.trim();

        // Try to find JSON object
        int startBrace = cleaned.indexOf('{');
        int endBrace = cleaned.lastIndexOf('}');

        if (startBrace != -1 && endBrace != -1 && endBrace > startBrace) {
            try {
                String extracted = cleaned.substring(startBrace, endBrace + 1);
                // Validate it's valid JSON by parsing it
                objectMapper.readTree(extracted);
                return extracted;
            } catch (Exception e) {
                // If extraction fails, return the original
            }
        }

        // Try to find JSON array
        int startBracket = cleaned.indexOf('[');
        int endBracket = cleaned.lastIndexOf(']');

        if (startBracket != -1 && endBracket != -1 && endBracket > startBracket) {
            try {
                String extracted = cleaned.substring(startBracket, endBracket + 1);
                // Validate it's valid JSON by parsing it
                objectMapper.readTree(extracted);
                return extracted;
            } catch (Exception e) {
                // If extraction fails, return the original
            }
        }

        // Return the original cleaned response
        return cleaned;
    }
}

