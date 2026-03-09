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
package io.helidon.hol.agentic.assistant.tools;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import io.helidon.service.registry.Service;

import dev.langchain4j.agent.tool.Tool;

/**
 * Tool service for persisting customer orders.
 */
@Service.Singleton
public class OrderService {
    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());

    @Tool("Saves a coffee order represented by item name to item price map and returns order ID.")
    public String saveOrder(Map<String, BigDecimal> orderItems) {
        StringBuilder msg = new StringBuilder("\n** New Coffee Order **\n");

        var orderId = UUID.randomUUID();
        msg.append("Order id: ").append(orderId).append("\n");
        for (var entry : orderItems.entrySet()) {
            msg.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        LOGGER.info(msg.toString());
        return orderId.toString();
    }
}
