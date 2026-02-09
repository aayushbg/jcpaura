# Quick Start Guide - AI-Powered Query System

## Setup and Start

### Prerequisites
1. Java 21+
2. Maven
3. MongoDB running
4. Ollama running with qwen2.5-coder:3b model

### Start Ollama
```bash
ollama run qwen2.5-coder:3b
```

### Start Application
```bash
cd A:\jcpaura
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

## Quick API Testing

### 1. Health Check
```bash
curl http://localhost:8081/api/aura-ai/health
```

**Expected Response:**
```json
{
  "success": true,
  "status": "AI Query Service is running",
  "timestamp": 1707458688000
}
```

### 2. Get API Documentation
```bash
curl http://localhost:8081/api/aura-ai/docs
```

### 3. Test Simple Query
```bash
curl -X POST http://localhost:8081/api/aura-ai/message \
  -H "Content-Type: application/json" \
  -d '{"message": "What is the availability in Karnataka?"}'
```

**What Happens:**
1. AI identifies the auraGeneralMetrics entity
2. AI generates query: `{ "circle": "Karnataka" }`
3. Query executes against MongoDB
4. AI formats response based on results

### 4. Test Conditional Query
```bash
curl -X POST http://localhost:8081/api/aura-ai/message \
  -H "Content-Type: application/json" \
  -d '{"message": "Show me all sites with error rate above 1%"}'
```

### 5. Test Aggregation Query
```bash
curl -X POST http://localhost:8081/api/aura-ai/message \
  -H "Content-Type: application/json" \
  -d '{"message": "How many active users are in each service type?"}'
```

### 6. Test Complex Query
```bash
curl -X POST http://localhost:8081/api/aura-ai/message \
  -H "Content-Type: application/json" \
  -d '{"message": "Find all GOOD health status sites in North circle with availability above 95%"}'
```

## Using Postman

### Create New Request
1. Method: **POST**
2. URL: `http://localhost:8081/api/aura-ai/message`
3. Headers:
   - Key: `Content-Type`
   - Value: `application/json`
4. Body (raw JSON):
```json
{
  "message": "What is the average throughput in Jio5G service?"
}
```
5. Click **Send**

## Response Format

All responses include:
- `success`: Boolean (true/false)
- `originalMessage`: Your input
- `entityIdentification`: Which table was identified
- `mongoQuery`: The generated MongoDB query
- `resultCount`: Number of results
- `queryResults`: Array of data returned
- `response`: AI-formatted readable response
- `timestamp`: Unix timestamp

## Example Response

```json
{
  "success": true,
  "originalMessage": "What is the availability in Karnataka?",
  "entityIdentification": "Entity: auraGeneralMetrics - The user is asking about network performance metrics for a specific geographic region (Karnataka).",
  "mongoQuery": "{ \"circle\": \"Karnataka\" }",
  "resultCount": 1,
  "queryResults": [
    {
      "_id": "6982edd620ae344dcc511a1e",
      "circle": "Karnataka",
      "service_type": "Jio5G",
      "site_id": "KA-BLR-021",
      "active_users": 1120,
      "availability_pct": 99.85,
      "avg_latency_ms": 20.1,
      "error_rate_pct": 0.5,
      "packet_loss_pct": 0.3,
      "throughput_mbps": 295.6,
      "health_status": "GOOD",
      "kpi_health_score": 90.3,
      "kpi_timestamp": "2026-01-28T14:10:00Z"
    }
  ],
  "response": "Based on the metrics from Karnataka circle, the availability is 99.85%, which indicates excellent network performance. With 1,120 active users, the service type is Jio5G at site KA-BLR-021. The error rate is very low at 0.5%, packet loss is minimal at 0.3%, and throughput is strong at 295.6 Mbps. The health status is GOOD with a score of 90.3 out of 100, and the average latency is a good 20.1 milliseconds.",
  "timestamp": 1707458688123
}
```

## Sample Test Queries

### Network Performance Queries
1. "What is the availability in Karnataka?"
2. "Show me all sites with low throughput"
3. "Which circles have the best health status?"
4. "What is the average latency across all sites?"

### Error Analysis Queries
5. "Show me sites with error rate above 2%"
6. "Which service type has the highest error rate?"
7. "Find all CRITICAL health status sites"

### User Activity Queries
8. "How many active users are in each service type?"
9. "Which site has the most active users?"
10. "What is the total active user count?"

