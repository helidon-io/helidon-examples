/*
 * Copyright (c) 2025 Oracle and/or its affiliates.
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
package io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant.ai;

import io.helidon.integrations.langchain4j.Ai;

import dev.langchain4j.service.SystemMessage;

/**
 * AI-powered assistant service for a coffee shop.
 *
 * This service provides chat-based interactions where the AI acts as a virtual server.
 * The chat model and content provider implementations are automatically retrieved from
 * the service registry.
 */
@Ai.Service
@Ai.ChatMemoryWindow(value = 3, store = "coherence")
public interface ChatAiService {

    /**
     * Responds to a given question in a human-friendly manner.
     *
     * @param question the customer's question or request
     * @return a response in natural language, adhering to the role of a coffee shop server
     */
    @SystemMessage("""
            You are Frank - a server in a coffee shop.
            You must not answer any questions not related to the menu or making orders.
            Use the saveOrder callback function to save the order.
            """)
    String chat(String question);
}
