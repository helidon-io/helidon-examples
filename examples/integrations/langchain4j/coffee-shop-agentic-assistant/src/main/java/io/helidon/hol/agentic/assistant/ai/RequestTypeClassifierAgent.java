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

import io.helidon.hol.agentic.assistant.dto.CoffeeRequestType;
import io.helidon.integrations.langchain4j.Ai;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@Ai.Agent("request-type-classifier")
@Ai.ChatModel("cheap-model")
public interface RequestTypeClassifierAgent {

    @UserMessage("""
            Analyze the user request and classify it into one of these categories:
            - MENU: questions about menu items, ingredients, pricing, recommendations, dietary options, availability
            - ORDER: requests to place, confirm, modify, or complete a purchase order
            - OFF_TOPIC: anything not related to coffee shop menu or ordering

            Reply with exactly one value: MENU, ORDER, or OFF_TOPIC.
            User request: {{question}}
            """)
    @Agent(value = "Classify coffee-shop request", outputKey = "requestType")
    CoffeeRequestType classify(@V("question") String question);
}
