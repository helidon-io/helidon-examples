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
import io.helidon.hol.agentic.assistant.guardrail.InputPromptGuardrail;
import io.helidon.testing.junit5.Testing;

import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Testing.Test
class CoffeeShopAssistantAgentTest {
    private static final String OFF_TOPIC_QUESTION = "Can you explain Kubernetes networking?";
    private static final String OFF_TOPIC_RESPONSE = "I can only help with coffee menu questions and placing orders.";
    private static final String OFF_TOPIC_SUMMARY =
            "summary: user asked an off-topic question and was redirected to coffee topics.";

    @Test
    void classifyRequestTypeDeterministically(RequestTypeClassifierAgent classifier) {
        assertThat(classifier.classify("What hot drinks do you have?"), is(CoffeeRequestType.MENU));
        assertThat(classifier.classify("Please order one cappuccino."), is(CoffeeRequestType.ORDER));
        assertThat(classifier.classify(OFF_TOPIC_QUESTION), is(CoffeeRequestType.OFF_TOPIC));
    }

    @Test
    void offTopicWorkflowIsDeterministic(CoffeeShopAssistantAgent assistant) {
        var response = assistant.chat(OFF_TOPIC_QUESTION, "");
        assertThat(response.message(), is(OFF_TOPIC_RESPONSE));
        assertThat(response.summary(), is(OFF_TOPIC_SUMMARY));
    }

    @Test
    void inputGuardrailRejectsForbiddenPhrase(InputPromptGuardrail inputPromptGuardrail) {
        var result = inputPromptGuardrail.validate(UserMessage.userMessage("Do you have decaf espresso?"));
        assertThat(result.isFatal(), is(true));
        assertThat(result.failures().getFirst().message(),
                   containsString("Forbidden phrase detected in prompt: decaf"));
    }
}
