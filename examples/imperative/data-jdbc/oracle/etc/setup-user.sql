--
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
--

WHENEVER SQLERROR EXIT SQL.SQLCODE

ALTER SESSION SET CONTAINER = FREEPDB1;

DECLARE
    TABLESPACE_COUNT PLS_INTEGER;
    DATAFILE_DIRECTORY VARCHAR2(4000);
BEGIN
    SELECT COUNT(*)
      INTO TABLESPACE_COUNT
      FROM DBA_TABLESPACES
     WHERE TABLESPACE_NAME = 'POKEMON_DATA';

    IF TABLESPACE_COUNT = 0 THEN
        SELECT SUBSTR(FILE_NAME, 1, INSTR(FILE_NAME, '/', -1))
          INTO DATAFILE_DIRECTORY
          FROM DBA_DATA_FILES
         WHERE TABLESPACE_NAME = 'SYSTEM'
           AND ROWNUM = 1;

        EXECUTE IMMEDIATE 'CREATE TABLESPACE POKEMON_DATA DATAFILE '''
                       || DATAFILE_DIRECTORY
                       || 'pokemon_data01.dbf'' SIZE 20M '
                       || 'AUTOEXTEND ON NEXT 10M MAXSIZE 100M';
    END IF;
END;
/

DECLARE
    USER_COUNT PLS_INTEGER;
BEGIN
    SELECT COUNT(*)
      INTO USER_COUNT
      FROM DBA_USERS
     WHERE USERNAME = 'POKEMON';

    IF USER_COUNT = 0 THEN
        EXECUTE IMMEDIATE 'CREATE USER POKEMON '
                       || 'IDENTIFIED BY changeit '
                       || 'DEFAULT TABLESPACE POKEMON_DATA '
                       || 'QUOTA 10M ON POKEMON_DATA';
    END IF;
END;
/

GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO POKEMON;

EXIT SUCCESS
