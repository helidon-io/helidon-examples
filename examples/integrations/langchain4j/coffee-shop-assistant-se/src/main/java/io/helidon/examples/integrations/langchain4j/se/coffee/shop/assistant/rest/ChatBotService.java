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
package io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant.rest;

import io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant.ai.ChatAiService;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * HTTP service for interacting with the AI assistant.
 *
 * This service exposes a REST endpoint that allows clients to send chat queries
 * to the AI assistant and receive responses.
 */
@Service.Singleton
public class ChatBotService implements HttpService {

    private final ChatAiService chatAiService;

    /**
     * Constructs a {@code ChatBotService} instance.
     *
     * @param chatAiService the AI assistant service responsible for handling chat queries
     */
    @Service.Inject
    public ChatBotService(ChatAiService chatAiService) {
        this.chatAiService = chatAiService;
    }

    @Override
    public void routing(HttpRules httpRules) {
        httpRules.get("/chat", this::chatWithAssistant);
    }

    /**
     * Handles chat requests from clients.
     *
     * This method extracts the user's question from the request query parameters,
     * forwards it to the AI assistant, and sends back the AI-generated response.
     * If no question is provided, "Hello" is used as the default.
     *
     * @param req the server request containing query parameters
     * @param res the server response to send back the AI assistant's answer
     */
    private void chatWithAssistant(ServerRequest req, ServerResponse res) {
        var question = req.query().first("question").orElse("Hello");
        var answer = chatAiService.chat(question);
        res.send(answer);
    }
}
