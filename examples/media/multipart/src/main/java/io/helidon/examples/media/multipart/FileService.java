/*
 * Copyright (c) 2020, 2024 Oracle and/or its affiliates.
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
package io.helidon.examples.media.multipart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.BadRequestException;
import io.helidon.http.ContentDisposition;
import io.helidon.http.Header;
import io.helidon.http.HeaderNames;
import io.helidon.http.HeaderValues;
import io.helidon.http.NotFoundException;
import io.helidon.http.ServerResponseHeaders;
import io.helidon.http.media.multipart.MultiPart;
import io.helidon.http.media.multipart.ReadablePart;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;

import static io.helidon.http.Status.MOVED_PERMANENTLY_301;

/**
 * File service.
 */
public final class FileService implements HttpService {
    private static final Header UI_LOCATION = HeaderValues.createCached(HeaderNames.LOCATION, "/ui");
    private final JsonBuilderFactory jsonFactory = Json.createBuilderFactory(Map.of());
    private final Path storage;

    /**
     * Create a new file upload service instance.
     */
    FileService() {
        storage = createStorage();
        System.out.println("Storage: " + storage);
    }

    @Override
    public void routing(HttpRules rules) {
        rules.get("/", this::list)
             .get("/{fileName}", this::download)
             .post("/", this::upload);
    }

    private static Path createStorage() {
        try {
            return Files.createTempDirectory("file-upload");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static List<String> listFiles(Path storage) {
        try (Stream<Path> walk = Files.walk(storage)) {
            return walk.filter(Files::isRegularFile)
                       .map(storage::relativize)
                       .map(Path::toString)
                       .toList();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static OutputStream newOutputStream(Path storage, String fileName) {
        try {
            return Files.newOutputStream(storage.resolve(fileName),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void list(ServerRequest req, ServerResponse res) {
        res.send(jsonFactory.createObjectBuilder()
                .add("files", jsonFactory.createArrayBuilder(listFiles(storage)))
                .build());
    }

    private void download(ServerRequest req, ServerResponse res) {
        Path filePath = req.path().pathParameters().first("fileName")
                .map(storage::resolve)
                .orElseThrow();
        if (!filePath.getParent().equals(storage)) {
            throw new BadRequestException("Invalid file name");
        }
        if (!Files.exists(filePath)) {
            throw new NotFoundException("File not found");
        }
        if (!Files.isRegularFile(filePath)) {
            throw new BadRequestException("Not a file");
        }
        ServerResponseHeaders headers = res.headers();
        headers.contentType(MediaTypes.APPLICATION_OCTET_STREAM);
        headers.set(ContentDisposition.builder()
                                      .filename(filePath.getFileName().toString())
                                      .build());
        res.send(filePath);
    }

    private void upload(ServerRequest req, ServerResponse res) {
        MultiPart mp = req.content().as(MultiPart.class);

        while (mp.hasNext()) {
            ReadablePart part = mp.next();
            if ("file[]".equals(URLDecoder.decode(part.name(), StandardCharsets.UTF_8))) {
                try (InputStream in = part.inputStream();
                        OutputStream out = newOutputStream(storage, part.fileName().orElseThrow())) {
                    in.transferTo(out);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write content", e);
                }
            }
        }

        res.status(MOVED_PERMANENTLY_301)
           .header(UI_LOCATION)
           .send();
    }
}
