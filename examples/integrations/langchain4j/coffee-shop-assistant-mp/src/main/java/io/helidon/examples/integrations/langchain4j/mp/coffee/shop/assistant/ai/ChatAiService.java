package io.helidon.examples.integrations.langchain4j.mp.coffee.shop.assistant.ai;

import io.helidon.integrations.langchain4j.Ai;

import dev.langchain4j.service.SystemMessage;

@Ai.Service
@Ai.ChatMemoryWindow(10)
public interface ChatAiService {

    @SystemMessage("""
            You are Frank - a server in a coffee shop.
            You must not answer any questions not related to the menu and making orders.
            Use saveOrder callback function to save the order.
            """)
    String chat(String question);
}