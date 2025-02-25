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
package io.helidon.examples.integrations.langchain4j.mp.coffee.shop.assistant.ai;

import java.util.logging.Logger;

import io.helidon.examples.integrations.langchain4j.mp.coffee.shop.assistant.data.MenuItem;
import io.helidon.examples.integrations.langchain4j.mp.coffee.shop.assistant.data.MenuItemsService;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
public class MenuItemsIngestor {
    private static final Logger LOGGER = Logger.getLogger(MenuItemsIngestor.class.getName());

    private final MenuItemsService menuItemsService;

    @Produces
    final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    @Produces
    @Named("EmbeddingStore")
    final EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    @Inject
    MenuItemsIngestor(MenuItemsService menuItemsService) {
        this.menuItemsService = menuItemsService;
    }

    public void ingest(@Observes @Initialized(ApplicationScoped.class) Object pointless) {
        // Create ingestor with given embedding model and embedding storage
        var ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        // Read menu items from JSON
        var menuItems = menuItemsService.getMenuItems();

        // Create text representations of menu items
        var documents = menuItems.stream()
                .map(this::generateDocument)
                .toList();

        // Feed it to the ingestor to create embeddings and store them in embedding storage
        ingestor.ingest(documents);

        LOGGER.info("Ingested menu items: " + documents.size());
    }

    private Document generateDocument(MenuItem item) {
        var str = String.format(
                "%s: %s. Category: %s. Price: $%.2f. Tags: %s. Add-ons: %s.",
                item.getName(),
                item.getDescription(),
                item.getCategory(),
                item.getPrice(),
                String.join(", ", item.getTags()),
                String.join(", ", item.getAddOns())
        );

        return Document.from(str);
    }
}
