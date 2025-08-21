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
package io.helidon.examples.microprofile.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Represents a pokémon entity with its associated properties.
 * This class is annotated with JPA annotations to map its properties to a database table.
 */
@Entity
@Table(name = "POKEMON")
public class Pokemon {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "NAME", unique = true, nullable = false)
    private String name;
    @ManyToOne
    @JoinColumn(name = "TYPE_ID", nullable = false)
    private Type type;

    /**
     * Constructs a new {@link Pokemon} instance with the specified values.
     *
     * @param name the name of the pokémon
     * @param type the type of the pokémon
     */
    public Pokemon(String name, Type type) {
        this.id = null;
        this.name = name;
        this.type = type;
    }

    /**
     * Constructs a new default {@link Pokemon} instance with default values.
     */
    public Pokemon() {
        this(null, null);
    }

    /**
     * Returns the unique identifier of the pokémon.
     *
     * @return the id of the pokémon
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the pokémon.
     *
     * @param id the new id of the pokémon
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Returns the name of the pokémon.
     *
     * @return the name of the pokémon
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the pokémon.
     *
     * @param name the new name of the pokémon
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the type of the pokémon.
     *
     * @return the type of the pokémon
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of the pokémon.
     *
     * @param type the new type of the pokémon
     */
    public void setType(Type type) {
        this.type = type;
    }

}
