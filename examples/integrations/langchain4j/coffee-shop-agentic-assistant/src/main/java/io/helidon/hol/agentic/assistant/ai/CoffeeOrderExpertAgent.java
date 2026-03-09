/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.helidon.hol.agentic.assistant.ai;

import io.helidon.hol.agentic.assistant.tools.OrderService;
import io.helidon.integrations.langchain4j.Ai;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agent responsible for coffee order workflows.
 */
@Ai.Agent("coffee-order-expert")
@Ai.ChatModel("expensive-model")
@Ai.ContentRetriever("menu-content-retriever")
@Ai.Tools(OrderService.class)
public interface CoffeeOrderExpertAgent {

    /**
     * Processes an order-related request.
     *
     * @param question user request
     * @return order response
     */
    @UserMessage("""
            You are Frank, a coffee shop server helping with orders.
            Use retrieved menu context for available items and prices.
            If the customer asks to place a final order, call the saveOrder tool.
            If details are missing (size, add-ons, quantity), ask follow-up questions first.

            Customer request: {{question}}
            """)
    @Agent(value = "Coffee order expert", outputKey = "lastResponse")
    String processOrder(@V("question") String question);
}
