package io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant.rest;

import io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant.ai.ChatAiService;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

@Service.Singleton
public class ChatBotService implements HttpService {

    private final ChatAiService chatAiService;

    @Service.Inject
    public ChatBotService(ChatAiService chatAiService) {
        this.chatAiService = chatAiService;
    }

    @Override
    public void routing(HttpRules httpRules) {
        httpRules.get("/chat", this::chatWithAssistant);
    }

    private void chatWithAssistant(ServerRequest req, ServerResponse res) {
        var question = req.query().first("question").orElse("Hello");
        var answer = chatAiService.chat(question);
        res.send(answer);
    }
}
