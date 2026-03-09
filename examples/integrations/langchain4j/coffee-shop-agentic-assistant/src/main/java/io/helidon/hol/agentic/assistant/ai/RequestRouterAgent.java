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

import dev.langchain4j.agentic.declarative.ActivationCondition;
import dev.langchain4j.agentic.declarative.ConditionalAgent;
import dev.langchain4j.service.V;

import static java.lang.System.Logger.Level.INFO;

@Ai.Agent("request-router")
public interface RequestRouterAgent {

    System.Logger LOGGER = System.getLogger(RequestRouterAgent.class.getName());

    @ConditionalAgent(subAgents = {
            CoffeeMenuExpertAgent.class,
            CoffeeOrderExpertAgent.class,
            OffTopicExpertAgent.class
    })
    String route(@V("question") String question);

    @ActivationCondition(CoffeeMenuExpertAgent.class)
    static boolean activateMenuExpert(@V("requestType") CoffeeRequestType requestType) {
        if (requestType == CoffeeRequestType.MENU) {
            LOGGER.log(INFO, "Activate Menu Expert");
            return true;
        }
        return false;
    }

    @ActivationCondition(CoffeeOrderExpertAgent.class)
    static boolean activateOrderExpert(@V("requestType") CoffeeRequestType requestType) {
        if (requestType == CoffeeRequestType.ORDER) {
            LOGGER.log(INFO, "Activate Order Expert");
            return true;
        }
        return false;
    }

    @ActivationCondition(OffTopicExpertAgent.class)
    static boolean activateOffTopicExpert(@V("requestType") CoffeeRequestType requestType) {
        if (requestType == CoffeeRequestType.OFF_TOPIC) {
            LOGGER.log(INFO, "Activate Off-topic Expert");
            return true;
        }
        return false;
    }
}
