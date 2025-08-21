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
 * Represents an owner with attributes such as id and name.
 * This class is annotated with JPA annotations to map its attributes to a database table.
 */
@Entity
@Table(name = "OWNER")
public class Owner {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "NAME", unique = true, nullable = false)
    private String name;

    /**
     * Constructs a new {@link Owner} instance with the specified values.
     *
     * @param name the name of the owner
     */
    public Owner(String name) {
        this.id = -1;
        this.name = name;
    }

    /**
     * Constructs a new default {@link Owner} instance with default values.
     */
    public Owner() {
        this(null);
    }

    /**
     * Returns the unique identifier of this owner.
     *
     * @return the id of the owner
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this owner.
     *
     * @param id the new id of the owner
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the name of this owner.
     *
     * @return the name of the owner
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this owner.
     *
     * @param name the new name of the owner
     */
    public void setName(String name) {
        this.name = name;
    }

}
