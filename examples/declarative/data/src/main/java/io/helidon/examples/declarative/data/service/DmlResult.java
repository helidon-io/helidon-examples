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
package io.helidon.examples.declarative.data.service;

/**
 * Simple DML database operation result.
 * <p>
 * This record contains information about the result of a DML (Data Manipulation Language) operation,
 * such as the number of records modified and the type of operation performed.
 *
 * @param records   the number of records modified in the database
 * @param operation the name of the database operation, e.g. {@code "DELETE"}
 */
public record DmlResult(long records,
                        String operation) {
}
