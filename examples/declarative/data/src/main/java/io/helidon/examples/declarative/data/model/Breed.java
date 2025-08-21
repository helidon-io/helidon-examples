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
package io.helidon.examples.declarative.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A breed entity.
 */
@Entity
@Table(name = "BREED")
public class Breed {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "NAME", unique = true, nullable = false)
    private String name;

    /**
     * Constructs a new {@link Breed} instance with the specified values.
     *
     * @param name the name of the breed
     */
    public Breed(String name) {
        this.id = -1;
        this.name = name;
    }

    /**
     * Constructs a new default {@link Breed} instance with default values.
     */
    public Breed() {
        this(null);
    }

    /**
     * Returns the unique identifier of this breed.
     *
     * @return the id of the breed
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this breed.
     *
     * @param id the new id of the breed
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the name of this breed.
     *
     * @return the name of the breed
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this breed.
     *
     * @param name the new name of the breed
     */
    public void setName(String name) {
        this.name = name;
    }

}
