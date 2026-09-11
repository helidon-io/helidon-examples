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

package io.helidon.examples.declarative.websocket;

import io.helidon.common.Api;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.Http;
import io.helidon.http.Status;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.RestServer;

@Http.Path("/rest")
@Service.Singleton
@RestServer.Endpoint
@SuppressWarnings(Api.SUPPRESS_PREVIEW)
class MessageQueueHttpEndpoint {
    private static final System.Logger LOGGER = System.getLogger(MessageQueueHttpEndpoint.class.getName());

    private final MessageQueue queue;

    @Service.Inject
    MessageQueueHttpEndpoint(MessageQueue queue) {
        this.queue = queue;
    }

    @Http.POST
    @Http.Consumes(MediaTypes.TEXT_PLAIN_VALUE)
    @RestServer.Status(Status.NO_CONTENT_204_CODE)
    void push(@Http.Entity String toPush) {
        // this is an example, in real-life application you cannot log in info level for each request,
        // and you need to sanitize user input before logging it
        LOGGER.log(System.Logger.Level.INFO, "push called '" + toPush + "'");
        queue.push(toPush);
    }
}
