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
package io.helidon.hol.agentic.assistant.guardrail;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import io.helidon.config.Config;
import io.helidon.service.registry.Service;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

/**
 * Input guardrail for user prompts, driven by configured forbidden phrases.
 */
@Service.Singleton
public class InputPromptGuardrail implements InputGuardrail {
    private static final String CONFIG_KEY = "app.forbidden-phrases";

    private final List<String> forbiddenPhrases;

    @Service.Inject
    InputPromptGuardrail(Config config) {
        this.forbiddenPhrases = loadForbiddenPhrases(config);
    }

    private static List<String> loadForbiddenPhrases(Config config) {
        var listValue = config.get(CONFIG_KEY)
                .asList(String.class)
                .orElse(List.of());
        if (!listValue.isEmpty()) {
            return listValue.stream()
                    .map(String::trim)
                    .filter(phrase -> !phrase.isEmpty())
                    .map(phrase -> phrase.toLowerCase(Locale.ROOT))
                    .toList();
        }
        return config.get(CONFIG_KEY)
                .asString()
                .stream()
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(String::trim)
                .filter(phrase -> !phrase.isEmpty())
                .map(phrase -> phrase.toLowerCase(Locale.ROOT))
                .toList();
    }

    private Optional<String> findForbiddenPhrase(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return Optional.empty();
        }
        var normalizedPrompt = prompt.toLowerCase(Locale.ROOT);
        return forbiddenPhrases.stream()
                .filter(normalizedPrompt::contains)
                .findFirst();
    }

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        return findForbiddenPhrase(userMessage.singleText())
                .map(phrase -> this.fatal("Forbidden phrase detected in prompt: " + phrase))
                .orElseGet(this::success);
    }
}
