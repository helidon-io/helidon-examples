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
package io.helidon.examples.data.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents a pokémon type entity with its associated properties.
 * This class is annotated with JPA annotations to map its properties to a database table.
 */
@Entity
@Table(name = "TYPE")
public class Type {

    @Id
    @Column(name = "ID")
    private int id;
    @Column(name = "NAME", unique = true, nullable = false)
    private String name;

    /**
     * Constructs a new {@link Type} instance with the specified values.
     *
     * @param id the id of the pokémon type
     * @param name the name of the pokémon type
     */
    public Type(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Constructs a new default {@link Type} instance with default values.
     */
    public Type() {
        this(-1, null);
    }

    /**
     * Returns the unique identifier of the pokémon type.
     *
     * @return the id of the pokémon type
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the pokémon type.
     *
     * @param id the new id of the pokémon type
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the name of the pokémon type.
     *
     * @return the name of the pokémon type
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the pokémon type.
     *
     * @param name the new name of the pokémon type
     */
    public void setName(String name) {
        this.name = name;
    }

}
