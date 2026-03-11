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
package io.helidon.hol.agentic.assistant.data;

import java.util.logging.Logger;

import io.helidon.service.registry.Service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;

/**
 * Populates embedding store with coffee menu entries.
 */
@Service.Singleton
public class MenuItemsIngestor {
    private static final Logger LOGGER = Logger.getLogger(MenuItemsIngestor.class.getName());

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final MenuItemsService menuItemsService;

    @Service.Inject
    MenuItemsIngestor(@Service.Named("menu-embedding-store") EmbeddingStore<TextSegment> embeddingStore,
                      EmbeddingModel embeddingModel,
                      MenuItemsService menuItemsService) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.menuItemsService = menuItemsService;
    }

    /**
     * Ingests menu items into embeddings.
     */
    public void ingest() {
        var ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        var documents = menuItemsService.getMenuItems().stream()
                .map(this::generateDocument)
                .toList();

        ingestor.ingest(documents);
        LOGGER.info("Ingested menu items: " + documents.size());
    }

    private Document generateDocument(MenuItem item) {
        var line = String.format("%s: %s. Category: %s. Price: $%.2f. Tags: %s. Add-ons: %s.",
                                 item.getName(),
                                 item.getDescription(),
                                 item.getCategory(),
                                 item.getPrice(),
                                 String.join(", ", item.getTags()),
                                 String.join(", ", item.getAddOns()));
        return Document.from(line);
    }
}
