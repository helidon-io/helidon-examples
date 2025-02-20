package io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import io.helidon.common.config.Config;
import io.helidon.service.registry.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service.Singleton
public class MenuItemsService {
    private static final String CONFIG_KEY = "app.menu-items";
    private final Path jsonPath;

    @Service.Inject
    MenuItemsService(Config config) {
        this.jsonPath = config.get(CONFIG_KEY)
                .as(Path.class)
                .orElseThrow(() -> new IllegalStateException(CONFIG_KEY + " is a required configuration key for RAG"));
    }

    public List<MenuItem> getMenuItems() {
        var objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonPath.toFile(), new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
