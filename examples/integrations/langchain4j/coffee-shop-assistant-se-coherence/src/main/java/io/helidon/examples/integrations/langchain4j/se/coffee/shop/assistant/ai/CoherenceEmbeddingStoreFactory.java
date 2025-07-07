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
package io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant.ai;

import java.util.function.Supplier;

import io.helidon.common.config.Config;
import io.helidon.service.registry.Service;

import com.oracle.coherence.ai.DocumentChunk;
import com.oracle.coherence.ai.VectorIndexExtractor;
import com.oracle.coherence.ai.hnsw.HnswIndex;
import com.tangosol.util.ValueExtractor;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.coherence.CoherenceEmbeddingStore;

/**
 * A factory service that provides an instance of {@link dev.langchain4j.store.embedding.coherence.CoherenceEmbeddingStore}.
 *
 * This class implements {@link Supplier} to supply a named embedding store instance.
 */
@Service.Singleton
@Service.Named("CoherenceEmbeddingStore")
public class CoherenceEmbeddingStoreFactory implements Supplier<CoherenceEmbeddingStore> {
    private static final System.Logger LOGGER = System.getLogger(CoherenceEmbeddingStoreFactory.class.getName());

    @Service.Inject
    @SuppressWarnings("checkstyle:VisibilityModifier")
    Config config;

    @Service.Inject
    @SuppressWarnings("checkstyle:VisibilityModifier")
    EmbeddingModel embeddingModel;

    @Override
    public CoherenceEmbeddingStore get() {
        var builder = CoherenceEmbeddingStore.builder();

        builder.session(config.get("langchain4j.coherence.embedding-store.session").as(String.class).orElse(null));
        builder.name(config.get("langchain4j.coherence.embedding-store.name").as(String.class).orElse(null));
        builder.normalizeEmbeddings(config.get("langchain4j.coherence.embedding-store.normalizeEmbeddings").as(Boolean.class)
                                            .orElse(false));

        VectorIndexExtractor extractor = null;
        if ("hnsw".equalsIgnoreCase(config.get("langchain4j.coherence.embedding-store.index").as(String.class).orElse(null))) {
            Integer dimension = embeddingModel != null ? (Integer) embeddingModel.dimension()
                    : config.get("langchain4j.coherence.embedding-store.dimension").as(Integer.class).orElse(null);
            if (dimension != null) {
                extractor = new HnswIndex<>(ValueExtractor.of(DocumentChunk::vector), dimension);
            } else {
                LOGGER.log(System.Logger.Level.WARNING,
                           "Cannot create embedding hnsw store index - No dimension name has been specified for the hnsw index.");
            }
        }
        builder.index(extractor);

        return builder.build();
    }
}
