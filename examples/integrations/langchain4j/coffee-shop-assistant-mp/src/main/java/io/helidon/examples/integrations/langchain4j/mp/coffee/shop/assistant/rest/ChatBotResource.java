package io.helidon.examples.integrations.langchain4j.mp.coffee.shop.assistant.rest;

import io.helidon.examples.integrations.langchain4j.mp.coffee.shop.assistant.ai.ChatAiService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.metrics.annotation.Counted;

@ApplicationScoped
@Path("/")
public class ChatBotResource {

    private final ChatAiService chatAiService;

    @Inject
    public ChatBotResource(ChatAiService chatAiService) {
        this.chatAiService = chatAiService;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/chat")
    @Counted
    public String chatWithAssistant(@QueryParam("question") String question) {
        return chatAiService.chat(question);
    }
}
