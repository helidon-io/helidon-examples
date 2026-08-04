-- Copyright (c) 2026 Oracle and/or its affiliates.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

CREATE TABLE TYPE (ID INTEGER NOT NULL PRIMARY KEY, NAME VARCHAR(255) UNIQUE NOT NULL);
CREATE TABLE POKEMON (
    ID @pokemon.id-definition@,
    NAME VARCHAR(255) UNIQUE NOT NULL,
    TYPE_ID INTEGER NOT NULL
@pokemon.table-end@
ALTER TABLE POKEMON ADD CONSTRAINT FK_POKEMON_TYPE_ID FOREIGN KEY (TYPE_ID) REFERENCES TYPE (ID);
INSERT INTO TYPE (ID, NAME) VALUES (1, 'Normal');
INSERT INTO TYPE (ID, NAME) VALUES (2, 'Fighting');
INSERT INTO TYPE (ID, NAME) VALUES (3, 'Flying');
INSERT INTO TYPE (ID, NAME) VALUES (4, 'Poison');
INSERT INTO TYPE (ID, NAME) VALUES (5, 'Ground');
INSERT INTO TYPE (ID, NAME) VALUES (6, 'Rock');
INSERT INTO TYPE (ID, NAME) VALUES (7, 'Bug');
INSERT INTO TYPE (ID, NAME) VALUES (8, 'Ghost');
INSERT INTO TYPE (ID, NAME) VALUES (9, 'Steel');
INSERT INTO TYPE (ID, NAME) VALUES (10, 'Fire');
INSERT INTO TYPE (ID, NAME) VALUES (11, 'Water');
INSERT INTO TYPE (ID, NAME) VALUES (12, 'Grass');
INSERT INTO TYPE (ID, NAME) VALUES (13, 'Electric');
INSERT INTO TYPE (ID, NAME) VALUES (14, 'Psychic');
INSERT INTO TYPE (ID, NAME) VALUES (15, 'Ice');
INSERT INTO TYPE (ID, NAME) VALUES (16, 'Dragon');
INSERT INTO TYPE (ID, NAME) VALUES (17, 'Dark');
INSERT INTO TYPE (ID, NAME) VALUES (18, 'Fairy');
INSERT INTO POKEMON (ID, NAME, TYPE_ID) VALUES (1, 'Pikachu', 13);
INSERT INTO POKEMON (ID, NAME, TYPE_ID) VALUES (2, 'Raichu', 13);
INSERT INTO POKEMON (ID, NAME, TYPE_ID) VALUES (3, 'Machop', 2);
INSERT INTO POKEMON (ID, NAME, TYPE_ID) VALUES (4, 'Snorlax', 1);
INSERT INTO POKEMON (ID, NAME, TYPE_ID) VALUES (5, 'Meowth', 1);
INSERT INTO POKEMON (ID, NAME, TYPE_ID) VALUES (6, 'Magikarp', 11);
INSERT INTO POKEMON (ID, NAME, TYPE_ID) VALUES (7, 'Ekans', 4);
INSERT INTO POKEMON (ID, NAME, TYPE_ID) VALUES (8, 'Arbok', 4);
INSERT INTO POKEMON (ID, NAME, TYPE_ID) VALUES (9, 'Sandshrew', 5);
INSERT INTO POKEMON (ID, NAME, TYPE_ID) VALUES (10, 'Sandslash', 5);
INSERT INTO POKEMON (ID, NAME, TYPE_ID) VALUES (11, 'Raikou', 13);
INSERT INTO POKEMON (ID, NAME, TYPE_ID) VALUES (12, 'Regirock', 6);
