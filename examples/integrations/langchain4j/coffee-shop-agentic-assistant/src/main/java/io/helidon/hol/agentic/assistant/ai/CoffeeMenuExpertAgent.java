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

@Ai.Agent("coffee-menu-expert")
@Ai.ChatModel("expensive-model")
@Ai.ContentRetriever("menu-content-retriever")
public interface CoffeeMenuExpertAgent {

    @UserMessage("""
            You are Frank, a coffee shop server and menu expert.
            Use retrieved menu context to answer menu questions accurately.
            Mention prices and possible add-ons when relevant.
            If information is not in the menu context, say so clearly.

            User request: {{question}}
            """)
    @Agent(value = "Coffee menu expert", outputKey = "lastResponse")
    String askMenuQuestion(@V("question") String question);
}
