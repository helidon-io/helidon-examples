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
package io.helidon.examples.microprofile.data.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Represents a pet entity with attributes such as id, name, weight, birth date, owner, and breed.
 * This class is annotated with JPA annotations to map its attributes to a database table.
 */
@Entity
@Table(name = "PET")
public class Pet {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "NAME", unique = true, nullable = false)
    private String name;

    @Column(name = "WEIGHT")
    private float weight;

    @Column(name = "BIRTH")
    private LocalDate birth;

    @ManyToOne
    @JoinColumn(name = "OWNER_ID", nullable = false)
    private Owner owner;

    @ManyToOne
    @JoinColumn(name = "BREED_ID", nullable = false)
    private Breed breed;

    /**
     * Constructs a new {@link Pet} instance with the specified values.
     *
     * @param name   the name of the pet
     * @param weight the weight of the pet
     * @param birth  the birth date of the pet
     * @param owner  the owner of the pet
     * @param breed  the breed of the pet
     */
    public Pet(String name, float weight, LocalDate birth, Owner owner, Breed breed) {
        this.id = -1;
        this.name = name;
        this.weight = weight;
        this.birth = birth;
        this.owner = owner;
        this.breed = breed;
    }

    /**
     * Constructs a new default {@link Pet} instance with default values.
     */
    public Pet() {
        this(null, -1.0f, null, null, null);
    }

    /**
     * Returns the unique identifier of this pet.
     *
     * @return the id of the pet
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this pet.
     *
     * @param id the new id of the pet
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the name of this pet.
     *
     * @return the name of the pet
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this pet.
     *
     * @param name the new name of the pet
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the weight of this pet.
     *
     * @return the weight of the pet
     */
    public float getWeight() {
        return weight;
    }

    /**
     * Sets the weight of this pet.
     *
     * @param weight the new weight of the pet
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }

    /**
     * Returns the birth date of this pet.
     *
     * @return the birth date of the pet
     */
    public LocalDate getBirth() {
        return birth;
    }

    /**
     * Sets the birth date of this pet.
     *
     * @param birth the new birth date of the pet
     */
    public void setBirth(LocalDate birth) {
        this.birth = birth;
    }

    /**
     * Returns the owner of this pet.
     *
     * @return the owner of the pet
     */
    public Owner getOwner() {
        return owner;
    }

    /**
     * Sets the owner of this pet.
     *
     * @param owner the new owner of the pet
     */
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    /**
     * Returns the breed of this pet.
     *
     * @return the breed of the pet
     */
    public Breed getBreed() {
        return breed;
    }

    /**
     * Sets the breed of this pet.
     *
     * @param breed the new breed of the pet
     */
    public void setBreed(Breed breed) {
        this.breed = breed;
    }

}
