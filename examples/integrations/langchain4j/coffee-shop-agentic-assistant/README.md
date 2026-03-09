# Coffee Shop Agentic Assistant (Helidon SE)

This example demonstrates an agentic coffee shop assistant built with Helidon and LangChain4j.
It combines multi-agent orchestration with RAG over a coffee menu ingested at startup.

## Features

- Agentic workflow with:
  - `@Ai.Agent`
  - `@SequenceAgent`
  - `@ConditionalAgent` + `@ActivationCondition`
  - `@Output`
- Intent classifier and router agents (`MENU`, `ORDER`, `OFF_TOPIC`)
- Menu expert and order expert agents
- Order tool callback (`saveOrder`)
- RAG ingestion from `data/menu.json` into in-memory embedding store (same ingestion approach as `coffee-shop-assistant-se`)
- LangChain4j input guardrail from config (`app.forbidden-phrases`)
- Web UI and REST endpoint (`POST /chat`)

## Build

```shell
mvn clean package
```

## Run

```shell
java -jar target/helidon-examples-integrations-langchain4j-coffee-shop-agentic-assistant.jar
```

Open:

```text
http://localhost:8080/
```

## Example questions

- `What hot drinks do you have?`
- `I want something with caramel under $6.`
- `Please order one cappuccino and one blueberry muffin.`
- `Can you explain Kubernetes networking?`
