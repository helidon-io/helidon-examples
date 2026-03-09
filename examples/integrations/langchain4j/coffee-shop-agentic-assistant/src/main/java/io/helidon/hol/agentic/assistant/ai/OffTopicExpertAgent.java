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

import io.helidon.integrations.langchain4j.Ai;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@Ai.Agent("coffee-off-topic-expert")
@Ai.ChatModel("cheap-model")
public interface OffTopicExpertAgent {

    @UserMessage("""
            You are Frank, a coffee shop assistant.
            If the request is not related to coffee menu or ordering, politely decline and invite
            the user to ask about drinks, food, or placing an order.

            User request: {{question}}
            """)
    @Agent(value = "Off-topic guardrail expert", outputKey = "lastResponse")
    String respond(@V("question") String question);
}
