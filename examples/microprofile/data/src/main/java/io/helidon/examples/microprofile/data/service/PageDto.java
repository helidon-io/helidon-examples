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
package io.helidon.examples.microprofile.data.service;

import java.util.List;

import io.helidon.data.Slice;

/**
 * Represents a single page of data, encapsulating a list of entities along with pagination metadata.
 * <p>
 * This class is designed to work with {@link Slice} objects, providing a convenient way to access
 * paginated data.
 *
 * @param data a list of entities on a single page
 * @param page page number, starting from {@code 0}
 * @param size actual page size (less than or equal to the requested page size)
 * @param <D> type of the data objects contained in the page
 */
public record PageDto<D>(List<D> data,
                         int page,
                         int size) {

    /**
     * Constructs a new {@link PageDto} instance from provided {@link Slice}.
     * <p>
     * This method extracts the necessary information from the {@link Slice} to construct a
     * {@link PageDto} object, including the list of data, page number, and page size.
     *
     * @param slice the source of the page data
     * @param <D> the type of data objects contained in the slice
     * @return a new {@link PageDto} instance representing the page data
     */
    static <D> PageDto<D> create(Slice<D> slice) {
        return new PageDto<>(slice.list(),
                             slice.request().page(),
                             slice.list().size());
    }

}
