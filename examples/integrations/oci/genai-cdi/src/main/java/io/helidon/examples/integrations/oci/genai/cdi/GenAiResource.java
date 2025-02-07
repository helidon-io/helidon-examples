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

package io.helidon.examples.integrations.oci.genai.cdi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.oracle.bmc.Region;
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
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * JAX-RS resource - REST API example of how to use OCI Generative AI Service.
 */
@Path("/genai")
public class GenAiResource {
    private static final Logger LOGGER = Logger.getLogger(GenAiResource.class.getName());

    private final GenerativeAiInferenceClient generativeAiInferenceClient;

    @Inject
    @ConfigProperty(name = "oci.genai.compartment.id")
    private String compartmentId;

    @Inject
    @ConfigProperty(name = "oci.genai.chat.model.id")
    private String chatModelId;

    @Inject
    @ConfigProperty(name = "oci.genai.embedding.model.id")
    private String embedModelId;

    @Inject
    GenAiResource(GenerativeAiInferenceClient generativeAiInferenceClient,
                  @ConfigProperty(name = "oci.genai.region") String region) {
        this.generativeAiInferenceClient = generativeAiInferenceClient;
        generativeAiInferenceClient.setRegion(Region.valueOf(region));
    }

    /**
     * Handles HTTP GET requests to initiate a chat session with the Oracle Cloud Infrastructure (OCI)
     * Generative AI service. It takes a user-provided message as input and returns the response from
     * the chat model.
     *
     * @param userMessage the message sent by the user to initiate or continue the conversation
     * @return the response from the chat model as a JSON string
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("chat")
    public String chat(@QueryParam("userMessage") String userMessage) {
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
        generativeAiInferenceClient.close();
        return chatResult.toString();
    }

    /**
     * Handles HTTP GET requests to generate embeddings for a list of text inputs using the Oracle Cloud Infrastructure (OCI)
     * Generative AI service. It takes a list of text inputs as query parameters and returns the generated embeddings as a JSON
     * string.
     *
     * @param embeddingInputs a list of text inputs to generate embeddings for
     * @return the generated embeddings as a JSON string
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("embedText")
    public String embedText(@QueryParam("embeddingInputs") List<String> embeddingInputs) {
        LOGGER.log(Level.INFO, "Start Running EmbedText Example ...");
        LOGGER.log(Level.INFO, "Embedding Inputs is: " + embeddingInputs);
        EmbedTextDetails embedTextDetails = EmbedTextDetails.builder()
                .servingMode(OnDemandServingMode.builder().modelId(embedModelId).build())
                .compartmentId(compartmentId)
                .inputs(embeddingInputs)
                .build();
        EmbedTextRequest embedTextRequest = EmbedTextRequest.builder()
                .embedTextDetails(embedTextDetails)
                .build();
        EmbedTextResponse embedTextResponse = generativeAiInferenceClient.embedText(embedTextRequest);
        EmbedTextResult embedTextResult = embedTextResponse.getEmbedTextResult();
        LOGGER.log(Level.INFO, embedTextResult.toString());
        generativeAiInferenceClient.close();
        return embedTextResult.toString();
    }
}

