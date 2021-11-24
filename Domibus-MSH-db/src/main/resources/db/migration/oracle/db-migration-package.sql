-- ********************************************************************************************************
-- Domibus 4.2.3 to 5.0 data migration package
--
-- Main entry point is the procedure 'migrate'. To be executed into a begin/end; block
--
-- Parameters to be adjusted:
-- BATCH_SIZE - size of the batch for data migration on each migrated table after which there is a commit;
--              default value is 10000
-- VERBOSE_LOGS - more information into the logs; default to false
--
-- Tables which are migrated: TB_USER_MESSAGE, TB_MESSAGE_FRAGMENT, TB_MESSAGE_GROUP, TB_MESSAGE_HEADER,
-- TB_MESSAGE_LOG, TB_RECEIPT, TB_RECEIPT_DATA, TB_RAWENVELOPE_LOG, TB_PROPERTY, TB_PART_INFO,
-- TB_ERROR_LOG, TB_MESSAGE_ACKNW, TB_SEND_ATTEMPT
-- ********************************************************************************************************
CREATE OR REPLACE PACKAGE MIGRATE_42_TO_50 IS
    -- batch size for commit of the migrated records
    BATCH_SIZE CONSTANT NUMBER := 10000;

    -- enable more verbose logs
    VERBOSE_LOGS CONSTANT BOOLEAN := FALSE;

    -- entry point for running the migration - to be executed in a BEGIN/END; block
    PROCEDURE migrate;

END MIGRATE_42_TO_50;
/

