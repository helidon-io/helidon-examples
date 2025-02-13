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

package io.helidon.examples.integrations.oci.genai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.config.Config;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SessionTokenAuthenticationDetailsProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import com.oracle.bmc.generativeaiinference.model.ChatContent;
import com.oracle.bmc.generativeaiinference.model.ChatDetails;
import com.oracle.bmc.generativeaiinference.model.ChatResult;
import com.oracle.bmc.generativeaiinference.model.EmbedTextDetails;
import com.oracle.bmc.generativeaiinference.model.EmbedTextResult;
import com.oracle.bmc.generativeaiinference.model.GenericChatRequest;
import com.oracle.bmc.generativeaiinference.model.Message;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.generativeaiinference.model.ServingMode;
import com.oracle.bmc.generativeaiinference.model.TextContent;
import com.oracle.bmc.generativeaiinference.model.UserMessage;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.bmc.generativeaiinference.requests.EmbedTextRequest;
import com.oracle.bmc.generativeaiinference.responses.ChatResponse;
import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;

/**
 * REST API example of how to use OCI Generative AI Service.
 */
public class GenAiService implements HttpService {
    private static final Logger LOGGER = Logger.getLogger(GenAiService.class.getName());
    private static final String CHAT_QUERY_PARAM = "userMessage";
    private static final String EMBEDDING_QUERY_PARAM = "embeddingInputs";
    private final GenerativeAiInferenceClient generativeAiInferenceClient;
    private final String compartmentId;
    private final String chatModelId;
    private final String embedModelId;

    GenAiService() {
        Config config = Config.global();
        // Initialize GenAI client based on OCI Auth as configured in config system
        try {
            AuthenticationDetailsProvider authProvider = new SessionTokenAuthenticationDetailsProvider(
                    ConfigFileReader.DEFAULT_FILE_PATH, config.get("oci.config.profile").asString().get());
            this.generativeAiInferenceClient = GenerativeAiInferenceClient.builder()
                    .region(Region.valueOf(config.get("oci.genai.region").asString().get()))
                    .build(authProvider);
        } catch (IOException ioe) {
            throw new RuntimeException("Can't create GenAIService as OCI Auth Failed" + ioe.getMessage());
        }
        this.compartmentId = config.get("oci.genai.compartment_id").asString().get();
        this.chatModelId = config.get("oci.genai.chat.model_id").asString().get();
        this.embedModelId = config.get("oci.genai.embedding.model_id").asString().get();
    }

    @Override
    public void routing(HttpRules rules) {
        rules.get("/chat", this::chat);
        rules.get("/embedText", this::embedText);
    }

    /**
     * Handles an HTTP GET request to initiate a chat session with the Oracle Cloud Infrastructure Generative AI service.
     *
     * This method takes a user-provided message as input and sends it to the Generative AI service for processing.
     * It then retrieves the response from the service and returns it back to the client.
     *
     * @param req the incoming HTTP request containing the user's message
     * @param res the outgoing HTTP response to send back to the client
     */
    public void chat(ServerRequest req, ServerResponse res) {
        String userMessage = req.query().get(CHAT_QUERY_PARAM);
        LOGGER.log(Level.INFO, "Start Running Chat Example ...");
        LOGGER.log(Level.INFO, "UserMessage is: " + userMessage);
        ChatContent content = TextContent.builder()
                .text(userMessage)
                .build();
        List<ChatContent> contents = new ArrayList<>();
        contents.add(content);
        Message message = UserMessage.builder()
                .content(contents)
                .build();
        List<Message> messages = new ArrayList<>();
        messages.add(message);
        GenericChatRequest chatRequest = GenericChatRequest.builder()
                .messages(messages)
                .isStream(false)
                .build();
        ServingMode servingmode = OnDemandServingMode.builder()
                .modelId(chatModelId)
                .build();
        ChatDetails details = ChatDetails.builder()
                .servingMode(servingmode)
                .compartmentId(compartmentId)
                .chatRequest(chatRequest)
                .build();
        ChatRequest request = ChatRequest.builder()
                .chatDetails(details)
                .build();
        ChatResponse response = generativeAiInferenceClient.chat(request);
        ChatResult chatResult = response.getChatResult();
        LOGGER.log(Level.INFO, "Chat Result is: " + chatResult.toString());
        res.send(chatResult.toString());
    }

    /**
     * Handles an HTTP GET request to generate embeddings for a given set of text inputs using the Oracle Cloud Infrastructure
     * Generative AI service.
     *
     * This method takes a comma-separated string of text inputs as a path parameter, splits them into individual inputs,
     * and sends them to the Generative AI service for embedding generation. It then retrieves the generated embeddings
     * from the service and returns them back to the client as a JSON string.
     *
     * @param req the incoming HTTP request containing the text inputs
     * @param res the outgoing HTTP response to send back to the client
     */
    public void embedText(ServerRequest req, ServerResponse res) {
        String embeddingInputs = req.query().get(EMBEDDING_QUERY_PARAM);
        List<String> embeddingInputsList = Arrays.asList(embeddingInputs.split(","));
        LOGGER.log(Level.INFO, "Start Running EmbedText Example ...");
        LOGGER.log(Level.INFO, "Embedding Inputs is: " + embeddingInputs);
        EmbedTextDetails embedTextDetails = EmbedTextDetails.builder()
                .servingMode(OnDemandServingMode.builder().modelId(embedModelId).build())
                .compartmentId(compartmentId)
                .inputs(embeddingInputsList)
                .build();
        EmbedTextRequest embedTextRequest = EmbedTextRequest.builder()
                .embedTextDetails(embedTextDetails)
                .build();
        EmbedTextResponse embedTextResponse = generativeAiInferenceClient.embedText(embedTextRequest);
        EmbedTextResult embedTextResult = embedTextResponse.getEmbedTextResult();
        LOGGER.log(Level.INFO, embedTextResult.toString());
        res.send(embedTextResult.toString());
    }
}

