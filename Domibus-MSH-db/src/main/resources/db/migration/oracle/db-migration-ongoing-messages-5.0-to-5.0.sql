-- ********************************************************************************************************
-- Domibus 4.2.7 to 4.2.7 ongoing messages data migration package
--
-- Main entry point is the procedure 'migrate'. To be executed into a begin/end; block
--
-- Parameters to be adjusted:
-- BATCH_SIZE - size of the batch for data migration on each migrated table after which there is a commit;
--              default value is 10000
-- BULK_COLLECT_LIMIT - limit to avoid reading a high number of records into memory; default value is 10000
-- ********************************************************************************************************
CREATE OR REPLACE PACKAGE MIGRATE_ONGOING_MESSAGES IS
    -- batch size for commit of the migrated records
    BATCH_SIZE CONSTANT NUMBER := 10000;

    -- limit loading a high number of records into memory
    BULK_COLLECT_LIMIT CONSTANT NUMBER := 10000;

    DEFAULT_MIGRATION_START_DATE            CONSTANT TIMESTAMP               := TIMESTAMP '1970-01-01 00:00:00.00';
    DEFAULT_MIGRATION_END_DATE              CONSTANT TIMESTAMP               := SYSTIMESTAMP;

    TYPE T_MIGRATION_DETAILS IS RECORD (
        -- optional, before end date (default: 1st of January 1970 00:00:00.00)
        startDate           TIMESTAMP            := DEFAULT_MIGRATION_START_DATE,

        -- optional (default: SYSTIMESTAMP)
        endDate             TIMESTAMP            := DEFAULT_MIGRATION_END_DATE
    );

    PROCEDURE migrate(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS);

END MIGRATE_ONGOING_MESSAGES;
/

