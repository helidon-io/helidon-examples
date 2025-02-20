package io.helidon.examples.integrations.langchain4j.mp.coffee.shop.assistant.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MenuItemsService {
    private final Path jsonPath;

    @Inject
    MenuItemsService(@ConfigProperty(name = "app.menu-items") String jsonPath) {
        this.jsonPath = Path.of(jsonPath);
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