CREATE OR REPLACE PACKAGE BODY MIGRATE_42_TO_50 IS

    /** -- Helper procedures and functions start -*/
    FUNCTION check_table_exists(tab_name VARCHAR2) RETURN BOOLEAN IS
        v_table_exists INT;
    BEGIN
        SELECT COUNT(*) INTO v_table_exists FROM USER_TABLES WHERE table_name = UPPER(tab_name);
        RETURN v_table_exists > 0;
    END check_table_exists;

    PROCEDURE drop_table_if_exists(tab_name IN VARCHAR2) IS
    BEGIN
        IF check_table_exists(tab_name) THEN
            BEGIN
                EXECUTE IMMEDIATE 'DROP TABLE ' || tab_name;
                DBMS_OUTPUT.PUT_LINE('Table ' || tab_name || ' dropped');
            EXCEPTION
                WHEN OTHERS THEN
                    DBMS_OUTPUT.PUT_LINE('drop_table_if_exists for '||tab_name||' -> execute immediate error: ' || DBMS_UTILITY.FORMAT_ERROR_STACK);
            END;

        END IF;
    END drop_table_if_exists;

    PROCEDURE truncate_or_create_table(tab_name IN VARCHAR2, create_sql IN VARCHAR2) IS
    BEGIN
        IF check_table_exists(tab_name) THEN
            EXECUTE IMMEDIATE 'TRUNCATE TABLE ' || tab_name;
            DBMS_OUTPUT.PUT_LINE('Table ' || tab_name || ' truncated');
        ELSE
            EXECUTE IMMEDIATE create_sql;
            DBMS_OUTPUT.PUT_LINE('Table ' || tab_name || ' created');
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Execute immediate error: ' || DBMS_UTILITY.FORMAT_ERROR_STACK);
    END truncate_or_create_table;

    PROCEDURE create_table(tab_name IN VARCHAR2, create_sql IN VARCHAR2) IS
    BEGIN
        EXECUTE IMMEDIATE create_sql;
        DBMS_OUTPUT.PUT_LINE('Table ' || tab_name || ' created');
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('create_table for '||tab_name||' -> execute immediate error: ' || DBMS_UTILITY.FORMAT_ERROR_STACK);
    END create_table;

    FUNCTION check_counts(tab_name1 VARCHAR2, tab_name2 VARCHAR2) RETURN BOOLEAN IS
        v_count_match BOOLEAN;
        v_count_tab1  NUMBER;
        v_count_tab2  NUMBER;
    BEGIN
        BEGIN
            EXECUTE IMMEDIATE 'SELECT /*+ PARALLEL(4) */ COUNT(*) FROM ' || tab_name1 INTO v_count_tab1;
            EXECUTE IMMEDIATE 'SELECT /*+ PARALLEL(4) */ COUNT(*) FROM ' || tab_name2 INTO v_count_tab2;
        EXCEPTION
            WHEN OTHERS THEN
                DBMS_OUTPUT.PUT_LINE('check_counts -> execute immediate error: ' || DBMS_UTILITY.FORMAT_ERROR_STACK);
        END;
        IF v_count_tab1 = v_count_tab2 THEN
            v_count_match := TRUE;
            DBMS_OUTPUT.PUT_LINE('Table ' || tab_name1 || ' has same number of records as table ' || tab_name2 ||
                                 ' records=' || v_count_tab1);
        ELSE
            v_count_match := FALSE;
            DBMS_OUTPUT.PUT_LINE('Table ' || tab_name1 || ' has different number of records as table ' || tab_name2);
        END IF;
        RETURN v_count_match;
    END check_counts;

    PROCEDURE log_verbose(message VARCHAR2) IS
    BEGIN
        IF VERBOSE_LOGS THEN
            DBMS_OUTPUT.PUT_LINE(message);
        END IF;
    END log_verbose;

    FUNCTION generate_scalable_seq(incr IN NUMBER, creation_time IN DATE) RETURN NUMBER IS
        seq_id NUMBER;
        len CONSTANT VARCHAR2(255) := 'FM0000000000';
        date_format CONSTANT VARCHAR2(255) := 'YYMMDDHH';
    BEGIN
        SELECT to_number(to_char(creation_time, date_format) || to_char(incr, len))
        INTO seq_id
        FROM dual;
        RETURN seq_id;
    END generate_scalable_seq;

    -- This function generates a sequence id based on DOMIBUS_SCALABLE_SEQUENCE for a new entry
    FUNCTION generate_id RETURN NUMBER IS
    BEGIN
        RETURN generate_scalable_seq(DOMIBUS_SCALABLE_SEQUENCE.nextval, SYSDATE);
    END generate_id;

    -- This function generates a new sequence id based on DOMIBUS_SCALABLE_SEQUENCE for an old entry based on old id_pk and old creation_time
    FUNCTION generate_new_id(old_id IN NUMBER, creation_time IN DATE) RETURN NUMBER IS
    BEGIN
        RETURN generate_scalable_seq(old_id, creation_time);
    END generate_new_id;

    PROCEDURE update_migration_pks(migration_key VARCHAR2, old_id NUMBER, new_id NUMBER, migration_pks IN OUT JSON_OBJECT_T) IS
        migration_object JSON_OBJECT_T;
    BEGIN
        log_verbose('Update migration primary keys mappings for ' || migration_key ||
                    ' having old_id=' || old_id || ', new_id=' || new_id);

        migration_object := migration_pks.get_object(migration_key);
        IF migration_object IS NULL THEN
            log_verbose('Create missing key mapping');
            migration_object := new JSON_OBJECT_T();
            migration_pks.put(migration_key, migration_object);
            -- refresh the migration_pks reference
            migration_object := migration_pks.get_object(migration_key);
        END IF;

        -- updates the migration_pks through the reference
        migration_object.put(TO_CHAR(old_id), new_id);
    END update_migration_pks;

    PROCEDURE lookup_migration_pk(migration_key VARCHAR2, migration_pks JSON_OBJECT_T, old_id NUMBER, new_id OUT NUMBER) IS
    BEGIN
        log_verbose('Lookup migration primary key mapping for ' || migration_key ||
                     ' having old_id=' || old_id);


        new_id := migration_pks.get_object(migration_key).get_number(TO_CHAR(old_id));
    END lookup_migration_pk;


    PROCEDURE lookup_audit_migration_pk(migration_key VARCHAR2, migration_pks JSON_OBJECT_T, missing_entity_date_prefix DATE, old_id NUMBER, new_id OUT NUMBER) IS
    BEGIN
        log_verbose('Lookup audit migration primary key mapping for ' || migration_key ||
                     ' having old_id=' || old_id);

        lookup_migration_pk(migration_key, migration_pks, old_id, new_id);

        IF new_id IS NULL THEN
            log_verbose('Audit primary key not found (deleted entity?): prefixing with current date');
            new_id := generate_scalable_seq(old_id, missing_entity_date_prefix);
        END IF;
    END lookup_audit_migration_pk;

    FUNCTION get_tb_d_mpc_rec(mpc_value VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF mpc_value IS NULL THEN
            DBMS_OUTPUT.PUT_LINE('No record added into TB_D_MPC');
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MPC WHERE VALUE = :1' INTO v_id_pk USING mpc_value;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_MPC: ' || mpc_value);
                v_id_pk := generate_id();
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_MPC(ID_PK, VALUE) VALUES (' || v_id_pk || ', :1)' USING mpc_value;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_mpc_rec;

    FUNCTION get_tb_d_role_rec(role VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF role IS NULL THEN
            log_verbose('No record added into TB_D_ROLE');
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_ROLE WHERE ROLE = :1' INTO v_id_pk USING role;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_ROLE: ' || role);
                v_id_pk := generate_id();
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_ROLE(ID_PK, ROLE) VALUES (' || v_id_pk || ', :1)' USING role;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_role_rec;

    FUNCTION get_tb_d_msh_role_rec(role VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF role IS NULL THEN
            log_verbose('No record added into TB_D_MSH_ROLE');
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MSH_ROLE WHERE ROLE = :1' INTO v_id_pk USING role;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_MSH_ROLE: ' || role);
                v_id_pk := generate_id();
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_MSH_ROLE(ID_PK, ROLE) VALUES (' || v_id_pk || ', :1)' USING role;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_msh_role_rec;

    FUNCTION get_tb_d_service_rec(service_type VARCHAR2, service_value VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF service_type IS NULL AND service_value IS NULL THEN
            log_verbose('No record added into TB_D_SERVICE');
            RETURN v_id_pk;
        END IF;
        BEGIN
            IF service_type IS NULL THEN
                EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_SERVICE WHERE TYPE IS NULL AND VALUE = :1' INTO v_id_pk USING service_value;
            ELSIF service_value IS NULL THEN
                EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_SERVICE WHERE TYPE = :1 AND VALUE IS NULL' INTO v_id_pk USING service_type;
            ELSE
                EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_SERVICE WHERE TYPE = :1 AND VALUE = :2' INTO v_id_pk USING service_type, service_value;
            END IF;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_SERVICE: ' || service_type || ' , ' || service_value);
                v_id_pk := generate_id();
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_SERVICE(ID_PK, TYPE, VALUE) VALUES (' || v_id_pk ||
                                  ', :1, :2)' USING service_type, service_value;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_service_rec;

    FUNCTION get_tb_d_msg_status_rec(message_status VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF message_status IS NULL THEN
            log_verbose('No record added into TB_D_MESSAGE_STATUS');
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MESSAGE_STATUS WHERE STATUS = :1' INTO v_id_pk USING message_status;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_MESSAGE_STATUS: ' || message_status);
                v_id_pk := generate_id();
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_MESSAGE_STATUS(ID_PK, STATUS) VALUES (' || v_id_pk ||
                                  ', :1)' USING message_status;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_msg_status_rec;

    FUNCTION get_tb_d_agreement_rec(agreement_type VARCHAR2, agreement_value VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF agreement_type IS NULL AND agreement_value IS NULL THEN
            log_verbose('No record added into TB_D_AGREEMENT');
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_AGREEMENT WHERE TYPE = :1 AND VALUE = :2' INTO v_id_pk USING agreement_type, agreement_value;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE(
                            'Add new record into TB_D_AGREEMENT: ' || agreement_type || ' , ' || agreement_value);
                v_id_pk := generate_id();
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_AGREEMENT(ID_PK, TYPE, VALUE) VALUES (' || v_id_pk ||
                                  ', :1, :2)' USING agreement_type, agreement_value;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_agreement_rec;

    FUNCTION get_tb_d_action_rec(action VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF action IS NULL THEN
            log_verbose('No record added into TB_D_ACTION');
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_ACTION WHERE ACTION = :1' INTO v_id_pk USING action;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_ACTION: ' || action);
                v_id_pk := generate_id();
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_ACTION(ID_PK, ACTION) VALUES (' || v_id_pk || ', :1)' USING action;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_action_rec;

    FUNCTION get_tb_d_party_rec(party_type VARCHAR2, party_value VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF party_type IS NULL AND party_value IS NULL THEN
            log_verbose('No record added into TB_D_PARTY');
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_PARTY WHERE TYPE = :1 AND VALUE = :2' INTO v_id_pk USING party_type, party_value;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_PARTY: ' || party_type || ' , ' || party_value);
                v_id_pk := generate_id();
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_PARTY(ID_PK, TYPE, VALUE) VALUES (' || v_id_pk ||
                                  ', :1, :2)' USING party_type, party_value;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_party_rec;

    FUNCTION get_msg_subtype(msg_subtype VARCHAR2) RETURN NUMBER IS
        v_test_message NUMBER := 0;
    BEGIN
        IF msg_subtype = 'TEST' THEN
            v_test_message := 1;
        END IF;
        RETURN v_test_message;
    END get_msg_subtype;

    FUNCTION get_tb_d_notif_status_rec(status VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF status IS NULL THEN
            log_verbose('No record added into TB_D_NOTIFICATION_STATUS');
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_NOTIFICATION_STATUS WHERE STATUS = :1' INTO v_id_pk USING status;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_NOTIFICATION_STATUS: ' || status);
                v_id_pk := generate_id();
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_NOTIFICATION_STATUS(ID_PK, STATUS) VALUES (' || v_id_pk ||
                                  ', :1)' USING status;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_notif_status_rec;

    FUNCTION get_tb_user_message_rec(message_id VARCHAR2) RETURN NUMBER IS
        v_id_pk   NUMBER;
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_MESSAGE';
    BEGIN
        IF message_id IS NULL THEN
            log_verbose('No record to look into ' || v_tab_new);
            RETURN v_id_pk;
        END IF;
        BEGIN
            -- TODO check index on message_id column?
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM ' || v_tab_new || ' WHERE MESSAGE_ID = :1' INTO v_id_pk USING message_id;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                DBMS_OUTPUT.PUT_LINE('No record found into ' || v_tab_new || ' for MESSAGE_ID = ' || message_id);
        END;
        RETURN v_id_pk;
    END get_tb_user_message_rec;

    FUNCTION get_tb_signal_message_rec(message_id VARCHAR2) RETURN NUMBER IS
        v_id_pk   NUMBER;
        v_tab_new VARCHAR2(30) := 'MIGR_TB_SIGNAL_MESSAGE';
    BEGIN
        IF message_id IS NULL THEN
            log_verbose('No record to look into ' || v_tab_new);
            RETURN v_id_pk;
        END IF;
        BEGIN
            -- TODO check index on signal_message_id column?
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM ' || v_tab_new || ' WHERE SIGNAL_MESSAGE_ID = :1' INTO v_id_pk USING message_id;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                DBMS_OUTPUT.PUT_LINE('No record found into ' || v_tab_new || ' for MESSAGE_ID = ' || message_id);
        END;
        RETURN v_id_pk;
    END get_tb_signal_message_rec;

    FUNCTION get_tb_d_msg_property_rec(prop_name VARCHAR2, prop_value VARCHAR2, prop_type VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF prop_name IS NULL AND prop_value IS NULL AND prop_type IS NULL THEN
            log_verbose('No record added into TB_D_MESSAGE_PROPERTY');
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MESSAGE_PROPERTY WHERE (NAME = :1 AND TYPE = :2 AND VALUE = :3) OR (NAME = :4 AND TYPE IS NULL AND VALUE = :5 )' INTO v_id_pk USING prop_name, prop_type, prop_value, prop_name, prop_value;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE(
                            'Add new record into TB_D_MESSAGE_PROPERTY: ' || prop_name || ' , ' || prop_value|| ' , ' || prop_type);
                v_id_pk := generate_id();
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_MESSAGE_PROPERTY(ID_PK, NAME, VALUE, TYPE) VALUES (' || v_id_pk ||
                                  ', :1, :2, :3)' USING prop_name, prop_value, prop_type;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_msg_property_rec;

    FUNCTION get_tb_d_part_property_rec(prop_name VARCHAR2, prop_value VARCHAR2, prop_type VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF prop_name IS NULL AND prop_value IS NULL AND prop_type IS NULL THEN
            log_verbose('No record added into TB_D_PART_PROPERTY');
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_PART_PROPERTY WHERE (NAME = :1 AND VALUE = :2 AND TYPE = :3) OR (NAME = :4 AND VALUE = :5 AND TYPE IS NULL)' INTO v_id_pk USING prop_name, prop_value, prop_type, prop_name, prop_value;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE(
                            'Add new record into TB_D_PART_PROPERTY: ' || prop_name || ' , ' || prop_value|| ' , ' || prop_type);
                v_id_pk := generate_id();
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_PART_PROPERTY(ID_PK, NAME, VALUE, TYPE) VALUES (' || v_id_pk ||
                                  ', :1, :2, :3)' USING prop_name, prop_value, prop_type;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_part_property_rec;

    /**-- TB_D_TIMEZONE_OFFSET migration --*/
    PROCEDURE prepare_timezone_offset(migration_pks IN OUT JSON_OBJECT_T) IS
        v_id_pk NUMBER;
    BEGIN
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_TIMEZONE_OFFSET WHERE NEXT_ATTEMPT_TIMEZONE_ID=''UTC''' INTO v_id_pk;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                v_id_pk := generate_id();
                update_migration_pks('timezone_offset', 1, v_id_pk, migration_pks);

                EXECUTE IMMEDIATE 'INSERT INTO TB_D_TIMEZONE_OFFSET (ID_PK, NEXT_ATTEMPT_TIMEZONE_ID, NEXT_ATTEMPT_OFFSET_SECONDS, CREATION_TIME, CREATED_BY) VALUES (' ||
                                  v_id_pk || ', ''UTC'', 0, SYSDATE, ''migration'')';
        END;
    END prepare_timezone_offset;

    /**-- CLOB to BLOB conversion --*/
    FUNCTION clob_to_blob(p_data IN CLOB) RETURN BLOB
    AS
        l_blob         BLOB;
        l_dest_offset  PLS_INTEGER := 1;
        l_src_offset   PLS_INTEGER := 1;
        l_lang_context PLS_INTEGER := DBMS_LOB.default_lang_ctx;
        l_warning      PLS_INTEGER := DBMS_LOB.warn_inconvertible_char;
    BEGIN
        DBMS_LOB.createtemporary(
                lob_loc => l_blob,
                cache => TRUE);
        DBMS_LOB.converttoblob(
                dest_lob => l_blob,
                src_clob => p_data,
                amount => DBMS_LOB.lobmaxsize,
                dest_offset => l_dest_offset,
                src_offset => l_src_offset,
                blob_csid => DBMS_LOB.default_csid,
                lang_context => l_lang_context,
                warning => l_warning);
        RETURN l_blob;
    END clob_to_blob;

    /**-- TB_USER_MESSAGE migration --*/
    PROCEDURE migrate_user_message(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab        VARCHAR2(30) := 'TB_USER_MESSAGE';
        v_tab_new    VARCHAR2(30) := 'MIGR_TB_USER_MESSAGE';

        v_id_pk NUMBER;

        CURSOR c_user_message IS
            SELECT UM.ID_PK,
                   MI.MESSAGE_ID,
                   MI.REF_TO_MESSAGE_ID,
                   UM.COLL_INFO_CONVERS_ID      CONVERSATION_ID,
                   ML.SOURCE_MESSAGE,
                   ML.MESSAGE_FRAGMENT,
                   MI.TIME_STAMP                EBMS3_TIMESTAMP,
                   UM.MPC,
                   UM.FROM_ROLE,
                   UM.TO_ROLE,
                   UM.SERVICE_TYPE,
                   UM.SERVICE_VALUE,
                   UM.AGREEMENT_REF_TYPE,
                   UM.AGREEMENT_REF_VALUE,
                   UM.COLLABORATION_INFO_ACTION ACTION,
                   PA1.TYPE                     FROM_PARTY_TYPE,
                   PA1.VALUE                    FROM_PARTY_VALUE,
                   PA2.TYPE                     TO_PARTY_TYPE,
                   PA2.VALUE                    TO_PARTY_VALUE,
                   ML.MESSAGE_SUBTYPE,
                   UM.CREATION_TIME
            FROM TB_MESSAGE_LOG ML
                     LEFT OUTER JOIN TB_MESSAGE_INFO MI ON ML.MESSAGE_ID = MI.MESSAGE_ID,
                 TB_USER_MESSAGE UM
                     LEFT OUTER JOIN TB_PARTY_ID PA1 ON UM.ID_PK = PA1.FROM_ID
                     LEFT OUTER JOIN TB_PARTY_ID PA2 ON UM.ID_PK = PA2.TO_ID
            WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK;
        TYPE T_USER_MESSAGE IS TABLE OF c_user_message%ROWTYPE;
        user_message T_USER_MESSAGE;
        v_batch_no   INT          := 1;
    BEGIN
        /** migrate old columns and add data into dictionary tables */
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_message;
        LOOP
            FETCH c_user_message BULK COLLECT INTO user_message;
            EXIT WHEN user_message.COUNT = 0;

            FOR i IN user_message.FIRST .. user_message.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(user_message(i).ID_PK, user_message(i).CREATION_TIME);
                        update_migration_pks('user_message', user_message(i).ID_PK, v_id_pk, migration_pks);
                        update_migration_pks('message_info', user_message(i).ID_PK, v_id_pk, migration_pks);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, CONVERSATION_ID, SOURCE_MESSAGE, MESSAGE_FRAGMENT, EBMS3_TIMESTAMP, MPC_ID_FK, FROM_ROLE_ID_FK, ' ||
                                          'TO_ROLE_ID_FK, SERVICE_ID_FK, AGREEMENT_ID_FK, ACTION_ID_FK, FROM_PARTY_ID_FK, TO_PARTY_ID_FK, TEST_MESSAGE) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16)'
                        USING v_id_pk,
                                user_message(i).MESSAGE_ID,
                                user_message(i).REF_TO_MESSAGE_ID,
                                user_message(i).CONVERSATION_ID,
                                user_message(i).SOURCE_MESSAGE,
                                user_message(i).MESSAGE_FRAGMENT,
                                user_message(i).EBMS3_TIMESTAMP,
                                get_tb_d_mpc_rec(user_message(i).MPC),
                                get_tb_d_role_rec(user_message(i).FROM_ROLE),
                                get_tb_d_role_rec(user_message(i).TO_ROLE),
                                get_tb_d_service_rec(user_message(i).SERVICE_TYPE, user_message(i).SERVICE_VALUE),
                                get_tb_d_agreement_rec(user_message(i).AGREEMENT_REF_TYPE, user_message(i).AGREEMENT_REF_VALUE),
                                get_tb_d_action_rec(user_message(i).ACTION),
                                get_tb_d_party_rec(user_message(i).FROM_PARTY_TYPE, user_message(i).FROM_PARTY_VALUE),
                                get_tb_d_party_rec(user_message(i).TO_PARTY_TYPE, user_message(i).TO_PARTY_VALUE),
                                get_msg_subtype(user_message(i).MESSAGE_SUBTYPE);
                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_message -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;

                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_message.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_message;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;

    END migrate_user_message;

    /**-- TB_MESSAGE_FRAGMENT migration --*/
    PROCEDURE migrate_message_fragment(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab              VARCHAR2(30) := 'TB_MESSAGE_FRAGMENT';
        v_tab_new          VARCHAR2(30) := 'MIGR_TB_SJ_MESSAGE_FRAGMENT';
        v_tab_user_message VARCHAR2(30) := 'TB_USER_MESSAGE';

        v_id_pk NUMBER;

        CURSOR c_message_fragment IS
            SELECT UM.ID_PK, -- 1:1 ID_PK implementation
                   MF.GROUP_ID,
                   MF.FRAGMENT_NUMBER,
                   MF.CREATION_TIME,
                   MF.CREATED_BY,
                   MF.MODIFICATION_TIME,
                   MF.MODIFIED_BY,
                   MG.ID_PK GROUP_ID_FK
            FROM TB_MESSAGE_FRAGMENT MF,
                 TB_MESSAGE_GROUP MG,
                 TB_USER_MESSAGE UM
            WHERE UM.FK_MESSAGE_FRAGMENT_ID = MF.ID_PK
              AND MF.GROUP_ID = MG.GROUP_ID;
        TYPE T_MESSAGE_FRAGMENT IS TABLE OF c_message_fragment%ROWTYPE;
        message_fragment   T_MESSAGE_FRAGMENT;
        v_batch_no         INT          := 1;
    BEGIN
        IF NOT check_table_exists(v_tab_user_message) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab_user_message || ' should exists before starting ' || v_tab || ' migration');
        END IF;

        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_message_fragment;
        LOOP
            FETCH c_message_fragment BULK COLLECT INTO message_fragment;
            EXIT WHEN message_fragment.COUNT = 0;

            FOR i IN message_fragment.FIRST .. message_fragment.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('user_message', migration_pks, message_fragment(i).ID_PK, v_id_pk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, GROUP_ID_FK, FRAGMENT_NUMBER, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7)'
                        USING v_id_pk,
                                message_fragment(i).GROUP_ID_FK,
                                message_fragment(i).FRAGMENT_NUMBER,
                                message_fragment(i).CREATION_TIME,
                                message_fragment(i).CREATED_BY,
                                message_fragment(i).MODIFICATION_TIME,
                                message_fragment(i).MODIFIED_BY;
                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_message_fragment -> execute immediate error: ' || DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;

                END LOOP;
            DBMS_OUTPUT.PUT_LINE(v_tab_new || ': Migrated ' || message_fragment.COUNT || ' records in total');
        END LOOP;

        COMMIT;
        CLOSE c_message_fragment;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;

    END migrate_message_fragment;

    /**-- TB_MESSAGE_GROUP migration --*/
    PROCEDURE migrate_message_group(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab                  VARCHAR2(30) := 'TB_MESSAGE_GROUP';
        v_tab_new              VARCHAR2(30) := 'MIGR_TB_SJ_MESSAGE_GROUP';
        v_tab_user_message_new VARCHAR2(30) := 'MIGR_TB_USER_MESSAGE';

        v_id_pk NUMBER;

        CURSOR c_message_group IS
            SELECT MG.ID_PK,
                   MG.GROUP_ID,
                   MG.MESSAGE_SIZE,
                   MG.FRAGMENT_COUNT,
                   MG.SENT_FRAGMENTS,
                   MG.RECEIVED_FRAGMENTS,
                   MG.COMPRESSION_ALGORITHM,
                   MG.COMPRESSED_MESSAGE_SIZE,
                   MG.SOAP_ACTION,
                   MG.REJECTED,
                   MG.EXPIRED,
                   MG.MSH_ROLE,
                   MG.SOURCE_MESSAGE_ID,
                   MG.CREATION_TIME,
                   MG.CREATED_BY,
                   MG.MODIFICATION_TIME,
                   MG.MODIFIED_BY
            FROM TB_MESSAGE_GROUP MG;
        TYPE T_MESSAGE_GROUP IS TABLE OF c_message_group%ROWTYPE;
        message_group          T_MESSAGE_GROUP;
        v_batch_no             INT          := 1;
    BEGIN
        IF NOT check_table_exists(v_tab_user_message_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab_user_message_new || ' should exists before starting ' || v_tab || ' migration');
        END IF;

        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_message_group;
        LOOP
            FETCH c_message_group BULK COLLECT INTO message_group;
            EXIT WHEN message_group.COUNT = 0;

            FOR i IN message_group.FIRST .. message_group.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(message_group(i).ID_PK, message_group(i).CREATION_TIME);
                        update_migration_pks('message_group', message_group(i).ID_PK, v_id_pk, migration_pks);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, GROUP_ID, MESSAGE_SIZE, FRAGMENT_COUNT, SENT_FRAGMENTS, RECEIVED_FRAGMENTS, COMPRESSION_ALGORITHM, COMPRESSED_MESSAGE_SIZE,' ||
                                          'SOAP_ACTION, REJECTED, EXPIRED, MSH_ROLE_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY, SOURCE_MESSAGE_ID_FK) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16, :p_17)'
                        USING v_id_pk,
                                message_group(i).GROUP_ID,
                                message_group(i).MESSAGE_SIZE,
                                message_group(i).FRAGMENT_COUNT,
                                message_group(i).SENT_FRAGMENTS,
                                message_group(i).RECEIVED_FRAGMENTS,
                                message_group(i).COMPRESSION_ALGORITHM,
                                message_group(i).COMPRESSED_MESSAGE_SIZE,
                                message_group(i).SOAP_ACTION,
                                message_group(i).REJECTED,
                                message_group(i).EXPIRED,
                                get_tb_d_msh_role_rec(message_group(i).MSH_ROLE),
                                message_group(i).CREATION_TIME,
                                message_group(i).CREATED_BY,
                                message_group(i).MODIFICATION_TIME,
                                message_group(i).MODIFIED_BY,
                                get_tb_user_message_rec(message_group(i).SOURCE_MESSAGE_ID); -- we look into migrated table here
                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_message_group -> execute immediate error: ' || DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;

                END LOOP;
            DBMS_OUTPUT.PUT_LINE(v_tab_new || ': Migrated ' || message_group.COUNT || ' records in total');
        END LOOP;

        COMMIT;
        CLOSE c_message_group;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;

    END migrate_message_group;

    /**-- TB_MESSAGE_GROUP migration --*/
    PROCEDURE migrate_message_header(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab               VARCHAR2(30) := 'TB_MESSAGE_HEADER';
        v_tab_new           VARCHAR2(30) := 'MIGR_TB_SJ_MESSAGE_HEADER';
        v_tab_message_group VARCHAR2(30) := 'TB_MESSAGE_GROUP';

        v_id_pk NUMBER;

        CURSOR c_message_header IS
            SELECT MG.ID_PK, -- 1:1 ID_PK implementation
                   MH.BOUNDARY,
                   MH."START",
                   MH.CREATION_TIME,
                   MH.CREATED_BY,
                   MH.MODIFICATION_TIME,
                   MH.MODIFIED_BY
            FROM TB_MESSAGE_HEADER MH,
                 TB_MESSAGE_GROUP MG
            WHERE MG.FK_MESSAGE_HEADER_ID = MH.ID_PK;
        TYPE T_MESSAGE_HEADER IS TABLE OF c_message_header%ROWTYPE;
        message_header      T_MESSAGE_HEADER;
        v_batch_no          INT          := 1;
    BEGIN
        IF NOT check_table_exists(v_tab_message_group) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab_message_group || ' should exists before starting ' || v_tab || ' migration');
        END IF;

        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_message_header;
        LOOP
            FETCH c_message_header BULK COLLECT INTO message_header;
            EXIT WHEN message_header.COUNT = 0;

            FOR i IN message_header.FIRST .. message_header.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('message_group', migration_pks, message_header(i).ID_PK, v_id_pk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, BOUNDARY, START_MULTIPART, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7)'
                        USING v_id_pk,
                                message_header(i).BOUNDARY,
                                message_header(i)."START",
                                message_header(i).CREATION_TIME,
                                message_header(i).CREATED_BY,
                                message_header(i).MODIFICATION_TIME,
                                message_header(i).MODIFIED_BY;
                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_message_header -> execute immediate error: ' || DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;

                END LOOP;
            DBMS_OUTPUT.PUT_LINE(v_tab_new || ': Migrated ' || message_header.COUNT || ' records in total');
        END LOOP;

        COMMIT;
        CLOSE c_message_header;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;

    END migrate_message_header;


    /**-- TB_SIGNAL_MESSAGE, TB_RECEIPT and TB_RECEIPT_DATA migration --*/
    PROCEDURE migrate_signal_receipt(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab_signal           VARCHAR2(30) := 'TB_SIGNAL_MESSAGE';
        v_tab_signal_new       VARCHAR2(30) := 'MIGR_TB_SIGNAL_MESSAGE';
        v_tab_messaging        VARCHAR2(30) := 'TB_MESSAGING';
        v_tab_user_message     VARCHAR2(30) := 'TB_USER_MESSAGE';
        v_tab_receipt          VARCHAR2(30) := 'TB_RECEIPT';
        v_tab_receipt_data     VARCHAR2(30) := 'TB_RECEIPT_DATA';
        v_tab_receipt_new      VARCHAR2(30) := 'MIGR_TB_RECEIPT';

        v_id_pk NUMBER;

        CURSOR c_signal_message_receipt IS
            SELECT UM.ID_PK, --  1:1 here
                   MI.MESSAGE_ID        SIGNAL_MESSAGE_ID,
                   MI.REF_TO_MESSAGE_ID REF_TO_MESSAGE_ID,
                   MI.TIME_STAMP        EBMS3_TIMESTAMP,
                   SM.CREATION_TIME,
                   SM.CREATED_BY,
                   SM.MODIFICATION_TIME,
                   SM.MODIFIED_BY,
                   RD.RAW_XML,
                   RD.CREATION_TIME     R_CREATION_TIME,
                   RD.CREATED_BY        R_CREATED_BY,
                   RD.MODIFICATION_TIME R_MODIFICATION_TIME,
                   RD.MODIFIED_BY       R_MODIFIED_BY
            FROM TB_MESSAGE_INFO MI,
                 TB_SIGNAL_MESSAGE SM,
                 TB_MESSAGING ME,
                 TB_USER_MESSAGE UM,
                 TB_RECEIPT RE,
                 TB_RECEIPT_DATA RD
            WHERE SM.MESSAGEINFO_ID_PK = MI.ID_PK
              AND ME.SIGNAL_MESSAGE_ID = SM.ID_PK
              AND ME.USER_MESSAGE_ID = UM.ID_PK
              AND RE.ID_PK = SM.RECEIPT_ID_PK
              AND RD.RECEIPT_ID = RE.ID_PK;
        TYPE T_SIGNAL_MESSAGE_RECEIPT IS TABLE OF c_signal_message_receipt%ROWTYPE;
        signal_message_receipt T_SIGNAL_MESSAGE_RECEIPT;
        v_batch_no             INT          := 1;
    BEGIN
        IF NOT check_table_exists(v_tab_messaging) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab_messaging || ' should exists before starting ' || v_tab_signal || ' migration');
        END IF;
        IF NOT check_table_exists(v_tab_user_message) THEN
            DBMS_OUTPUT.PUT_LINE(
                        v_tab_user_message || ' should exists before starting ' || v_tab_signal || ' migration');
        END IF;

        /** migrate old columns and add data into dictionary tables */
        DBMS_OUTPUT.PUT_LINE(
                    v_tab_signal || ' ,' || v_tab_receipt || ' and' || v_tab_receipt_data || ' migration started...');
        OPEN c_signal_message_receipt;
        LOOP
            FETCH c_signal_message_receipt BULK COLLECT INTO signal_message_receipt;
            EXIT WHEN signal_message_receipt.COUNT = 0;

            FOR i IN signal_message_receipt.FIRST .. signal_message_receipt.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('user_message', migration_pks, signal_message_receipt(i).ID_PK, v_id_pk);

                        -- new tb_signal_message table
                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_signal_new ||
                                          ' (ID_PK, SIGNAL_MESSAGE_ID, REF_TO_MESSAGE_ID, EBMS3_TIMESTAMP, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8)'
                        USING v_id_pk,
                                signal_message_receipt(i).SIGNAL_MESSAGE_ID,
                                signal_message_receipt(i).REF_TO_MESSAGE_ID,
                                signal_message_receipt(i).EBMS3_TIMESTAMP,
                                signal_message_receipt(i).CREATION_TIME,
                                signal_message_receipt(i).CREATED_BY,
                                signal_message_receipt(i).MODIFICATION_TIME,
                                signal_message_receipt(i).MODIFIED_BY;

                        -- new tb_receipt table
                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_receipt_new ||
                                          ' (ID_PK, RAW_XML, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                        USING v_id_pk,
                                clob_to_blob(signal_message_receipt(i).RAW_XML),
                                signal_message_receipt(i).R_CREATION_TIME,
                                signal_message_receipt(i).R_CREATED_BY,
                                signal_message_receipt(i).R_MODIFICATION_TIME,
                                signal_message_receipt(i).R_MODIFIED_BY;
                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_signal_new || ' and ' || v_tab_receipt_new || ': Commit after ' ||
                                        BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_signal_receipt -> execute immediate error: ' || DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;

                END LOOP;
            DBMS_OUTPUT.PUT_LINE(
                        'Migrated ' || signal_message_receipt.COUNT || ' records in total into ' || v_tab_signal_new ||
                        ' and ' || v_tab_receipt_new);
        END LOOP;

        COMMIT;
        CLOSE c_signal_message_receipt;

        -- check counts
        IF check_counts(v_tab_signal, v_tab_signal_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab_signal || ' migration is done');
        END IF;
        IF check_counts(v_tab_receipt, v_tab_receipt_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab_receipt || ' and ' || v_tab_receipt_data || ' migration is done');
        END IF;

    END migrate_signal_receipt;

    /**-- TB_RAWENVELOPE_LOG migration --*/
    PROCEDURE migrate_raw_envelope_log(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab              VARCHAR2(30) := 'TB_RAWENVELOPE_LOG';
        v_tab_user_new     VARCHAR2(30) := 'MIGR_TB_USER_MESSAGE_RAW';
        v_tab_signal_new   VARCHAR2(30) := 'MIGR_TB_SIGNAL_MESSAGE_RAW';
        v_tab_user_message VARCHAR2(30) := 'TB_USER_MESSAGE';
        v_tab_messaging    VARCHAR2(30) := 'TB_MESSAGING';
        v_count_user       NUMBER       := 0;
        v_count_signal     NUMBER       := 0;
        v_tab_migrated     VARCHAR2(30) := v_tab_signal_new;

        v_id_pk NUMBER;

        CURSOR c_raw_envelope IS
            SELECT UM.ID_PK, --  1:1 here
                   'USER' AS "TYPE",
                   RA.RAW_XML,
                   RA.CREATION_TIME,
                   RA.CREATED_BY,
                   RA.MODIFICATION_TIME,
                   RA.MODIFIED_BY
            FROM TB_USER_MESSAGE UM,
                 TB_RAWENVELOPE_LOG RA
            WHERE UM.ID_PK = RA.USERMESSAGE_ID_FK
            UNION ALL
            SELECT UM.ID_PK, --  1:1 here
                   'SIGNAL' AS "TYPE",
                   RA.RAW_XML,
                   RA.CREATION_TIME,
                   RA.CREATED_BY,
                   RA.MODIFICATION_TIME,
                   RA.MODIFIED_BY
            FROM TB_SIGNAL_MESSAGE SM,
                 TB_MESSAGING ME,
                 TB_USER_MESSAGE UM,
                 TB_RAWENVELOPE_LOG RA
            WHERE ME.SIGNAL_MESSAGE_ID = SM.ID_PK
              AND ME.USER_MESSAGE_ID = UM.ID_PK
              AND SM.ID_PK = RA.SIGNALMESSAGE_ID_FK;
        TYPE T_RAW_ENVELOPE IS TABLE OF c_raw_envelope%ROWTYPE;
        raw_envelope       T_RAW_ENVELOPE;
        v_batch_no         INT          := 1;
    BEGIN
        IF NOT check_table_exists(v_tab_messaging) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab_messaging || ' should exists before starting ' || v_tab || ' migration');
        END IF;
        IF NOT check_table_exists(v_tab_user_message) THEN
            DBMS_OUTPUT.PUT_LINE(
                        v_tab_user_message || ' should exists before starting ' || v_tab || ' migration');
        END IF;

        DBMS_OUTPUT.PUT_LINE(
                    v_tab || ' migration started...');
        OPEN c_raw_envelope;
        LOOP
            FETCH c_raw_envelope BULK COLLECT INTO raw_envelope;
            EXIT WHEN raw_envelope.COUNT = 0;

            FOR i IN raw_envelope.FIRST .. raw_envelope.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('user_message', migration_pks, raw_envelope(i).ID_PK, v_id_pk);

                        IF raw_envelope(i)."TYPE" = 'USER' THEN
                            v_count_user := v_count_user + 1;
                            BEGIN
                                EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_user_new ||
                                                  ' (ID_PK, RAW_XML, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                                  'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                                USING v_id_pk,
                                        clob_to_blob(raw_envelope(i).RAW_XML),
                                        raw_envelope(i).CREATION_TIME,
                                        raw_envelope(i).CREATED_BY,
                                        raw_envelope(i).MODIFICATION_TIME,
                                        raw_envelope(i).MODIFIED_BY;
                            EXCEPTION
                                WHEN OTHERS THEN
                                    DBMS_OUTPUT.PUT_LINE('migrate_raw_envelope_log for ' || v_tab_user_new ||
                                                         '-> execute immediate error: ' ||
                                                         DBMS_UTILITY.FORMAT_ERROR_STACK);
                            END;

                        ELSE
                            v_count_signal := v_count_signal + 1;
                            BEGIN
                                EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_signal_new ||
                                                  ' (ID_PK, RAW_XML, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                                  'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                                USING v_id_pk,
                                        clob_to_blob(raw_envelope(i).RAW_XML),
                                        raw_envelope(i).CREATION_TIME,
                                        raw_envelope(i).CREATED_BY,
                                        raw_envelope(i).MODIFICATION_TIME,
                                        raw_envelope(i).MODIFIED_BY;
                            EXCEPTION
                                WHEN OTHERS THEN
                                    DBMS_OUTPUT.PUT_LINE('migrate_raw_envelope_log for ' || v_tab_signal_new ||
                                                         '-> execute immediate error: ' ||
                                                         DBMS_UTILITY.FORMAT_ERROR_STACK);
                            END;
                        END IF;
                        -- just for logging
                        IF v_count_user > 0 THEN
                            v_tab_migrated := v_tab_user_new;
                        ELSE
                            v_tab_migrated := v_tab_signal_new;
                        END IF;
                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_migrated || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    END;

                END LOOP;
            DBMS_OUTPUT.PUT_LINE(
                        'Migrated ' || raw_envelope.COUNT || ' records in total into ' || v_tab_migrated);
        END LOOP;

        COMMIT;
        CLOSE c_raw_envelope;

        -- check counts
        IF check_counts(v_tab, v_tab_migrated) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_raw_envelope_log;

    /**-- TB_MESSAGE_LOG migration --*/
    PROCEDURE migrate_message_log IS
        v_tab            VARCHAR2(30) := 'TB_MESSAGE_LOG';
        v_tab_user_new   VARCHAR2(30) := 'MIGR_TB_USER_MESSAGE_LOG';
        v_tab_signal_new VARCHAR2(30) := 'MIGR_TB_SIGNAL_MESSAGE_LOG';
        v_count_user     NUMBER       := 0;
        v_count_signal   NUMBER       := 0;
        v_count          NUMBER       := 0;
        CURSOR c_message_log IS
            SELECT ML.ID_PK,
                   ML.MESSAGE_ID,
                   ML.MESSAGE_TYPE,
                   ML.BACKEND,
                   ML.RECEIVED,
                   ML.DOWNLOADED,
                   ML.FAILED,
                   ML.RESTORED,
                   ML.DELETED,
                   ML.NEXT_ATTEMPT,
                   ML.SEND_ATTEMPTS,
                   ML.SEND_ATTEMPTS_MAX,
                   ML.SCHEDULED,
                   ML.VERSION,
                   ML.MESSAGE_STATUS,
                   ML.MSH_ROLE,
                   ML.NOTIFICATION_STATUS,
                   ML.CREATION_TIME,
                   ML.CREATED_BY,
                   ML.MODIFICATION_TIME,
                   ML.MODIFIED_BY
            FROM TB_MESSAGE_LOG ML;
        TYPE T_MESSAGE_LOG IS TABLE OF c_message_log%ROWTYPE;
        message_log      T_MESSAGE_LOG;
        v_batch_no       INT          := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_message_log;
        LOOP
            FETCH c_message_log BULK COLLECT INTO message_log;
            EXIT WHEN message_log.COUNT = 0;

            FOR i IN message_log.FIRST .. message_log.LAST
                LOOP
                    BEGIN
                        IF message_log(i).MESSAGE_TYPE = 'USER_MESSAGE' THEN
                            BEGIN
                                EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_user_new ||
                                                  ' (ID_PK, BACKEND, RECEIVED, DOWNLOADED, FAILED, RESTORED, DELETED, NEXT_ATTEMPT, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, SCHEDULED, ' ||
                                                  'VERSION, MESSAGE_STATUS_ID_FK, MSH_ROLE_ID_FK, NOTIFICATION_STATUS_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                                  'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16, :p_17, :p_18, :p_19)'
                                    USING
                                    get_tb_user_message_rec(message_log(i).MESSAGE_ID), -- return ID_PK from the newly migrated table
                                    message_log(i).BACKEND,
                                    message_log(i).RECEIVED,
                                    message_log(i).DOWNLOADED,
                                    message_log(i).FAILED,
                                    message_log(i).RESTORED,
                                    message_log(i).DELETED,
                                    message_log(i).NEXT_ATTEMPT,
                                    message_log(i).SEND_ATTEMPTS,
                                    message_log(i).SEND_ATTEMPTS_MAX,
                                    message_log(i).SCHEDULED,
                                    message_log(i).VERSION,
                                    get_tb_d_msg_status_rec(message_log(i).MESSAGE_STATUS),
                                    get_tb_d_msh_role_rec(message_log(i).MSH_ROLE),
                                    get_tb_d_notif_status_rec(message_log(i).NOTIFICATION_STATUS),
                                    message_log(i).CREATION_TIME,
                                    message_log(i).CREATED_BY,
                                    message_log(i).MODIFICATION_TIME,
                                    message_log(i).MODIFIED_BY;
                            EXCEPTION
                                WHEN OTHERS THEN
                                    DBMS_OUTPUT.PUT_LINE('migrate_message_log for ' || v_tab_user_new ||
                                                         ' -> execute immediate error: ' ||
                                                         DBMS_UTILITY.FORMAT_ERROR_STACK);
                            END;
                            v_count_user := v_count_user + 1;
                        ELSE
                            BEGIN
                                -- signal message
                                EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_signal_new ||
                                                  ' (ID_PK, RECEIVED, DELETED, MESSAGE_STATUS_ID_FK, MSH_ROLE_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                                  'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9)'
                                    USING
                                    get_tb_signal_message_rec(message_log(i).MESSAGE_ID), -- return ID_PK from the newly migrated table
                                    message_log(i).RECEIVED,
                                    message_log(i).DELETED,
                                    get_tb_d_msg_status_rec(message_log(i).MESSAGE_STATUS),
                                    get_tb_d_msh_role_rec(message_log(i).MSH_ROLE),
                                    message_log(i).CREATION_TIME,
                                    message_log(i).CREATED_BY,
                                    message_log(i).MODIFICATION_TIME,
                                    message_log(i).MODIFIED_BY;
                            EXCEPTION
                                WHEN OTHERS THEN
                                    DBMS_OUTPUT.PUT_LINE('migrate_message_log for ' || v_tab_signal_new || ' -> execute immediate error: ' || DBMS_UTILITY.FORMAT_ERROR_STACK);
                            END;
                            v_count_signal := v_count_signal + 1;
                        END IF;
                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab || ': Commit after ' || BATCH_SIZE * v_batch_no ||
                                        ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_message_log -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                    v_count := i;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE(
                        'Migrated ' || message_log.COUNT || ' records in total. ' || v_count_user || ' into ' ||
                        v_tab_user_new ||
                        ' and ' || v_count_signal || ' into ' || v_tab_signal_new);
        END LOOP;

        COMMIT;
        CLOSE c_message_log;

        -- check counts
        IF v_count_user + v_count_signal = v_count THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;

    END migrate_message_log;

    /**- TB_PROPERTY, TB_USER_MESSAGE data migration --*/
    PROCEDURE migrate_property(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab              VARCHAR2(30) := 'TB_PROPERTY';
        v_tab_message_new  VARCHAR2(30) := 'MIGR_TB_MESSAGE_PROPERTIES';

        v_user_message_id_fk NUMBER;

        CURSOR c_property IS
            SELECT UM.ID_PK USER_MESSAGE_ID_FK,
                   TP.NAME,
                   TP.VALUE,
                   TP.TYPE,
                   TP.CREATION_TIME,
                   TP.CREATED_BY,
                   TP.MODIFICATION_TIME,
                   TP.MODIFIED_BY
            FROM TB_PROPERTY TP,
                 TB_USER_MESSAGE UM
            WHERE TP.MESSAGEPROPERTIES_ID = UM.ID_PK
              AND TP.PARTPROPERTIES_ID IS NULL;
        TYPE T_PROPERTY IS TABLE OF c_property%ROWTYPE;
        property           T_PROPERTY;
        v_batch_no         INT          := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_property;
        LOOP
            FETCH c_property BULK COLLECT INTO property;
            EXIT WHEN property.COUNT = 0;

            FOR i IN property.FIRST .. property.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('user_message', migration_pks, property(i).USER_MESSAGE_ID_FK, v_user_message_id_fk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_message_new ||
                                          ' (USER_MESSAGE_ID_FK, MESSAGE_PROPERTY_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                        USING v_user_message_id_fk,
                                get_tb_d_msg_property_rec(property(i).NAME, property(i).VALUE, property(i).TYPE),
                                property(i).CREATION_TIME,
                                property(i).CREATED_BY,
                                property(i).MODIFICATION_TIME,
                                property(i).MODIFIED_BY;
                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_message_log -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE(
                        'Migrated ' || property.COUNT || ' records in total into ' ||
                        v_tab_message_new);
        END LOOP;

        COMMIT;
        CLOSE c_property;
    END migrate_property;

    /**- TB_PART_INFO, TB_USER_MESSAGE data migration --*/
    PROCEDURE migrate_part_info_user(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab              VARCHAR2(30) := 'TB_PART_INFO';
        v_tab_new          VARCHAR2(30) := 'MIGR_TB_PART_INFO';
        v_tab_user_message VARCHAR2(30) := 'TB_USER_MESSAGE';

        v_id_pk NUMBER;
        v_user_message_id_fk NUMBER;

        CURSOR c_part_info IS
            SELECT PI.ID_PK,
                   PI.BINARY_DATA,
                   PI.DESCRIPTION_LANG,
                   PI.DESCRIPTION_VALUE,
                   PI.HREF,
                   PI.IN_BODY,
                   PI.FILENAME,
                   PI.MIME,
                   PI.PART_ORDER,
                   PI.ENCRYPTED,
                   PI.CREATED_BY,
                   PI.CREATION_TIME,
                   PI.MODIFIED_BY,
                   PI.MODIFICATION_TIME,
                   UM.ID_PK USER_MESSAGE_ID_FK
            FROM TB_USER_MESSAGE UM,
                 TB_PART_INFO PI
            WHERE PI.PAYLOADINFO_ID = UM.ID_PK;
        TYPE T_PART_INFO IS TABLE OF c_part_info%ROWTYPE;
        part_info          T_PART_INFO;
        v_batch_no         INT          := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_part_info;
        LOOP
            FETCH c_part_info BULK COLLECT INTO part_info;
            EXIT WHEN part_info.COUNT = 0;

            FOR i IN part_info.FIRST .. part_info.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(part_info(i).ID_PK, part_info(i).CREATION_TIME);
                        update_migration_pks('part_info', part_info(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('user_message', migration_pks, part_info(i).USER_MESSAGE_ID_FK, v_user_message_id_fk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, BINARY_DATA, DESCRIPTION_LANG, DESCRIPTION_VALUE, HREF, IN_BODY, FILENAME, MIME,' ||
                                          'PART_ORDER, ENCRYPTED, USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15)'
                        USING v_id_pk,
                                part_info(i).BINARY_DATA,
                                part_info(i).DESCRIPTION_LANG,
                                part_info(i).DESCRIPTION_VALUE,
                                part_info(i).HREF,
                                part_info(i).IN_BODY,
                                part_info(i).FILENAME,
                                part_info(i).MIME,
                                part_info(i).PART_ORDER,
                                part_info(i).ENCRYPTED,
                                v_user_message_id_fk,
                                part_info(i).CREATION_TIME,
                                part_info(i).CREATED_BY,
                                part_info(i).MODIFICATION_TIME,
                                part_info(i).MODIFIED_BY;
                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_part_info_user for ' || v_tab_new ||
                                                 ' -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE(
                        'Migrated ' || part_info.COUNT || ' records in total into ' ||
                        v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_part_info;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' and '|| v_tab_user_message ||' migration is done');
        END IF;
    END migrate_part_info_user;

    /**- TB_PART_INFO, TB_PROPERTY data migration --*/
    PROCEDURE migrate_part_info_property(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab_info     VARCHAR2(30) := 'TB_PART_INFO';
        v_tab_property VARCHAR2(30) := 'TB_PROPERTY';
        v_tab_new      VARCHAR2(30) := 'MIGR_TB_PART_PROPERTIES';

        v_part_info_id_fk NUMBER;

        CURSOR c_part_prop IS
            SELECT PR.NAME,
                   PR.VALUE,
                   PR.TYPE,
                   PI.ID_PK PART_INFO_ID_FK,
                   PR.CREATION_TIME,
                   PR.CREATED_BY,
                   PR.MODIFICATION_TIME,
                   PR.MODIFIED_BY
            FROM TB_PART_INFO PI,
                 TB_PROPERTY PR
            WHERE PR.PARTPROPERTIES_ID = PI.ID_PK;
        TYPE T_PART_PROP IS TABLE OF c_part_prop%ROWTYPE;
        part_prop      T_PART_PROP;
        v_batch_no     INT          := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab_info || ' and ' || v_tab_property || ' migration started...');
        OPEN c_part_prop;
        LOOP
            FETCH c_part_prop BULK COLLECT INTO part_prop;
            EXIT WHEN part_prop.COUNT = 0;

            FOR i IN part_prop.FIRST .. part_prop.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('part_info', migration_pks, part_prop(i).PART_INFO_ID_FK, v_part_info_id_fk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (PART_INFO_ID_FK, PART_INFO_PROPERTY_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                        USING v_part_info_id_fk,
                                get_tb_d_part_property_rec(part_prop(i).NAME,
                                                           part_prop(i).VALUE,
                                                           part_prop(i).TYPE),
                                part_prop(i).CREATION_TIME,
                                part_prop(i).CREATED_BY,
                                part_prop(i).MODIFICATION_TIME,
                                part_prop(i).MODIFIED_BY;
                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_part_info_property for ' || v_tab_new ||
                                                 ' -> execute immediate error: ' || DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE(
                        'Migrated ' || part_prop.COUNT || ' records in total into ' ||
                        v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_part_prop;
    END migrate_part_info_property;

    /**- TB_ERROR_LOG data migration --*/
    PROCEDURE migrate_error_log(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab      VARCHAR2(30) := 'TB_ERROR_LOG';
        v_tab_new  VARCHAR2(30) := 'MIGR_TB_ERROR_LOG';

        v_id_pk NUMBER;
        v_user_message_id_fk NUMBER;

        CURSOR c_error_log IS
            SELECT EL.ID_PK,
                   EL.ERROR_CODE,
                   EL.ERROR_DETAIL,
                   EL.ERROR_SIGNAL_MESSAGE_ID,
                   EL.MESSAGE_IN_ERROR_ID,
                   EL.MSH_ROLE,
                   EL.NOTIFIED,
                   EL.TIME_STAMP,
                   EL.CREATION_TIME,
                   EL.CREATED_BY,
                   EL.MODIFICATION_TIME,
                   EL.MODIFIED_BY,
                   UMMI.ID_PK USER_MESSAGE_ID_FK
            FROM TB_ERROR_LOG EL
                     LEFT JOIN
                 (SELECT MI.MESSAGE_ID, UM.ID_PK
                  FROM TB_MESSAGE_INFO MI,
                       TB_USER_MESSAGE UM
                  WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK) UMMI
                 ON EL.MESSAGE_IN_ERROR_ID = UMMI.MESSAGE_ID;
        TYPE T_ERROR_LOG IS TABLE OF c_error_log%ROWTYPE;
        error_log  T_ERROR_LOG;
        v_batch_no INT          := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_error_log;
        LOOP
            FETCH c_error_log BULK COLLECT INTO error_log;
            EXIT WHEN error_log.COUNT = 0;

            FOR i IN error_log.FIRST .. error_log.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(error_log(i).ID_PK, error_log(i).CREATION_TIME);
                        update_migration_pks('error_log', error_log(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('message_info', migration_pks, error_log(i).USER_MESSAGE_ID_FK, v_user_message_id_fk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, ERROR_CODE, ERROR_DETAIL, ERROR_SIGNAL_MESSAGE_ID, MESSAGE_IN_ERROR_ID, MSH_ROLE_ID_FK,' ||
                                          ' NOTIFIED, TIME_STAMP, USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13)'
                        USING v_id_pk,
                                error_log(i).ERROR_CODE,
                                error_log(i).ERROR_DETAIL,
                                error_log(i).ERROR_SIGNAL_MESSAGE_ID,
                                error_log(i).MESSAGE_IN_ERROR_ID,
                                get_tb_d_msh_role_rec(error_log(i).MSH_ROLE),
                                error_log(i).NOTIFIED,
                                error_log(i).TIME_STAMP,
                                v_user_message_id_fk,
                                error_log(i).CREATION_TIME,
                                error_log(i).CREATED_BY,
                                error_log(i).MODIFICATION_TIME,
                                error_log(i).MODIFIED_BY;
                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_error_log -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE(
                        'Migrated ' || error_log.COUNT || ' records in total into ' ||
                        v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_error_log;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_error_log;

    /**- TB_MESSAGE_ACKNW data migration --*/
    PROCEDURE migrate_message_acknw(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab      VARCHAR2(30) := 'TB_MESSAGE_ACKNW';
        v_tab_new  VARCHAR2(30) := 'MIGR_TB_MESSAGE_ACKNW';

        v_id_pk NUMBER;
        v_user_message_id_fk NUMBER;

        CURSOR c_message_acknw IS
            SELECT MA.ID_PK,
                   MA.FROM_VALUE,
                   MA.TO_VALUE,
                   MA.ACKNOWLEDGE_DATE,
                   MA.CREATION_TIME,
                   MA.CREATED_BY,
                   MA.MODIFICATION_TIME,
                   MA.MODIFIED_BY,
                   UM.ID_PK USER_MESSAGE_ID_FK
            FROM
                TB_MESSAGE_ACKNW MA,
                TB_MESSAGE_INFO MI,
                TB_USER_MESSAGE UM
            WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK
              AND MI.MESSAGE_ID = MA.MESSAGE_ID;
        TYPE T_MESSAGE_ACKNW IS TABLE OF c_message_acknw%ROWTYPE;
        message_acknw  T_MESSAGE_ACKNW;
        v_batch_no INT          := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_message_acknw;
        LOOP
            FETCH c_message_acknw BULK COLLECT INTO message_acknw;
            EXIT WHEN message_acknw.COUNT = 0;

            FOR i IN message_acknw.FIRST .. message_acknw.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(message_acknw(i).ID_PK, message_acknw(i).CREATION_TIME);
                        update_migration_pks('message_acknw', message_acknw(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('user_message', migration_pks, message_acknw(i).USER_MESSAGE_ID_FK, v_user_message_id_fk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, FROM_VALUE, TO_VALUE, ACKNOWLEDGE_DATE, USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9)'
                        USING v_id_pk,
                                message_acknw(i).FROM_VALUE,
                                message_acknw(i).TO_VALUE,
                                message_acknw(i).ACKNOWLEDGE_DATE,
                                v_user_message_id_fk,
                                message_acknw(i).CREATION_TIME,
                                message_acknw(i).CREATED_BY,
                                message_acknw(i).MODIFICATION_TIME,
                                message_acknw(i).MODIFIED_BY;
                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_message_acknw -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE(
                        'Migrated ' || message_acknw.COUNT || ' records in total into ' ||
                        v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_message_acknw;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_message_acknw;

    /**- TB_SEND_ATTEMPT data migration --*/
    PROCEDURE migrate_send_attempt(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab        VARCHAR2(30) := 'TB_SEND_ATTEMPT';
        v_tab_new    VARCHAR2(30) := 'MIGR_TB_SEND_ATTEMPT';

        v_id_pk NUMBER;
        v_user_message_id_fk NUMBER;

        CURSOR c_send_attempt IS
            SELECT SA.ID_PK,
                   SA.START_DATE,
                   SA.END_DATE,
                   SA.STATUS,
                   SA.ERROR,
                   SA.CREATION_TIME,
                   SA.CREATED_BY,
                   SA.MODIFICATION_TIME,
                   SA.MODIFIED_BY,
                   UM.ID_PK USER_MESSAGE_ID_FK
            FROM TB_SEND_ATTEMPT SA,
                 TB_MESSAGE_INFO MI,
                 TB_USER_MESSAGE UM
            WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK
              AND MI.MESSAGE_ID = SA.MESSAGE_ID;
        TYPE T_SEND_ATTEMPT IS TABLE OF c_send_attempt%ROWTYPE;
        send_attempt T_SEND_ATTEMPT;
        v_batch_no   INT          := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_send_attempt;
        LOOP
            FETCH c_send_attempt BULK COLLECT INTO send_attempt;
            EXIT WHEN send_attempt.COUNT = 0;

            FOR i IN send_attempt.FIRST .. send_attempt.LAST
            LOOP
                BEGIN
                    v_id_pk := generate_scalable_seq(send_attempt(i).ID_PK, send_attempt(i).CREATION_TIME);
                    update_migration_pks('send_attempt', send_attempt(i).ID_PK, v_id_pk, migration_pks);
                    lookup_migration_pk('user_message', migration_pks, send_attempt(i).USER_MESSAGE_ID_FK, v_user_message_id_fk);

                    EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                      ' (ID_PK, START_DATE, END_DATE, STATUS, ERROR, USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                      'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10)'
                    USING v_id_pk,
                            send_attempt(i).START_DATE,
                            send_attempt(i).END_DATE,
                            send_attempt(i).STATUS,
                            send_attempt(i).ERROR,
                            v_user_message_id_fk,
                            send_attempt(i).CREATION_TIME,
                            send_attempt(i).CREATED_BY,
                            send_attempt(i).MODIFICATION_TIME,
                            send_attempt(i).MODIFIED_BY;

                    IF i MOD BATCH_SIZE = 0 THEN
                        COMMIT;
                        DBMS_OUTPUT.PUT_LINE(
                                v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                        v_batch_no := v_batch_no + 1;
                    END IF;
                EXCEPTION
                    WHEN OTHERS THEN
                        DBMS_OUTPUT.PUT_LINE('migrate_send_attempt -> execute immediate error: ' ||
                                             DBMS_UTILITY.FORMAT_ERROR_STACK);
                END;
            END LOOP;
            DBMS_OUTPUT.PUT_LINE(
                        'Migrated ' || send_attempt.COUNT || ' records in total into ' ||
                        v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_send_attempt;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_send_attempt;

    PROCEDURE migrate_action_audit(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_ACTION_AUDIT';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_ACTION_AUDIT';

        v_id_pk NUMBER;

        CURSOR c_action_audit IS
            SELECT AA.ID_PK,
                   AA.AUDIT_TYPE,
                   AA.ENTITY_ID,
                   AA.MODIFICATION_TYPE,
                   AA.REVISION_DATE,
                   AA.USER_NAME,
                   AA.FROM_QUEUE,
                   AA.TO_QUEUE,
                   AA.CREATION_TIME,
                   AA.CREATED_BY,
                   AA.MODIFICATION_TIME,
                   AA.MODIFIED_BY
            FROM TB_ACTION_AUDIT AA;
        TYPE T_ACTION_AUDIT IS TABLE OF c_action_audit%rowtype;
        action_audit T_ACTION_AUDIT;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_action_audit;
        LOOP
            FETCH c_action_audit BULK COLLECT INTO action_audit;
            EXIT WHEN action_audit.COUNT = 0;

            FOR i IN action_audit.FIRST .. action_audit.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(action_audit(i).ID_PK, action_audit(i).CREATION_TIME);
                        update_migration_pks('action_audit', action_audit(i).ID_PK, v_id_pk, migration_pks);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, AUDIT_TYPE, ENTITY_ID, MODIFICATION_TYPE, REVISION_DATE, USER_NAME, FROM_QUEUE, TO_QUEUE, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11,:p_12)'
                        USING v_id_pk,
                                action_audit(i).AUDIT_TYPE,
                                action_audit(i).ENTITY_ID,
                                action_audit(i).MODIFICATION_TYPE,
                                action_audit(i).REVISION_DATE,
                                action_audit(i).USER_NAME,
                                action_audit(i).FROM_QUEUE,
                                action_audit(i).TO_QUEUE,
                                action_audit(i).CREATION_TIME,
                                action_audit(i).CREATED_BY,
                                action_audit(i).MODIFICATION_TIME,
                                action_audit(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_action_audit -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || action_audit.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_action_audit;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_action_audit;

    PROCEDURE migrate_alert(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_ALERT';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_ALERT';

        v_id_pk NUMBER;
        v_fk_timezone_offset NUMBER;

        CURSOR c_alert IS
            SELECT A.ID_PK,
                   A.ALERT_TYPE,
                   A.ATTEMPTS_NUMBER,
                   A.MAX_ATTEMPTS_NUMBER,
                   A.PROCESSED,
                   A.PROCESSED_TIME,
                   A.REPORTING_TIME,
                   A.REPORTING_TIME_FAILURE,
                   A.NEXT_ATTEMPT,
                   A.ALERT_STATUS,
                   A.ALERT_LEVEL,
                   A.CREATION_TIME,
                   A.CREATED_BY,
                   A.MODIFICATION_TIME,
                   A.MODIFIED_BY
            FROM TB_ALERT A;
        TYPE T_ALERT IS TABLE OF c_alert%rowtype;
        alert T_ALERT;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_alert;
        LOOP
            FETCH c_alert BULK COLLECT INTO alert;
            EXIT WHEN alert.COUNT = 0;

            FOR i IN alert.FIRST .. alert.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(alert(i).ID_PK, alert(i).CREATION_TIME);
                        update_migration_pks('alert', alert(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('timezone_offset', migration_pks, 1, v_fk_timezone_offset);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, ALERT_TYPE, ATTEMPTS_NUMBER, MAX_ATTEMPTS_NUMBER, PROCESSED, PROCESSED_TIME, REPORTING_TIME, REPORTING_TIME_FAILURE, NEXT_ATTEMPT, FK_TIMEZONE_OFFSET, ALERT_STATUS, ALERT_LEVEL, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11,:p_12,:p_13,:p_14,:p_15,:p_16)'
                        USING v_id_pk,
                                alert(i).ALERT_TYPE,
                                alert(i).ATTEMPTS_NUMBER,
                                alert(i).MAX_ATTEMPTS_NUMBER,
                                alert(i).PROCESSED,
                                alert(i).PROCESSED_TIME,
                                alert(i).REPORTING_TIME,
                                alert(i).REPORTING_TIME_FAILURE,
                                alert(i).NEXT_ATTEMPT,
                                v_fk_timezone_offset,
                                alert(i).ALERT_STATUS,
                                alert(i).ALERT_LEVEL,
                                alert(i).CREATION_TIME,
                                alert(i).CREATED_BY,
                                alert(i).MODIFICATION_TIME,
                                alert(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_alert -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || alert.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_alert;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_alert;

    PROCEDURE migrate_event(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_EVENT';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_EVENT';

        v_id_pk NUMBER;

        CURSOR c_event IS
            SELECT E.ID_PK,
                   E.EVENT_TYPE,
                   E.REPORTING_TIME,
                   E.LAST_ALERT_DATE,
                   E.CREATION_TIME,
                   E.CREATED_BY,
                   E.MODIFICATION_TIME,
                   E.MODIFIED_BY
            FROM TB_EVENT E;
        TYPE T_EVENT IS TABLE OF c_event%rowtype;
        event T_EVENT;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_event;
        LOOP
            FETCH c_event BULK COLLECT INTO event;
            EXIT WHEN event.COUNT = 0;

            FOR i IN event.FIRST .. event.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(event(i).ID_PK, event(i).CREATION_TIME);
                        update_migration_pks('event', event(i).ID_PK, v_id_pk, migration_pks);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, EVENT_TYPE, REPORTING_TIME, LAST_ALERT_DATE, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8)'
                        USING v_id_pk,
                                event(i).EVENT_TYPE,
                                event(i).REPORTING_TIME,
                                event(i).LAST_ALERT_DATE,
                                event(i).CREATION_TIME,
                                event(i).CREATED_BY,
                                event(i).MODIFICATION_TIME,
                                event(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_event -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || event.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_event;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_event;

    PROCEDURE migrate_event_alert(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_EVENT_ALERT';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_EVENT_ALERT';

        v_fk_event NUMBER;
        v_fk_alert NUMBER;

        CURSOR c_event_alert IS
            SELECT EA.FK_EVENT,
                   EA.FK_ALERT,
                   EA.CREATION_TIME,
                   EA.CREATED_BY,
                   EA.MODIFICATION_TIME,
                   EA.MODIFIED_BY
            FROM TB_EVENT_ALERT EA;
        TYPE T_EVENT_ALERT IS TABLE OF c_event_alert%rowtype;
        event_alert T_EVENT_ALERT;
        v_batch_no   INT          := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_event_alert;
        LOOP
            FETCH c_event_alert BULK COLLECT INTO event_alert;
            EXIT WHEN event_alert.COUNT = 0;

            FOR i IN event_alert.FIRST .. event_alert.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('alert', migration_pks, event_alert(i).FK_EVENT, v_fk_alert);
                        lookup_migration_pk('event', migration_pks, event_alert(i).FK_ALERT, v_fk_event);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (FK_EVENT, FK_ALERT, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6)'
                        USING v_fk_event,
                                v_fk_alert,
                                event_alert(i).CREATION_TIME,
                                event_alert(i).CREATED_BY,
                                event_alert(i).MODIFICATION_TIME,
                                event_alert(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_event_alert -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || event_alert.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_event_alert;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_event_alert;

    PROCEDURE migrate_event_property(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_EVENT_PROPERTY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_EVENT_PROPERTY';

        v_id_pk NUMBER;
        v_fk_event NUMBER;

        CURSOR c_event_property IS
            SELECT EP.ID_PK,
                   EP.PROPERTY_TYPE,
                   EP.FK_EVENT,
                   EP.DTYPE,
                   EP.STRING_VALUE,
                   EP.DATE_VALUE,
                   EP.CREATION_TIME,
                   EP.CREATED_BY,
                   EP.MODIFICATION_TIME,
                   EP.MODIFIED_BY
            FROM TB_EVENT_PROPERTY EP;
        TYPE T_EVENT_PROPERTY IS TABLE OF c_event_property%rowtype;
        event_property T_EVENT_PROPERTY;
        v_batch_no   INT          := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_event_property;
        LOOP
            FETCH c_event_property BULK COLLECT INTO event_property;
            EXIT WHEN event_property.COUNT = 0;

            FOR i IN event_property.FIRST .. event_property.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(event_property(i).ID_PK, event_property(i).CREATION_TIME);
                        lookup_migration_pk('event', migration_pks, event_property(i).FK_EVENT, v_fk_event);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, PROPERTY_TYPE, FK_EVENT, DTYPE, STRING_VALUE, DATE_VALUE, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10)'
                        USING v_id_pk,
                                event_property(i).PROPERTY_TYPE,
                                v_fk_event,
                                event_property(i).DTYPE,
                                event_property(i).STRING_VALUE,
                                event_property(i).DATE_VALUE,
                                event_property(i).CREATION_TIME,
                                event_property(i).CREATED_BY,
                                event_property(i).MODIFICATION_TIME,
                                event_property(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_event_property -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || event_property.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_event_property;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_event_property;

    PROCEDURE migrate_authentication_entry(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_AUTHENTICATION_ENTRY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_AUTHENTICATION_ENTRY';

        v_id_pk NUMBER;

        CURSOR c_authentication_entry IS
            SELECT AE.ID_PK,
                   AE.CERTIFICATE_ID,
                   AE.USERNAME,
                   AE.PASSWD,
                   AE.AUTH_ROLES,
                   AE.ORIGINAL_USER,
                   AE.BACKEND,
                   AE.PASSWORD_CHANGE_DATE,
                   AE.DEFAULT_PASSWORD,
                   AE.ATTEMPT_COUNT,
                   AE.SUSPENSION_DATE,
                   AE.USER_ENABLED,
                   AE.CREATION_TIME,
                   AE.CREATED_BY,
                   AE.MODIFICATION_TIME,
                   AE.MODIFIED_BY
            FROM TB_AUTHENTICATION_ENTRY AE;
        TYPE T_AUTHENTICATION_ENTRY IS TABLE OF c_authentication_entry%rowtype;
        authentication_entry T_AUTHENTICATION_ENTRY;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_authentication_entry;
        LOOP
            FETCH c_authentication_entry BULK COLLECT INTO authentication_entry;
            EXIT WHEN authentication_entry.COUNT = 0;

            FOR i IN authentication_entry.FIRST .. authentication_entry.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(authentication_entry(i).ID_PK, authentication_entry(i).CREATION_TIME);
                        update_migration_pks('authentication_entry', authentication_entry(i).ID_PK, v_id_pk, migration_pks);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, CERTIFICATE_ID, USERNAME, PASSWD, AUTH_ROLES, ORIGINAL_USER, BACKEND, PASSWORD_CHANGE_DATE, DEFAULT_PASSWORD, ATTEMPT_COUNT, SUSPENSION_DATE, USER_ENABLED, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11,:p_12,:p_13,:p_14,:p_15,:p_16)'
                        USING v_id_pk,
                                authentication_entry(i).CERTIFICATE_ID,
                                authentication_entry(i).USERNAME,
                                authentication_entry(i).PASSWD,
                                authentication_entry(i).AUTH_ROLES,
                                authentication_entry(i).ORIGINAL_USER,
                                authentication_entry(i).BACKEND,
                                authentication_entry(i).PASSWORD_CHANGE_DATE,
                                authentication_entry(i).DEFAULT_PASSWORD,
                                authentication_entry(i).ATTEMPT_COUNT,
                                authentication_entry(i).SUSPENSION_DATE,
                                authentication_entry(i).USER_ENABLED,
                                authentication_entry(i).CREATION_TIME,
                                authentication_entry(i).CREATED_BY,
                                authentication_entry(i).MODIFICATION_TIME,
                                authentication_entry(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_authentication_entry -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || authentication_entry.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_authentication_entry;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;

    END migrate_authentication_entry;

    PROCEDURE migrate_plugin_user_passwd_history(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PLUGIN_USER_PASSWD_HISTORY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PLUGIN_USR_PASSWD_HIST';

        v_id_pk NUMBER;
        v_user_id NUMBER;

        CURSOR c_plugin_user_passwd_history IS
            SELECT PUPH.ID_PK,
                   PUPH.USER_ID,
                   PUPH.USER_PASSWORD,
                   PUPH.PASSWORD_CHANGE_DATE,
                   PUPH.CREATION_TIME,
                   PUPH.CREATED_BY,
                   PUPH.MODIFICATION_TIME,
                   PUPH.MODIFIED_BY
            FROM TB_PLUGIN_USER_PASSWD_HISTORY PUPH;
        TYPE T_PLUGIN_USER_PASSWD_HISTORY IS TABLE OF c_plugin_user_passwd_history%rowtype;
        plugin_user_passwd_history T_PLUGIN_USER_PASSWD_HISTORY;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_plugin_user_passwd_history;
        LOOP
            FETCH c_plugin_user_passwd_history BULK COLLECT INTO plugin_user_passwd_history;
            EXIT WHEN plugin_user_passwd_history.COUNT = 0;

            FOR i IN plugin_user_passwd_history.FIRST .. plugin_user_passwd_history.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(plugin_user_passwd_history(i).ID_PK, plugin_user_passwd_history(i).CREATION_TIME);
                        lookup_migration_pk('authentication_entry', migration_pks, plugin_user_passwd_history(i).USER_ID, v_user_id);


                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, USER_ID, USER_PASSWORD, PASSWORD_CHANGE_DATE, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8)'
                        USING v_id_pk,
                                v_user_id,
                                plugin_user_passwd_history(i).USER_PASSWORD,
                                plugin_user_passwd_history(i).PASSWORD_CHANGE_DATE,
                                plugin_user_passwd_history(i).CREATION_TIME,
                                plugin_user_passwd_history(i).CREATED_BY,
                                plugin_user_passwd_history(i).MODIFICATION_TIME,
                                plugin_user_passwd_history(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || 'records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_plugin_user_passwd_history -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || plugin_user_passwd_history.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_plugin_user_passwd_history;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_plugin_user_passwd_history;

    PROCEDURE migrate_backend_filter(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_BACKEND_FILTER';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_BACKEND_FILTER';

        v_id_pk NUMBER;

        CURSOR c_backend_filter IS
            SELECT BF.ID_PK,
                   BF.BACKEND_NAME,
                   BF.PRIORITY,
                   BF.CREATION_TIME,
                   BF.CREATED_BY,
                   BF.MODIFICATION_TIME,
                   BF.MODIFIED_BY
            FROM TB_BACKEND_FILTER BF;
        TYPE T_BACKEND_FILTER IS TABLE OF c_backend_filter%rowtype;
        backend_filter T_BACKEND_FILTER;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_backend_filter;
        LOOP
            FETCH c_backend_filter BULK COLLECT INTO backend_filter;
            EXIT WHEN backend_filter.COUNT = 0;

            FOR i IN backend_filter.FIRST .. backend_filter.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(backend_filter(i).ID_PK, backend_filter(i).CREATION_TIME);
                        update_migration_pks('backend_filter', backend_filter(i).ID_PK, v_id_pk, migration_pks);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, BACKEND_NAME, PRIORITY, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7)'
                        USING v_id_pk,
                                backend_filter(i).BACKEND_NAME,
                                backend_filter(i).PRIORITY,
                                backend_filter(i).CREATION_TIME,
                                backend_filter(i).CREATED_BY,
                                backend_filter(i).MODIFICATION_TIME,
                                backend_filter(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_backend_filter -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || backend_filter.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_backend_filter;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_backend_filter;

    PROCEDURE migrate_routing_criteria(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_ROUTING_CRITERIA';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_ROUTING_CRITERIA';

        v_id_pk NUMBER;
        v_fk_backend_filter NUMBER;

        CURSOR c_routing_criteria IS
            SELECT RC.ID_PK,
                   RC.EXPRESSION,
                   RC.NAME,
                   RC.FK_BACKEND_FILTER,
                   RC.PRIORITY,
                   RC.CREATION_TIME,
                   RC.CREATED_BY,
                   RC.MODIFICATION_TIME,
                   RC.MODIFIED_BY
            FROM TB_ROUTING_CRITERIA RC;
        TYPE T_ROUTING_CRITERIA IS TABLE OF c_routing_criteria%rowtype;
        routing_criteria T_ROUTING_CRITERIA;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_routing_criteria;
        LOOP
            FETCH c_routing_criteria BULK COLLECT INTO routing_criteria;
            EXIT WHEN routing_criteria.COUNT = 0;

            FOR i IN routing_criteria.FIRST .. routing_criteria.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(routing_criteria(i).ID_PK, routing_criteria(i).CREATION_TIME);
                        update_migration_pks('routing_criteria', routing_criteria(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('backend_filter', migration_pks, routing_criteria(i).FK_BACKEND_FILTER, v_fk_backend_filter);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, EXPRESSION, NAME, FK_BACKEND_FILTER, PRIORITY, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9)'
                        USING v_id_pk,
                                routing_criteria(i).EXPRESSION,
                                routing_criteria(i).NAME,
                                v_fk_backend_filter,
                                routing_criteria(i).PRIORITY,
                                routing_criteria(i).CREATION_TIME,
                                routing_criteria(i).CREATED_BY,
                                routing_criteria(i).MODIFICATION_TIME,
                                routing_criteria(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_routing_criteria -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || routing_criteria.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_routing_criteria;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_routing_criteria;

    PROCEDURE migrate_certificate(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_CERTIFICATE';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_CERTIFICATE';

        v_id_pk NUMBER;

        CURSOR c_certificate IS
            SELECT C.ID_PK,
                   C.CERTIFICATE_ALIAS,
                   C.NOT_VALID_BEFORE_DATE,
                   C.NOT_VALID_AFTER_DATE,
                   C.REVOKE_NOTIFICATION_DATE,
                   C.ALERT_IMM_NOTIFICATION_DATE,
                   C.ALERT_EXP_NOTIFICATION_DATE,
                   C.CERTIFICATE_STATUS,
                   C.CERTIFICATE_TYPE,
                   C.CREATION_TIME,
                   C.CREATED_BY,
                   C.MODIFICATION_TIME,
                   C.MODIFIED_BY
            FROM TB_CERTIFICATE C;
        TYPE T_CERTIFICATE IS TABLE OF c_certificate%rowtype;
        certificate T_CERTIFICATE;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_certificate;
        LOOP
            FETCH c_certificate BULK COLLECT INTO certificate;
            EXIT WHEN certificate.COUNT = 0;

            FOR i IN certificate.FIRST .. certificate.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(certificate(i).ID_PK, certificate(i).CREATION_TIME);
                        update_migration_pks('certificate', certificate(i).ID_PK, v_id_pk, migration_pks);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, CERTIFICATE_ALIAS, NOT_VALID_BEFORE_DATE, NOT_VALID_AFTER_DATE, REVOKE_NOTIFICATION_DATE, ALERT_IMM_NOTIFICATION_DATE, ALERT_EXP_NOTIFICATION_DATE, CERTIFICATE_STATUS, CERTIFICATE_TYPE, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11,:p_12,:p_13)'
                        USING v_id_pk,
                                certificate(i).CERTIFICATE_ALIAS,
                                certificate(i).NOT_VALID_BEFORE_DATE,
                                certificate(i).NOT_VALID_AFTER_DATE,
                                certificate(i).REVOKE_NOTIFICATION_DATE,
                                certificate(i).ALERT_IMM_NOTIFICATION_DATE,
                                certificate(i).ALERT_EXP_NOTIFICATION_DATE,
                                certificate(i).CERTIFICATE_STATUS,
                                certificate(i).CERTIFICATE_TYPE,
                                certificate(i).CREATION_TIME,
                                certificate(i).CREATED_BY,
                                certificate(i).MODIFICATION_TIME,
                                certificate(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_certificate -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || certificate.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_certificate;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_certificate;

    PROCEDURE migrate_command(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_COMMAND';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_COMMAND';

        v_id_pk NUMBER;

        CURSOR c_command IS
            SELECT C.ID_PK,
                   C.SERVER_NAME,
                   C.COMMAND_NAME,
                   C.CREATION_TIME,
                   C.CREATED_BY,
                   C.MODIFICATION_TIME,
                   C.MODIFIED_BY
            FROM TB_COMMAND C;
        TYPE T_COMMAND IS TABLE OF c_command%rowtype;
        command T_COMMAND;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_command;
        LOOP
            FETCH c_command BULK COLLECT INTO command;
            EXIT WHEN command.COUNT = 0;

            FOR i IN command.FIRST .. command.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(command(i).ID_PK, command(i).CREATION_TIME);
                        update_migration_pks('command', command(i).ID_PK, v_id_pk, migration_pks);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, SERVER_NAME, COMMAND_NAME, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7)'
                        USING v_id_pk,
                                command(i).SERVER_NAME,
                                command(i).COMMAND_NAME,
                                command(i).CREATION_TIME,
                                command(i).CREATED_BY,
                                command(i).MODIFICATION_TIME,
                                command(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_command -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || command.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_command;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_command;

    PROCEDURE migrate_command_property(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_COMMAND_PROPERTY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_COMMAND_PROPERTY';

        v_fk_command NUMBER;

        CURSOR c_command_property IS
            SELECT CP.PROPERTY_NAME,
                   CP.PROPERTY_VALUE,
                   CP.FK_COMMAND,
                   CP.CREATION_TIME,
                   CP.CREATED_BY,
                   CP.MODIFICATION_TIME,
                   CP.MODIFIED_BY
            FROM TB_COMMAND_PROPERTY CP;
        TYPE T_COMMAND_PROPERTY IS TABLE OF c_command_property%rowtype;
        command_property T_COMMAND_PROPERTY;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_command_property;
        LOOP
            FETCH c_command_property BULK COLLECT INTO command_property;
            EXIT WHEN command_property.COUNT = 0;

            FOR i IN command_property.FIRST .. command_property.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('command', migration_pks, command_property(i).FK_COMMAND, v_fk_command);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (PROPERTY_NAME, PROPERTY_VALUE, FK_COMMAND, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7)'
                        USING command_property(i).PROPERTY_NAME,
                                command_property(i).PROPERTY_VALUE,
                                v_fk_command,
                                command_property(i).CREATION_TIME,
                                command_property(i).CREATED_BY,
                                command_property(i).MODIFICATION_TIME,
                                command_property(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_command_property -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || command_property.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_command_property;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_command_property;

    PROCEDURE migrate_encryption_key IS
        v_tab VARCHAR2(30) := 'TB_ENCRYPTION_KEY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_ENCRYPTION_KEY';

        v_id_pk NUMBER;

        CURSOR c_encryption_key IS
            SELECT EK.ID_PK,
                   EK.KEY_USAGE,
                   EK.SECRET_KEY,
                   EK.INIT_VECTOR,
                   EK.CREATION_TIME,
                   EK.CREATED_BY,
                   EK.MODIFICATION_TIME,
                   EK.MODIFIED_BY
            FROM TB_ENCRYPTION_KEY EK;
        TYPE T_ENCRYPTION_KEY IS TABLE OF c_encryption_key%rowtype;
        encryption_key T_ENCRYPTION_KEY;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_encryption_key;
        LOOP
            FETCH c_encryption_key BULK COLLECT INTO encryption_key;
            EXIT WHEN encryption_key.COUNT = 0;

            FOR i IN encryption_key.FIRST .. encryption_key.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(encryption_key(i).ID_PK, encryption_key(i).CREATION_TIME);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, KEY_USAGE, SECRET_KEY, INIT_VECTOR, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8)'
                        USING v_id_pk,
                                encryption_key(i).KEY_USAGE,
                                encryption_key(i).SECRET_KEY,
                                encryption_key(i).INIT_VECTOR,
                                encryption_key(i).CREATION_TIME,
                                encryption_key(i).CREATED_BY,
                                encryption_key(i).MODIFICATION_TIME,
                                encryption_key(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_encryption_key -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || encryption_key.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_encryption_key;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_encryption_key;

    PROCEDURE migrate_message_acknw_prop(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_MESSAGE_ACKNW_PROP';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_MESSAGE_ACKNW_PROP';

        v_id_pk NUMBER;
        v_fk_msg_acknowledge NUMBER;

        CURSOR c_message_acknw_prop IS
            SELECT MAP.ID_PK,
                   MAP.PROPERTY_NAME,
                   MAP.PROPERTY_VALUE,
                   MAP.FK_MSG_ACKNOWLEDGE,
                   MAP.CREATION_TIME,
                   MAP.CREATED_BY,
                   MAP.MODIFICATION_TIME,
                   MAP.MODIFIED_BY
            FROM TB_MESSAGE_ACKNW_PROP MAP;
        TYPE T_MESSAGE_ACKNW_PROP IS TABLE OF c_message_acknw_prop%rowtype;
        message_acknw_prop T_MESSAGE_ACKNW_PROP;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_message_acknw_prop;
        LOOP
            FETCH c_message_acknw_prop BULK COLLECT INTO message_acknw_prop;
            EXIT WHEN message_acknw_prop.COUNT = 0;

            FOR i IN message_acknw_prop.FIRST .. message_acknw_prop.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(message_acknw_prop(i).ID_PK, message_acknw_prop(i).CREATION_TIME);
                        lookup_migration_pk('message_acknw', migration_pks, message_acknw_prop(i).FK_MSG_ACKNOWLEDGE, v_fk_msg_acknowledge);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, PROPERTY_NAME, PROPERTY_VALUE, FK_MSG_ACKNOWLEDGE, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8)'
                        USING v_id_pk,
                                message_acknw_prop(i).PROPERTY_NAME,
                                message_acknw_prop(i).PROPERTY_VALUE,
                                v_fk_msg_acknowledge,
                                message_acknw_prop(i).CREATION_TIME,
                                message_acknw_prop(i).CREATED_BY,
                                message_acknw_prop(i).MODIFICATION_TIME,
                                message_acknw_prop(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_message_acknw_prop -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || message_acknw_prop.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_message_acknw_prop;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_message_acknw_prop;

    PROCEDURE migrate_messaging_lock(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_MESSAGING_LOCK';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_MESSAGING_LOCK';

        v_id_pk NUMBER;
        v_fk_timezone_offset NUMBER;

        CURSOR c_messaging_lock IS
            SELECT ML.ID_PK,
                   ML.MESSAGE_TYPE,
                   ML.MESSAGE_RECEIVED,
                   ML.MESSAGE_STATE,
                   ML.MESSAGE_ID,
                   ML.INITIATOR,
                   ML.MPC,
                   ML.SEND_ATTEMPTS,
                   ML.SEND_ATTEMPTS_MAX,
                   ML.NEXT_ATTEMPT,
                   ML.MESSAGE_STALED,
                   ML.CREATION_TIME,
                   ML.CREATED_BY,
                   ML.MODIFICATION_TIME,
                   ML.MODIFIED_BY
            FROM TB_MESSAGING_LOCK ML;
        TYPE T_MESSAGING_LOCK IS TABLE OF c_messaging_lock%rowtype;
        messaging_lock T_MESSAGING_LOCK;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_messaging_lock;
        LOOP
            FETCH c_messaging_lock BULK COLLECT INTO messaging_lock;
            EXIT WHEN messaging_lock.COUNT = 0;

            FOR i IN messaging_lock.FIRST .. messaging_lock.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(messaging_lock(i).ID_PK, messaging_lock(i).CREATION_TIME);
                        lookup_migration_pk('timezone_offset', migration_pks, 1, v_fk_timezone_offset);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, MESSAGE_TYPE, MESSAGE_RECEIVED, MESSAGE_STATE, MESSAGE_ID, INITIATOR, MPC, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, NEXT_ATTEMPT, FK_TIMEZONE_OFFSET, MESSAGE_STALED, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11,:p_12,:p_13,:p_14,:p_15,:p_16)'
                        USING v_id_pk,
                                messaging_lock(i).MESSAGE_TYPE,
                                messaging_lock(i).MESSAGE_RECEIVED,
                                messaging_lock(i).MESSAGE_STATE,
                                messaging_lock(i).MESSAGE_ID,
                                messaging_lock(i).INITIATOR,
                                messaging_lock(i).MPC,
                                messaging_lock(i).SEND_ATTEMPTS,
                                messaging_lock(i).SEND_ATTEMPTS_MAX,
                                messaging_lock(i).NEXT_ATTEMPT,
                                v_fk_timezone_offset,
                                messaging_lock(i).MESSAGE_STALED,
                                messaging_lock(i).CREATION_TIME,
                                messaging_lock(i).CREATED_BY,
                                messaging_lock(i).MODIFICATION_TIME,
                                messaging_lock(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_messaging_lock -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || messaging_lock.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_messaging_lock;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_messaging_lock;

    PROCEDURE migrate_pm_business_process(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_BUSINESS_PROCESS';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_BUSINESS_PROCESS';

        v_id_pk NUMBER;

        CURSOR c_pm_business_process IS
            SELECT PBP.ID_PK,
                   PBP.CREATION_TIME,
                   PBP.CREATED_BY,
                   PBP.MODIFICATION_TIME,
                   PBP.MODIFIED_BY
            FROM TB_PM_BUSINESS_PROCESS PBP;
        TYPE T_PM_BUSINESS_PROCESS IS TABLE OF c_pm_business_process%rowtype;
        pm_business_process T_PM_BUSINESS_PROCESS;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_business_process;
        LOOP
            FETCH c_pm_business_process BULK COLLECT INTO pm_business_process;
            EXIT WHEN pm_business_process.COUNT = 0;

            FOR i IN pm_business_process.FIRST .. pm_business_process.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_business_process(i).ID_PK, pm_business_process(i).CREATION_TIME);
                        update_migration_pks('pm_business_process', pm_business_process(i).ID_PK, v_id_pk, migration_pks);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5)'
                        USING v_id_pk,
                                pm_business_process(i).CREATION_TIME,
                                pm_business_process(i).CREATED_BY,
                                pm_business_process(i).MODIFICATION_TIME,
                                pm_business_process(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_business_process -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_business_process.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_business_process;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_business_process;

    PROCEDURE migrate_pm_action(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_ACTION';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_ACTION';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_action IS
            SELECT PA.ID_PK,
                   PA.NAME,
                   PA.VALUE,
                   PA.FK_BUSINESSPROCESS,
                   PA.CREATION_TIME,
                   PA.CREATED_BY,
                   PA.MODIFICATION_TIME,
                   PA.MODIFIED_BY
            FROM TB_PM_ACTION PA;
        TYPE T_PM_ACTION IS TABLE OF c_pm_action%rowtype;
        pm_action T_PM_ACTION;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_action;
        LOOP
            FETCH c_pm_action BULK COLLECT INTO pm_action;
            EXIT WHEN pm_action.COUNT = 0;

            FOR i IN pm_action.FIRST .. pm_action.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_action(i).ID_PK, pm_action(i).CREATION_TIME);
                        update_migration_pks('pm_action', pm_action(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_action(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8)'
                        USING v_id_pk,
                                pm_action(i).NAME,
                                pm_action(i).VALUE,
                                v_fk_businessprocess,
                                pm_action(i).CREATION_TIME,
                                pm_action(i).CREATED_BY,
                                pm_action(i).MODIFICATION_TIME,
                                pm_action(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_action -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_action.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_action;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_action;

    PROCEDURE migrate_pm_agreement(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_AGREEMENT';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_AGREEMENT';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_agreement IS
            SELECT PA.ID_PK,
                   PA.NAME,
                   PA.TYPE,
                   PA.VALUE,
                   PA.FK_BUSINESSPROCESS,
                   PA.CREATION_TIME,
                   PA.CREATED_BY,
                   PA.MODIFICATION_TIME,
                   PA.MODIFIED_BY
            FROM TB_PM_AGREEMENT PA;
        TYPE T_PM_AGREEMENT IS TABLE OF c_pm_agreement%rowtype;
        pm_agreement T_PM_AGREEMENT;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_agreement;
        LOOP
            FETCH c_pm_agreement BULK COLLECT INTO pm_agreement;
            EXIT WHEN pm_agreement.COUNT = 0;

            FOR i IN pm_agreement.FIRST .. pm_agreement.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_agreement(i).ID_PK, pm_agreement(i).CREATION_TIME);
                        update_migration_pks('pm_agreement', pm_agreement(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_agreement(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, NAME, TYPE, VALUE, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9)'
                        USING v_id_pk,
                                pm_agreement(i).NAME,
                                pm_agreement(i).TYPE,
                                pm_agreement(i).VALUE,
                                v_fk_businessprocess,
                                pm_agreement(i).CREATION_TIME,
                                pm_agreement(i).CREATED_BY,
                                pm_agreement(i).MODIFICATION_TIME,
                                pm_agreement(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_agreement -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_agreement.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_agreement;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_agreement;

    PROCEDURE migrate_pm_error_handling(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_ERROR_HANDLING';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_ERROR_HANDLING';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_error_handling IS
            SELECT PEH.ID_PK,
                   PEH.BUSINESS_ERROR_NOTIFY_CONSUMER,
                   PEH.BUSINESS_ERROR_NOTIFY_PRODUCER,
                   PEH.DELIVERY_FAIL_NOTIFY_PRODUCER,
                   PEH.ERROR_AS_RESPONSE,
                   PEH.NAME,
                   PEH.FK_BUSINESSPROCESS,
                   PEH.CREATION_TIME,
                   PEH.CREATED_BY,
                   PEH.MODIFICATION_TIME,
                   PEH.MODIFIED_BY
            FROM TB_PM_ERROR_HANDLING PEH;
        TYPE T_PM_ERROR_HANDLING IS TABLE OF c_pm_error_handling%rowtype;
        pm_error_handling T_PM_ERROR_HANDLING;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_error_handling;
        LOOP
            FETCH c_pm_error_handling BULK COLLECT INTO pm_error_handling;
            EXIT WHEN pm_error_handling.COUNT = 0;

            FOR i IN pm_error_handling.FIRST .. pm_error_handling.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_error_handling(i).ID_PK, pm_error_handling(i).CREATION_TIME);
                        update_migration_pks('pm_error_handling', pm_error_handling(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_error_handling(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, BUSINESS_ERROR_NOTIFY_CONSUMER, BUSINESS_ERROR_NOTIFY_PRODUCER, DELIVERY_FAIL_NOTIFY_PRODUCER, ERROR_AS_RESPONSE, NAME, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11)'
                        USING v_id_pk,
                                pm_error_handling(i).BUSINESS_ERROR_NOTIFY_CONSUMER,
                                pm_error_handling(i).BUSINESS_ERROR_NOTIFY_PRODUCER,
                                pm_error_handling(i).DELIVERY_FAIL_NOTIFY_PRODUCER,
                                pm_error_handling(i).ERROR_AS_RESPONSE,
                                pm_error_handling(i).NAME,
                                v_fk_businessprocess,
                                pm_error_handling(i).CREATION_TIME,
                                pm_error_handling(i).CREATED_BY,
                                pm_error_handling(i).MODIFICATION_TIME,
                                pm_error_handling(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_error_handling -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_error_handling.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_error_handling;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_error_handling;

    PROCEDURE migrate_pm_mep(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_MEP';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_MEP';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_mep IS
            SELECT PM.ID_PK,
                   PM.LEG_COUNT,
                   PM.NAME,
                   PM.VALUE,
                   PM.FK_BUSINESSPROCESS,
                   PM.CREATION_TIME,
                   PM.CREATED_BY,
                   PM.MODIFICATION_TIME,
                   PM.MODIFIED_BY
            FROM TB_PM_MEP PM;
        TYPE T_PM_MEP IS TABLE OF c_pm_mep%rowtype;
        pm_mep T_PM_MEP;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_mep;
        LOOP
            FETCH c_pm_mep BULK COLLECT INTO pm_mep;
            EXIT WHEN pm_mep.COUNT = 0;

            FOR i IN pm_mep.FIRST .. pm_mep.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_mep(i).ID_PK, pm_mep(i).CREATION_TIME);
                        update_migration_pks('pm_mep', pm_mep(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_mep(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, LEG_COUNT, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9)'
                        USING v_id_pk,
                                pm_mep(i).LEG_COUNT,
                                pm_mep(i).NAME,
                                pm_mep(i).VALUE,
                                v_fk_businessprocess,
                                pm_mep(i).CREATION_TIME,
                                pm_mep(i).CREATED_BY,
                                pm_mep(i).MODIFICATION_TIME,
                                pm_mep(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_mep -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_mep.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_mep;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_mep;

    PROCEDURE migrate_pm_mep_binding(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_MEP_BINDING';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_MEP_BINDING';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_mep_binding IS
            SELECT PMB.ID_PK,
                   PMB.NAME,
                   PMB.VALUE,
                   PMB.FK_BUSINESSPROCESS,
                   PMB.CREATION_TIME,
                   PMB.CREATED_BY,
                   PMB.MODIFICATION_TIME,
                   PMB.MODIFIED_BY
            FROM TB_PM_MEP_BINDING PMB;
        TYPE T_PM_MEP_BINDING IS TABLE OF c_pm_mep_binding%rowtype;
        pm_mep_binding T_PM_MEP_BINDING;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_mep_binding;
        LOOP
            FETCH c_pm_mep_binding BULK COLLECT INTO pm_mep_binding;
            EXIT WHEN pm_mep_binding.COUNT = 0;

            FOR i IN pm_mep_binding.FIRST .. pm_mep_binding.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_mep_binding(i).ID_PK, pm_mep_binding(i).CREATION_TIME);
                        update_migration_pks('pm_mep_binding', pm_mep_binding(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_mep_binding(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8)'
                        USING v_id_pk,
                                pm_mep_binding(i).NAME,
                                pm_mep_binding(i).VALUE,
                                v_fk_businessprocess,
                                pm_mep_binding(i).CREATION_TIME,
                                pm_mep_binding(i).CREATED_BY,
                                pm_mep_binding(i).MODIFICATION_TIME,
                                pm_mep_binding(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_mep_binding -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_mep_binding.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_mep_binding;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_mep_binding;

    PROCEDURE migrate_pm_message_property(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_MESSAGE_PROPERTY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_MESSAGE_PROPERTY';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_message_property IS
            SELECT PMP.ID_PK,
                   PMP.DATATYPE,
                   PMP.KEY_,
                   PMP.NAME,
                   PMP.REQUIRED_,
                   PMP.FK_BUSINESSPROCESS,
                   PMP.CREATION_TIME,
                   PMP.CREATED_BY,
                   PMP.MODIFICATION_TIME,
                   PMP.MODIFIED_BY
            FROM TB_PM_MESSAGE_PROPERTY PMP;
        TYPE T_PM_MESSAGE_PROPERTY IS TABLE OF c_pm_message_property%rowtype;
        pm_message_property T_PM_MESSAGE_PROPERTY;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_message_property;
        LOOP
            FETCH c_pm_message_property BULK COLLECT INTO pm_message_property;
            EXIT WHEN pm_message_property.COUNT = 0;

            FOR i IN pm_message_property.FIRST .. pm_message_property.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_message_property(i).ID_PK, pm_message_property(i).CREATION_TIME);
                        update_migration_pks('pm_message_property', pm_message_property(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_message_property(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, DATATYPE, KEY_, NAME, REQUIRED_, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10)'
                        USING v_id_pk,
                                pm_message_property(i).DATATYPE,
                                pm_message_property(i).KEY_,
                                pm_message_property(i).NAME,
                                pm_message_property(i).REQUIRED_,
                                v_fk_businessprocess,
                                pm_message_property(i).CREATION_TIME,
                                pm_message_property(i).CREATED_BY,
                                pm_message_property(i).MODIFICATION_TIME,
                                pm_message_property(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_message_property -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_message_property.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_message_property;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_message_property;

    PROCEDURE migrate_pm_message_property_set(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_MESSAGE_PROPERTY_SET';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_MSG_PROPERTY_SET';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_message_property_set IS
            SELECT PMPS.ID_PK,
                   PMPS.NAME,
                   PMPS.FK_BUSINESSPROCESS,
                   PMPS.CREATION_TIME,
                   PMPS.CREATED_BY,
                   PMPS.MODIFICATION_TIME,
                   PMPS.MODIFIED_BY
            FROM TB_PM_MESSAGE_PROPERTY_SET PMPS;
        TYPE T_PM_MESSAGE_PROPERTY_SET IS TABLE OF c_pm_message_property_set%rowtype;
        pm_message_property_set T_PM_MESSAGE_PROPERTY_SET;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_message_property_set;
        LOOP
            FETCH c_pm_message_property_set BULK COLLECT INTO pm_message_property_set;
            EXIT WHEN pm_message_property_set.COUNT = 0;

            FOR i IN pm_message_property_set.FIRST .. pm_message_property_set.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_message_property_set(i).ID_PK, pm_message_property_set(i).CREATION_TIME);
                        update_migration_pks('pm_message_property_set', pm_message_property_set(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_message_property_set(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, NAME, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7)'
                        USING v_id_pk,
                                pm_message_property_set(i).NAME,
                                v_fk_businessprocess,
                                pm_message_property_set(i).CREATION_TIME,
                                pm_message_property_set(i).CREATED_BY,
                                pm_message_property_set(i).MODIFICATION_TIME,
                                pm_message_property_set(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_message_property_set -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_message_property_set.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_message_property_set;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_message_property_set;

    PROCEDURE migrate_pm_join_property_set(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_JOIN_PROPERTY_SET';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_JOIN_PROPERTY_SET';

        v_property_fk NUMBER;
        v_set_fk NUMBER;

        CURSOR c_pm_join_property_set IS
            SELECT PJPS.PROPERTY_FK,
                   PJPS.SET_FK,
                   PJPS.CREATION_TIME,
                   PJPS.CREATED_BY,
                   PJPS.MODIFICATION_TIME,
                   PJPS.MODIFIED_BY
            FROM TB_PM_JOIN_PROPERTY_SET PJPS;
        TYPE T_PM_JOIN_PROPERTY_SET IS TABLE OF c_pm_join_property_set%rowtype;
        pm_join_property_set T_PM_JOIN_PROPERTY_SET;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_join_property_set;
        LOOP
            FETCH c_pm_join_property_set BULK COLLECT INTO pm_join_property_set;
            EXIT WHEN pm_join_property_set.COUNT = 0;

            FOR i IN pm_join_property_set.FIRST .. pm_join_property_set.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('pm_message_property_set', migration_pks, pm_join_property_set(i).PROPERTY_FK, v_property_fk);
                        lookup_migration_pk('pm_message_property', migration_pks, pm_join_property_set(i).SET_FK, v_set_fk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (PROPERTY_FK, SET_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6)'
                        USING v_property_fk,
                                v_set_fk,
                                pm_join_property_set(i).CREATION_TIME,
                                pm_join_property_set(i).CREATED_BY,
                                pm_join_property_set(i).MODIFICATION_TIME,
                                pm_join_property_set(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_join_property_set -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_join_property_set.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_join_property_set;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_join_property_set;

    PROCEDURE migrate_pm_party(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_PARTY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PARTY';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_party IS
            SELECT PP.ID_PK,
                   PP.ENDPOINT,
                   PP.NAME,
                   PP.PASSWORD,
                   PP.USERNAME,
                   PP.FK_BUSINESSPROCESS,
                   PP.CREATION_TIME,
                   PP.CREATED_BY,
                   PP.MODIFICATION_TIME,
                   PP.MODIFIED_BY
            FROM TB_PM_PARTY PP;
        TYPE T_PM_PARTY IS TABLE OF c_pm_party%rowtype;
        pm_party T_PM_PARTY;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_party;
        LOOP
            FETCH c_pm_party BULK COLLECT INTO pm_party;
            EXIT WHEN pm_party.COUNT = 0;

            FOR i IN pm_party.FIRST .. pm_party.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_party(i).ID_PK, pm_party(i).CREATION_TIME);
                        update_migration_pks('pm_party', pm_party(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_party(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, ENDPOINT, NAME, PASSWORD, USERNAME, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10)'
                        USING v_id_pk,
                                pm_party(i).ENDPOINT,
                                pm_party(i).NAME,
                                pm_party(i).PASSWORD,
                                pm_party(i).USERNAME,
                                v_fk_businessprocess,
                                pm_party(i).CREATION_TIME,
                                pm_party(i).CREATED_BY,
                                pm_party(i).MODIFICATION_TIME,
                                pm_party(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_party -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_party.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_party;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_party;

    PROCEDURE migrate_pm_configuration(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_CONFIGURATION';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_CONFIGURATION';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;
        v_fk_party NUMBER;

        CURSOR c_pm_configuration IS
            SELECT PC.ID_PK,
                   PC.FK_BUSINESSPROCESSES,
                   PC.FK_PARTY,
                   PC.CREATION_TIME,
                   PC.CREATED_BY,
                   PC.MODIFICATION_TIME,
                   PC.MODIFIED_BY
            FROM TB_PM_CONFIGURATION PC;
        TYPE T_PM_CONFIGURATION IS TABLE OF c_pm_configuration%rowtype;
        pm_configuration T_PM_CONFIGURATION;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_configuration;
        LOOP
            FETCH c_pm_configuration BULK COLLECT INTO pm_configuration;
            EXIT WHEN pm_configuration.COUNT = 0;

            FOR i IN pm_configuration.FIRST .. pm_configuration.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_configuration(i).ID_PK, pm_configuration(i).CREATION_TIME);
                        update_migration_pks('pm_configuration', pm_configuration(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_configuration(i).FK_BUSINESSPROCESSES, v_fk_businessprocess);
                        lookup_migration_pk('pm_party', migration_pks, pm_configuration(i).FK_PARTY, v_fk_party);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, FK_BUSINESSPROCESSES, FK_PARTY, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7)'
                        USING v_id_pk,
                                v_fk_businessprocess,
                                v_fk_party,
                                pm_configuration(i).CREATION_TIME,
                                pm_configuration(i).CREATED_BY,
                                pm_configuration(i).MODIFICATION_TIME,
                                pm_configuration(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_configuration -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_configuration.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_configuration;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_configuration;

    PROCEDURE migrate_pm_mpc(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_MPC';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_MPC';

        v_id_pk NUMBER;
        v_fk_configuration NUMBER;

        CURSOR c_pm_mpc IS
            SELECT PM.ID_PK,
                   PM.DEFAULT_MPC,
                   PM.IS_ENABLED,
                   PM.NAME,
                   PM.QUALIFIED_NAME,
                   PM.RETENTION_DOWNLOADED,
                   PM.RETENTION_UNDOWNLOADED,
                   PM.RETENTION_SENT,
                   PM.DELETE_MESSAGE_METADATA,
                   PM.MAX_BATCH_DELETE,
                   PM.FK_CONFIGURATION,
                   PM.CREATION_TIME,
                   PM.CREATED_BY,
                   PM.MODIFICATION_TIME,
                   PM.MODIFIED_BY
            FROM TB_PM_MPC PM;
        TYPE T_PM_MPC IS TABLE OF c_pm_mpc%rowtype;
        pm_mpc T_PM_MPC;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_mpc;
        LOOP
            FETCH c_pm_mpc BULK COLLECT INTO pm_mpc;
            EXIT WHEN pm_mpc.COUNT = 0;

            FOR i IN pm_mpc.FIRST .. pm_mpc.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_mpc(i).ID_PK, pm_mpc(i).CREATION_TIME);
                        update_migration_pks('pm_mpc', pm_mpc(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_configuration', migration_pks, pm_mpc(i).FK_CONFIGURATION, v_fk_configuration);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, DEFAULT_MPC, IS_ENABLED, NAME, QUALIFIED_NAME, RETENTION_DOWNLOADED, RETENTION_UNDOWNLOADED, RETENTION_SENT, DELETE_MESSAGE_METADATA, MAX_BATCH_DELETE, FK_CONFIGURATION, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11,:p_12,:p_13,:p_14,:p_15)'
                        USING v_id_pk,
                                pm_mpc(i).DEFAULT_MPC,
                                pm_mpc(i).IS_ENABLED,
                                pm_mpc(i).NAME,
                                pm_mpc(i).QUALIFIED_NAME,
                                pm_mpc(i).RETENTION_DOWNLOADED,
                                pm_mpc(i).RETENTION_UNDOWNLOADED,
                                pm_mpc(i).RETENTION_SENT,
                                pm_mpc(i).DELETE_MESSAGE_METADATA,
                                pm_mpc(i).MAX_BATCH_DELETE,
                                v_fk_configuration,
                                pm_mpc(i).CREATION_TIME,
                                pm_mpc(i).CREATED_BY,
                                pm_mpc(i).MODIFICATION_TIME,
                                pm_mpc(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_mpc -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_mpc.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_mpc;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_mpc;

    PROCEDURE migrate_pm_party_id_type(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_PARTY_ID_TYPE';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PARTY_ID_TYPE';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_party_id_type IS
            SELECT PPIT.ID_PK,
                   PPIT.NAME,
                   PPIT.VALUE,
                   PPIT.FK_BUSINESSPROCESS,
                   PPIT.CREATION_TIME,
                   PPIT.CREATED_BY,
                   PPIT.MODIFICATION_TIME,
                   PPIT.MODIFIED_BY
            FROM TB_PM_PARTY_ID_TYPE PPIT;
        TYPE T_PM_PARTY_ID_TYPE IS TABLE OF c_pm_party_id_type%rowtype;
        pm_party_id_type T_PM_PARTY_ID_TYPE;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_party_id_type;
        LOOP
            FETCH c_pm_party_id_type BULK COLLECT INTO pm_party_id_type;
            EXIT WHEN pm_party_id_type.COUNT = 0;

            FOR i IN pm_party_id_type.FIRST .. pm_party_id_type.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_party_id_type(i).ID_PK, pm_party_id_type(i).CREATION_TIME);
                        update_migration_pks('pm_party_id_type', pm_party_id_type(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_party_id_type(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8)'
                        USING v_id_pk,
                                pm_party_id_type(i).NAME,
                                pm_party_id_type(i).VALUE,
                                v_fk_businessprocess,
                                pm_party_id_type(i).CREATION_TIME,
                                pm_party_id_type(i).CREATED_BY,
                                pm_party_id_type(i).MODIFICATION_TIME,
                                pm_party_id_type(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_party_id_type -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_party_id_type.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_party_id_type;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_party_id_type;

    PROCEDURE migrate_pm_party_identifier(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_PARTY_IDENTIFIER';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PARTY_IDENTIFIER';

        v_id_pk NUMBER;
        v_fk_party_id_type NUMBER;
        v_fk_party NUMBER;

        CURSOR c_pm_party_identifier IS
            SELECT PPI.ID_PK,
                   PPI.PARTY_ID,
                   PPI.FK_PARTY_ID_TYPE,
                   PPI.FK_PARTY,
                   PPI.CREATION_TIME,
                   PPI.CREATED_BY,
                   PPI.MODIFICATION_TIME,
                   PPI.MODIFIED_BY
            FROM TB_PM_PARTY_IDENTIFIER PPI;
        TYPE T_PM_PARTY_IDENTIFIER IS TABLE OF c_pm_party_identifier%rowtype;
        pm_party_identifier T_PM_PARTY_IDENTIFIER;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_party_identifier;
        LOOP
            FETCH c_pm_party_identifier BULK COLLECT INTO pm_party_identifier;
            EXIT WHEN pm_party_identifier.COUNT = 0;

            FOR i IN pm_party_identifier.FIRST .. pm_party_identifier.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_party_identifier(i).ID_PK, pm_party_identifier(i).CREATION_TIME);
                        update_migration_pks('pm_party_identifier', pm_party_identifier(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_party_id_type', migration_pks, pm_party_identifier(i).FK_PARTY_ID_TYPE, v_fk_party_id_type);
                        lookup_migration_pk('pm_party', migration_pks, pm_party_identifier(i).FK_PARTY, v_fk_party);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, PARTY_ID, FK_PARTY_ID_TYPE, FK_PARTY, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8)'
                        USING v_id_pk,
                                pm_party_identifier(i).PARTY_ID,
                                v_fk_party_id_type,
                                v_fk_party,
                                pm_party_identifier(i).CREATION_TIME,
                                pm_party_identifier(i).CREATED_BY,
                                pm_party_identifier(i).MODIFICATION_TIME,
                                pm_party_identifier(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_party_identifier -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_party_identifier.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_party_identifier;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_party_identifier;

    PROCEDURE migrate_pm_payload(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_PAYLOAD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PAYLOAD';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_payload IS
            SELECT PP.ID_PK,
                   PP.CID,
                   PP.IN_BODY,
                   PP.MAX_SIZE,
                   PP.MIME_TYPE,
                   PP.NAME,
                   PP.REQUIRED_,
                   PP.SCHEMA_FILE,
                   PP.FK_BUSINESSPROCESS,
                   PP.CREATION_TIME,
                   PP.CREATED_BY,
                   PP.MODIFICATION_TIME,
                   PP.MODIFIED_BY
            FROM TB_PM_PAYLOAD PP;
        TYPE T_PM_PAYLOAD IS TABLE OF c_pm_payload%rowtype;
        pm_payload T_PM_PAYLOAD;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_payload;
        LOOP
            FETCH c_pm_payload BULK COLLECT INTO pm_payload;
            EXIT WHEN pm_payload.COUNT = 0;

            FOR i IN pm_payload.FIRST .. pm_payload.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_payload(i).ID_PK, pm_payload(i).CREATION_TIME);
                        update_migration_pks('pm_payload', pm_payload(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_payload(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, CID, IN_BODY, MAX_SIZE, MIME_TYPE, NAME, REQUIRED_, SCHEMA_FILE, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11,:p_12,:p_13)'
                        USING v_id_pk,
                                pm_payload(i).CID,
                                pm_payload(i).IN_BODY,
                                pm_payload(i).MAX_SIZE,
                                pm_payload(i).MIME_TYPE,
                                pm_payload(i).NAME,
                                pm_payload(i).REQUIRED_,
                                pm_payload(i).SCHEMA_FILE,
                                v_fk_businessprocess,
                                pm_payload(i).CREATION_TIME,
                                pm_payload(i).CREATED_BY,
                                pm_payload(i).MODIFICATION_TIME,
                                pm_payload(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_payload -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_payload.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_payload;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_payload;

    PROCEDURE migrate_pm_payload_profile(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_PAYLOAD_PROFILE';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PAYLOAD_PROFILE';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_payload_profile IS
            SELECT PPP.ID_PK,
                   PPP.MAX_SIZE,
                   PPP.NAME,
                   PPP.FK_BUSINESSPROCESS,
                   PPP.CREATION_TIME,
                   PPP.CREATED_BY,
                   PPP.MODIFICATION_TIME,
                   PPP.MODIFIED_BY
            FROM TB_PM_PAYLOAD_PROFILE PPP;
        TYPE T_PM_PAYLOAD_PROFILE IS TABLE OF c_pm_payload_profile%rowtype;
        pm_payload_profile T_PM_PAYLOAD_PROFILE;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_payload_profile;
        LOOP
            FETCH c_pm_payload_profile BULK COLLECT INTO pm_payload_profile;
            EXIT WHEN pm_payload_profile.COUNT = 0;

            FOR i IN pm_payload_profile.FIRST .. pm_payload_profile.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_payload_profile(i).ID_PK, pm_payload_profile(i).CREATION_TIME);
                        update_migration_pks('pm_payload_profile', pm_payload_profile(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_payload_profile(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, MAX_SIZE, NAME, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8)'
                        USING v_id_pk,
                                pm_payload_profile(i).MAX_SIZE,
                                pm_payload_profile(i).NAME,
                                v_fk_businessprocess,
                                pm_payload_profile(i).CREATION_TIME,
                                pm_payload_profile(i).CREATED_BY,
                                pm_payload_profile(i).MODIFICATION_TIME,
                                pm_payload_profile(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_payload_profile -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_payload_profile.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;
    END migrate_pm_payload_profile;

    PROCEDURE migrate_pm_join_payload_profile(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_JOIN_PAYLOAD_PROFILE';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_JOIN_PAYLD_PROFILE';

        v_fk_payload NUMBER;
        v_fk_profile NUMBER;

        CURSOR c_pm_join_payload_profile IS
            SELECT PJPP.FK_PAYLOAD,
                   PJPP.FK_PROFILE,
                   PJPP.CREATION_TIME,
                   PJPP.CREATED_BY,
                   PJPP.MODIFICATION_TIME,
                   PJPP.MODIFIED_BY
            FROM TB_PM_JOIN_PAYLOAD_PROFILE PJPP;
        TYPE T_PM_JOIN_PAYLOAD_PROFILE IS TABLE OF c_pm_join_payload_profile%rowtype;
        pm_join_payload_profile T_PM_JOIN_PAYLOAD_PROFILE;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_join_payload_profile;
        LOOP
            FETCH c_pm_join_payload_profile BULK COLLECT INTO pm_join_payload_profile;
            EXIT WHEN pm_join_payload_profile.COUNT = 0;

            FOR i IN pm_join_payload_profile.FIRST .. pm_join_payload_profile.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('pm_payload', migration_pks, pm_join_payload_profile(i).FK_PAYLOAD, v_fk_payload);
                        lookup_migration_pk('pm_payload_profile', migration_pks, pm_join_payload_profile(i).FK_PROFILE, v_fk_profile);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (FK_PAYLOAD, FK_PROFILE, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6)'
                        USING v_fk_payload,
                                v_fk_profile,
                                pm_join_payload_profile(i).CREATION_TIME,
                                pm_join_payload_profile(i).CREATED_BY,
                                pm_join_payload_profile(i).MODIFICATION_TIME,
                                pm_join_payload_profile(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_join_payload_profile -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_join_payload_profile.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;
    END migrate_pm_join_payload_profile;

    PROCEDURE migrate_pm_reception_awareness(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_RECEPTION_AWARENESS';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_RECEPTION_AWARENESS';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_reception_awareness IS
            SELECT PRA.ID_PK,
                   PRA.DUPLICATE_DETECTION,
                   PRA.NAME,
                   PRA.RETRY_COUNT,
                   PRA.RETRY_TIMEOUT,
                   PRA.STRATEGY,
                   PRA.FK_BUSINESSPROCESS,
                   PRA.CREATION_TIME,
                   PRA.CREATED_BY,
                   PRA.MODIFICATION_TIME,
                   PRA.MODIFIED_BY
            FROM TB_PM_RECEPTION_AWARENESS PRA;
        TYPE T_PM_RECEPTION_AWARENESS IS TABLE OF c_pm_reception_awareness%rowtype;
        pm_reception_awareness T_PM_RECEPTION_AWARENESS;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_reception_awareness;
        LOOP
            FETCH c_pm_reception_awareness BULK COLLECT INTO pm_reception_awareness;
            EXIT WHEN pm_reception_awareness.COUNT = 0;

            FOR i IN pm_reception_awareness.FIRST .. pm_reception_awareness.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_reception_awareness(i).ID_PK, pm_reception_awareness(i).CREATION_TIME);
                        update_migration_pks('pm_reception_awareness', pm_reception_awareness(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_reception_awareness(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, DUPLICATE_DETECTION, NAME, RETRY_COUNT, RETRY_TIMEOUT, STRATEGY, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11)'
                        USING v_id_pk,
                                pm_reception_awareness(i).DUPLICATE_DETECTION,
                                pm_reception_awareness(i).NAME,
                                pm_reception_awareness(i).RETRY_COUNT,
                                pm_reception_awareness(i).RETRY_TIMEOUT,
                                pm_reception_awareness(i).STRATEGY,
                                v_fk_businessprocess,
                                pm_reception_awareness(i).CREATION_TIME,
                                pm_reception_awareness(i).CREATED_BY,
                                pm_reception_awareness(i).MODIFICATION_TIME,
                                pm_reception_awareness(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_reception_awareness -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_reception_awareness.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;
    END migrate_pm_reception_awareness;

    PROCEDURE migrate_pm_reliability(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_RELIABILITY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_RELIABILITY';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_reliability IS
            SELECT PR.ID_PK,
                   PR.NAME,
                   PR.NON_REPUDIATION,
                   PR.REPLY_PATTERN,
                   PR.FK_BUSINESSPROCESS,
                   PR.CREATION_TIME,
                   PR.CREATED_BY,
                   PR.MODIFICATION_TIME,
                   PR.MODIFIED_BY
            FROM TB_PM_RELIABILITY PR;
        TYPE T_PM_RELIABILITY IS TABLE OF c_pm_reliability%rowtype;
        pm_reliability T_PM_RELIABILITY;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_reliability;
        LOOP
            FETCH c_pm_reliability BULK COLLECT INTO pm_reliability;
            EXIT WHEN pm_reliability.COUNT = 0;

            FOR i IN pm_reliability.FIRST .. pm_reliability.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_reliability(i).ID_PK, pm_reliability(i).CREATION_TIME);
                        update_migration_pks('pm_reliability', pm_reliability(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_reliability(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, NAME, NON_REPUDIATION, REPLY_PATTERN, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9)'
                        USING v_id_pk,
                                pm_reliability(i).NAME,
                                pm_reliability(i).NON_REPUDIATION,
                                pm_reliability(i).REPLY_PATTERN,
                                v_fk_businessprocess,
                                pm_reliability(i).CREATION_TIME,
                                pm_reliability(i).CREATED_BY,
                                pm_reliability(i).MODIFICATION_TIME,
                                pm_reliability(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_reliability -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_reliability.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;
    END migrate_pm_reliability;

    PROCEDURE migrate_pm_role(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_ROLE';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_ROLE';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_role IS
            SELECT PR.ID_PK,
                   PR.NAME,
                   PR.VALUE,
                   PR.FK_BUSINESSPROCESS,
                   PR.CREATION_TIME,
                   PR.CREATED_BY,
                   PR.MODIFICATION_TIME,
                   PR.MODIFIED_BY
            FROM TB_PM_ROLE PR;
        TYPE T_PM_ROLE IS TABLE OF c_pm_role%rowtype;
        pm_role T_PM_ROLE;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_role;
        LOOP
            FETCH c_pm_role BULK COLLECT INTO pm_role;
            EXIT WHEN pm_role.COUNT = 0;

            FOR i IN pm_role.FIRST .. pm_role.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_role(i).ID_PK, pm_role(i).CREATION_TIME);
                        update_migration_pks('pm_role', pm_role(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_role(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8)'
                        USING v_id_pk,
                                pm_role(i).NAME,
                                pm_role(i).VALUE,
                                v_fk_businessprocess,
                                pm_role(i).CREATION_TIME,
                                pm_role(i).CREATED_BY,
                                pm_role(i).MODIFICATION_TIME,
                                pm_role(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_role -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_role.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;
    END migrate_pm_role;

    PROCEDURE migrate_pm_security(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_SECURITY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_SECURITY';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_security IS
            SELECT PS.ID_PK,
                   PS.NAME,
                   PS.POLICY,
                   PS.SIGNATURE_METHOD,
                   PS.FK_BUSINESSPROCESS,
                   PS.CREATION_TIME,
                   PS.CREATED_BY,
                   PS.MODIFICATION_TIME,
                   PS.MODIFIED_BY
            FROM TB_PM_SECURITY PS;
        TYPE T_PM_SECURITY IS TABLE OF c_pm_security%rowtype;
        pm_security T_PM_SECURITY;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_security;
        LOOP
            FETCH c_pm_security BULK COLLECT INTO pm_security;
            EXIT WHEN pm_security.COUNT = 0;

            FOR i IN pm_security.FIRST .. pm_security.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_security(i).ID_PK, pm_security(i).CREATION_TIME);
                        update_migration_pks('pm_security', pm_security(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_security(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, NAME, POLICY, SIGNATURE_METHOD, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9)'
                        USING v_id_pk,
                                pm_security(i).NAME,
                                pm_security(i).POLICY,
                                pm_security(i).SIGNATURE_METHOD,
                                v_fk_businessprocess,
                                pm_security(i).CREATION_TIME,
                                pm_security(i).CREATED_BY,
                                pm_security(i).MODIFICATION_TIME,
                                pm_security(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_security -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_security.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;
    END migrate_pm_security;

    PROCEDURE migrate_pm_service(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_SERVICE';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_SERVICE';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_service IS
            SELECT PS.ID_PK,
                   PS.NAME,
                   PS.SERVICE_TYPE,
                   PS.VALUE,
                   PS.FK_BUSINESSPROCESS,
                   PS.CREATION_TIME,
                   PS.CREATED_BY,
                   PS.MODIFICATION_TIME,
                   PS.MODIFIED_BY
            FROM TB_PM_SERVICE PS;
        TYPE T_PM_SERVICE IS TABLE OF c_pm_service%rowtype;
        pm_service T_PM_SERVICE;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_service;
        LOOP
            FETCH c_pm_service BULK COLLECT INTO pm_service;
            EXIT WHEN pm_service.COUNT = 0;

            FOR i IN pm_service.FIRST .. pm_service.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_service(i).ID_PK, pm_service(i).CREATION_TIME);
                        update_migration_pks('pm_service', pm_service(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_service(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, NAME, SERVICE_TYPE, VALUE, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9)'
                        USING v_id_pk,
                                pm_service(i).NAME,
                                pm_service(i).SERVICE_TYPE,
                                pm_service(i).VALUE,
                                v_fk_businessprocess,
                                pm_service(i).CREATION_TIME,
                                pm_service(i).CREATED_BY,
                                pm_service(i).MODIFICATION_TIME,
                                pm_service(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_service -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_service.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;
    END migrate_pm_service;

    PROCEDURE migrate_pm_splitting(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_SPLITTING';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_SPLITTING';

        v_id_pk NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_splitting IS
            SELECT PS.ID_PK,
                   PS.NAME,
                   PS.FRAGMENT_SIZE,
                   PS.COMPRESSION,
                   PS.COMPRESSION_ALGORITHM,
                   PS.JOIN_INTERVAL,
                   PS.FK_BUSINESSPROCESS,
                   PS.CREATION_TIME,
                   PS.CREATED_BY,
                   PS.MODIFICATION_TIME,
                   PS.MODIFIED_BY
            FROM TB_PM_SPLITTING PS;
        TYPE T_PM_SPLITTING IS TABLE OF c_pm_splitting%rowtype;
        pm_splitting T_PM_SPLITTING;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_splitting;
        LOOP
            FETCH c_pm_splitting BULK COLLECT INTO pm_splitting;
            EXIT WHEN pm_splitting.COUNT = 0;

            FOR i IN pm_splitting.FIRST .. pm_splitting.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_splitting(i).ID_PK, pm_splitting(i).CREATION_TIME);
                        update_migration_pks('pm_splitting', pm_splitting(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_splitting(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, NAME, FRAGMENT_SIZE, COMPRESSION, COMPRESSION_ALGORITHM, JOIN_INTERVAL, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11)'
                        USING v_id_pk,
                                pm_splitting(i).NAME,
                                pm_splitting(i).FRAGMENT_SIZE,
                                pm_splitting(i).COMPRESSION,
                                pm_splitting(i).COMPRESSION_ALGORITHM,
                                pm_splitting(i).JOIN_INTERVAL,
                                v_fk_businessprocess,
                                pm_splitting(i).CREATION_TIME,
                                pm_splitting(i).CREATED_BY,
                                pm_splitting(i).MODIFICATION_TIME,
                                pm_splitting(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_splitting -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_splitting.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;
    END migrate_pm_splitting;

    PROCEDURE migrate_pm_leg(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_LEG';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_LEG';

        v_id_pk NUMBER;
        v_fk_action NUMBER;
        v_fk_mpc NUMBER;
        v_fk_error_handling NUMBER;
        v_fk_payload_profile NUMBER;
        v_fk_property_set NUMBER;
        v_fk_reception_awareness NUMBER;
        v_fk_reliability NUMBER;
        v_fk_security NUMBER;
        v_fk_service NUMBER;
        v_fk_businessprocess NUMBER;
        v_fk_splitting NUMBER;

        CURSOR c_pm_leg IS
            SELECT PL.ID_PK,
                   PL.COMPRESS_PAYLOADS,
                   PL.NAME,
                   PL.FK_ACTION,
                   PL.FK_MPC,
                   PL.FK_ERROR_HANDLING,
                   PL.FK_PAYLOAD_PROFILE,
                   PL.FK_PROPERTY_SET,
                   PL.FK_RECEPTION_AWARENESS,
                   PL.FK_RELIABILITY,
                   PL.FK_SECURITY,
                   PL.FK_SERVICE,
                   PL.FK_BUSINESSPROCESS,
                   PL.FK_SPLITTING,
                   PL.CREATION_TIME,
                   PL.CREATED_BY,
                   PL.MODIFICATION_TIME,
                   PL.MODIFIED_BY
            FROM TB_PM_LEG PL;
        TYPE T_PM_LEG IS TABLE OF c_pm_leg%rowtype;
        pm_leg T_PM_LEG;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_leg;
        LOOP
            FETCH c_pm_leg BULK COLLECT INTO pm_leg;
            EXIT WHEN pm_leg.COUNT = 0;

            FOR i IN pm_leg.FIRST .. pm_leg.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_leg(i).ID_PK, pm_leg(i).CREATION_TIME);
                        update_migration_pks('pm_leg', pm_leg(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_action', migration_pks, pm_leg(i).FK_ACTION, v_fk_action);
                        lookup_migration_pk('pm_mpc', migration_pks, pm_leg(i).FK_MPC, v_fk_mpc);
                        lookup_migration_pk('pm_error_handling', migration_pks, pm_leg(i).FK_ERROR_HANDLING, v_fk_error_handling);
                        lookup_migration_pk('pm_payload_profile', migration_pks, pm_leg(i).FK_PAYLOAD_PROFILE, v_fk_payload_profile);
                        lookup_migration_pk('pm_message_property_set', migration_pks, pm_leg(i).FK_PROPERTY_SET, v_fk_property_set);
                        lookup_migration_pk('pm_reception_awareness', migration_pks, pm_leg(i).FK_RECEPTION_AWARENESS, v_fk_reception_awareness);
                        lookup_migration_pk('pm_reliability', migration_pks, pm_leg(i).FK_RELIABILITY, v_fk_reliability);
                        lookup_migration_pk('pm_security', migration_pks, pm_leg(i).FK_SECURITY, v_fk_security);
                        lookup_migration_pk('pm_service', migration_pks, pm_leg(i).FK_SERVICE, v_fk_service);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_leg(i).FK_BUSINESSPROCESS, v_fk_businessprocess);
                        lookup_migration_pk('pm_splitting', migration_pks, pm_leg(i).FK_SPLITTING, v_fk_splitting);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, COMPRESS_PAYLOADS, NAME, FK_ACTION, FK_MPC, FK_ERROR_HANDLING, FK_PAYLOAD_PROFILE, FK_PROPERTY_SET, FK_RECEPTION_AWARENESS, FK_RELIABILITY, FK_SECURITY, FK_SERVICE, FK_BUSINESSPROCESS, FK_SPLITTING, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11,:p_12,:p_13,:p_14,:p_15,:p_16,:p_17,:p_18)'
                        USING v_id_pk,
                                pm_leg(i).COMPRESS_PAYLOADS,
                                pm_leg(i).NAME,
                                v_fk_action,
                                v_fk_mpc,
                                v_fk_error_handling,
                                v_fk_payload_profile,
                                v_fk_property_set,
                                v_fk_reception_awareness,
                                v_fk_reliability,
                                v_fk_security,
                                v_fk_service,
                                v_fk_businessprocess,
                                v_fk_splitting,
                                pm_leg(i).CREATION_TIME,
                                pm_leg(i).CREATED_BY,
                                pm_leg(i).MODIFICATION_TIME,
                                pm_leg(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_leg -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_leg.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;
    END migrate_pm_leg;

    PROCEDURE migrate_pm_leg_mpc(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_LEG_MPC';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_LEG_MPC';

        v_legconfiguration_id_pk NUMBER;
        v_partympcmap_id_pk NUMBER;
        v_partympcmap_key NUMBER;

        CURSOR c_pm_leg_mpc IS
            SELECT PLM.LEGCONFIGURATION_ID_PK,
                   PLM.PARTYMPCMAP_ID_PK,
                   PLM.PARTYMPCMAP_KEY,
                   PLM.CREATION_TIME,
                   PLM.CREATED_BY,
                   PLM.MODIFICATION_TIME,
                   PLM.MODIFIED_BY
            FROM TB_PM_LEG_MPC PLM;
        TYPE T_PM_LEG_MPC IS TABLE OF c_pm_leg_mpc%rowtype;
        pm_leg_mpc T_PM_LEG_MPC;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_leg_mpc;
        LOOP
            FETCH c_pm_leg_mpc BULK COLLECT INTO pm_leg_mpc;
            EXIT WHEN pm_leg_mpc.COUNT = 0;

            FOR i IN pm_leg_mpc.FIRST .. pm_leg_mpc.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('pm_leg', migration_pks, pm_leg_mpc(i).LEGCONFIGURATION_ID_PK, v_legconfiguration_id_pk);
                        lookup_migration_pk('pm_mpc', migration_pks, pm_leg_mpc(i).PARTYMPCMAP_ID_PK, v_partympcmap_id_pk);
                        lookup_migration_pk('pm_party', migration_pks, pm_leg_mpc(i).PARTYMPCMAP_KEY, v_partympcmap_key);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (LEGCONFIGURATION_ID_PK, PARTYMPCMAP_ID_PK, PARTYMPCMAP_KEY, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7)'
                        USING v_legconfiguration_id_pk,
                                v_partympcmap_id_pk,
                                v_partympcmap_key,
                                pm_leg_mpc(i).CREATION_TIME,
                                pm_leg_mpc(i).CREATED_BY,
                                pm_leg_mpc(i).MODIFICATION_TIME,
                                pm_leg_mpc(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_leg_mpc -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_leg_mpc.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;
    END migrate_pm_leg_mpc;

    PROCEDURE migrate_pm_process(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_PROCESS';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PROCESS';

        v_id_pk NUMBER;
        v_fk_agreement NUMBER;
        v_fk_initiator_role NUMBER;
        v_fk_mep NUMBER;
        v_fk_mep_binding NUMBER;
        v_fk_responder_role NUMBER;
        v_fk_businessprocess NUMBER;

        CURSOR c_pm_process IS
            SELECT PP.ID_PK,
                   PP.NAME,
                   PP.FK_AGREEMENT,
                   PP.FK_INITIATOR_ROLE,
                   PP.FK_MEP,
                   PP.FK_MEP_BINDING,
                   PP.FK_RESPONDER_ROLE,
                   PP.FK_BUSINESSPROCESS,
                   PP.USE_DYNAMIC_INITIATOR,
                   PP.USE_DYNAMIC_RESPONDER,
                   PP.CREATION_TIME,
                   PP.CREATED_BY,
                   PP.MODIFICATION_TIME,
                   PP.MODIFIED_BY
            FROM TB_PM_PROCESS PP;
        TYPE T_PM_PROCESS IS TABLE OF c_pm_process%rowtype;
        pm_process T_PM_PROCESS;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_process;
        LOOP
            FETCH c_pm_process BULK COLLECT INTO pm_process;
            EXIT WHEN pm_process.COUNT = 0;

            FOR i IN pm_process.FIRST .. pm_process.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_process(i).ID_PK, pm_process(i).CREATION_TIME);
                        update_migration_pks('pm_process', pm_process(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('pm_agreement', migration_pks, pm_process(i).FK_AGREEMENT, v_fk_agreement);
                        lookup_migration_pk('pm_role', migration_pks, pm_process(i).FK_INITIATOR_ROLE, v_fk_initiator_role);
                        lookup_migration_pk('pm_mep', migration_pks, pm_process(i).FK_MEP, v_fk_mep);
                        lookup_migration_pk('pm_mep_binding', migration_pks, pm_process(i).FK_MEP_BINDING, v_fk_mep_binding);
                        lookup_migration_pk('pm_role', migration_pks, pm_process(i).FK_RESPONDER_ROLE, v_fk_responder_role);
                        lookup_migration_pk('pm_business_process', migration_pks, pm_process(i).FK_BUSINESSPROCESS, v_fk_businessprocess);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, NAME, FK_AGREEMENT, FK_INITIATOR_ROLE, FK_MEP, FK_MEP_BINDING, FK_RESPONDER_ROLE, FK_BUSINESSPROCESS, USE_DYNAMIC_INITIATOR, USE_DYNAMIC_RESPONDER, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11,:p_12,:p_13,:p_14)'
                        USING v_id_pk,
                                pm_process(i).NAME,
                                v_fk_agreement,
                                v_fk_initiator_role,
                                v_fk_mep,
                                v_fk_mep_binding,
                                v_fk_responder_role,
                                v_fk_businessprocess,
                                pm_process(i).USE_DYNAMIC_INITIATOR,
                                pm_process(i).USE_DYNAMIC_RESPONDER,
                                pm_process(i).CREATION_TIME,
                                pm_process(i).CREATED_BY,
                                pm_process(i).MODIFICATION_TIME,
                                pm_process(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_process -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_process.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

    END migrate_pm_process;

    PROCEDURE migrate_pm_join_process_init_party(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_JOIN_PROCESS_INIT_PARTY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_JOIN_PROC_INI_PARTY';

        v_process_fk NUMBER;
        v_party_fk NUMBER;

        CURSOR c_pm_join_process_init_party IS
            SELECT PJPIP.PROCESS_FK,
                   PJPIP.PARTY_FK,
                   PJPIP.CREATION_TIME,
                   PJPIP.CREATED_BY,
                   PJPIP.MODIFICATION_TIME,
                   PJPIP.MODIFIED_BY
            FROM TB_PM_JOIN_PROCESS_INIT_PARTY PJPIP;
        TYPE T_PM_JOIN_PROCESS_INIT_PARTY IS TABLE OF c_pm_join_process_init_party%rowtype;
        pm_join_process_init_party T_PM_JOIN_PROCESS_INIT_PARTY;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_join_process_init_party;
        LOOP
            FETCH c_pm_join_process_init_party BULK COLLECT INTO pm_join_process_init_party;
            EXIT WHEN pm_join_process_init_party.COUNT = 0;

            FOR i IN pm_join_process_init_party.FIRST .. pm_join_process_init_party.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('pm_process', migration_pks, pm_join_process_init_party(i).PROCESS_FK, v_process_fk);
                        lookup_migration_pk('pm_party', migration_pks, pm_join_process_init_party(i).PARTY_FK, v_party_fk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (PROCESS_FK, PARTY_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6)'
                        USING v_process_fk,
                                v_party_fk,
                                pm_join_process_init_party(i).CREATION_TIME,
                                pm_join_process_init_party(i).CREATED_BY,
                                pm_join_process_init_party(i).MODIFICATION_TIME,
                                pm_join_process_init_party(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_join_process_init_party -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_join_process_init_party.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;
    END migrate_pm_join_process_init_party;

    PROCEDURE migrate_pm_join_process_leg(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_JOIN_PROCESS_LEG';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_JOIN_PROCESS_LEG';

        v_process_fk NUMBER;
        v_leg_fk NUMBER;

        CURSOR c_pm_join_process_leg IS
            SELECT PJPL.PROCESS_FK,
                   PJPL.LEG_FK,
                   PJPL.CREATION_TIME,
                   PJPL.CREATED_BY,
                   PJPL.MODIFICATION_TIME,
                   PJPL.MODIFIED_BY
            FROM TB_PM_JOIN_PROCESS_LEG PJPL;
        TYPE T_PM_JOIN_PROCESS_LEG IS TABLE OF c_pm_join_process_leg%rowtype;
        pm_join_process_leg T_PM_JOIN_PROCESS_LEG;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_join_process_leg;
        LOOP
            FETCH c_pm_join_process_leg BULK COLLECT INTO pm_join_process_leg;
            EXIT WHEN pm_join_process_leg.COUNT = 0;

            FOR i IN pm_join_process_leg.FIRST .. pm_join_process_leg.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('pm_process', migration_pks, pm_join_process_leg(i).PROCESS_FK, v_process_fk);
                        lookup_migration_pk('pm_leg', migration_pks, pm_join_process_leg(i).LEG_FK, v_leg_fk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (PROCESS_FK, LEG_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6)'
                        USING v_process_fk,
                                v_leg_fk,
                                pm_join_process_leg(i).CREATION_TIME,
                                pm_join_process_leg(i).CREATED_BY,
                                pm_join_process_leg(i).MODIFICATION_TIME,
                                pm_join_process_leg(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_join_process_leg -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_join_process_leg.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;
    END migrate_pm_join_process_leg;

    PROCEDURE migrate_pm_join_process_resp_party(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_JOIN_PROCESS_RESP_PARTY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_JOIN_PROC_RSP_PARTY';

        v_process_fk NUMBER;
        v_party_fk NUMBER;

        CURSOR c_pm_join_process_resp_party IS
            SELECT PJPRP.PROCESS_FK,
                   PJPRP.PARTY_FK,
                   PJPRP.CREATION_TIME,
                   PJPRP.CREATED_BY,
                   PJPRP.MODIFICATION_TIME,
                   PJPRP.MODIFIED_BY
            FROM TB_PM_JOIN_PROCESS_RESP_PARTY PJPRP;
        TYPE T_PM_JOIN_PROCESS_RESP_PARTY IS TABLE OF c_pm_join_process_resp_party%rowtype;
        pm_join_process_resp_party T_PM_JOIN_PROCESS_RESP_PARTY;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_join_process_resp_party;
        LOOP
            FETCH c_pm_join_process_resp_party BULK COLLECT INTO pm_join_process_resp_party;
            EXIT WHEN pm_join_process_resp_party.COUNT = 0;

            FOR i IN pm_join_process_resp_party.FIRST .. pm_join_process_resp_party.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('pm_process', migration_pks, pm_join_process_resp_party(i).PROCESS_FK, v_process_fk);
                        lookup_migration_pk('pm_party', migration_pks, pm_join_process_resp_party(i).PARTY_FK, v_party_fk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (PROCESS_FK, PARTY_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6)'
                        USING v_process_fk,
                                v_party_fk,
                                pm_join_process_resp_party(i).CREATION_TIME,
                                pm_join_process_resp_party(i).CREATED_BY,
                                pm_join_process_resp_party(i).MODIFICATION_TIME,
                                pm_join_process_resp_party(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_join_process_resp_party -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_join_process_resp_party.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_join_process_resp_party;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_join_process_resp_party;

    PROCEDURE migrate_pm_configuration_raw(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_PM_CONFIGURATION_RAW';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_CONFIGURATION_RAW';

        v_id_pk NUMBER;

        CURSOR c_pm_configuration_raw IS
            SELECT PCR.ID_PK,
                   PCR.CONFIGURATION_DATE,
                   PCR.XML,
                   PCR.DESCRIPTION,
                   PCR.CREATION_TIME,
                   PCR.CREATED_BY,
                   PCR.MODIFICATION_TIME,
                   PCR.MODIFIED_BY
            FROM TB_PM_CONFIGURATION_RAW PCR;
        TYPE T_PM_CONFIGURATION_RAW IS TABLE OF c_pm_configuration_raw%rowtype;
        pm_configuration_raw T_PM_CONFIGURATION_RAW;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_configuration_raw;
        LOOP
            FETCH c_pm_configuration_raw BULK COLLECT INTO pm_configuration_raw;
            EXIT WHEN pm_configuration_raw.COUNT = 0;

            FOR i IN pm_configuration_raw.FIRST .. pm_configuration_raw.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(pm_configuration_raw(i).ID_PK, pm_configuration_raw(i).CREATION_TIME);
                        update_migration_pks('pm_configuration_raw', pm_configuration_raw(i).ID_PK, v_id_pk, migration_pks);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, CONFIGURATION_DATE, XML, DESCRIPTION, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8)'
                        USING v_id_pk,
                                pm_configuration_raw(i).CONFIGURATION_DATE,
                                pm_configuration_raw(i).XML,
                                pm_configuration_raw(i).DESCRIPTION,
                                pm_configuration_raw(i).CREATION_TIME,
                                pm_configuration_raw(i).CREATED_BY,
                                pm_configuration_raw(i).MODIFICATION_TIME,
                                pm_configuration_raw(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_configuration_raw -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_configuration_raw.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_configuration_raw;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_configuration_raw;

    PROCEDURE migrate_user(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_USER';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER';

        v_id_pk NUMBER;

        CURSOR c_user IS
            SELECT U.ID_PK,
                   U.USER_EMAIL,
                   U.USER_ENABLED,
                   U.USER_PASSWORD,
                   U.USER_NAME,
                   U.OPTLOCK,
                   U.ATTEMPT_COUNT,
                   U.SUSPENSION_DATE,
                   U.USER_DELETED,
                   U.PASSWORD_CHANGE_DATE,
                   U.DEFAULT_PASSWORD,
                   U.CREATION_TIME,
                   U.CREATED_BY,
                   U.MODIFICATION_TIME,
                   U.MODIFIED_BY
            FROM TB_USER U;
        TYPE T_USER IS TABLE OF c_user%rowtype;
        v_user T_USER;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user;
        LOOP
            FETCH c_user BULK COLLECT INTO v_user;
            EXIT WHEN v_user.COUNT = 0;

            FOR i IN v_user.FIRST .. v_user.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(v_user(i).ID_PK, v_user(i).CREATION_TIME);
                        update_migration_pks('user', v_user(i).ID_PK, v_id_pk, migration_pks);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, USER_EMAIL, USER_ENABLED, USER_PASSWORD, USER_NAME, OPTLOCK, ATTEMPT_COUNT, SUSPENSION_DATE, USER_DELETED, PASSWORD_CHANGE_DATE, DEFAULT_PASSWORD, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11,:p_12,:p_13,:p_14,:p_15)'
                        USING v_id_pk,
                                v_user(i).USER_EMAIL,
                                v_user(i).USER_ENABLED,
                                v_user(i).USER_PASSWORD,
                                v_user(i).USER_NAME,
                                v_user(i).OPTLOCK,
                                v_user(i).ATTEMPT_COUNT,
                                v_user(i).SUSPENSION_DATE,
                                v_user(i).USER_DELETED,
                                v_user(i).PASSWORD_CHANGE_DATE,
                                v_user(i).DEFAULT_PASSWORD,
                                v_user(i).CREATION_TIME,
                                v_user(i).CREATED_BY,
                                v_user(i).MODIFICATION_TIME,
                                v_user(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || v_user.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user;

    PROCEDURE migrate_user_msg_deletion_job IS
        v_tab VARCHAR2(30) := 'TB_USER_MSG_DELETION_JOB';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_MSG_DELETION_JOB';

        v_id_pk NUMBER;

        CURSOR c_user_msg_deletion_job IS
            SELECT UMDJ.ID_PK,
                   UMDJ.PROCEDURE_NAME,
                   UMDJ.MPC,
                   UMDJ.START_RETENTION_DATE,
                   UMDJ.END_RETENTION_DATE,
                   UMDJ.MAX_COUNT,
                   UMDJ.STATE,
                   UMDJ.ACTUAL_START_DATE,
                   UMDJ.CREATION_TIME,
                   UMDJ.CREATED_BY,
                   UMDJ.MODIFICATION_TIME,
                   UMDJ.MODIFIED_BY
            FROM TB_USER_MSG_DELETION_JOB UMDJ;
        TYPE T_USER_MSG_DELETION_JOB IS TABLE OF c_user_msg_deletion_job%rowtype;
        user_msg_deletion_job T_USER_MSG_DELETION_JOB;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_msg_deletion_job;
        LOOP
            FETCH c_user_msg_deletion_job BULK COLLECT INTO user_msg_deletion_job;
            EXIT WHEN user_msg_deletion_job.COUNT = 0;

            FOR i IN user_msg_deletion_job.FIRST .. user_msg_deletion_job.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(user_msg_deletion_job(i).ID_PK, user_msg_deletion_job(i).CREATION_TIME);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, PROCEDURE_NAME, MPC, START_RETENTION_DATE, END_RETENTION_DATE, MAX_COUNT, STATE, ACTUAL_START_DATE, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11,:p_12)'
                        USING v_id_pk,
                                user_msg_deletion_job(i).PROCEDURE_NAME,
                                user_msg_deletion_job(i).MPC,
                                user_msg_deletion_job(i).START_RETENTION_DATE,
                                user_msg_deletion_job(i).END_RETENTION_DATE,
                                user_msg_deletion_job(i).MAX_COUNT,
                                user_msg_deletion_job(i).STATE,
                                user_msg_deletion_job(i).ACTUAL_START_DATE,
                                user_msg_deletion_job(i).CREATION_TIME,
                                user_msg_deletion_job(i).CREATED_BY,
                                user_msg_deletion_job(i).MODIFICATION_TIME,
                                user_msg_deletion_job(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_msg_deletion_job -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_msg_deletion_job.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_msg_deletion_job;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_msg_deletion_job;

    PROCEDURE migrate_user_password_history(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_USER_PASSWORD_HISTORY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_PASSWORD_HISTORY';

        v_id_pk NUMBER;
        v_user_id NUMBER;

        CURSOR c_user_password_history IS
            SELECT UPH.ID_PK,
                   UPH.USER_ID,
                   UPH.USER_PASSWORD,
                   UPH.PASSWORD_CHANGE_DATE,
                   UPH.CREATION_TIME,
                   UPH.CREATED_BY,
                   UPH.MODIFICATION_TIME,
                   UPH.MODIFIED_BY
            FROM TB_USER_PASSWORD_HISTORY UPH;
        TYPE T_USER_PASSWORD_HISTORY IS TABLE OF c_user_password_history%rowtype;
        user_password_history T_USER_PASSWORD_HISTORY;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_password_history;
        LOOP
            FETCH c_user_password_history BULK COLLECT INTO user_password_history;
            EXIT WHEN user_password_history.COUNT = 0;

            FOR i IN user_password_history.FIRST .. user_password_history.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(user_password_history(i).ID_PK, user_password_history(i).CREATION_TIME);
                        lookup_migration_pk('user', migration_pks, user_password_history(i).USER_ID, v_user_id);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, USER_ID, USER_PASSWORD, PASSWORD_CHANGE_DATE, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8)'
                        USING v_id_pk,
                                v_user_id,
                                user_password_history(i).USER_PASSWORD,
                                user_password_history(i).PASSWORD_CHANGE_DATE,
                                user_password_history(i).CREATION_TIME,
                                user_password_history(i).CREATED_BY,
                                user_password_history(i).MODIFICATION_TIME,
                                user_password_history(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_password_history -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_password_history.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_password_history;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_password_history;

    PROCEDURE migrate_user_role(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_USER_ROLE';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_ROLE';

        v_id_pk NUMBER;

        CURSOR c_user_role IS
            SELECT UR.ID_PK,
                   UR.ROLE_NAME,
                   UR.CREATION_TIME,
                   UR.CREATED_BY,
                   UR.MODIFICATION_TIME,
                   UR.MODIFIED_BY
            FROM TB_USER_ROLE UR;
        TYPE T_USER_ROLE IS TABLE OF c_user_role%rowtype;
        user_role T_USER_ROLE;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_role;
        LOOP
            FETCH c_user_role BULK COLLECT INTO user_role;
            EXIT WHEN user_role.COUNT = 0;

            FOR i IN user_role.FIRST .. user_role.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(user_role(i).ID_PK, user_role(i).CREATION_TIME);
                        update_migration_pks('user_role', user_role(i).ID_PK, v_id_pk, migration_pks);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, ROLE_NAME, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6)'
                        USING v_id_pk,
                                user_role(i).ROLE_NAME,
                                user_role(i).CREATION_TIME,
                                user_role(i).CREATED_BY,
                                user_role(i).MODIFICATION_TIME,
                                user_role(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_role -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_role.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_role;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_role;

    PROCEDURE migrate_user_roles(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_USER_ROLES';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_ROLES';

        v_user_id NUMBER;
        v_role_id NUMBER;

        CURSOR c_user_roles IS
            SELECT UR.USER_ID,
                   UR.ROLE_ID,
                   UR.CREATION_TIME,
                   UR.CREATED_BY,
                   UR.MODIFICATION_TIME,
                   UR.MODIFIED_BY
            FROM TB_USER_ROLES UR;
        TYPE T_USER_ROLES IS TABLE OF c_user_roles%rowtype;
        user_roles T_USER_ROLES;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_roles;
        LOOP
            FETCH c_user_roles BULK COLLECT INTO user_roles;
            EXIT WHEN user_roles.COUNT = 0;

            FOR i IN user_roles.FIRST .. user_roles.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('user', migration_pks, user_roles(i).USER_ID, v_user_id);
                        lookup_migration_pk('user_role', migration_pks, user_roles(i).ROLE_ID, v_role_id);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (USER_ID, ROLE_ID, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6)'
                        USING v_user_id,
                                v_role_id,
                                user_roles(i).CREATION_TIME,
                                user_roles(i).CREATED_BY,
                                user_roles(i).MODIFICATION_TIME,
                                user_roles(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_roles -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_roles.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_roles;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_roles;

    PROCEDURE migrate_ws_plugin_tb_message_log IS
        v_tab VARCHAR2(30) := 'WS_PLUGIN_TB_MESSAGE_LOG';
        v_tab_new VARCHAR2(30) := 'MIGR_WS_PLUGIN_TB_MESSAGE_LOG';

        v_id_pk NUMBER;

        CURSOR c_ws_plugin_message_log IS
            SELECT PTML.ID_PK,
                   PTML.MESSAGE_ID,
                   PTML.CONVERSATION_ID,
                   PTML.REF_TO_MESSAGE_ID,
                   PTML.FROM_PARTY_ID,
                   PTML.FINAL_RECIPIENT,
                   PTML.ORIGINAL_SENDER,
                   PTML.RECEIVED
            FROM WS_PLUGIN_TB_MESSAGE_LOG PTML;
        TYPE T_WS_PLUGIN_MESSAGE_LOG IS TABLE OF c_ws_plugin_message_log%rowtype;
        ws_plugin_message_log T_WS_PLUGIN_MESSAGE_LOG;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_ws_plugin_message_log;
        LOOP
            FETCH c_ws_plugin_message_log BULK COLLECT INTO ws_plugin_message_log;
            EXIT WHEN ws_plugin_message_log.COUNT = 0;

            FOR i IN ws_plugin_message_log.FIRST .. ws_plugin_message_log.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(ws_plugin_message_log(i).ID_PK, SYSDATE);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, MESSAGE_ID, CONVERSATION_ID, REF_TO_MESSAGE_ID, FROM_PARTY_ID, FINAL_RECIPIENT, ORIGINAL_SENDER, RECEIVED)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8)'
                        USING v_id_pk,
                                ws_plugin_message_log(i).MESSAGE_ID,
                                ws_plugin_message_log(i).CONVERSATION_ID,
                                ws_plugin_message_log(i).REF_TO_MESSAGE_ID,
                                ws_plugin_message_log(i).FROM_PARTY_ID,
                                ws_plugin_message_log(i).FINAL_RECIPIENT,
                                ws_plugin_message_log(i).ORIGINAL_SENDER,
                                ws_plugin_message_log(i).RECEIVED;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_ws_plugin_message_log -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || ws_plugin_message_log.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_ws_plugin_message_log;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_ws_plugin_tb_message_log;

    PROCEDURE migrate_rev_info(migration_pks IN OUT JSON_OBJECT_T) IS
        v_tab VARCHAR2(30) := 'TB_REV_INFO';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_REV_INFO';

        v_id NUMBER;

        CURSOR c_rev_info IS
            SELECT RI.ID,
                   RI.TIMESTAMP,
                   RI.REVISION_DATE,
                   RI.USER_NAME
            FROM TB_REV_INFO RI;
        TYPE T_REV_INFO IS TABLE OF c_rev_info%rowtype;
        rev_info T_REV_INFO;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_rev_info;
        LOOP
            FETCH c_rev_info BULK COLLECT INTO rev_info;
            EXIT WHEN rev_info.COUNT = 0;

            FOR i IN rev_info.FIRST .. rev_info.LAST
                LOOP
                    BEGIN
                        v_id := generate_scalable_seq(rev_info(i).ID, rev_info(i).REVISION_DATE);
                        update_migration_pks('rev_info', rev_info(i).ID, v_id, migration_pks);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID, TIMESTAMP, REVISION_DATE, USER_NAME)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4)'
                        USING v_id,
                                rev_info(i).TIMESTAMP,
                                rev_info(i).REVISION_DATE,
                                rev_info(i).USER_NAME;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_rev_info -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || rev_info.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_rev_info;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_rev_info;

    PROCEDURE migrate_rev_changes(migration_pks IN OUT JSON_OBJECT_T, missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_REV_CHANGES';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_REV_CHANGES';

        v_rev NUMBER;
        v_id_pk NUMBER;
        v_entity_id NUMBER;

        CURSOR c_rev_changes IS
            SELECT RC.ID_PK,
                   RC.REV,
                   RC.AUDIT_ORDER,
                   RC.ENTIY_NAME,
                   RC.GROUP_NAME,
                   RC.ENTITY_ID,
                   RC.MODIFICATION_TYPE,
                   RC.CREATION_TIME,
                   RC.CREATED_BY,
                   RC.MODIFICATION_TIME,
                   RC.MODIFIED_BY
            FROM TB_REV_CHANGES RC;
        TYPE T_REV_CHANGES IS TABLE OF c_rev_changes%rowtype;
        rev_changes T_REV_CHANGES;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_rev_changes;
        LOOP
            FETCH c_rev_changes BULK COLLECT INTO rev_changes;
            EXIT WHEN rev_changes.COUNT = 0;

            FOR i IN rev_changes.FIRST .. rev_changes.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, rev_changes(i).REV, v_rev);
                        v_id_pk := generate_scalable_seq(rev_changes(i).ID_PK, rev_changes(i).CREATION_TIME);

                        CASE rev_changes(i).ENTIY_NAME
                            WHEN 'eu.domibus.core.user.plugin.AuthenticationEntity' THEN lookup_audit_migration_pk('authentication_entry', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            WHEN 'eu.domibus.core.plugin.routing.BackendFilterEntity' THEN lookup_audit_migration_pk('backend_filter', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            WHEN 'eu.domibus.core.plugin.routing.RoutingCriteriaEntity' THEN lookup_audit_migration_pk('routing_criteria', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            WHEN 'eu.domibus.core.user.ui.User' THEN lookup_audit_migration_pk('user', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            WHEN 'eu.domibus.common.model.configuration.Configuration' THEN lookup_audit_migration_pk('pm_configuration', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            WHEN 'eu.domibus.common.model.configuration.ConfigurationRaw' THEN lookup_audit_migration_pk('pm_configuration_raw', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            WHEN 'eu.domibus.common.model.configuration.Party' THEN lookup_audit_migration_pk('pm_party', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            WHEN 'eu.domibus.common.model.configuration.PartyIdType' THEN lookup_audit_migration_pk('pm_party_id_type', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            WHEN 'eu.domibus.core.certificate.Certificate' THEN lookup_audit_migration_pk('certificate', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            WHEN 'eu.domibus.core.user.ui.UserRole' THEN lookup_audit_migration_pk('user_role', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            WHEN 'eu.domibus.core.audit.model.TruststoreAudit' THEN lookup_audit_migration_pk('action_audit', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            WHEN 'eu.domibus.core.audit.model.PModeAudit' THEN lookup_audit_migration_pk('action_audit', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            WHEN 'eu.domibus.core.audit.model.PModeArchiveAudit' THEN lookup_audit_migration_pk('action_audit', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            WHEN 'eu.domibus.core.audit.model.MessageAudit' THEN lookup_audit_migration_pk('action_audit', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            WHEN 'eu.domibus.core.audit.model.JmsMessageAudit' THEN lookup_audit_migration_pk('action_audit', migration_pks, missing_entity_date_prefix, CAST(rev_changes(i).ENTITY_ID AS NUMBER), v_entity_id);
                            ELSE
                                BEGIN
                                    -- use the previous entity ID value for unknown entities
                                    DBMS_OUTPUT.PUT_LINE('Unknown entity name ' || rev_changes(i).ENTIY_NAME);
                                    v_entity_id := generate_scalable_seq(rev_changes(i).ENTITY_ID, missing_entity_date_prefix);
                                END;
                            END CASE;

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, REV, AUDIT_ORDER, ENTIY_NAME, GROUP_NAME, ENTITY_ID, MODIFICATION_TYPE, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11)'
                        USING v_id_pk,
                                v_rev,
                                rev_changes(i).AUDIT_ORDER,
                                rev_changes(i).ENTIY_NAME,
                                rev_changes(i).GROUP_NAME,
                                TO_CHAR(v_entity_id),
                                rev_changes(i).MODIFICATION_TYPE,
                                rev_changes(i).CREATION_TIME,
                                rev_changes(i).CREATED_BY,
                                rev_changes(i).MODIFICATION_TIME,
                                rev_changes(i).MODIFIED_BY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_rev_changes -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || rev_changes.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_rev_changes;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_rev_changes;

    PROCEDURE migrate_authentication_entry_aud(migration_pks IN OUT JSON_OBJECT_T, missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_AUTHENTICATION_ENTRY_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_AUTH_ENTRY_AUD';

        v_rev NUMBER;
        v_id_pk NUMBER;

        CURSOR c_authentication_entry_aud IS
            SELECT AEA.ID_PK,
                   AEA.REV,
                   AEA.REVTYPE,
                   AEA.CERTIFICATE_ID,
                   AEA.CERTIFICATEID_MOD,
                   AEA.USERNAME,
                   AEA.USERNAME_MOD,
                   AEA.PASSWD,
                   AEA.PASSWORD_MOD,
                   AEA.AUTH_ROLES,
                   AEA.AUTHROLES_MOD,
                   AEA.ORIGINAL_USER,
                   AEA.ORIGINALUSER_MOD,
                   AEA.BACKEND,
                   AEA.BACKEND_MOD,
                   AEA.USER_ENABLED,
                   AEA.ACTIVE_MOD,
                   AEA.PASSWORD_CHANGE_DATE,
                   AEA.PASSWORDCHANGEDATE_MOD,
                   AEA.DEFAULT_PASSWORD,
                   AEA.DEFAULTPASSWORD_MOD
            FROM TB_AUTHENTICATION_ENTRY_AUD AEA;
        TYPE T_AUTHENTICATION_ENTRY_AUD IS TABLE OF c_authentication_entry_aud%rowtype;
        authentication_entry_aud T_AUTHENTICATION_ENTRY_AUD;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_authentication_entry_aud;
        LOOP
            FETCH c_authentication_entry_aud BULK COLLECT INTO authentication_entry_aud;
            EXIT WHEN authentication_entry_aud.COUNT = 0;

            FOR i IN authentication_entry_aud.FIRST .. authentication_entry_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, authentication_entry_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('authentication_entry', migration_pks, missing_entity_date_prefix, authentication_entry_aud(i).ID_PK, v_id_pk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, REV, REVTYPE, CERTIFICATE_ID, CERTIFICATEID_MOD, USERNAME, USERNAME_MOD, PASSWD, PASSWORD_MOD, AUTH_ROLES, AUTHROLES_MOD, ORIGINAL_USER, ORIGINALUSER_MOD, BACKEND, BACKEND_MOD, USER_ENABLED, ACTIVE_MOD, PASSWORD_CHANGE_DATE, PASSWORDCHANGEDATE_MOD, DEFAULT_PASSWORD, DEFAULTPASSWORD_MOD)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11,:p_12,:p_13,:p_14,:p_15,:p_16,:p_17,:p_18,:p_19,:p_20,:p_21)'
                        USING v_id_pk,
                                v_rev,
                                authentication_entry_aud(i).REVTYPE,
                                authentication_entry_aud(i).CERTIFICATE_ID,
                                authentication_entry_aud(i).CERTIFICATEID_MOD,
                                authentication_entry_aud(i).USERNAME,
                                authentication_entry_aud(i).USERNAME_MOD,
                                authentication_entry_aud(i).PASSWD,
                                authentication_entry_aud(i).PASSWORD_MOD,
                                authentication_entry_aud(i).AUTH_ROLES,
                                authentication_entry_aud(i).AUTHROLES_MOD,
                                authentication_entry_aud(i).ORIGINAL_USER,
                                authentication_entry_aud(i).ORIGINALUSER_MOD,
                                authentication_entry_aud(i).BACKEND,
                                authentication_entry_aud(i).BACKEND_MOD,
                                authentication_entry_aud(i).USER_ENABLED,
                                authentication_entry_aud(i).ACTIVE_MOD,
                                authentication_entry_aud(i).PASSWORD_CHANGE_DATE,
                                authentication_entry_aud(i).PASSWORDCHANGEDATE_MOD,
                                authentication_entry_aud(i).DEFAULT_PASSWORD,
                                authentication_entry_aud(i).DEFAULTPASSWORD_MOD;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_authentication_entry_aud -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || authentication_entry_aud.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_authentication_entry_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_authentication_entry_aud;

    PROCEDURE migrate_back_rcriteria_aud(migration_pks IN OUT JSON_OBJECT_T, missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_BACK_RCRITERIA_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_BACK_RCRITERIA_AUD';

        v_rev NUMBER;
        v_id_pk NUMBER;
        v_fk_backend_filter NUMBER;

        CURSOR c_back_rcriteria_aud IS
            SELECT BRA.ID_PK,
                   BRA.REV,
                   BRA.REVTYPE,
                   BRA.FK_BACKEND_FILTER,
                   BRA.PRIORITY
            FROM TB_BACK_RCRITERIA_AUD BRA;
        TYPE T_BACK_RCRITERIA_AUD IS TABLE OF c_back_rcriteria_aud%rowtype;
        back_rcriteria_aud T_BACK_RCRITERIA_AUD;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_back_rcriteria_aud;
        LOOP
            FETCH c_back_rcriteria_aud BULK COLLECT INTO back_rcriteria_aud;
            EXIT WHEN back_rcriteria_aud.COUNT = 0;

            FOR i IN back_rcriteria_aud.FIRST .. back_rcriteria_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, back_rcriteria_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('authentication_entry', migration_pks, missing_entity_date_prefix, back_rcriteria_aud(i).ID_PK, v_id_pk);
                        lookup_audit_migration_pk('backend_filter', migration_pks, missing_entity_date_prefix, back_rcriteria_aud(i).FK_BACKEND_FILTER, v_fk_backend_filter);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, REV, REVTYPE, FK_BACKEND_FILTER, PRIORITY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5)'
                        USING v_id_pk,
                                v_rev,
                                back_rcriteria_aud(i).REVTYPE,
                                v_fk_backend_filter,
                                back_rcriteria_aud(i).PRIORITY;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_back_rcriteria_aud -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || back_rcriteria_aud.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_back_rcriteria_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_back_rcriteria_aud;

    PROCEDURE migrate_backend_filter_aud(migration_pks IN OUT JSON_OBJECT_T, missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_BACKEND_FILTER_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_BACKEND_FILTER_AUD';

        v_rev NUMBER;
        v_id_pk NUMBER;

        CURSOR c_backend_filter_aud IS
            SELECT BFA.ID_PK,
                   BFA.REV,
                   BFA.REVTYPE,
                   BFA.BACKEND_NAME,
                   BFA.BACKENDNAME_MOD,
                   BFA.PRIORITY,
                   BFA.INDEX_MOD,
                   BFA.ROUTINGCRITERIAS_MOD
            FROM TB_BACKEND_FILTER_AUD BFA;
        TYPE T_BACKEND_FILTER_AUD IS TABLE OF c_backend_filter_aud%rowtype;
        backend_filter_aud T_BACKEND_FILTER_AUD;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_backend_filter_aud;
        LOOP
            FETCH c_backend_filter_aud BULK COLLECT INTO backend_filter_aud;
            EXIT WHEN backend_filter_aud.COUNT = 0;

            FOR i IN backend_filter_aud.FIRST .. backend_filter_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, backend_filter_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('backend_filter', migration_pks, missing_entity_date_prefix, backend_filter_aud(i).ID_PK, v_id_pk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, REV, REVTYPE, BACKEND_NAME, BACKENDNAME_MOD, PRIORITY, INDEX_MOD, ROUTINGCRITERIAS_MOD)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8)'
                        USING v_id_pk,
                                v_rev,
                                backend_filter_aud(i).REVTYPE,
                                backend_filter_aud(i).BACKEND_NAME,
                                backend_filter_aud(i).BACKENDNAME_MOD,
                                backend_filter_aud(i).PRIORITY,
                                backend_filter_aud(i).INDEX_MOD,
                                backend_filter_aud(i).ROUTINGCRITERIAS_MOD;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_backend_filter_aud -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || backend_filter_aud.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_backend_filter_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;

    END migrate_backend_filter_aud;

    PROCEDURE migrate_certificate_aud(migration_pks IN OUT JSON_OBJECT_T, missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_CERTIFICATE_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_CERTIFICATE_AUD';

        v_rev NUMBER;
        v_id_pk NUMBER;

        CURSOR c_certificate_aud IS
            SELECT CA.ID_PK,
                   CA.REV,
                   CA.REVTYPE,
                   CA.CERTIFICATE_ALIAS,
                   CA.NOT_VALID_BEFORE_DATE,
                   CA.NOT_VALID_AFTER_DATE,
                   CA.REVOKE_NOTIFICATION_DATE,
                   CA.ALERT_IMM_NOTIFICATION_DATE,
                   CA.ALERT_EXP_NOTIFICATION_DATE,
                   CA.CERTIFICATE_STATUS,
                   CA.CERTIFICATE_TYPE
            FROM TB_CERTIFICATE_AUD CA;
        TYPE T_CERTIFICATE_AUD IS TABLE OF c_certificate_aud%rowtype;
        certificate_aud T_CERTIFICATE_AUD;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_certificate_aud;
        LOOP
            FETCH c_certificate_aud BULK COLLECT INTO certificate_aud;
            EXIT WHEN certificate_aud.COUNT = 0;

            FOR i IN certificate_aud.FIRST .. certificate_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, certificate_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('certificate', migration_pks, missing_entity_date_prefix, certificate_aud(i).ID_PK, v_id_pk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, REV, REVTYPE, CERTIFICATE_ALIAS, NOT_VALID_BEFORE_DATE, NOT_VALID_AFTER_DATE, REVOKE_NOTIFICATION_DATE, ALERT_IMM_NOTIFICATION_DATE, ALERT_EXP_NOTIFICATION_DATE, CERTIFICATE_STATUS, CERTIFICATE_TYPE)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11)'
                        USING v_id_pk,
                                v_rev,
                                certificate_aud(i).REVTYPE,
                                certificate_aud(i).CERTIFICATE_ALIAS,
                                certificate_aud(i).NOT_VALID_BEFORE_DATE,
                                certificate_aud(i).NOT_VALID_AFTER_DATE,
                                certificate_aud(i).REVOKE_NOTIFICATION_DATE,
                                certificate_aud(i).ALERT_IMM_NOTIFICATION_DATE,
                                certificate_aud(i).ALERT_EXP_NOTIFICATION_DATE,
                                certificate_aud(i).CERTIFICATE_STATUS,
                                certificate_aud(i).CERTIFICATE_TYPE;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_certificate_aud -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || certificate_aud.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_certificate_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_certificate_aud;

    PROCEDURE migrate_pm_configuration_aud(migration_pks IN OUT JSON_OBJECT_T, missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_PM_CONFIGURATION_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_CONFIGURATION_AUD';

        v_rev NUMBER;
        v_id_pk NUMBER;

        CURSOR c_pm_configuration_aud IS
            SELECT PCA.ID_PK,
                   PCA.REV,
                   PCA.REVTYPE,
                   PCA.EXPRESSION,
                   PCA.EXPRESSION_MOD,
                   PCA.NAME,
                   PCA.NAME_MOD
            FROM TB_PM_CONFIGURATION_AUD PCA;
        TYPE T_PM_CONFIGURATION_AUD IS TABLE OF c_pm_configuration_aud%rowtype;
        pm_configuration_aud T_PM_CONFIGURATION_AUD;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_configuration_aud;
        LOOP
            FETCH c_pm_configuration_aud BULK COLLECT INTO pm_configuration_aud;
            EXIT WHEN pm_configuration_aud.COUNT = 0;

            FOR i IN pm_configuration_aud.FIRST .. pm_configuration_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, pm_configuration_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('pm_configuration', migration_pks, missing_entity_date_prefix, pm_configuration_aud(i).ID_PK, v_id_pk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, REV, REVTYPE, EXPRESSION, EXPRESSION_MOD, NAME, NAME_MOD)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7)'
                        USING v_id_pk,
                                v_rev,
                                pm_configuration_aud(i).REVTYPE,
                                pm_configuration_aud(i).EXPRESSION,
                                pm_configuration_aud(i).EXPRESSION_MOD,
                                pm_configuration_aud(i).NAME,
                                pm_configuration_aud(i).NAME_MOD;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_configuration_aud -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_configuration_aud.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_configuration_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_configuration_aud;

    PROCEDURE migrate_pm_configuration_raw_aud(migration_pks IN OUT JSON_OBJECT_T, missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_PM_CONFIGURATION_RAW_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_CONF_RAW_AUD';

        v_rev NUMBER;
        v_id_pk NUMBER;

        CURSOR c_pm_configuration_raw_aud IS
            SELECT PCRA.ID_PK,
                   PCRA.REV,
                   PCRA.REVTYPE,
                   PCRA.CONFIGURATION_DATE,
                   PCRA.CONFIGURATIONDATE_MOD,
                   PCRA.DESCRIPTION,
                   PCRA.DESCRIPTION_MOD,
                   PCRA.XML,
                   PCRA.XML_MOD
            FROM TB_PM_CONFIGURATION_RAW_AUD PCRA;
        TYPE T_PM_CONFIGURATION_RAW_AUD IS TABLE OF c_pm_configuration_raw_aud%rowtype;
        pm_configuration_raw_aud T_PM_CONFIGURATION_RAW_AUD;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_configuration_raw_aud;
        LOOP
            FETCH c_pm_configuration_raw_aud BULK COLLECT INTO pm_configuration_raw_aud;
            EXIT WHEN pm_configuration_raw_aud.COUNT = 0;

            FOR i IN pm_configuration_raw_aud.FIRST .. pm_configuration_raw_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, pm_configuration_raw_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('pm_configuration_raw', migration_pks, missing_entity_date_prefix, pm_configuration_raw_aud(i).ID_PK, v_id_pk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, REV, REVTYPE, CONFIGURATION_DATE, CONFIGURATIONDATE_MOD, DESCRIPTION, DESCRIPTION_MOD, XML, XML_MOD)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9)'
                        USING v_id_pk,
                                v_rev,
                                pm_configuration_raw_aud(i).REVTYPE,
                                pm_configuration_raw_aud(i).CONFIGURATION_DATE,
                                pm_configuration_raw_aud(i).CONFIGURATIONDATE_MOD,
                                pm_configuration_raw_aud(i).DESCRIPTION,
                                pm_configuration_raw_aud(i).DESCRIPTION_MOD,
                                pm_configuration_raw_aud(i).XML,
                                pm_configuration_raw_aud(i).XML_MOD;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_configuration_raw_aud -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_configuration_raw_aud.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_configuration_raw_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_configuration_raw_aud;

    PROCEDURE migrate_pm_party_aud(migration_pks IN OUT JSON_OBJECT_T, missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_PM_PARTY_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PARTY_AUD';

        v_rev NUMBER;
        v_id_pk NUMBER;

        CURSOR c_pm_party_aud IS
            SELECT PPA.ID_PK,
                   PPA.REV,
                   PPA.REVTYPE,
                   PPA.ENDPOINT,
                   PPA.ENDPOINT_MOD,
                   PPA.NAME,
                   PPA.NAME_MOD,
                   PPA.PASSWORD,
                   PPA.PASSWORD_MOD,
                   PPA.USERNAME,
                   PPA.USERNAME_MOD
            FROM TB_PM_PARTY_AUD PPA;
        TYPE T_PM_PARTY_AUD IS TABLE OF c_pm_party_aud%rowtype;
        pm_party_aud T_PM_PARTY_AUD;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_party_aud;
        LOOP
            FETCH c_pm_party_aud BULK COLLECT INTO pm_party_aud;
            EXIT WHEN pm_party_aud.COUNT = 0;

            FOR i IN pm_party_aud.FIRST .. pm_party_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, pm_party_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('pm_party', migration_pks, missing_entity_date_prefix, pm_party_aud(i).ID_PK, v_id_pk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, REV, REVTYPE, ENDPOINT, ENDPOINT_MOD, NAME, NAME_MOD, PASSWORD, PASSWORD_MOD, USERNAME, USERNAME_MOD)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11)'
                        USING v_id_pk,
                                v_rev,
                                pm_party_aud(i).REVTYPE,
                                pm_party_aud(i).ENDPOINT,
                                pm_party_aud(i).ENDPOINT_MOD,
                                pm_party_aud(i).NAME,
                                pm_party_aud(i).NAME_MOD,
                                pm_party_aud(i).PASSWORD,
                                pm_party_aud(i).PASSWORD_MOD,
                                pm_party_aud(i).USERNAME,
                                pm_party_aud(i).USERNAME_MOD;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_party_aud -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_party_aud.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_party_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_party_aud;

    PROCEDURE migrate_pm_party_id_type_aud(migration_pks IN OUT JSON_OBJECT_T, missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_PM_PARTY_ID_TYPE_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PARTY_ID_TYPE_AUD';

        v_rev NUMBER;
        v_id_pk NUMBER;

        CURSOR c_pm_party_id_type_aud IS
            SELECT PPITA.ID_PK,
                   PPITA.REV,
                   PPITA.REVTYPE,
                   PPITA.NAME,
                   PPITA.NAME_MOD,
                   PPITA.VALUE,
                   PPITA.VALUE_MOD
            FROM TB_PM_PARTY_ID_TYPE_AUD PPITA;
        TYPE T_PM_PARTY_ID_TYPE_AUD IS TABLE OF c_pm_party_id_type_aud%rowtype;
        pm_party_id_type_aud T_PM_PARTY_ID_TYPE_AUD;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_party_id_type_aud;
        LOOP
            FETCH c_pm_party_id_type_aud BULK COLLECT INTO pm_party_id_type_aud;
            EXIT WHEN pm_party_id_type_aud.COUNT = 0;

            FOR i IN pm_party_id_type_aud.FIRST .. pm_party_id_type_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, pm_party_id_type_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('pm_party_id_type', migration_pks, missing_entity_date_prefix, pm_party_id_type_aud(i).ID_PK, v_id_pk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, REV, REVTYPE, NAME, NAME_MOD, VALUE, VALUE_MOD)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7)'
                        USING v_id_pk,
                                v_rev,
                                pm_party_id_type_aud(i).REVTYPE,
                                pm_party_id_type_aud(i).NAME,
                                pm_party_id_type_aud(i).NAME_MOD,
                                pm_party_id_type_aud(i).VALUE,
                                pm_party_id_type_aud(i).VALUE_MOD;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_party_id_type_aud -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_party_id_type_aud.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_party_id_type_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_party_id_type_aud;

    PROCEDURE migrate_pm_party_identifier_aud(migration_pks IN OUT JSON_OBJECT_T, missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_PM_PARTY_IDENTIFIER_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PARTY_IDENTIF_AUD';

        v_rev NUMBER;
        v_id_pk NUMBER;
        v_fk_party NUMBER;

        CURSOR c_pm_party_identifier_aud IS
            SELECT PPIA.ID_PK,
                   PPIA.REV,
                   PPIA.REVTYPE,
                   PPIA.FK_PARTY
            FROM TB_PM_PARTY_IDENTIFIER_AUD PPIA;
        TYPE T_PM_PARTY_IDENTIFIER_AUD IS TABLE OF c_pm_party_identifier_aud%rowtype;
        pm_party_identifier_aud T_PM_PARTY_IDENTIFIER_AUD;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_party_identifier_aud;
        LOOP
            FETCH c_pm_party_identifier_aud BULK COLLECT INTO pm_party_identifier_aud;
            EXIT WHEN pm_party_identifier_aud.COUNT = 0;

            FOR i IN pm_party_identifier_aud.FIRST .. pm_party_identifier_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, pm_party_identifier_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('pm_party_identifier', migration_pks, missing_entity_date_prefix, pm_party_identifier_aud(i).ID_PK, v_id_pk);
                        lookup_audit_migration_pk('pm_party', migration_pks, missing_entity_date_prefix, pm_party_identifier_aud(i).FK_PARTY, v_fk_party);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, REV, REVTYPE, FK_PARTY)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4)'
                        USING v_id_pk,
                                v_rev,
                                pm_party_identifier_aud(i).REVTYPE,
                                v_fk_party;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_party_identifier_aud -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || pm_party_identifier_aud.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_party_identifier_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_party_identifier_aud;

    PROCEDURE migrate_routing_criteria_aud(migration_pks IN OUT JSON_OBJECT_T, missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_ROUTING_CRITERIA_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_ROUTING_CRITERIA_AUD';

        v_rev NUMBER;
        v_id_pk NUMBER;

        CURSOR c_routing_criteria_aud IS
            SELECT RCA.ID_PK,
                   RCA.REV,
                   RCA.REVTYPE,
                   RCA.EXPRESSION,
                   RCA.EXPRESSION_MOD,
                   RCA.NAME,
                   RCA.NAME_MOD
            FROM TB_ROUTING_CRITERIA_AUD RCA;
        TYPE T_ROUTING_CRITERIA_AUD IS TABLE OF c_routing_criteria_aud%rowtype;
        routing_criteria_aud T_ROUTING_CRITERIA_AUD;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_routing_criteria_aud;
        LOOP
            FETCH c_routing_criteria_aud BULK COLLECT INTO routing_criteria_aud;
            EXIT WHEN routing_criteria_aud.COUNT = 0;

            FOR i IN routing_criteria_aud.FIRST .. routing_criteria_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, routing_criteria_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('routing_criteria', migration_pks, missing_entity_date_prefix, routing_criteria_aud(i).ID_PK, v_id_pk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, REV, REVTYPE, EXPRESSION, EXPRESSION_MOD, NAME, NAME_MOD)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7)'
                        USING v_id_pk,
                                v_rev,
                                routing_criteria_aud(i).REVTYPE,
                                routing_criteria_aud(i).EXPRESSION,
                                routing_criteria_aud(i).EXPRESSION_MOD,
                                routing_criteria_aud(i).NAME,
                                routing_criteria_aud(i).NAME_MOD;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_routing_criteria_aud -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || routing_criteria_aud.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_routing_criteria_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_routing_criteria_aud;

    PROCEDURE migrate_user_aud(migration_pks IN OUT JSON_OBJECT_T, missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_USER_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_AUD';

        v_rev NUMBER;
        v_id_pk NUMBER;

        CURSOR c_user_aud IS
            SELECT UA.ID_PK,
                   UA.REV,
                   UA.REVTYPE,
                   UA.USER_ENABLED,
                   UA.ACTIVE_MOD,
                   UA.USER_DELETED,
                   UA.DELETED_MOD,
                   UA.USER_EMAIL,
                   UA.EMAIL_MOD,
                   UA.USER_PASSWORD,
                   UA.PASSWORD_MOD,
                   UA.USER_NAME,
                   UA.USERNAME_MOD,
                   UA.OPTLOCK,
                   UA.VERSION_MOD,
                   UA.ROLES_MOD,
                   UA.PASSWORD_CHANGE_DATE,
                   UA.PASSWORDCHANGEDATE_MOD,
                   UA.DEFAULT_PASSWORD,
                   UA.DEFAULTPASSWORD_MOD
            FROM TB_USER_AUD UA;
        TYPE T_USER_AUD IS TABLE OF c_user_aud%rowtype;
        user_aud T_USER_AUD;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_aud;
        LOOP
            FETCH c_user_aud BULK COLLECT INTO user_aud;
            EXIT WHEN user_aud.COUNT = 0;

            FOR i IN user_aud.FIRST .. user_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, user_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('user', migration_pks, missing_entity_date_prefix, user_aud(i).ID_PK, v_id_pk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, REV, REVTYPE, USER_ENABLED, ACTIVE_MOD, USER_DELETED, DELETED_MOD, USER_EMAIL, EMAIL_MOD, USER_PASSWORD, PASSWORD_MOD, USER_NAME, USERNAME_MOD, OPTLOCK, VERSION_MOD, ROLES_MOD, PASSWORD_CHANGE_DATE, PASSWORDCHANGEDATE_MOD, DEFAULT_PASSWORD, DEFAULTPASSWORD_MOD)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6,:p_7,:p_8,:p_9,:p_10,:p_11,:p_12,:p_13,:p_14,:p_15,:p_16,:p_17,:p_18,:p_19,:p_20)'
                        USING v_id_pk,
                                v_rev,
                                user_aud(i).REVTYPE,
                                user_aud(i).USER_ENABLED,
                                user_aud(i).ACTIVE_MOD,
                                user_aud(i).USER_DELETED,
                                user_aud(i).DELETED_MOD,
                                user_aud(i).USER_EMAIL,
                                user_aud(i).EMAIL_MOD,
                                user_aud(i).USER_PASSWORD,
                                user_aud(i).PASSWORD_MOD,
                                user_aud(i).USER_NAME,
                                user_aud(i).USERNAME_MOD,
                                user_aud(i).OPTLOCK,
                                user_aud(i).VERSION_MOD,
                                user_aud(i).ROLES_MOD,
                                user_aud(i).PASSWORD_CHANGE_DATE,
                                user_aud(i).PASSWORDCHANGEDATE_MOD,
                                user_aud(i).DEFAULT_PASSWORD,
                                user_aud(i).DEFAULTPASSWORD_MOD;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_aud -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_aud.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_aud;

    PROCEDURE migrate_user_role_aud(migration_pks IN OUT JSON_OBJECT_T, missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_USER_ROLE_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_ROLE_AUD';

        v_rev NUMBER;
        v_id_pk NUMBER;

        CURSOR c_user_role_aud IS
            SELECT URA.ID_PK,
                   URA.REV,
                   URA.REVTYPE,
                   URA.ROLE_NAME,
                   URA.NAME_MOD,
                   URA.USERS_MOD
            FROM TB_USER_ROLE_AUD URA;
        TYPE T_USER_ROLE_AUD IS TABLE OF c_user_role_aud%rowtype;
        user_role_aud T_USER_ROLE_AUD;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_role_aud;
        LOOP
            FETCH c_user_role_aud BULK COLLECT INTO user_role_aud;
            EXIT WHEN user_role_aud.COUNT = 0;

            FOR i IN user_role_aud.FIRST .. user_role_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, user_role_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('user_role', migration_pks, missing_entity_date_prefix, user_role_aud(i).ID_PK, v_id_pk);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, REV, REVTYPE, ROLE_NAME, NAME_MOD, USERS_MOD)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4,:p_5,:p_6)'
                        USING v_id_pk,
                                v_rev,
                                user_role_aud(i).REVTYPE,
                                user_role_aud(i).ROLE_NAME,
                                user_role_aud(i).NAME_MOD,
                                user_role_aud(i).USERS_MOD;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_role_aud -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_role_aud.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_role_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_role_aud;

    PROCEDURE migrate_user_roles_aud(migration_pks IN OUT JSON_OBJECT_T, missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_USER_ROLES_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_ROLES_AUD';

        v_rev NUMBER;
        v_user_id NUMBER;
        v_role_id NUMBER;

        CURSOR c_user_roles_aud IS
            SELECT URA.REV,
                   URA.REVTYPE,
                   URA.USER_ID,
                   URA.ROLE_ID
            FROM TB_USER_ROLES_AUD URA;
        TYPE T_USER_ROLES_AUD IS TABLE OF c_user_roles_aud%rowtype;
        user_roles_aud T_USER_ROLES_AUD;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_roles_aud;
        LOOP
            FETCH c_user_roles_aud BULK COLLECT INTO user_roles_aud;
            EXIT WHEN user_roles_aud.COUNT = 0;

            FOR i IN user_roles_aud.FIRST .. user_roles_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, user_roles_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('user', migration_pks, missing_entity_date_prefix, user_roles_aud(i).USER_ID, v_user_id);
                        lookup_audit_migration_pk('user_role', migration_pks, missing_entity_date_prefix, user_roles_aud(i).ROLE_ID, v_role_id);

                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (REV, REVTYPE, USER_ID, ROLE_ID)' ||
                                          ' VALUES (:p_1,:p_2,:p_3,:p_4)'
                        USING v_rev,
                                user_roles_aud(i).REVTYPE,
                                v_user_id,
                                v_role_id;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_roles_aud -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_roles_aud.COUNT || ' records in total into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_roles_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_roles_aud;

    /**-- main entry point for running the migration --*/
    PROCEDURE migrate IS
        migration_pks JSON_OBJECT_T;
        missing_entity_date_prefix DATE;
    BEGIN
        migration_pks := new JSON_OBJECT_T();
        prepare_timezone_offset(migration_pks);

        -- keep it in this order
        -- START migrate to the new schema (including primary keys to the new format)
        migrate_user_message(migration_pks);
        migrate_message_fragment(migration_pks);
        migrate_message_group(migration_pks);
        migrate_message_header(migration_pks);

        migrate_signal_receipt(migration_pks);
        migrate_message_log;

        migrate_raw_envelope_log(migration_pks);

        migrate_property(migration_pks);
        migrate_part_info_user(migration_pks);
        migrate_part_info_property(migration_pks);

        migrate_error_log(migration_pks);
        migrate_message_acknw(migration_pks);
        migrate_send_attempt(migration_pks);
        -- END migrate to the new schema (including primary keys to the new format)

        -- START migrate primary keys to new format
        migrate_action_audit(migration_pks);
        migrate_alert(migration_pks);
        migrate_event(migration_pks);
        migrate_event_alert(migration_pks);
        migrate_event_property(migration_pks);
        --
        migrate_authentication_entry(migration_pks);
        migrate_plugin_user_passwd_history(migration_pks);
        --
        migrate_backend_filter(migration_pks);
        migrate_routing_criteria(migration_pks);
        --
        migrate_certificate(migration_pks);
        --
        migrate_command(migration_pks);
        migrate_command_property(migration_pks);
        --
        migrate_encryption_key; -- SECRET_KEY & INIT_VECTOR (BLOB > LONGBLOB)
        --
        migrate_message_acknw_prop(migration_pks);
        --
        -- migrate_message_ui(migration_pks); -- not part of this task (UI replication to be fixed later)
        --
        migrate_messaging_lock(migration_pks);
        --
        migrate_pm_business_process(migration_pks);
        migrate_pm_action(migration_pks);
        migrate_pm_agreement(migration_pks);
        migrate_pm_error_handling(migration_pks);
        migrate_pm_mep(migration_pks);
        migrate_pm_mep_binding(migration_pks);
        migrate_pm_message_property(migration_pks);
        migrate_pm_message_property_set(migration_pks);
        migrate_pm_join_property_set(migration_pks);
        migrate_pm_party(migration_pks);
        migrate_pm_configuration(migration_pks);
        migrate_pm_mpc(migration_pks);
        migrate_pm_party_id_type(migration_pks);
        migrate_pm_party_identifier(migration_pks);
        migrate_pm_payload(migration_pks);
        migrate_pm_payload_profile(migration_pks);
        migrate_pm_join_payload_profile(migration_pks);
        migrate_pm_reception_awareness(migration_pks);
        migrate_pm_reliability(migration_pks);
        migrate_pm_role(migration_pks);
        migrate_pm_security(migration_pks);
        migrate_pm_service(migration_pks);
        migrate_pm_splitting(migration_pks);
        migrate_pm_leg(migration_pks);
        migrate_pm_leg_mpc(migration_pks);
        migrate_pm_process(migration_pks);
        migrate_pm_join_process_init_party(migration_pks);
        migrate_pm_join_process_leg(migration_pks);
        migrate_pm_join_process_resp_party(migration_pks);
        --
        migrate_pm_configuration_raw(migration_pks); -- XML (BLOB > LONGBLOB)
        --
        migrate_user(migration_pks);
        migrate_user_password_history(migration_pks);
        migrate_user_role(migration_pks);
        migrate_user_roles(migration_pks);
        --
        migrate_user_msg_deletion_job;
        --
        migrate_ws_plugin_tb_message_log;
        --
        missing_entity_date_prefix := SYSDATE;
        migrate_rev_info(migration_pks);
        migrate_rev_changes(migration_pks, missing_entity_date_prefix);
        migrate_authentication_entry_aud(migration_pks, missing_entity_date_prefix);
        migrate_back_rcriteria_aud(migration_pks, missing_entity_date_prefix);
        migrate_backend_filter_aud(migration_pks, missing_entity_date_prefix);
        migrate_certificate_aud(migration_pks, missing_entity_date_prefix);
        migrate_pm_configuration_aud(migration_pks, missing_entity_date_prefix);
        migrate_pm_configuration_raw_aud(migration_pks, missing_entity_date_prefix); -- XML (BLOB > LONGBLOB)
        migrate_pm_party_aud(migration_pks, missing_entity_date_prefix);
        migrate_pm_party_id_type_aud(migration_pks, missing_entity_date_prefix);
        migrate_pm_party_identifier_aud(migration_pks, missing_entity_date_prefix);
        migrate_routing_criteria_aud(migration_pks, missing_entity_date_prefix);
        migrate_user_aud(migration_pks, missing_entity_date_prefix);
        migrate_user_role_aud(migration_pks, missing_entity_date_prefix);
        migrate_user_roles_aud(migration_pks, missing_entity_date_prefix);
        -- END migrate primary keys to new format

    END migrate;

END MIGRATE_42_TO_50;
/