# AI-Powered Query System - Implementation Guide

## Summary

A new **4-step AI-powered query system** has been implemented that converts natural language messages into MongoDB queries, executes them, and returns formatted responses. This system leverages Ollama's Qwen2.5-coder model to intelligently understand user requests and generate appropriate database queries.

## New Files Created

### 1. Service Layer
- **`src/main/java/com/jio/jcpaura/service/AuraAIQueryService.java`** (359 lines)
  - Core service implementing the 4-step pipeline
  - Methods: `processQuery()`, `stepGetEntities()`, `stepGetQueryBuilder()`, `stepExecuteQuery()`, `stepGetResponse()`
  - Integrates with OllamaService for AI interactions
  - Integrates with AuraGeneralMetricsService for data queries

### 2. Controller Layer
- **`src/main/java/com/jio/jcpaura/controller/AuraAIController.java`** (129 lines)
  - REST controller with AI query endpoints
  - Endpoints:
    - `POST /api/aura-ai/message` - Process natural language queries
    - `GET /api/aura-ai/health` - Health check
    - `GET /api/aura-ai/docs` - API documentation

### 3. Testing
- **`src/test/java/com/jio/jcpaura/AuraAIQuerySystemTests.java`** (113 lines)
  - Integration tests for the AI query system
  - Test cases for simple queries, conditional queries, aggregations, error handling

### 4. Documentation
- **`AI_QUERY_SYSTEM_GUIDE.md`** - Complete API documentation and usage guide

## How It Works

### The 4-Step Pipeline

```
User Message
    ↓
[Step 1: Entity Identification]
    ↓ (Identifies which table to query)
[Step 2: Query Builder]
    ↓ (Generates MongoDB query)
[Step 3: Execute Query]
    ↓ (Executes query, gets results)
[Step 4: Response Formatting]
    ↓
Final Formatted Response
```

### Step 1: Entity Identification
- **Input**: User's natural language message
- **System Prompt**: Contains schema information for all available tables
- **Output**: Entity identification and reasoning
- **Example**:
  - Input: "What is the availability in Karnataka?"
  - Output: "Entity: auraGeneralMetrics - The user is asking about network performance metrics for a specific region."

### Step 2: Query Builder
- **Input**: User message + Entity identification
- **System Prompt**: MongoDB syntax rules and examples
- **Output**: Valid MongoDB query or aggregation pipeline
- **Example**:
  - Input: "Entity identified as auraGeneralMetrics. User asks about availability in Karnataka."
  - Output: `{ "circle": "Karnataka" }`

### Step 3: Execute Query
- **Input**: Generated MongoDB query
- **Process**: Determines if it's simple query or aggregation, executes accordingly
- **Output**: Raw data from database
- **Example**:
  - Executes query against auraGeneralMetrics collection
  - Returns matching documents

### Step 4: Response Formatting
- **Input**: User message + Entity + Query + Results
- **System Prompt**: Guidelines for professional data presentation
- **Output**: User-friendly, formatted response
- **Example**:
  - Input: Original question + All data collected
  - Output: "Based on Karnataka circle metrics, availability is 99.85%..."

## API Usage Examples

### 1. Simple Query
```bash
curl -X POST http://localhost:8081/api/aura-ai/message \
  -H "Content-Type: application/json" \
  -d '{"message": "What is the availability in Karnataka?"}'
```

**Response:**
```json
{
  "success": true,
  "originalMessage": "What is the availability in Karnataka?",
  "entityIdentification": "Entity: auraGeneralMetrics - The user is asking about network performance metrics...",
  "mongoQuery": "{ \"circle\": \"Karnataka\" }",
  "resultCount": 1,
  "queryResults": [...],
  "response": "Based on the Karnataka circle metrics, the availability is 99.85%...",
  "timestamp": 1707458688000
}
```

### 2. Conditional Query
```bash
curl -X POST http://localhost:8081/api/aura-ai/message \
  -H "Content-Type: application/json" \
  -d '{"message": "Show me all sites with error rate above 1%"}'
```

