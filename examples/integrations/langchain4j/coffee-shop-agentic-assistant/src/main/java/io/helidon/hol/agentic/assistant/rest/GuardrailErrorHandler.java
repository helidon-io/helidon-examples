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

package io.helidon.hol.agentic.assistant.rest;

import io.helidon.http.Status;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.ErrorHandler;
import io.helidon.webserver.http.spi.ErrorHandlerProvider;

import dev.langchain4j.agentic.agent.AgentInvocationException;
import dev.langchain4j.guardrail.GuardrailException;

@Service.Singleton
class GuardrailErrorHandler implements ErrorHandlerProvider<AgentInvocationException> {
    @Override
    public Class<AgentInvocationException> errorType() {
        return AgentInvocationException.class;
    }

    @Override
    public ErrorHandler<AgentInvocationException> create() {
        return (req, res, t) -> {
            var guardrailMessage = findGuardrailMessage(t);
            if (guardrailMessage == null) {
                throw t;
            }
            res.status(Status.BAD_REQUEST_400)
                    .send(guardrailMessage);
        };
    }

    private static String findGuardrailMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof GuardrailException guardrailException) {
                return guardrailException.getMessage();
            }
            current = current.getCause();
        }
        return null;
    }
}
