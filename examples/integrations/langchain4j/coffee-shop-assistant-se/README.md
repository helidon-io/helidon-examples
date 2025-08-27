# **Coffee Shop Assistant (Helidon SE Version)**

This is a **demo application** showcasing the **Helidon SE integration with LangChain4J**. It demonstrates how to build an **AI-powered coffee shop assistant** using **Helidon Inject**, OpenAI models, and embedding storage.

NOTE: LangChain4J integration is a preview feature. The APIs shown here are subject to change. These APIs will be finalized in a future release of Helidon.

## **Features**

- Integration with **OpenAI chat models**.
- Utilization of **embedding models**, **embedding store**, **ingestor**, and **content retriever**.
- **Helidon Inject** for dependency injection.
- **Embedding store initialization** from a JSON file.
- Support for **callback functions** to enhance interactions.

## **Build the Application**

To build the application, run:

```shell
mvn clean package
```

## **Run the Application**

Execute the following command to start the application:

```shell
java -jar target/helidon-examples-integrations-langchain4j-coffee-shop-assistant-se.jar
```

Once running, you can interact with the assistant via your browser.

Example:

```
http://localhost:8080/chat?question="What can you offer today?"
```

## Sample Questions and Expected Responses

Here are some example queries you can try:

### Menu and Recommendations

- **"What hot drinks do you have?"**  
  - *Expected Response:* A list of **hot drinks** such as **Latte, Cappuccino, Espresso, and Hot Chocolate**.

- **"I'm looking for something sweet. What do you recommend?"**  
  - *Expected Response:* Suggestions like **Caramel Frappuccino, Blueberry Muffin, Chocolate Chip Cookie, and Hot Chocolate**.

- **"What drinks can I get with caramel?"**  
  - *Expected Response:* Options like **Caramel Frappuccino** and **Latte with caramel syrup add-on**.

### Dietary Preferences

- **"Do you have any vegan options?"**  
  - *Expected Response:* Items like **Avocado Toast, Iced Matcha Latte (with non-dairy milk), and Blueberry Muffin (if applicable)**.

### Orders and Availability

- **"Do you have any breakfast items?"**  
  - *Expected Response:* Options such as **Avocado Toast, Blueberry Muffin, and Bagel with Cream Cheese**.

- **"Can I order a coffee and a cookie?"**  
  - *Expected Response:*  
  *"Your order for a coffee and a chocolate chip cookie has been saved. The total is $5.00. Would you like anything else?"*

## Try metrics

Helidon provides `MetricsChatModelListener` which generates metrics that follow the [OpenTelemetry Semantic Conventions for GenAI Metrics v1.36.0](https://github.com/open-telemetry/semantic-conventions/blob/v1.36.0/docs/gen-ai/gen-ai-metrics.md). This is done out-of-box for Chat API calls. To view the captured metrics use following: 

```shell
# Prometheus Format
curl -s -X GET http://localhost:8080/observe/metrics
...
# HELP gen_ai_client_token_usage_token Measures number of input and output tokens used
# TYPE gen_ai_client_token_usage_token histogram
gen_ai_client_token_usage_token{gen_ai_operation_name="chat",gen_ai_request_model="gpt-4o-mini",gen_ai_response_model="gpt-4o-mini-2024-07-18",gen_ai_token_type="output",scope="vendor",quantile="0.5",} 71.0
...
gen_ai_client_token_usage_token{gen_ai_operation_name="chat",gen_ai_request_model="gpt-4o-mini",gen_ai_response_model="gpt-4o-mini-2024-07-18",gen_ai_token_type="input",scope="vendor",quantile="0.5",} 156.0
...
# HELP gen_ai_client_token_usage_token_max Measures number of input and output tokens used
# TYPE gen_ai_client_token_usage_token_max gauge
gen_ai_client_token_usage_token_max{gen_ai_operation_name="chat",gen_ai_request_model="gpt-4o-mini",gen_ai_response_model="gpt-4o-mini-2024-07-18",gen_ai_token_type="output",scope="vendor",} 71.0
gen_ai_client_token_usage_token_max{gen_ai_operation_name="chat",gen_ai_request_model="gpt-4o-mini",gen_ai_response_model="gpt-4o-mini-2024-07-18",gen_ai_token_type="input",scope="vendor",} 156.0
....
# HELP gen_ai_client_operation_duration_seconds_max GenAI operation duration
# TYPE gen_ai_client_operation_duration_seconds_max gauge
gen_ai_client_operation_duration_seconds_max{error_type="",gen_ai_operation_name="chat",gen_ai_request_model="gpt-4o-mini",gen_ai_response_model="gpt-4o-mini-2024-07-18",scope="vendor",} 2.0
# HELP gen_ai_client_operation_duration_seconds GenAI operation duration
# TYPE gen_ai_client_operation_duration_seconds histogram
gen_ai_client_operation_duration_seconds{error_type="",gen_ai_operation_name="chat",gen_ai_request_model="gpt-4o-mini",gen_ai_response_model="gpt-4o-mini-2024-07-18",scope="vendor",quantile="0.5",} 2.0
...
```