### 3. Aggregation Query
```bash
curl -X POST http://localhost:8081/api/aura-ai/message \
  -H "Content-Type: application/json" \
  -d '{"message": "How many active users are in each service type?"}'
```

### 4. Health Check
```bash
curl http://localhost:8081/api/aura-ai/health
```

### 5. Documentation
```bash
curl http://localhost:8081/api/aura-ai/docs
```

## Architecture Diagram

```
┌─────────────────────────────────────┐
│   REST Client (Web/Mobile)          │
└────────────────┬────────────────────┘
                 │
                 ↓ POST /api/aura-ai/message
┌─────────────────────────────────────┐
│   AuraAIController                  │
│   - Validates input                 │
│   - Calls AuraAIQueryService        │
└────────────────┬────────────────────┘
                 │
                 ↓
┌─────────────────────────────────────┐
│   AuraAIQueryService                │
│  ┌─────────────────────────────────┐│
│  │ Step 1: getEntities()           ││
│  │ (Entity Identification)         ││
│  └────────────┬────────────────────┘│
│               ↓                      │
│  ┌─────────────────────────────────┐│
│  │ Step 2: getQueryBuilder()       ││
│  │ (Query Generation)              ││
│  └────────────┬────────────────────┘│
│               ↓                      │
│  ┌─────────────────────────────────┐│
│  │ Step 3: executeQuery()          ││
│  │ (Query Execution)               ││
│  └────────────┬────────────────────┘│
│               ↓                      │
│  ┌─────────────────────────────────┐│
│  │ Step 4: getResponse()           ││
│  │ (Response Formatting)           ││
│  └─────────────────────────────────┘│
└────────────────┬────────────────────┘
                 │
      ┌──────────┴──────────┐
      ↓                     ↓
┌──────────────┐    ┌──────────────────────────┐
│ OllamaService│    │ AuraGeneralMetricsService│
│ (AI Model)   │    │ (Data Access)            │
└──────┬───────┘    └──────┬───────────────────┘
       │                   │
       ↓                   ↓
┌──────────────┐    ┌──────────────────────────┐
│ Ollama API   │    │ MongoDB                  │
│ :11434       │    │ (auraGeneralMetrics)     │
└──────────────┘    └──────────────────────────┘
```

## Integration with Existing Code

The system integrates seamlessly with existing components:

### With OllamaService
```java
// Uses OllamaService for AI model interactions
ollamaService.generateResponse(systemPrompt, userPrompt);
```

### With AuraGeneralMetricsService
```java
// Uses existing query execution methods
metricsService.executeQuery(jsonQuery);
metricsService.executeAggregation(pipelineJson);
```

### With MongoDB
```java
// Leverages existing MongoDB connectivity
// Executes generated queries against auraGeneralMetrics collection
```

## Key Features

1. **4-Step Pipeline**: Structured approach to query processing
2. **Entity Awareness**: Understands which table to query
3. **Smart Query Generation**: Creates valid MongoDB queries
4. **Flexible Query Support**: Handles simple queries and aggregations
5. **Natural Language Processing**: Understands human language
6. **Response Formatting**: Professional, readable output
7. **Error Handling**: Graceful error management
8. **Documentation**: Built-in API docs endpoint

## System Prompts

The system uses three specialized prompts for different steps:

### 1. Entity Identification Prompt
- Teaches model about available tables
- Describes schema and field purposes
- Example output format: "Entity: tableName - reason"

### 2. Query Builder Prompt
- MongoDB syntax rules
- Query examples
- Instruction to return ONLY JSON output

### 3. Response Formatting Prompt
- Guidelines for professional communication
- Data presentation best practices
- Instructions for clarity and usefulness

## Supported Query Types

1. **Simple Filters**
   - "Show me metrics for Karnataka"
   - Generated: `{ "circle": "Karnataka" }`

