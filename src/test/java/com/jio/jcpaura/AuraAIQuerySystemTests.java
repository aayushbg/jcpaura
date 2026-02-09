package com.jio.jcpaura;

import com.jio.jcpaura.service.AuraAIQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Map;

/**
 * Integration tests for the AI Query System
 */
@SpringBootTest
class AuraAIQuerySystemTests {

    @Autowired
    private AuraAIQueryService aiQueryService;

    /**
     * Test 1: Simple entity identification and query
     */
    @Test
    void testSimpleQuery() {
        String message = "What is the availability in Karnataka?";
        Map<String, Object> result = aiQueryService.processQuery(message);

        assert result.containsKey("success");
        assert result.get("success").equals(true);
        assert result.containsKey("entityIdentification");
        assert result.containsKey("mongoQuery");
        assert result.containsKey("queryResults");
        assert result.containsKey("response");

        System.out.println("Test 1 - Simple Query:");
        System.out.println("Original Message: " + message);
        System.out.println("Entity: " + result.get("entityIdentification"));
        System.out.println("Query: " + result.get("mongoQuery"));
        System.out.println("Result Count: " + result.get("resultCount"));
        System.out.println("Response: " + result.get("response"));
        System.out.println("---");
    }

    /**
     * Test 2: Query with conditions
     */
    @Test
    void testConditionalQuery() {
        String message = "Show me all sites with error rate above 1%";
        Map<String, Object> result = aiQueryService.processQuery(message);

        assert result.containsKey("success");
        assert result.get("success").equals(true);
        assert result.containsKey("mongoQuery");

        System.out.println("Test 2 - Conditional Query:");
        System.out.println("Original Message: " + message);
        System.out.println("Entity: " + result.get("entityIdentification"));
        System.out.println("Query: " + result.get("mongoQuery"));
        System.out.println("Result Count: " + result.get("resultCount"));
        System.out.println("---");
    }

    /**
     * Test 3: Aggregation query
     */
    @Test
    void testAggregationQuery() {
        String message = "Count how many metrics we have for each service type";
        Map<String, Object> result = aiQueryService.processQuery(message);

        assert result.containsKey("success");
        assert result.get("success").equals(true);

        System.out.println("Test 3 - Aggregation Query:");
        System.out.println("Original Message: " + message);
        System.out.println("Entity: " + result.get("entityIdentification"));
        System.out.println("Query: " + result.get("mongoQuery"));
        System.out.println("Result Count: " + result.get("resultCount"));
        System.out.println("Response: " + result.get("response"));
        System.out.println("---");
    }

    /**
     * Test 4: Empty message error handling
     */
    @Test
    void testEmptyMessageHandling() {
        String message = "";
        // This should be caught by the controller before reaching the service
        // but the service should handle it gracefully
        try {
            Map<String, Object> result = aiQueryService.processQuery(message);
            // Even if it processes, it should handle gracefully
            assert result.containsKey("success");
        } catch (Exception e) {
            System.out.println("Test 4 - Empty Message Handling:");
            System.out.println("Caught exception as expected: " + e.getMessage());
            System.out.println("---");
        }
    }

    /**
     * Test 5: Complex multi-condition query
     */
    @Test
    void testComplexQuery() {
        String message = "Find all sites in North circle with GOOD health status and high throughput above 250 Mbps";
        Map<String, Object> result = aiQueryService.processQuery(message);

        assert result.containsKey("success");
        assert result.get("success").equals(true);

        System.out.println("Test 5 - Complex Query:");
        System.out.println("Original Message: " + message);
        System.out.println("Entity: " + result.get("entityIdentification"));
        System.out.println("Query: " + result.get("mongoQuery"));
        System.out.println("Result Count: " + result.get("resultCount"));
        System.out.println("---");
    }

    /**
     * Test 6: Response formatting verification
     */
    @Test
    void testResponseFormatting() {
        String message = "What are the total active users in the system?";
        Map<String, Object> result = aiQueryService.processQuery(message);

        // Verify response is well-formatted and readable
        String response = (String) result.get("response");
        assert response != null;
        assert response.length() > 0;
        assert !response.contains("null");

        System.out.println("Test 6 - Response Formatting:");
        System.out.println("Original Message: " + message);
        System.out.println("Formatted Response:");
        System.out.println(response);
        System.out.println("---");
    }
}