CREATE OR REPLACE PACKAGE BODY MIGRATE_ONGOING_MESSAGES IS

    TYPE T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS TABLE OF NUMBER INDEX BY PLS_INTEGER;

    TYPE T_MESSAGE_STATUS_PRIMARY_KEYS IS TABLE OF TB_D_MESSAGE_STATUS.ID_PK%TYPE INDEX BY PLS_INTEGER;

    FUNCTION lookup_value_safely(lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS, lookup_key NUMBER) RETURN NUMBER IS
        value NUMBER;
    BEGIN
        value := lookup_key;

        IF value IS NOT NULL THEN
            value := lookup_table(value);
        END IF;

        RETURN value;
    END lookup_value_safely;

    FUNCTION new_remote_pk(db_link IN VARCHAR2) RETURN NUMBER IS
        remote_pk NUMBER;
    BEGIN
        EXECUTE IMMEDIATE 'SELECT HIBERNATE_SEQUENCE.nextval@' || db_link || ' FROM DUAL' INTO remote_pk;
        RETURN remote_pk;
    END new_remote_pk;

    FUNCTION migrate_tb_d_message_status(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, db_link IN VARCHAR2) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_d_message_status IS
            SELECT ID_PK, STATUS, CREATED_BY, CREATION_TIME
            FROM TB_D_MESSAGE_STATUS
            WHERE ID_PK IN ongoing_message_status_pks;

        TYPE T_D_MESSAGE_STATUS IS TABLE OF c_d_message_status%ROWTYPE;
        message_status T_D_MESSAGE_STATUS;

        remote_id TB_D_MESSAGE_STATUS.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_D_MESSAGE_STATUS entries...');
        OPEN c_d_message_status;
        LOOP
            FETCH c_d_message_status BULK COLLECT INTO message_status LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_status.COUNT = 0;

            FOR i IN message_status.FIRST .. message_status.LAST LOOP
                    BEGIN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MESSAGE_STATUS@' || db_link || ' WHERE STAATUS = :p_1'
                            INTO remote_id
                            USING message_status(i).STATUS;
                    EXCEPTION
                        WHEN NO_DATA_FOUND THEN
                            remote_id := new_remote_pk(db_link);
                            EXECUTE IMMEDIATE 'INSERT INTO TB_D_MESSAGE_STATUS@' || db_link || ' (ID_PK, STATUS, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4)'
                                USING remote_id,
                                message_status(i).STATUS,
                                message_status(i).CREATED_BY,
                                message_status(i).CREATION_TIME;
                    END;

                    localToRemotePks(message_status(i).ID_PK) := remote_id;
                    dbms_output.put_line('Local to remote mapping: TB_D_MESSAGE_STATUS[' || message_status(i).ID_PK || '] = ' || remote_id);
                END LOOP;

            dbms_output.put_line('Processed ' || message_status.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_message_status;

        RETURN localToRemotePks;
    END migrate_tb_d_message_status;

    FUNCTION migrate_tb_d_party(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_d_party IS
            SELECT ID_PK, "VALUE", "TYPE", CREATED_BY, CREATION_TIME
            FROM TB_D_PARTY
            WHERE ID_PK IN
                  (SELECT ID_PK FROM
                      ((SELECT DISTINCT um.FROM_PARTY_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_USER_MESSAGE_LOG uml
                                            ON um.ID_PK = uml.ID_PK
                        WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                       UNION

                       (SELECT DISTINCT um.TO_PARTY_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_USER_MESSAGE_LOG uml
                                            ON um.ID_PK = uml.ID_PK
                        WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                       UNION

                       (SELECT DISTINCT um.FROM_PARTY_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_SIGNAL_MESSAGE_LOG sml
                                            ON um.ID_PK = sml.ID_PK
                        WHERE sml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND sml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                       UNION

                       (SELECT DISTINCT um.TO_PARTY_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_SIGNAL_MESSAGE_LOG sml
                                            ON um.ID_PK = sml.ID_PK
                        WHERE sml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND sml.RECEIVED BETWEEN migration.startDate AND migration.endDate)));

        TYPE T_D_PARTY IS TABLE OF c_d_party%ROWTYPE;
        party T_D_PARTY;

        remote_id TB_D_PARTY.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_D_PARTY entries...');
        OPEN c_d_party;
        LOOP
            FETCH c_d_party BULK COLLECT INTO party LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN party.COUNT = 0;

            FOR i IN party.FIRST .. party.LAST LOOP
                    BEGIN
                        IF party(i).TYPE IS NULL THEN
                            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_PARTY@' || db_link || ' WHERE VALUE = :p_1 AND TYPE IS NULL'
                                INTO remote_id
                                USING party(i).VALUE;
                        ELSE
                            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_PARTY@' || db_link || ' WHERE VALUE = :p_1 AND TYPE = :p_2'
                                INTO remote_id
                                USING party(i).VALUE,
                                party(i).TYPE;
                        END IF;
                    EXCEPTION
                        WHEN NO_DATA_FOUND THEN
                            remote_id := new_remote_pk(db_link);
                            EXECUTE IMMEDIATE 'INSERT INTO TB_D_PARTY@' || db_link || ' (ID_PK, VALUE, TYPE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5)'
                                USING remote_id,
                                party(i).VALUE,
                                party(i).TYPE,
                                party(i).CREATED_BY,
                                party(i).CREATION_TIME;
                    END;

                    localToRemotePks(party(i).ID_PK) := remote_id;
                    dbms_output.put_line('Local to remote mapping: TB_D_PARTY[' || party(i).ID_PK || '] = ' || remote_id);
                END LOOP;

            dbms_output.put_line('Processed ' || party.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_party;

        RETURN localToRemotePks;
    END migrate_tb_d_party;

    FUNCTION migrate_tb_d_mpc(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_d_mpc IS
            SELECT ID_PK, "VALUE", CREATED_BY, CREATION_TIME
            FROM TB_D_MPC
            WHERE ID_PK IN
                  (SELECT ID_PK FROM
                      ((SELECT DISTINCT um.MPC_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_USER_MESSAGE_LOG uml
                                            ON um.ID_PK = uml.ID_PK
                        WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                       UNION

                       (SELECT DISTINCT um.MPC_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_SIGNAL_MESSAGE_LOG sml
                                            ON um.ID_PK = sml.ID_PK
                        WHERE sml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND sml.RECEIVED BETWEEN migration.startDate AND migration.endDate)));

        TYPE T_D_PARTY IS TABLE OF c_d_mpc%ROWTYPE;
        mpc T_D_PARTY;

        remote_id TB_D_MPC.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_D_MPC entries...');
        OPEN c_d_mpc;
        LOOP
            FETCH c_d_mpc BULK COLLECT INTO mpc LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN mpc.COUNT = 0;

            FOR i IN mpc.FIRST .. mpc.LAST LOOP
                    BEGIN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MPC@' || db_link || ' WHERE VALUE = :p_1'
                            INTO remote_id
                            USING mpc(i).VALUE;
                    EXCEPTION
                        WHEN NO_DATA_FOUND THEN
                            remote_id := new_remote_pk(db_link);
                            EXECUTE IMMEDIATE 'INSERT INTO TB_D_MPC@' || db_link || ' (ID_PK, VALUE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4)'
                                USING remote_id,
                                mpc(i).VALUE,
                                mpc(i).CREATED_BY,
                                mpc(i).CREATION_TIME;
                    END;

                    localToRemotePks(mpc(i).ID_PK) := remote_id;
                    dbms_output.put_line('Local to remote mapping: TB_D_MPC[' || mpc(i).ID_PK || '] = ' || remote_id);
                END LOOP;

            dbms_output.put_line('Processed ' || mpc.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_mpc;

        RETURN localToRemotePks;
    END migrate_tb_d_mpc;

    FUNCTION migrate_tb_d_role(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_d_role IS
            SELECT ID_PK, ROLE, CREATED_BY, CREATION_TIME
            FROM TB_D_ROLE
            WHERE ID_PK IN
                  (SELECT ID_PK FROM
                      ((SELECT DISTINCT um.FROM_ROLE_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_USER_MESSAGE_LOG uml
                                            ON um.ID_PK = uml.ID_PK
                        WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                       UNION

                       (SELECT DISTINCT um.TO_ROLE_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_USER_MESSAGE_LOG uml
                                            ON um.ID_PK = uml.ID_PK
                        WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                       UNION

                       (SELECT DISTINCT um.FROM_ROLE_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_SIGNAL_MESSAGE_LOG sml
                                            ON um.ID_PK = sml.ID_PK
                        WHERE sml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND sml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                       UNION

                       (SELECT DISTINCT um.TO_ROLE_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_SIGNAL_MESSAGE_LOG sml
                                            ON um.ID_PK = sml.ID_PK
                        WHERE sml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND sml.RECEIVED BETWEEN migration.startDate AND migration.endDate)));

        TYPE T_D_ROLE IS TABLE OF c_d_role%ROWTYPE;
        role T_D_ROLE;

        remote_id TB_D_ROLE.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_D_ROLE entries...');
        OPEN c_d_role;
        LOOP
            FETCH c_d_role BULK COLLECT INTO role LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN role.COUNT = 0;

            FOR i IN role.FIRST .. role.LAST LOOP
                    BEGIN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_ROLE@' || db_link || ' WHERE ROLE = :p_1'
                            INTO remote_id
                            USING role(i).ROLE;
                    EXCEPTION
                        WHEN NO_DATA_FOUND THEN
                            remote_id := new_remote_pk(db_link);
                            EXECUTE IMMEDIATE 'INSERT INTO TB_D_ROLE@' || db_link || ' (ID_PK, ROLE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4)'
                                USING remote_id,
                                role(i).ROLE,
                                role(i).CREATED_BY,
                                role(i).CREATION_TIME;
                    END;

                    localToRemotePks(role(i).ID_PK) := remote_id;
                    dbms_output.put_line('Local to remote mapping: TB_D_ROLE[' || role(i).ID_PK || '] = ' || remote_id);
                END LOOP;

            dbms_output.put_line('Processed ' || role.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_role;

        RETURN localToRemotePks;
    END migrate_tb_d_role;

    FUNCTION migrate_tb_d_service(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_d_service IS
            SELECT ID_PK, "VALUE", "TYPE", CREATED_BY, CREATION_TIME
            FROM TB_D_SERVICE
            WHERE ID_PK IN
                  (SELECT ID_PK FROM
                      ((SELECT DISTINCT um.SERVICE_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_USER_MESSAGE_LOG uml
                                            ON um.ID_PK = uml.ID_PK
                        WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                       UNION

                       (SELECT DISTINCT um.SERVICE_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_SIGNAL_MESSAGE_LOG sml
                                            ON um.ID_PK = sml.ID_PK
                        WHERE sml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND sml.RECEIVED BETWEEN migration.startDate AND migration.endDate)));

        TYPE T_D_SERVICE IS TABLE OF c_d_service%ROWTYPE;
        service T_D_SERVICE;

        remote_id TB_D_SERVICE.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_D_SERVICE entries...');
        OPEN c_d_service;
        LOOP
            FETCH c_d_service BULK COLLECT INTO service LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN service.COUNT = 0;

            FOR i IN service.FIRST .. service.LAST LOOP
                    BEGIN
                        IF service(i).TYPE IS NULL THEN
                            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_SERVICE@' || db_link || ' WHERE VALUE = :p_1 AND TYPE IS NULL'
                                INTO remote_id
                                USING service(i).VALUE;
                        ELSE
                            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_SERVICE@' || db_link || ' WHERE VALUE = :p_1 AND TYPE = :p_2'
                                INTO remote_id
                                USING service(i).VALUE,
                                service(i).TYPE;
                        END IF;
                    EXCEPTION
                        WHEN NO_DATA_FOUND THEN
                            remote_id := new_remote_pk(db_link);
                            EXECUTE IMMEDIATE 'INSERT INTO TB_D_SERVICE@' || db_link || ' (ID_PK, VALUE, TYPE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5)'
                                USING remote_id,
                                service(i).VALUE,
                                service(i).TYPE,
                                service(i).CREATED_BY,
                                service(i).CREATION_TIME;
                    END;

                    localToRemotePks(service(i).ID_PK) := remote_id;
                    dbms_output.put_line('Local to remote mapping: TB_D_SERVICE[' || service(i).ID_PK || '] = ' || remote_id);
                END LOOP;

            dbms_output.put_line('Processed ' || service.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_service;

        RETURN localToRemotePks;
    END migrate_tb_d_service;

    FUNCTION migrate_tb_d_agreement(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_d_agreement IS
            SELECT ID_PK, "VALUE", "TYPE", CREATED_BY, CREATION_TIME
            FROM TB_D_AGREEMENT
            WHERE ID_PK IN
                  (SELECT ID_PK FROM
                      ((SELECT DISTINCT um.AGREEMENT_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_USER_MESSAGE_LOG uml
                                            ON um.ID_PK = uml.ID_PK
                        WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                       UNION

                       (SELECT DISTINCT um.AGREEMENT_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_SIGNAL_MESSAGE_LOG sml
                                            ON um.ID_PK = sml.ID_PK
                        WHERE sml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND sml.RECEIVED BETWEEN migration.startDate AND migration.endDate)));

        TYPE T_D_AGREEMENT IS TABLE OF c_d_agreement%ROWTYPE;
        agreement T_D_AGREEMENT;

        remote_id TB_D_AGREEMENT.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_D_AGREEMENT entries...');
        OPEN c_d_agreement;
        LOOP
            FETCH c_d_agreement BULK COLLECT INTO agreement LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN agreement.COUNT = 0;

            FOR i IN agreement.FIRST .. agreement.LAST LOOP
                    BEGIN
                        IF agreement(i).TYPE IS NULL THEN
                            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_AGREEMENT@' || db_link || ' WHERE VALUE = :p_1 AND TYPE IS NULL'
                                INTO remote_id
                                USING agreement(i).VALUE;
                        ELSE
                            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_AGREEMENT@' || db_link || ' WHERE VALUE = :p_1 AND TYPE = :p_2'
                                INTO remote_id
                                USING agreement(i).VALUE,
                                agreement(i).TYPE;
                        END IF;
                    EXCEPTION
                        WHEN NO_DATA_FOUND THEN
                            remote_id := new_remote_pk(db_link);
                            EXECUTE IMMEDIATE 'INSERT INTO TB_D_AGREEMENT@' || db_link || ' (ID_PK, VALUE, TYPE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5)'
                                USING remote_id,
                                agreement(i).VALUE,
                                agreement(i).TYPE,
                                agreement(i).CREATED_BY,
                                agreement(i).CREATION_TIME;
                    END;

                    localToRemotePks(agreement(i).ID_PK) := remote_id;
                    dbms_output.put_line('Local to remote mapping: TB_D_AGREEMENT[' || agreement(i).ID_PK || '] = ' || remote_id);
                END LOOP;

            dbms_output.put_line('Processed ' || agreement.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_agreement;

        RETURN localToRemotePks;
    END migrate_tb_d_agreement;

    FUNCTION migrate_tb_d_action(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_d_action IS
            SELECT ID_PK, ACTION, CREATED_BY, CREATION_TIME
            FROM TB_D_ACTION
            WHERE ID_PK IN
                  (SELECT ID_PK FROM
                      ((SELECT DISTINCT um.ACTION_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_USER_MESSAGE_LOG uml
                                            ON um.ID_PK = uml.ID_PK
                        WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                       UNION

                       (SELECT DISTINCT um.ACTION_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE um
                                 INNER JOIN TB_SIGNAL_MESSAGE_LOG sml
                                            ON um.ID_PK = sml.ID_PK
                        WHERE sml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND sml.RECEIVED BETWEEN migration.startDate AND migration.endDate)));

        TYPE T_D_ACTION IS TABLE OF c_d_action%ROWTYPE;
        action T_D_ACTION;

        remote_id TB_D_ROLE.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_D_ACTION entries...');
        OPEN c_d_action;
        LOOP
            FETCH c_d_action BULK COLLECT INTO action LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN action.COUNT = 0;

            FOR i IN action.FIRST .. action.LAST LOOP
                    BEGIN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_ACTION@' || db_link || ' WHERE ROLE = :p_1'
                            INTO remote_id
                            USING action(i).ACTION;
                    EXCEPTION
                        WHEN NO_DATA_FOUND THEN
                            remote_id := new_remote_pk(db_link);
                            EXECUTE IMMEDIATE 'INSERT INTO TB_D_ACTION@' || db_link || ' (ID_PK, ROLE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4)'
                                USING remote_id,
                                action(i).ACTION,
                                action(i).CREATED_BY,
                                action(i).CREATION_TIME;
                    END;

                    localToRemotePks(action(i).ID_PK) := remote_id;
                    dbms_output.put_line('Local to remote mapping: TB_D_ACTION[' || action(i).ID_PK || '] = ' || remote_id);
                END LOOP;

            dbms_output.put_line('Processed ' || action.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_action;

        RETURN localToRemotePks;
    END migrate_tb_d_action;

    FUNCTION migrate_tb_d_msh_role(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_d_msh_role IS
            SELECT ID_PK, ROLE, CREATED_BY, CREATION_TIME
            FROM TB_D_MSH_ROLE
            WHERE ID_PK IN
                  (SELECT ID_PK FROM
                      ((SELECT DISTINCT uml.MSH_ROLE_ID_FK AS ID_PK
                        FROM TB_USER_MESSAGE_LOG uml
                        WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                       UNION

                       (SELECT DISTINCT sml.MSH_ROLE_ID_FK AS ID_PK
                        FROM TB_SIGNAL_MESSAGE_LOG sml
                        WHERE sml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND sml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                       UNION

                       (SELECT DISTINCT el.MSH_ROLE_ID_FK AS ID_PK
                        FROM TB_ERROR_LOG el
                        WHERE el.USER_MESSAGE_ID_FK IN
                              (SELECT ID_PK FROM
                                  ((SELECT um.ID_PK AS ID_PK
                                    FROM TB_USER_MESSAGE um
                                             INNER JOIN TB_USER_MESSAGE_LOG uml
                                                        ON um.ID_PK = uml.ID_PK
                                    WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                                      AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                                   UNION

                                   (SELECT um.ID_PK AS ID_PK
                                    FROM TB_USER_MESSAGE um
                                             INNER JOIN TB_SIGNAL_MESSAGE_LOG sml
                                                        ON um.ID_PK = sml.ID_PK
                                    WHERE sml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                                      AND sml.RECEIVED BETWEEN migration.startDate AND migration.endDate))))

                       UNION

                       (SELECT DISTINCT sjmg.MSH_ROLE_ID_FK AS ID_PK
                        FROM TB_SJ_MESSAGE_GROUP sjmg
                        WHERE sjmg.SOURCE_MESSAGE_ID_FK IN
                              (SELECT ID_PK FROM
                                  ((SELECT um.ID_PK AS ID_PK
                                    FROM TB_USER_MESSAGE um
                                             INNER JOIN TB_USER_MESSAGE_LOG uml
                                                        ON um.ID_PK = uml.ID_PK
                                    WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                                      AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                                   UNION

                                   (SELECT um.ID_PK AS ID_PK
                                    FROM TB_USER_MESSAGE um
                                             INNER JOIN TB_SIGNAL_MESSAGE_LOG sml
                                                        ON um.ID_PK = sml.ID_PK
                                    WHERE sml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                                      AND sml.RECEIVED BETWEEN migration.startDate AND migration.endDate))))));

        TYPE T_D_MSH_ROLE IS TABLE OF c_d_msh_role%ROWTYPE;
        msh_role T_D_MSH_ROLE;

        remote_id TB_D_MSH_ROLE.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_D_MSH_ROLE entries...');
        OPEN c_d_msh_role;
        LOOP
            FETCH c_d_msh_role BULK COLLECT INTO msh_role LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN msh_role.COUNT = 0;

            FOR i IN msh_role.FIRST .. msh_role.LAST LOOP
                    BEGIN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MSH_ROLE@' || db_link || ' WHERE ROLE = :p_1'
                            INTO remote_id
                            USING msh_role(i).ROLE;
                    EXCEPTION
                        WHEN NO_DATA_FOUND THEN
                            remote_id := new_remote_pk(db_link);
                            EXECUTE IMMEDIATE 'INSERT INTO TB_D_MSH_ROLE@' || db_link || ' (ID_PK, ROLE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4)'
                                USING remote_id,
                                msh_role(i).ROLE,
                                msh_role(i).CREATED_BY,
                                msh_role(i).CREATION_TIME;
                    END;

                    localToRemotePks(msh_role(i).ID_PK) := remote_id;
                    dbms_output.put_line('Local to remote mapping: TB_D_MSH_ROLE[' || msh_role(i).ID_PK || '] = ' || remote_id);
                END LOOP;

            dbms_output.put_line('Processed ' || msh_role.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_msh_role;

        RETURN localToRemotePks;
    END migrate_tb_d_msh_role;

    FUNCTION migrate_tb_d_timezone_offset(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_d_timezone_offset IS
            SELECT ID_PK, NEXT_ATTEMPT_TIMEZONE_ID, NEXT_ATTEMPT_OFFSET_SECONDS, CREATED_BY, CREATION_TIME
            FROM TB_D_TIMEZONE_OFFSET
            WHERE ID_PK IN
                  (SELECT ID_PK FROM
                      ((SELECT DISTINCT uml.FK_TIMEZONE_OFFSET AS ID_PK
                        FROM TB_USER_MESSAGE_LOG uml
                        WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                          AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                       UNION

                       (SELECT DISTINCT ml.FK_TIMEZONE_OFFSET AS ID_PK
                        FROM TB_MESSAGING_LOCK ml
                        WHERE ml.MESSAGE_ID IN
                              (SELECT MESSAGE_ID FROM (
                                                          (SELECT um.MESSAGE_ID AS MESSAGE_ID
                                                           FROM TB_USER_MESSAGE um
                                                                    INNER JOIN TB_USER_MESSAGE_LOG uml
                                                                               ON um.ID_PK = uml.ID_PK
                                                           WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                                                             AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate)

                                                          UNION

                                                          (SELECT um.MESSAGE_ID AS MESSAGE_ID
                                                           FROM TB_USER_MESSAGE um
                                                                    INNER JOIN TB_SIGNAL_MESSAGE_LOG sml
                                                                               ON um.ID_PK = sml.ID_PK
                                                           WHERE sml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                                                             AND sml.RECEIVED BETWEEN migration.startDate AND migration.endDate))))));

        TYPE T_D_TIMEZONE_OFFSET IS TABLE OF c_d_timezone_offset%ROWTYPE;
        timezone_offset T_D_TIMEZONE_OFFSET;

        remote_id TB_D_TIMEZONE_OFFSET.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_D_TIMEZONE_OFFSET entries...');
        OPEN c_d_timezone_offset;
        LOOP
            FETCH c_d_timezone_offset BULK COLLECT INTO timezone_offset LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN timezone_offset.COUNT = 0;

            FOR i IN timezone_offset.FIRST .. timezone_offset.LAST LOOP
                    BEGIN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_TIMEZONE_OFFSET@' || db_link || ' WHERE NEXT_ATTEMPT_TIMEZONE_ID = :p_1 AND NEXT_ATTEMPT_OFFSET_SECONDS = :p_2'
                            INTO remote_id
                            USING timezone_offset(i).NEXT_ATTEMPT_TIMEZONE_ID,
                            timezone_offset(i).NEXT_ATTEMPT_OFFSET_SECONDS;
                    EXCEPTION
                        WHEN NO_DATA_FOUND THEN
                            remote_id := new_remote_pk(db_link);
                            EXECUTE IMMEDIATE 'INSERT INTO TB_D_TIMEZONE_OFFSET@' || db_link || ' (ID_PK, NEXT_ATTEMPT_TIMEZONE_ID, NEXT_ATTEMPT_OFFSET_SECONDS, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5)'
                                USING remote_id,
                                timezone_offset(i).NEXT_ATTEMPT_TIMEZONE_ID,
                                timezone_offset(i).NEXT_ATTEMPT_OFFSET_SECONDS,
                                timezone_offset(i).CREATED_BY,
                                timezone_offset(i).CREATION_TIME;
                    END;

                    localToRemotePks(timezone_offset(i).ID_PK) := remote_id;
                    dbms_output.put_line('Local to remote mapping: TB_D_TIMEZONE_OFFSET[' || timezone_offset(i).ID_PK || '] = ' || remote_id);
                END LOOP;

            dbms_output.put_line('Processed ' || timezone_offset.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_timezone_offset;

        RETURN localToRemotePks;
    END migrate_tb_d_timezone_offset;

    FUNCTION migrate_tb_d_notification_status(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_d_notification_status IS
            SELECT ID_PK, STATUS, CREATED_BY, CREATION_TIME
            FROM TB_D_NOTIFICATION_STATUS
            WHERE ID_PK IN (SELECT DISTINCT uml.NOTIFICATION_STATUS_ID_FK
                            FROM TB_USER_MESSAGE_LOG uml
                            WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                              AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate);

        TYPE T_D_NOTIFICATION_STATUS IS TABLE OF c_d_notification_status%ROWTYPE;
        notification_status T_D_NOTIFICATION_STATUS;

        remote_id TB_D_NOTIFICATION_STATUS.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_D_NOTIFICATION_STATUS entries...');
        OPEN c_d_notification_status;
        LOOP
            FETCH c_d_notification_status BULK COLLECT INTO notification_status LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN notification_status.COUNT = 0;

            FOR i IN notification_status.FIRST .. notification_status.LAST LOOP
                    BEGIN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_NOTIFICATION_STATUS@' || db_link || ' WHERE STATUS = :p_1'
                            INTO remote_id
                            USING notification_status(i).STATUS;
                    EXCEPTION
                        WHEN NO_DATA_FOUND THEN
                            remote_id := new_remote_pk(db_link);
                            EXECUTE IMMEDIATE 'INSERT INTO TB_D_NOTIFICATION_STATUS@' || db_link || ' (ID_PK, STATUS, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4)'
                                USING remote_id,
                                notification_status(i).STATUS,
                                notification_status(i).CREATED_BY,
                                notification_status(i).CREATION_TIME;
                    END;

                    localToRemotePks(notification_status(i).ID_PK) := remote_id;
                    dbms_output.put_line('Local to remote mapping: TB_D_NOTIFICATION_STATUS[' || notification_status(i).ID_PK || '] = ' || remote_id);
                END LOOP;

            dbms_output.put_line('Processed ' || notification_status.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_notification_status;

        RETURN localToRemotePks;
    END migrate_tb_d_notification_status;

    FUNCTION migrate_tb_d_message_property(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_d_message_property IS
            SELECT ID_PK, NAME, "VALUE", "TYPE", CREATED_BY, CREATION_TIME
            FROM TB_D_MESSAGE_PROPERTY
            WHERE ID_PK IN (SELECT DISTINCT uml.NOTIFICATION_STATUS_ID_FK
                            FROM TB_USER_MESSAGE_LOG uml
                            WHERE uml.MESSAGE_STATUS_ID_FK in ongoing_message_status_pks
                              AND uml.RECEIVED BETWEEN migration.startDate AND migration.endDate);

        TYPE T_D_MESSAGE_PROPERTY IS TABLE OF c_d_message_property%ROWTYPE;
        message_property T_D_MESSAGE_PROPERTY;

        remote_id TB_D_MESSAGE_PROPERTY.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_D_MESSAGE_PROPERTY entries...');
        OPEN c_d_message_property;
        LOOP
            FETCH c_d_message_property BULK COLLECT INTO message_property LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_property.COUNT = 0;

            FOR i IN message_property.FIRST .. message_property.LAST LOOP
                    BEGIN
                        IF message_property(i).VALUE IS NULL AND message_property(i).TYPE IS NULL THEN
                            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MESSAGE_PROPERTY@' || db_link || ' WHERE NAME = :p_1 AND VALUE IS NULL AND TYPE IS NULL'
                                INTO remote_id
                                USING message_property(i).NAME;
                        ELSIF message_property(i).VALUE IS NULL THEN
                            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MESSAGE_PROPERTY@' || db_link || ' WHERE NAME = :p_1 AND VALUE IS NULL AND TYPE = :p_2'
                                INTO remote_id
                                USING message_property(i).NAME,
                                message_property(i).TYPE;
                        ELSIF message_property(i).TYPE IS NULL THEN
                            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MESSAGE_PROPERTY@' || db_link || ' WHERE NAME = :p_1 AND VALUE = :p_2 AND TYPE IS NULL'
                                INTO remote_id
                                USING message_property(i).NAME,
                                message_property(i).VALUE;
                        ELSE
                            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MESSAGE_PROPERTY@' || db_link || ' WHERE NAME = :p_1 AND VALUE = :p_2 AND TYPE = :p_3'
                                INTO remote_id
                                USING message_property(i).NAME,
                                message_property(i).VALUE,
                                message_property(i).TYPE;
                        END IF;
                    EXCEPTION
                        WHEN NO_DATA_FOUND THEN
                            remote_id := new_remote_pk(db_link);
                            EXECUTE IMMEDIATE 'INSERT INTO TB_D_MESSAGE_PROPERTY@' || db_link || ' (ID_PK, NAME, VALUE, TYPE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                                USING remote_id,
                                message_property(i).NAME,
                                message_property(i).VALUE,
                                message_property(i).TYPE,
                                message_property(i).CREATED_BY,
                                message_property(i).CREATION_TIME;
                    END;

                    localToRemotePks(message_property(i).ID_PK) := remote_id;
                    dbms_output.put_line('Local to remote mapping: TB_D_MESSAGE_PROPERTY[' || message_property(i).ID_PK || '] = ' || remote_id);
                END LOOP;

            dbms_output.put_line('Processed ' || message_property.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_message_property;

        RETURN localToRemotePks;
    END migrate_tb_d_message_property;

    FUNCTION migrate_user_message(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, action_lookup_table IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, agreement_lookup_table IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, service_lookup_table IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, mpc_lookup_table IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, party_lookup_table IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, role_lookup_table IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_user_message IS
            SELECT ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, CONVERSATION_ID, SOURCE_MESSAGE, MESSAGE_FRAGMENT, TEST_MESSAGE, EBMS3_TIMESTAMP, ACTION_ID_FK, AGREEMENT_ID_FK, SERVICE_ID_FK, MPC_ID_FK, FROM_PARTY_ID_FK, FROM_ROLE_ID_FK, TO_PARTY_ID_FK, TO_ROLE_ID_FK, CREATION_TIME, CREATED_BY
            FROM TB_USER_MESSAGE
            WHERE ID_PK IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE_LOG
                WHERE MESSAGE_STATUS_ID_FK IN ongoing_message_status_pks
                  AND RECEIVED BETWEEN migration.startDate AND migration.endDate);

        TYPE T_USER_MESSAGE IS TABLE OF c_user_message%ROWTYPE;
        user_message T_USER_MESSAGE;

        remote_id TB_USER_MESSAGE.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_USER_MESSAGE entries...');

        OPEN c_user_message;
        LOOP
            FETCH c_user_message BULK COLLECT INTO user_message LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_message.COUNT = 0;

            FOR i IN user_message.FIRST .. user_message.LAST LOOP
                    remote_id := new_remote_pk(db_link);

                    EXECUTE IMMEDIATE 'INSERT INTO TB_USER_MESSAGE@' || db_link || ' (ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, CONVERSATION_ID, SOURCE_MESSAGE, MESSAGE_FRAGMENT, TEST_MESSAGE, EBMS3_TIMESTAMP, ACTION_ID_FK, AGREEMENT_ID_FK, SERVICE_ID_FK, MPC_ID_FK, FROM_PARTY_ID_FK, FROM_ROLE_ID_FK, TO_PARTY_ID_FK, TO_ROLE_ID_FK, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16, :p_17, :p_18)'
                        USING remote_id,
                        user_message(i).MESSAGE_ID,
                        user_message(i).REF_TO_MESSAGE_ID,
                        user_message(i).CONVERSATION_ID,
                        user_message(i).SOURCE_MESSAGE,
                        user_message(i).MESSAGE_FRAGMENT,
                        user_message(i).TEST_MESSAGE,
                        user_message(i).EBMS3_TIMESTAMP,
                        lookup_value_safely(action_lookup_table, user_message(i).ACTION_ID_FK),
                        lookup_value_safely(agreement_lookup_table, user_message(i).AGREEMENT_ID_FK),
                        lookup_value_safely(service_lookup_table, user_message(i).SERVICE_ID_FK),
                        lookup_value_safely(mpc_lookup_table, user_message(i).MPC_ID_FK),
                        lookup_value_safely(party_lookup_table, user_message(i).FROM_PARTY_ID_FK),
                        lookup_value_safely(role_lookup_table, user_message(i).FROM_ROLE_ID_FK),
                        lookup_value_safely(party_lookup_table, user_message(i).TO_PARTY_ID_FK),
                        lookup_value_safely(role_lookup_table, user_message(i).TO_ROLE_ID_FK),
                        user_message(i).CREATION_TIME,
                        user_message(i).CREATED_BY;

                    localToRemotePks(user_message(i).ID_PK) := remote_id;

                    dbms_output.put_line('Local to remote mapping: TB_USER_MESSAGE[' || user_message(i).ID_PK || '] = ' || remote_id);
                END LOOP;
            dbms_output.put_line('Wrote ' || user_message.COUNT || ' records');
        END LOOP;
        CLOSE c_user_message;

        RETURN localToRemotePks;
    END migrate_user_message;

    FUNCTION migrate_signal_message(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, user_message_lookup_table IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_signal_message IS
            SELECT ID_PK, SIGNAL_MESSAGE_ID, REF_TO_MESSAGE_ID, EBMS3_TIMESTAMP, CREATION_TIME, CREATED_BY
            FROM TB_SIGNAL_MESSAGE
            WHERE ID_PK IN (
                SELECT ID_PK
                FROM TB_SIGNAL_MESSAGE_LOG
                WHERE MESSAGE_STATUS_ID_FK IN ongoing_message_status_pks
                  AND RECEIVED BETWEEN migration.startDate AND migration.endDate);

        TYPE T_SIGNAL_MESSAGE IS TABLE OF c_signal_message%ROWTYPE;
        signal_message T_SIGNAL_MESSAGE;

        remote_id TB_SIGNAL_MESSAGE.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_SIGNAL_MESSAGE entries...');

        OPEN c_signal_message;
        LOOP
            FETCH c_signal_message BULK COLLECT INTO signal_message LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN signal_message.COUNT = 0;

            FOR i IN signal_message.FIRST .. signal_message.LAST LOOP
                    remote_id := user_message_lookup_table(signal_message(i).ID_PK);

                    EXECUTE IMMEDIATE 'INSERT INTO TB_SIGNAL_MESSAGE@' || db_link || ' (ID_PK, SIGNAL_MESSAGE_ID, REF_TO_MESSAGE_ID, EBMS3_TIMESTAMP, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16, :p_17, :p_18)'
                        USING remote_id,
                        signal_message(i).SIGNAL_MESSAGE_ID,
                        signal_message(i).REF_TO_MESSAGE_ID,
                        signal_message(i).EBMS3_TIMESTAMP,
                        signal_message(i).CREATION_TIME,
                        signal_message(i).CREATED_BY;

                    localToRemotePks(signal_message(i).ID_PK) := remote_id;

                    dbms_output.put_line('Local to remote mapping: TB_SIGNAL_MESSAGE[' || signal_message(i).ID_PK || '] = ' || remote_id);
                END LOOP;
            dbms_output.put_line('Wrote ' || signal_message.COUNT || ' records');
        END LOOP;
        CLOSE c_signal_message;

        RETURN localToRemotePks;
    END migrate_signal_message;

    PROCEDURE migrate_user_message_log(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, user_message_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS, timezone_offset_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS, message_status_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS, msh_role_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS, notification_status_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_user_message_log IS
            SELECT ID_PK, BACKEND, RECEIVED, ACKNOWLEDGED, DOWNLOADED, ARCHIVED, EXPORTED, FAILED, RESTORED, DELETED, NEXT_ATTEMPT, FK_TIMEZONE_OFFSET, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, SCHEDULED, VERSION, MESSAGE_STATUS_ID_FK, MSH_ROLE_ID_FK, NOTIFICATION_STATUS_ID_FK, CREATION_TIME, CREATED_BY, PROCESSING_TYPE
            FROM TB_USER_MESSAGE_LOG
            WHERE MESSAGE_STATUS_ID_FK IN ongoing_message_status_pks
              AND RECEIVED BETWEEN migration.startDate AND migration.endDate;

        TYPE T_USER_MESSAGE_LOG IS TABLE OF c_user_message_log%ROWTYPE;
        user_message_log T_USER_MESSAGE_LOG;
    BEGIN
        dbms_output.put_line('Migrating TB_MESSAGE_LOG entries...');
        OPEN c_user_message_log;
        LOOP
            FETCH c_user_message_log BULK COLLECT INTO user_message_log LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_message_log.COUNT = 0;

            FOR i IN user_message_log.FIRST .. user_message_log.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_MESSAGE_LOG@' || db_link || ' (ID_PK, BACKEND, RECEIVED, ACKNOWLEDGED, DOWNLOADED, ARCHIVED, EXPORTED, FAILED, RESTORED, DELETED, NEXT_ATTEMPT, FK_TIMEZONE_OFFSET, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, SCHEDULED, VERSION, MESSAGE_STATUS_ID_FK, MSH_ROLE_ID_FK, NOTIFICATION_STATUS_ID_FK, CREATION_TIME, CREATED_BY, PROCESSING_TYPE) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16, :p_17, :p_18, :p_19, :p_20, :p_21, :p_22)'
                        USING user_message_lookup_table(user_message_log(i).ID_PK),
                        user_message_log(i).BACKEND,
                        user_message_log(i).RECEIVED,
                        user_message_log(i).ACKNOWLEDGED,
                        user_message_log(i).DOWNLOADED,
                        user_message_log(i).ARCHIVED,
                        user_message_log(i).EXPORTED,
                        user_message_log(i).FAILED,
                        user_message_log(i).RESTORED,
                        user_message_log(i).DELETED,
                        user_message_log(i).NEXT_ATTEMPT,
                        lookup_value_safely(timezone_offset_lookup_table, user_message_log(i).FK_TIMEZONE_OFFSET),
                        user_message_log(i).SEND_ATTEMPTS,
                        user_message_log(i).SEND_ATTEMPTS_MAX,
                        user_message_log(i).SCHEDULED,
                        user_message_log(i).VERSION,
                        lookup_value_safely(message_status_lookup_table, user_message_log(i).MESSAGE_STATUS_ID_FK),
                        msh_role_lookup_table(user_message_log(i).MSH_ROLE_ID_FK),
                        lookup_value_safely(notification_status_lookup_table, user_message_log(i).NOTIFICATION_STATUS_ID_FK),
                        user_message_log(i).CREATION_TIME,
                        user_message_log(i).CREATED_BY,
                        user_message_log(i).PROCESSING_TYPE;
                END LOOP;
            dbms_output.put_line('Wrote ' || user_message_log.COUNT || ' records');
        END LOOP;
        CLOSE c_user_message_log;
    END migrate_user_message_log;

    -- TODO continue from here
    PROCEDURE migrate_signal_message_log(ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS, user_message_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
    BEGIN
    END migrate_signal_message_log;

    FUNCTION migrate_message_info(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_message_info IS
            SELECT ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, TIME_STAMP, CREATED_BY, CREATION_TIME
            FROM TB_MESSAGE_INFO
            WHERE MESSAGE_ID IN (
                SELECT MESSAGE_ID
                FROM TB_MESSAGE_LOG
                WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                  AND RECEIVED BETWEEN migration.startDate AND migration.endDate
            );

        TYPE T_MESSAGE_INFO IS TABLE OF c_message_info%ROWTYPE;
        message_info T_MESSAGE_INFO;

        remote_id TB_MESSAGE_INFO.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_MESSAGE_INFO entries...');

        OPEN c_message_info;
        LOOP
            FETCH c_message_info BULK COLLECT INTO message_info LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_info.COUNT = 0;

            FOR i IN message_info.FIRST .. message_info.LAST LOOP
                    remote_id := new_remote_pk(db_link);

                    EXECUTE IMMEDIATE 'INSERT INTO TB_MESSAGE_INFO@' || db_link || ' (ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, TIME_STAMP, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                        USING remote_id,
                        message_info(i).MESSAGE_ID,
                        message_info(i).REF_TO_MESSAGE_ID,
                        message_info(i).TIME_STAMP,
                        message_info(i).CREATED_BY,
                        message_info(i).CREATION_TIME;

                    localToRemotePks(message_info(i).ID_PK) := remote_id;

                    dbms_output.put_line('Local to remote mapping: TB_MESSAGE_INFO[' || message_info(i).ID_PK || '] = ' || remote_id);
                END LOOP;
            dbms_output.put_line('Wrote ' || message_info.COUNT || ' records');
        END LOOP;
        CLOSE c_message_info;

        RETURN localToRemotePks;
    END migrate_message_info;

    PROCEDURE migrate_error(signalMessageLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_error IS
            SELECT ID_PK, CATEGORY, DESCRIPTION_LANG, DESCRIPTION_VALUE, ERROR_CODE, ERROR_DETAIL, ORIGIN, REF_TO_MESSAGE_ID, SEVERITY, SHORT_DESCRIPTION, SIGNALMESSAGE_ID, CREATED_BY, CREATION_TIME
            FROM TB_ERROR
            WHERE SIGNALMESSAGE_ID IN (
                SELECT ID_PK
                FROM TB_SIGNAL_MESSAGE
                WHERE MESSAGEINFO_ID_PK IN (
                    SELECT ID_PK
                    FROM TB_MESSAGE_INFO
                    WHERE MESSAGE_ID IN (
                        SELECT MESSAGE_ID
                        FROM TB_MESSAGE_LOG
                        WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                          AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                    )
                )
            );

        TYPE T_ERROR IS TABLE OF c_error%ROWTYPE;
        error T_ERROR;
    BEGIN
        dbms_output.put_line('Migrating TB_ERROR entries...');

        OPEN c_error;
        LOOP
            FETCH c_error BULK COLLECT INTO error LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN error.COUNT = 0;

            FOR i IN error.FIRST .. error.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_ERROR@' || db_link || ' (ID_PK, CATEGORY, DESCRIPTION_LANG, DESCRIPTION_VALUE, ERROR_CODE, ERROR_DETAIL, ORIGIN, REF_TO_MESSAGE_ID, SEVERITY, SHORT_DESCRIPTION, SIGNALMESSAGE_ID, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12)'
                        USING error(i).CATEGORY,
                        error(i).DESCRIPTION_LANG,
                        error(i).DESCRIPTION_VALUE,
                        error(i).ERROR_CODE,
                        error(i).ERROR_DETAIL,
                        error(i).ORIGIN,
                        error(i).REF_TO_MESSAGE_ID,
                        error(i).SEVERITY,
                        error(i).SHORT_DESCRIPTION,
                        signalMessageLookupTable(error(i).SIGNALMESSAGE_ID),
                        error(i).CREATED_BY,
                        error(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || error.COUNT || ' records');
        END LOOP;
        CLOSE c_error;
    END migrate_error;

    PROCEDURE migrate_rawenvelope_log(userMessageLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_raw_envelope_log IS
            SELECT ID_PK, USERMESSAGE_ID_FK, SIGNALMESSAGE_ID_FK, MESSAGE_ID, CREATED_BY, CREATION_TIME
            FROM TB_RAWENVELOPE_LOG
            WHERE USERMESSAGE_ID_FK IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE MESSAGEINFO_ID_PK IN (
                    SELECT ID_PK
                    FROM TB_MESSAGE_INFO
                    WHERE MESSAGE_ID IN (
                        SELECT MESSAGE_ID
                        FROM TB_MESSAGE_LOG
                        WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                          AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                    )
                )
            );

        TYPE T_RAW_ENVELOPE_LOG IS TABLE OF c_raw_envelope_log%ROWTYPE;
        raw_envelope_log T_RAW_ENVELOPE_LOG;

        remote_id TB_RAWENVELOPE_LOG.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        a_null CHAR(1);
    BEGIN
        dbms_output.put_line('Migrating TB_RAWENVELOPE_LOG entries...');

        OPEN c_raw_envelope_log;

        LOOP
            FETCH c_raw_envelope_log BULK COLLECT INTO raw_envelope_log LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN raw_envelope_log.COUNT = 0;

            FOR i IN raw_envelope_log.FIRST .. raw_envelope_log.LAST LOOP
                    remote_id := new_remote_pk(db_link);

                    EXECUTE IMMEDIATE 'INSERT INTO TB_RAWENVELOPE_LOG@' || db_link || ' (ID_PK, USERMESSAGE_ID_FK, SIGNALMESSAGE_ID_FK, MESSAGE_ID, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                        USING remote_id,
                        lookup_value_safely(userMessageLookupTable, raw_envelope_log(i).USERMESSAGE_ID_FK),
                        a_null,
                        raw_envelope_log(i).MESSAGE_ID,
                        raw_envelope_log(i).CREATED_BY,
                        raw_envelope_log(i).CREATION_TIME;

                    -- CLOBs are special when copying
                    EXECUTE IMMEDIATE 'UPDATE TB_RAWENVELOPE_LOG@' || db_link || ' SET RAW_XML = (SELECT RAW_XML FROM TB_RAWENVELOPE_LOG WHERE ID_PK = :p_1) WHERE ID_PK = :p_2'
                        USING raw_envelope_log(i).ID_PK,
                        remote_id;
                END LOOP;
            dbms_output.put_line('Wrote ' || raw_envelope_log.COUNT || ' records');
        END LOOP;
        CLOSE c_raw_envelope_log;
    END migrate_rawenvelope_log;

    FUNCTION migrate_part_info(userMessageLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_part_info IS
            SELECT ID_PK, DESCRIPTION_LANG, DESCRIPTION_VALUE, HREF, IN_BODY, SCHEMA_LOCATION, SCHEMA_NAMESPACE, SCHEMA_VERSION, PAYLOADINFO_ID, FILENAME, MIME, PART_ORDER, ENCRYPTED, CREATED_BY, CREATION_TIME
            FROM TB_PART_INFO
            WHERE PAYLOADINFO_ID IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE MESSAGEINFO_ID_PK IN (
                    SELECT ID_PK
                    FROM TB_MESSAGE_INFO
                    WHERE MESSAGE_ID IN (
                        SELECT MESSAGE_ID
                        FROM TB_MESSAGE_LOG
                        WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                          AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                    )
                )
            );

        TYPE T_PART_INFO IS TABLE OF c_part_info%ROWTYPE;
        part_info T_PART_INFO;

        remote_id TB_PART_INFO.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_PART_INFO entries...');

        OPEN c_part_info;
        LOOP
            FETCH c_part_info BULK COLLECT INTO part_info LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN part_info.COUNT = 0;

            FOR i IN part_info.FIRST .. part_info.LAST LOOP
                    remote_id := new_remote_pk(db_link);

                    EXECUTE IMMEDIATE 'INSERT INTO TB_PART_INFO@' || db_link || ' (ID_PK, DESCRIPTION_LANG, DESCRIPTION_VALUE, HREF, IN_BODY, SCHEMA_LOCATION, SCHEMA_NAMESPACE, SCHEMA_VERSION, PAYLOADINFO_ID, FILENAME, MIME, PART_ORDER, ENCRYPTED, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15)'
                        USING remote_id,
                        part_info(i).DESCRIPTION_LANG,
                        part_info(i).DESCRIPTION_VALUE,
                        part_info(i).HREF,
                        part_info(i).IN_BODY,
                        part_info(i).SCHEMA_LOCATION,
                        part_info(i).SCHEMA_NAMESPACE,
                        part_info(i).SCHEMA_VERSION,
                        userMessageLookupTable(part_info(i).PAYLOADINFO_ID),
                        part_info(i).FILENAME,
                        part_info(i).MIME,
                        part_info(i).PART_ORDER,
                        part_info(i).ENCRYPTED,
                        part_info(i).CREATED_BY,
                        part_info(i).CREATION_TIME;

                    -- BLOBs are special when copying
                    EXECUTE IMMEDIATE 'UPDATE TB_PART_INFO@' || db_link || ' SET BINARY_DATA = (SELECT BINARY_DATA FROM TB_PART_INFO WHERE ID_PK = :p_1) WHERE ID_PK = :p_2'
                        USING part_info(i).ID_PK,
                        remote_id;

                    localToRemotePks(part_info(i).ID_PK) := remote_id;

                    dbms_output.put_line('Local to remote mapping: TB_PART_INFO[' || part_info(i).ID_PK || '] = ' || remote_id);
                END LOOP;
            dbms_output.put_line('Wrote ' || part_info.COUNT || ' records');
        END LOOP;
        CLOSE c_part_info;

        RETURN localToRemotePks;
    END migrate_part_info;

    PROCEDURE migrate_property(userMessageLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, partInfoLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_property IS
            SELECT ID_PK, NAME, VALUE, MESSAGEPROPERTIES_ID, PARTPROPERTIES_ID, "TYPE", CREATED_BY, CREATION_TIME
            FROM TB_PROPERTY
            WHERE MESSAGEPROPERTIES_ID IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE MESSAGEINFO_ID_PK IN (
                    SELECT ID_PK
                    FROM TB_MESSAGE_INFO
                    WHERE MESSAGE_ID IN (
                        SELECT MESSAGE_ID
                        FROM TB_MESSAGE_LOG
                        WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                          AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                    )
                )
            )
            UNION ALL
            SELECT ID_PK, NAME, VALUE, MESSAGEPROPERTIES_ID, PARTPROPERTIES_ID, "TYPE", CREATED_BY, CREATION_TIME
            FROM TB_PROPERTY
            WHERE PARTPROPERTIES_ID IN (
                SELECT ID_PK
                FROM TB_PART_INFO
                WHERE PAYLOADINFO_ID IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE
                    WHERE MESSAGEINFO_ID_PK IN (
                        SELECT ID_PK
                        FROM TB_MESSAGE_INFO
                        WHERE MESSAGE_ID IN (
                            SELECT MESSAGE_ID
                            FROM TB_MESSAGE_LOG
                            WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                              AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                        )
                    )
                )
            );

        TYPE T_PROPERTY IS TABLE OF c_property%ROWTYPE;
        property T_PROPERTY;
    BEGIN
        dbms_output.put_line('Migrating TB_PROPERTY entries...');

        OPEN c_property;
        LOOP
            FETCH c_property BULK COLLECT INTO property LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN property.COUNT = 0;

            FOR i IN property.FIRST .. property.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_PROPERTY@' || db_link || ' (ID_PK, NAME, VALUE, MESSAGEPROPERTIES_ID, PARTPROPERTIES_ID, TYPE, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7)'
                        USING property(i).NAME,
                        property(i).VALUE,
                        lookup_value_safely(userMessageLookupTable, property(i).MESSAGEPROPERTIES_ID),
                        lookup_value_safely(partInfoLookupTable, property(i).PARTPROPERTIES_ID),
                        property(i).TYPE,
                        property(i).CREATED_BY,
                        property(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || property.COUNT || ' records');
        END LOOP;
        CLOSE c_property;
    END migrate_property;

    PROCEDURE migrate_party_id(userMessageLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_party_id IS
            SELECT ID_PK, "TYPE", VALUE, TO_ID, FROM_ID, CREATED_BY, CREATION_TIME
            FROM TB_PARTY_ID
            WHERE TO_ID IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE MESSAGEINFO_ID_PK IN (
                    SELECT ID_PK
                    FROM TB_MESSAGE_INFO
                    WHERE MESSAGE_ID IN (
                        SELECT MESSAGE_ID
                        FROM TB_MESSAGE_LOG
                        WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                          AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                    )
                )
            )
            UNION ALL
            SELECT ID_PK, "TYPE", VALUE, TO_ID, FROM_ID, CREATED_BY, CREATION_TIME
            FROM TB_PARTY_ID
            WHERE FROM_ID IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE MESSAGEINFO_ID_PK IN (
                    SELECT ID_PK
                    FROM TB_MESSAGE_INFO
                    WHERE MESSAGE_ID IN (
                        SELECT MESSAGE_ID
                        FROM TB_MESSAGE_LOG
                        WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                          AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                    )
                )
            );

        TYPE T_PARTY_ID IS TABLE OF c_party_id%ROWTYPE;
        party_id T_PARTY_ID;
    BEGIN
        dbms_output.put_line('Migrating TB_PARTY_ID entries...');

        OPEN c_party_id;
        LOOP
            FETCH c_party_id BULK COLLECT INTO party_id LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN party_id.COUNT = 0;

            FOR i IN party_id.FIRST .. party_id.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_PARTY_ID@' || db_link || ' (ID_PK, TYPE, VALUE, TO_ID, FROM_ID, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                        USING party_id(i).TYPE,
                        party_id(i).VALUE,
                        lookup_value_safely(userMessageLookupTable, party_id(i).TO_ID),
                        lookup_value_safely(userMessageLookupTable, party_id(i).FROM_ID),
                        party_id(i).CREATED_BY,
                        party_id(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || party_id.COUNT || ' records');
        END LOOP;
        CLOSE c_party_id;
    END migrate_party_id;

    PROCEDURE migrate_error_log(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_error_log IS
            SELECT ID_PK, ERROR_CODE, ERROR_DETAIL, ERROR_SIGNAL_MESSAGE_ID, MESSAGE_IN_ERROR_ID, MSH_ROLE, NOTIFIED, TIME_STAMP, CREATED_BY, CREATION_TIME
            FROM TB_ERROR_LOG
            WHERE MESSAGE_IN_ERROR_ID IN (
                SELECT MESSAGE_ID
                FROM TB_MESSAGE_LOG
                WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                  AND RECEIVED BETWEEN migration.startDate AND migration.endDate
            );

        TYPE T_ERROR_LOG IS TABLE OF c_error_log%ROWTYPE;
        error_log T_ERROR_LOG;
    BEGIN
        dbms_output.put_line('Migrating TB_ERROR_LOG entries...');

        OPEN c_error_log;
        LOOP
            FETCH c_error_log BULK COLLECT INTO error_log LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN error_log.COUNT = 0;

            FOR i IN error_log.FIRST .. error_log.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_ERROR_LOG@' || db_link || ' (ID_PK, ERROR_CODE, ERROR_DETAIL, ERROR_SIGNAL_MESSAGE_ID, MESSAGE_IN_ERROR_ID, MSH_ROLE, NOTIFIED, TIME_STAMP, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9)'
                        USING error_log(i).ERROR_CODE,
                        error_log(i).ERROR_DETAIL,
                        error_log(i).ERROR_SIGNAL_MESSAGE_ID,
                        error_log(i).MESSAGE_IN_ERROR_ID,
                        error_log(i).MSH_ROLE,
                        error_log(i).NOTIFIED,
                        error_log(i).TIME_STAMP,
                        error_log(i).CREATED_BY,
                        error_log(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || error_log.COUNT || ' records');
        END LOOP;
        CLOSE c_error_log;
    END migrate_error_log;

    PROCEDURE migrate_messaging(userMessageLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_messaging IS
            SELECT ID_PK, ID, SIGNAL_MESSAGE_ID, USER_MESSAGE_ID, CREATED_BY, CREATION_TIME
            FROM TB_MESSAGING
            WHERE USER_MESSAGE_ID IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE MESSAGEINFO_ID_PK IN (
                    SELECT ID_PK
                    FROM TB_MESSAGE_INFO
                    WHERE MESSAGE_ID IN (
                        SELECT MESSAGE_ID
                        FROM TB_MESSAGE_LOG
                        WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                          AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                    )
                )
            );

        TYPE T_MESSAGING IS TABLE OF c_messaging%ROWTYPE;
        messaging T_MESSAGING;
        a_null CHAR(1);
    BEGIN
        dbms_output.put_line('Migrating TB_MESSAGING entries...');

        OPEN c_messaging;
        LOOP
            FETCH c_messaging BULK COLLECT INTO messaging LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN messaging.COUNT = 0;

            FOR i IN messaging.FIRST .. messaging.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_MESSAGING@' || db_link || ' (ID_PK, ID, SIGNAL_MESSAGE_ID, USER_MESSAGE_ID, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5)'
                        USING messaging(i).ID,
                        a_null,
                        lookup_value_safely(userMessageLookupTable, messaging(i).USER_MESSAGE_ID),
                        messaging(i).CREATED_BY,
                        messaging(i).CREATION_TIME;

                END LOOP;
            dbms_output.put_line('Wrote ' || messaging.COUNT || ' records');
        END LOOP;
        CLOSE c_messaging;
    END migrate_messaging;

    PROCEDURE migrate_action_audit(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_action_audit IS
            SELECT ID_PK, AUDIT_TYPE, ENTITY_ID, MODIFICATION_TYPE, REVISION_DATE, USER_NAME, FROM_QUEUE, TO_QUEUE, CREATED_BY, CREATION_TIME
            FROM TB_ACTION_AUDIT
            WHERE ENTITY_ID IN (
                SELECT MESSAGE_ID
                FROM TB_MESSAGE_LOG
                WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                  AND RECEIVED BETWEEN migration.startDate AND migration.endDate
            );

        TYPE T_ACTION_AUDIT IS TABLE OF c_action_audit%ROWTYPE;
        action_audit T_ACTION_AUDIT;
    BEGIN
        dbms_output.put_line('Migrating TB_ACTION_AUDIT entries...');

        OPEN c_action_audit;
        LOOP
            FETCH c_action_audit BULK COLLECT INTO action_audit LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN action_audit.COUNT = 0;

            FOR i IN action_audit.FIRST .. action_audit.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_ACTION_AUDIT@' || db_link || ' (ID_PK, AUDIT_TYPE, ENTITY_ID, MODIFICATION_TYPE, REVISION_DATE, USER_NAME, FROM_QUEUE, TO_QUEUE, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9)'
                        USING action_audit(i).AUDIT_TYPE,
                        action_audit(i).ENTITY_ID,
                        action_audit(i).MODIFICATION_TYPE,
                        action_audit(i).REVISION_DATE,
                        action_audit(i).USER_NAME,
                        action_audit(i).FROM_QUEUE,
                        action_audit(i).TO_QUEUE,
                        action_audit(i).CREATED_BY,
                        action_audit(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || action_audit.COUNT || ' records');
        END LOOP;
        CLOSE c_action_audit;
    END migrate_action_audit;

    PROCEDURE migrate_send_attempt(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_send_attempt IS
            SELECT ID_PK, MESSAGE_ID, START_DATE, END_DATE, STATUS, ERROR, CREATED_BY, CREATION_TIME
            FROM TB_SEND_ATTEMPT
            WHERE MESSAGE_ID IN (
                SELECT MESSAGE_ID
                FROM TB_MESSAGE_LOG
                WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                  AND RECEIVED BETWEEN migration.startDate AND migration.endDate
            );

        TYPE T_SEND_ATTEMPT IS TABLE OF c_send_attempt%ROWTYPE;
        send_attempt T_SEND_ATTEMPT;
    BEGIN
        dbms_output.put_line('Migrating TB_SEND_ATTEMPT entries...');

        OPEN c_send_attempt;
        LOOP
            FETCH c_send_attempt BULK COLLECT INTO send_attempt LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN send_attempt.COUNT = 0;

            FOR i IN send_attempt.FIRST .. send_attempt.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_SEND_ATTEMPT@' || db_link || ' (ID_PK, MESSAGE_ID, START_DATE, END_DATE, STATUS, ERROR, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7)'
                        USING send_attempt(i).MESSAGE_ID,
                        send_attempt(i).START_DATE,
                        send_attempt(i).END_DATE,
                        send_attempt(i).STATUS,
                        send_attempt(i).ERROR,
                        send_attempt(i).CREATED_BY,
                        send_attempt(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || send_attempt.COUNT || ' records');
        END LOOP;
        CLOSE c_send_attempt;
    END migrate_send_attempt;

    PROCEDURE migrate_messaging_lock(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_messaging_lock IS
            SELECT ID_PK, MESSAGE_TYPE, MESSAGE_RECEIVED, MESSAGE_STATE, MESSAGE_ID, INITIATOR, MPC, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, NEXT_ATTEMPT, MESSAGE_STALED, CREATED_BY, CREATION_TIME
            FROM TB_MESSAGING_LOCK
            WHERE MESSAGE_ID IN (
                SELECT MESSAGE_ID
                FROM TB_MESSAGE_LOG
                WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                  AND RECEIVED BETWEEN migration.startDate AND migration.endDate
            );

        TYPE T_SEND_ATTEMPT IS TABLE OF c_messaging_lock%ROWTYPE;
        messaging_lock T_SEND_ATTEMPT;
    BEGIN
        dbms_output.put_line('Migrating TB_MESSAGING_LOCK entries...');

        OPEN c_messaging_lock;
        LOOP
            FETCH c_messaging_lock BULK COLLECT INTO messaging_lock LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN messaging_lock.COUNT = 0;

            FOR i IN messaging_lock.FIRST .. messaging_lock.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_MESSAGING_LOCK@' || db_link || ' (ID_PK, MESSAGE_TYPE, MESSAGE_RECEIVED, MESSAGE_STATE, MESSAGE_ID, INITIATOR, MPC, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, NEXT_ATTEMPT, MESSAGE_STALED, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12)'
                        USING messaging_lock(i).MESSAGE_TYPE,
                        messaging_lock(i).MESSAGE_RECEIVED,
                        messaging_lock(i).MESSAGE_STATE,
                        messaging_lock(i).MESSAGE_ID,
                        messaging_lock(i).INITIATOR,
                        messaging_lock(i).MPC,
                        messaging_lock(i).SEND_ATTEMPTS,
                        messaging_lock(i).SEND_ATTEMPTS_MAX,
                        messaging_lock(i).NEXT_ATTEMPT,
                        messaging_lock(i).MESSAGE_STALED,
                        messaging_lock(i).CREATED_BY,
                        messaging_lock(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || messaging_lock.COUNT || ' records');
        END LOOP;
        CLOSE c_messaging_lock;
    END migrate_messaging_lock;

    PROCEDURE migrate(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        ongoing_message_status_pks T_MESSAGE_STATUS_PRIMARY_KEYS;

        message_status_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
        party_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
        mpc_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
        role_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
        service_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
        agreement_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
        action_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
        msh_role_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
        timezone_offset_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
        notification_status_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
        message_property_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS;

        user_message_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
        signal_message_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
        part_info_lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        SELECT ID_PK
        INTO ongoing_message_status_pks
        FROM TB_D_MESSAGE_STATUS
        WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT');

        message_status_lookup_table := migrate_tb_d_message_status(ongoing_message_status_pks, db_link);
        party_lookup_table := migrate_tb_d_party(ongoing_message_status_pks, db_link, migration);
        mpc_lookup_table := migrate_tb_d_mpc(ongoing_message_status_pks, db_link, migration);
        role_lookup_table := migrate_tb_d_role(ongoing_message_status_pks, db_link, migration);
        service_lookup_table := migrate_tb_d_service(ongoing_message_status_pks, db_link, migration);
        agreement_lookup_table := migrate_tb_d_agreement(ongoing_message_status_pks, db_link, migration);
        action_lookup_table := migrate_tb_d_action(ongoing_message_status_pks, db_link, migration);
        msh_role_lookup_table := migrate_tb_d_msh_role(ongoing_message_status_pks, db_link, migration);
        timezone_offset_lookup_table := migrate_tb_d_timezone_offset(ongoing_message_status_pks, db_link, migration);
        notification_status_lookup_table := migrate_tb_d_notification_status(ongoing_message_status_pks, db_link, migration);
        message_property_lookup_table := migrate_tb_d_message_property(ongoing_message_status_pks, db_link, migration);

        user_message_lookup_table := migrate_user_message(ongoing_message_status_pks, action_lookup_table, agreement_lookup_table, service_lookup_table, mpc_lookup_table, party_lookup_table, role_lookup_table, db_link, migration);
        signal_message_lookup_table := migrate_signal_message(ongoing_message_status_pks, user_message_lookup_table, db_link, migration);
        migrate_user_message_log(ongoing_message_status_pks, user_message_lookup_table, timezone_offset_lookup_table, message_status_lookup_table, msh_role_lookup_table, notification_status_lookup_table, db_link, migration);
        migrate_signal_message_log(ongoing_message_status_pks, user_message_lookup_table, db_link, migration);

        --- OLD migration
        migrate_rawenvelope_log(user_message_lookup_table, db_link, migration);
        part_info_lookup_table := migrate_part_info(user_message_lookup_table, db_link, migration);
        migrate_property(user_message_lookup_table, part_info_lookup_table,db_link, migration);
        migrate_party_id(user_message_lookup_table, db_link, migration);
        migrate_error_log(db_link, migration);
        migrate_messaging(user_message_lookup_table, db_link, migration);
        migrate_action_audit(db_link, migration);
        migrate_send_attempt(db_link, migration);
        migrate_messaging_lock(db_link, migration);

        dbms_output.put_line('Done');
        dbms_output.put_line('Please review the changes and either COMMIT them or ROLLBACK!');
    END migrate;

END MIGRATE_ONGOING_MESSAGES;
/
