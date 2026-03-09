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
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@Ai.Agent("summarizer")
@Ai.ChatModel("cheap-model")
public interface SummarizerAgent {

    @SystemMessage("""
        You summarize a coffee-shop assistant conversation.
        Keep factual summary and try to preserve as much information as possible.
        Include user preferences, selected items, and outstanding order questions.
        """)
    @UserMessage("""
        Previous Summary:
        {{previousSummary}}

        Last User Message:
        {{question}}

        Last AI Response:
        {{lastResponse}}
        """)
    @Agent(value = "Conversation summarizer", outputKey = "nextSummary")
    String chat(@V("previousSummary") String previousSummary,
                @V("question") String question,
                @V("lastResponse") String lastResponse);
}