2. **Conditional Queries**
   - "Sites with error rate > 5%"
   - Generated: `{ "error_rate_pct": { "$gt": 5 } }`

3. **Multiple Conditions**
   - "North circle with GOOD health and high throughput"
   - Generated: `{ "circle": "North", "health_status": "GOOD", "throughput_mbps": { "$gt": 250 } }`

4. **Aggregations**
   - "Count users by service type"
   - Generated: Aggregation pipeline with $group

5. **Complex Analysis**
   - Any combination of above
   - Generated: Complex MongoDB query or pipeline

## Configuration

No additional configuration needed! The system uses existing settings:

- **Ollama URL**: Configured in `application.properties` (default: `http://localhost:11434`)
- **Ollama Model**: Configured in `application.properties` (default: `qwen2.5-coder:3b`)
- **MongoDB**: Uses existing MongoDB configuration

## Running the System

### Start Application
```bash
cd A:\jcpaura
mvn spring-boot:run
```

### Verify Services are Running
1. Ollama: `curl http://localhost:11434/api/tags`
2. MongoDB: Ensure MongoDB is running
3. Application: http://localhost:8081

### Test an Endpoint
```bash
# Health check
curl http://localhost:8081/api/aura-ai/health

# Test query
curl -X POST http://localhost:8081/api/aura-ai/message \
  -H "Content-Type: application/json" \
  -d '{"message": "What is the availability in Karnataka?"}'
```

## Performance Notes

- **Ollama Inference**: 2-5 seconds per step (4 steps = 8-20 seconds total)
- **Query Execution**: < 100ms for MongoDB queries
- **Response Size**: Typically 1-10KB depending on results
- **Concurrent Requests**: Limited by Ollama's processing capacity

## Error Scenarios

| Scenario | Handling |
|----------|----------|
| Empty message | 400 Bad Request |
| Ollama unavailable | 500 error with message |
| Invalid MongoDB query generated | Attempts JSON extraction |
| Query returns no results | Returns empty array, formatted response |
| Database error | 500 error with details |

## Testing

### Run Integration Tests
```bash
mvn test -Dtest=AuraAIQuerySystemTests
```

### Manual Testing with Postman
1. Create POST request to `http://localhost:8081/api/aura-ai/message`
2. Set body to raw JSON
3. Send test queries
4. Verify responses

### Sample Test Queries
1. "What is the availability in Karnataka?"
2. "Show me all sites with error rate above 1%"
3. "Count how many metrics we have for each service type"
4. "Find all GOOD health status sites in North circle"
5. "What is the average latency in Jio5G service?"

## Future Enhancements

1. **Query Caching**: Cache frequently asked queries
2. **Multi-Table Support**: Query across multiple collections
3. **Real-time Updates**: WebSocket support for live data
4. **Query History**: Track and learn from user queries
5. **Performance Optimization**: Index recommendations
6. **Advanced Analytics**: Trend analysis, predictions
7. **Custom Prompts**: User-defined system prompts

## Troubleshooting

### Issue: "Ollama service is not available"
**Solution**: 
- Check Ollama is running: `curl http://localhost:11434/api/tags`
- Verify model exists: `ollama list`
- Ensure qwen2.5-coder:3b is installed

### Issue: "mongoTemplate bean not found"
**Solution**: Verify `MongoDBConfiguration.java` is properly configured

### Issue: "Empty response from Ollama"
**Solution**: Check Ollama service logs and verify model is loaded

### Issue: Null fields in response
**Solution**: This is normal for optional fields without data in the database

## Related Documentation

- See `AI_QUERY_SYSTEM_GUIDE.md` for detailed API documentation
- See `API_DOCUMENTATION.md` for other endpoints
- See `TESTING_GUIDE.md` for testing procedures

## Support

For issues or questions:
1. Check the logs in `app.log`
2. Review error responses from API
3. Verify Ollama and MongoDB are running
4. Check `AI_QUERY_SYSTEM_GUIDE.md` for examples
5. Review test cases in `AuraAIQuerySystemTests.java`

