package io.helidon.examples.data.mysql;

import io.helidon.data.Data;

@Data.Repository
public interface TypeRepository extends Data.GenericRepository <Type, Integer>{

    /**
     * Retrieves a {@link Type} entity by its name.
     *
     * @param name the name of the breed
     * @return the {@link Type} entity if found
     * @throws io.helidon.transaction.TxException when Breed with provided name was not found
     */
    Type getByName(String name);

}