### Aggregation Queries
11. "Count the number of sites by circle"
12. "Group metrics by service type and show average availability"
13. "Show me the health distribution of all sites"

## Troubleshooting

### Issue: "message cannot be empty"
- Make sure your JSON includes a non-empty `message` field

### Issue: "Ollama service is not available"
```bash
# Check Ollama is running
curl http://localhost:11434/api/tags

# If not, start Ollama
ollama run qwen2.5-coder:3b
```

### Issue: "Error executing query"
- Check MongoDB is running
- Verify the generated query is valid MongoDB syntax
- Check logs in `app.log`

### Issue: Response takes too long
- Ollama inference takes 2-5 seconds per step
- Total time: 8-20 seconds for full pipeline
- This is normal behavior

### Issue: Null fields in response
- This is expected for optional fields without data
- The database document may not have all fields populated

## Advanced Usage

### Using JSON in Request
```bash
# On Windows PowerShell
$body = @{
    message = "What is the availability in Karnataka?"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8081/api/aura-ai/message" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body
```

### Using Python
```python
import requests
import json

url = "http://localhost:8081/api/aura-ai/message"
payload = {
    "message": "What is the availability in Karnataka?"
}
headers = {
    "Content-Type": "application/json"
}

response = requests.post(url, json=payload, headers=headers)
print(json.dumps(response.json(), indent=2))
```

### Using JavaScript/Node.js
```javascript
const message = "What is the availability in Karnataka?";

fetch('http://localhost:8081/api/aura-ai/message', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({ message: message })
})
  .then(response => response.json())
  .then(data => console.log(JSON.stringify(data, null, 2)))
  .catch(error => console.error('Error:', error));
```

## Next Steps

1. **Test the API**: Use the sample queries above
2. **Integrate with UI**: Use the `/api/aura-ai/message` endpoint in your frontend
3. **Monitor Logs**: Check `app.log` for any issues
4. **Customize Prompts**: Edit system prompts in `AuraAIQueryService.java` if needed
5. **Add More Tables**: Extend the system for additional collections

## Files to Reference

- **API Documentation**: `AI_QUERY_SYSTEM_GUIDE.md`
- **Implementation Details**: `AI_IMPLEMENTATION_GUIDE.md`
- **Service Code**: `src/main/java/com/jio/jcpaura/service/AuraAIQueryService.java`
- **Controller Code**: `src/main/java/com/jio/jcpaura/controller/AuraAIController.java`
- **Test Cases**: `src/test/java/com/jio/jcpaura/AuraAIQuerySystemTests.java`

## Performance Tips

1. **First Request**: Might be slower due to model loading
2. **Subsequent Requests**: Faster as model stays in memory
3. **Batch Queries**: Process multiple queries sequentially for better throughput
4. **Monitor Resources**: Ollama requires RAM and GPU (if available)
5. **Database Indexing**: Ensure MongoDB has indexes on frequently queried fields

## Security Notes

1. **Input Validation**: All user inputs are validated before processing
2. **MongoDB Injection**: System generates queries from AI output, validate if needed
3. **Ollama Access**: Ensure Ollama service is on secure network
4. **Error Messages**: Production should limit error details in responses

## Support Resources

- Ollama Documentation: https://github.com/ollama/ollama
- MongoDB Query Guide: https://docs.mongodb.com/manual/
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- This Project's Logs: `app.log`

---

## Common Workflows

### Workflow 1: Quick Availability Check
```bash
# Check availability in a specific region
curl -X POST http://localhost:8081/api/aura-ai/message \
  -H "Content-Type: application/json" \
  -d '{"message": "What is the availability in Karnataka?"}'
```

### Workflow 2: Health Status Analysis
```bash
# Find problematic sites
curl -X POST http://localhost:8081/api/aura-ai/message \
  -H "Content-Type: application/json" \
  -d '{"message": "Show me all sites with WARNING or CRITICAL health status"}'
```

### Workflow 3: Performance Comparison
```bash
# Compare service types
curl -X POST http://localhost:8081/api/aura-ai/message \
  -H "Content-Type: application/json" \
  -d '{"message": "Compare throughput between Jio5G and Jio4G services"}'
```

### Workflow 4: User Analytics
```bash
# Get user distribution
curl -X POST http://localhost:8081/api/aura-ai/message \
  -H "Content-Type: application/json" \
  -d '{"message": "Show the distribution of active users across all service types"}'
```

---

**Happy querying!** 🚀

