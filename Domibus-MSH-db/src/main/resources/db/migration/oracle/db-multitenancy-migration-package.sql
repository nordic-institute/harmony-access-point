-- ********************************************************************************************************
-- Domibus 4.2.3 to 5.0 data migration package
--
-- Main entry point is the procedure 'migrate'. To be executed into a begin/end; block
--
-- Parameters to be adjusted:
-- BATCH_SIZE - size of the batch for data migration on each migrated table after which there is a commit;
--              default value is 10000
-- BULK_COLLECT_LIMIT - limit to avoid reading a high number of records into memory; default value is 10000
-- VERBOSE_LOGS - more information into the logs; default to false
--
-- Tables which are migrated: TB_USER_MESSAGE, TB_MESSAGE_FRAGMENT, TB_MESSAGE_GROUP, TB_MESSAGE_HEADER,
-- TB_MESSAGE_LOG, TB_RECEIPT, TB_RECEIPT_DATA, TB_RAWENVELOPE_LOG, TB_PROPERTY, TB_PART_INFO,
-- TB_ERROR_LOG, TB_MESSAGE_ACKNW, TB_SEND_ATTEMPT
-- ********************************************************************************************************
CREATE OR REPLACE PACKAGE MIGRATE_42_TO_50 IS
    -- batch size for commit of the migrated records
    BATCH_SIZE CONSTANT NUMBER := 10000;

    -- limit loading a high number of records into memory
    BULK_COLLECT_LIMIT CONSTANT NUMBER := 10000;

    -- enable more verbose logs
    VERBOSE_LOGS CONSTANT BOOLEAN := FALSE;

    -- entry point for running the multitenancy general schema migration - to be executed in a BEGIN/END; block
    PROCEDURE migrate_multitenancy;

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
        date_format CONSTANT VARCHAR2(255) := 'YYMMDDHH24';
        len CONSTANT VARCHAR2(255) := 'FM0000000000';
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

    /**-- TB_D_TIMEZONE_OFFSET migration --*/
    PROCEDURE prepare_timezone_offset(migration_pks IN OUT JSON_OBJECT_T) IS
        v_id_pk NUMBER;
    BEGIN
        BEGIN
            SELECT ID_PK INTO v_id_pk FROM TB_D_TIMEZONE_OFFSET WHERE NEXT_ATTEMPT_TIMEZONE_ID='UTC';
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                v_id_pk := generate_id();
                update_migration_pks('timezone_offset', 1, v_id_pk, migration_pks);

                INSERT INTO TB_D_TIMEZONE_OFFSET (ID_PK, NEXT_ATTEMPT_TIMEZONE_ID, NEXT_ATTEMPT_OFFSET_SECONDS, CREATION_TIME, CREATED_BY)
                VALUES (v_id_pk, 'UTC', 0, SYSDATE, 'migration');

                COMMIT;
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
            FETCH c_alert BULK COLLECT INTO alert LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN alert.COUNT = 0;

            FOR i IN alert.FIRST .. alert.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(alert(i).ID_PK, alert(i).CREATION_TIME);
                        update_migration_pks('alert', alert(i).ID_PK, v_id_pk, migration_pks);
                        lookup_migration_pk('timezone_offset', migration_pks, 1, v_fk_timezone_offset);

                        INSERT INTO MIGR_TB_ALERT (ID_PK, ALERT_TYPE, ATTEMPTS_NUMBER, MAX_ATTEMPTS_NUMBER, PROCESSED,
                                                   PROCESSED_TIME, REPORTING_TIME, REPORTING_TIME_FAILURE, NEXT_ATTEMPT,
                                                   FK_TIMEZONE_OFFSET, ALERT_STATUS, ALERT_LEVEL, CREATION_TIME,
                                                   CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (v_id_pk,
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
                            alert(i).MODIFIED_BY);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_alert -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || alert.COUNT || ' records into ' || v_tab_new);
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
            FETCH c_event BULK COLLECT INTO event LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN event.COUNT = 0;

            FOR i IN event.FIRST .. event.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(event(i).ID_PK, event(i).CREATION_TIME);
                        update_migration_pks('event', event(i).ID_PK, v_id_pk, migration_pks);

                        INSERT INTO MIGR_TB_EVENT (ID_PK, EVENT_TYPE, REPORTING_TIME, LAST_ALERT_DATE, CREATION_TIME,
                                                   CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (v_id_pk,
                            event(i).EVENT_TYPE,
                            event(i).REPORTING_TIME,
                            event(i).LAST_ALERT_DATE,
                            event(i).CREATION_TIME,
                            event(i).CREATED_BY,
                            event(i).MODIFICATION_TIME,
                            event(i).MODIFIED_BY);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_event -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || event.COUNT || ' records into ' || v_tab_new);
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
            FETCH c_event_alert BULK COLLECT INTO event_alert LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN event_alert.COUNT = 0;

            FOR i IN event_alert.FIRST .. event_alert.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('alert', migration_pks, event_alert(i).FK_ALERT, v_fk_alert);
                        lookup_migration_pk('event', migration_pks, event_alert(i).FK_EVENT, v_fk_event);

                        INSERT INTO MIGR_TB_EVENT_ALERT (FK_EVENT, FK_ALERT, CREATION_TIME, CREATED_BY,
                                                         MODIFICATION_TIME, MODIFIED_BY)
                         VALUES (v_fk_event,
                            v_fk_alert,
                            event_alert(i).CREATION_TIME,
                            event_alert(i).CREATED_BY,
                            event_alert(i).MODIFICATION_TIME,
                            event_alert(i).MODIFIED_BY);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_event_alert -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || event_alert.COUNT || ' records into ' || v_tab_new);
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
            FETCH c_event_property BULK COLLECT INTO event_property LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN event_property.COUNT = 0;

            FOR i IN event_property.FIRST .. event_property.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(event_property(i).ID_PK, event_property(i).CREATION_TIME);
                        lookup_migration_pk('event', migration_pks, event_property(i).FK_EVENT, v_fk_event);

                        INSERT INTO MIGR_TB_EVENT_PROPERTY (ID_PK, PROPERTY_TYPE, FK_EVENT, DTYPE, STRING_VALUE,
                                                            DATE_VALUE, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                            MODIFIED_BY)
                        VALUES (v_id_pk,
                            event_property(i).PROPERTY_TYPE,
                            v_fk_event,
                            event_property(i).DTYPE,
                            event_property(i).STRING_VALUE,
                            event_property(i).DATE_VALUE,
                            event_property(i).CREATION_TIME,
                            event_property(i).CREATED_BY,
                            event_property(i).MODIFICATION_TIME,
                            event_property(i).MODIFIED_BY);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_event_property -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || event_property.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_event_property;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_event_property;

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
            FETCH c_command BULK COLLECT INTO command LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN command.COUNT = 0;

            FOR i IN command.FIRST .. command.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(command(i).ID_PK, command(i).CREATION_TIME);
                        update_migration_pks('command', command(i).ID_PK, v_id_pk, migration_pks);

                        INSERT INTO MIGR_TB_COMMAND (ID_PK, SERVER_NAME, COMMAND_NAME, CREATION_TIME, CREATED_BY,
                                                     MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (v_id_pk,
                            command(i).SERVER_NAME,
                            command(i).COMMAND_NAME,
                            command(i).CREATION_TIME,
                            command(i).CREATED_BY,
                            command(i).MODIFICATION_TIME,
                            command(i).MODIFIED_BY);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_command -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || command.COUNT || ' records into ' || v_tab_new);
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
            FETCH c_command_property BULK COLLECT INTO command_property LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN command_property.COUNT = 0;

            FOR i IN command_property.FIRST .. command_property.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('command', migration_pks, command_property(i).FK_COMMAND, v_fk_command);

                        INSERT INTO MIGR_TB_COMMAND_PROPERTY (PROPERTY_NAME, PROPERTY_VALUE, FK_COMMAND, CREATION_TIME,
                                                              CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (command_property(i).PROPERTY_NAME,
                            command_property(i).PROPERTY_VALUE,
                            v_fk_command,
                            command_property(i).CREATION_TIME,
                            command_property(i).CREATED_BY,
                            command_property(i).MODIFICATION_TIME,
                            command_property(i).MODIFIED_BY);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_command_property -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || command_property.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_command_property;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_command_property;

    PROCEDURE migrate_user_domain IS
        v_tab VARCHAR2(30) := 'TB_USER_DOMAIN';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_DOMAIN';

        v_id_pk NUMBER;

        CURSOR c_user_domain IS
            SELECT UD.ID_PK,
                   UD.USER_NAME,
                   UD.DOMAIN,
                   UD.PREFERRED_DOMAIN,
                   UD.CREATION_TIME,
                   UD.CREATED_BY,
                   UD.MODIFICATION_TIME,
                   UD.MODIFIED_BY
            FROM TB_USER_DOMAIN UD;
        TYPE T_USER_DOMAIN IS TABLE OF c_user_domain%rowtype;
        user_domain T_USER_DOMAIN;
        v_batch_no INT := 1;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_domain;
        LOOP
            FETCH c_user_domain BULK COLLECT INTO user_domain LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_domain.COUNT = 0;

            FOR i IN user_domain.FIRST .. user_domain.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(user_domain(i).ID_PK, user_domain(i).CREATION_TIME);

                        INSERT INTO MIGR_TB_USER_DOMAIN (ID_PK, USER_NAME, DOMAIN, PREFERRED_DOMAIN, CREATION_TIME,
                                                         CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (v_id_pk,
                            user_domain(i).USER_NAME,
                            user_domain(i).DOMAIN,
                            user_domain(i).PREFERRED_DOMAIN,
                            user_domain(i).CREATION_TIME,
                            user_domain(i).CREATED_BY,
                            user_domain(i).MODIFICATION_TIME,
                            user_domain(i).MODIFIED_BY);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_domain -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_domain.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_domain;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_domain;

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
            FETCH c_user BULK COLLECT INTO v_user LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN v_user.COUNT = 0;

            FOR i IN v_user.FIRST .. v_user.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(v_user(i).ID_PK, v_user(i).CREATION_TIME);
                        update_migration_pks('user', v_user(i).ID_PK, v_id_pk, migration_pks);

                        INSERT INTO MIGR_TB_USER (ID_PK, USER_EMAIL, USER_ENABLED, USER_PASSWORD, USER_NAME, OPTLOCK,
                                                  ATTEMPT_COUNT, SUSPENSION_DATE, USER_DELETED, PASSWORD_CHANGE_DATE,
                                                  DEFAULT_PASSWORD, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                  MODIFIED_BY)
                        VALUES (v_id_pk,
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
                            v_user(i).MODIFIED_BY);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || v_user.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user;

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
            FETCH c_user_password_history BULK COLLECT INTO user_password_history LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_password_history.COUNT = 0;

            FOR i IN user_password_history.FIRST .. user_password_history.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(user_password_history(i).ID_PK, user_password_history(i).CREATION_TIME);
                        lookup_migration_pk('user', migration_pks, user_password_history(i).USER_ID, v_user_id);

                        INSERT INTO MIGR_TB_USER_PASSWORD_HISTORY (ID_PK, USER_ID, USER_PASSWORD, PASSWORD_CHANGE_DATE,
                                                                   CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                                   MODIFIED_BY)
                        VALUES (v_id_pk,
                            v_user_id,
                            user_password_history(i).USER_PASSWORD,
                            user_password_history(i).PASSWORD_CHANGE_DATE,
                            user_password_history(i).CREATION_TIME,
                            user_password_history(i).CREATED_BY,
                            user_password_history(i).MODIFICATION_TIME,
                            user_password_history(i).MODIFIED_BY);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_password_history -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_password_history.COUNT || ' records into ' || v_tab_new);
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
            FETCH c_user_role BULK COLLECT INTO user_role LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_role.COUNT = 0;

            FOR i IN user_role.FIRST .. user_role.LAST
                LOOP
                    BEGIN
                        v_id_pk := generate_scalable_seq(user_role(i).ID_PK, user_role(i).CREATION_TIME);
                        update_migration_pks('user_role', user_role(i).ID_PK, v_id_pk, migration_pks);

                        INSERT INTO MIGR_TB_USER_ROLE (ID_PK, ROLE_NAME, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                       MODIFIED_BY)
                        VALUES (v_id_pk,
                            user_role(i).ROLE_NAME,
                            user_role(i).CREATION_TIME,
                            user_role(i).CREATED_BY,
                            user_role(i).MODIFICATION_TIME,
                            user_role(i).MODIFIED_BY);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_role -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_role.COUNT || ' records into ' || v_tab_new);
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
            FETCH c_user_roles BULK COLLECT INTO user_roles LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_roles.COUNT = 0;

            FOR i IN user_roles.FIRST .. user_roles.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('user', migration_pks, user_roles(i).USER_ID, v_user_id);
                        lookup_migration_pk('user_role', migration_pks, user_roles(i).ROLE_ID, v_role_id);

                        INSERT INTO MIGR_TB_USER_ROLES (USER_ID, ROLE_ID, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                        MODIFIED_BY)
                        VALUES (v_user_id,
                            v_role_id,
                            user_roles(i).CREATION_TIME,
                            user_roles(i).CREATED_BY,
                            user_roles(i).MODIFICATION_TIME,
                            user_roles(i).MODIFIED_BY);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_roles -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_roles.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_roles;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_roles;

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
            FETCH c_rev_info BULK COLLECT INTO rev_info LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN rev_info.COUNT = 0;

            FOR i IN rev_info.FIRST .. rev_info.LAST
                LOOP
                    BEGIN
                        v_id := generate_scalable_seq(rev_info(i).ID, rev_info(i).REVISION_DATE);
                        update_migration_pks('rev_info', rev_info(i).ID, v_id, migration_pks);

                        INSERT INTO MIGR_TB_REV_INFO (ID, TIMESTAMP, REVISION_DATE, USER_NAME)
                        VALUES (v_id,
                            rev_info(i).TIMESTAMP,
                            rev_info(i).REVISION_DATE,
                            rev_info(i).USER_NAME);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_rev_info -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || rev_info.COUNT || ' records into ' || v_tab_new);
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
            FETCH c_rev_changes BULK COLLECT INTO rev_changes LIMIT BULK_COLLECT_LIMIT;
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

                        INSERT INTO MIGR_TB_REV_CHANGES (ID_PK, REV, AUDIT_ORDER, ENTIY_NAME, GROUP_NAME, ENTITY_ID,
                                                         MODIFICATION_TYPE, CREATION_TIME, CREATED_BY,
                                                         MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (v_id_pk,
                            v_rev,
                            rev_changes(i).AUDIT_ORDER,
                            rev_changes(i).ENTIY_NAME,
                            rev_changes(i).GROUP_NAME,
                            TO_CHAR(v_entity_id),
                            rev_changes(i).MODIFICATION_TYPE,
                            rev_changes(i).CREATION_TIME,
                            rev_changes(i).CREATED_BY,
                            rev_changes(i).MODIFICATION_TIME,
                            rev_changes(i).MODIFIED_BY);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_rev_changes -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || rev_changes.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_rev_changes;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_rev_changes;

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
            FETCH c_user_aud BULK COLLECT INTO user_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_aud.COUNT = 0;

            FOR i IN user_aud.FIRST .. user_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, user_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('user', migration_pks, missing_entity_date_prefix, user_aud(i).ID_PK, v_id_pk);

                        INSERT INTO MIGR_TB_USER_AUD (ID_PK, REV, REVTYPE, USER_ENABLED, ACTIVE_MOD, USER_DELETED,
                                                      DELETED_MOD, USER_EMAIL, EMAIL_MOD, USER_PASSWORD, PASSWORD_MOD,
                                                      USER_NAME, USERNAME_MOD, OPTLOCK, VERSION_MOD, ROLES_MOD,
                                                      PASSWORD_CHANGE_DATE, PASSWORDCHANGEDATE_MOD, DEFAULT_PASSWORD,
                                                      DEFAULTPASSWORD_MOD)
                        VALUES (v_id_pk,
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
                            user_aud(i).DEFAULTPASSWORD_MOD);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_aud.COUNT || ' records into ' || v_tab_new);
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
            FETCH c_user_role_aud BULK COLLECT INTO user_role_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_role_aud.COUNT = 0;

            FOR i IN user_role_aud.FIRST .. user_role_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, user_role_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('user_role', migration_pks, missing_entity_date_prefix, user_role_aud(i).ID_PK, v_id_pk);

                        INSERT INTO MIGR_TB_USER_ROLE_AUD (ID_PK, REV, REVTYPE, ROLE_NAME, NAME_MOD, USERS_MOD)
                        VALUES (v_id_pk,
                            v_rev,
                            user_role_aud(i).REVTYPE,
                            user_role_aud(i).ROLE_NAME,
                            user_role_aud(i).NAME_MOD,
                            user_role_aud(i).USERS_MOD);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_role_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_role_aud.COUNT || ' records into ' || v_tab_new);
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
            FETCH c_user_roles_aud BULK COLLECT INTO user_roles_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_roles_aud.COUNT = 0;

            FOR i IN user_roles_aud.FIRST .. user_roles_aud.LAST
                LOOP
                    BEGIN
                        lookup_migration_pk('rev_info', migration_pks, user_roles_aud(i).REV, v_rev);
                        lookup_audit_migration_pk('user', migration_pks, missing_entity_date_prefix, user_roles_aud(i).USER_ID, v_user_id);
                        lookup_audit_migration_pk('user_role', migration_pks, missing_entity_date_prefix, user_roles_aud(i).ROLE_ID, v_role_id);

                        INSERT INTO MIGR_TB_USER_ROLES_AUD (REV, REVTYPE, USER_ID, ROLE_ID)
                        VALUES (v_rev,
                            user_roles_aud(i).REVTYPE,
                            v_user_id,
                            v_role_id);

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_roles_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_roles_aud.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_roles_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_roles_aud;

    /**-- main entry point for running the multitenancy general schema migration --*/
    PROCEDURE migrate_multitenancy IS
        migration_pks JSON_OBJECT_T;
        missing_entity_date_prefix DATE;
    BEGIN
        migration_pks := new JSON_OBJECT_T();

        -- keep it in this order
        prepare_timezone_offset(migration_pks);

        -- START migrate primary keys to new format
        migrate_alert(migration_pks);
        migrate_event(migration_pks);
        migrate_event_alert(migration_pks);
        migrate_event_property(migration_pks);
        --
        migrate_command(migration_pks);
        migrate_command_property(migration_pks);
        --
        migrate_user_domain;
        --
        migrate_user(migration_pks);
        migrate_user_password_history(migration_pks);
        migrate_user_role(migration_pks);
        migrate_user_roles(migration_pks);
        --
        missing_entity_date_prefix := SYSDATE;
        migrate_rev_info(migration_pks);
        migrate_rev_changes(migration_pks, missing_entity_date_prefix);
        migrate_user_aud(migration_pks, missing_entity_date_prefix);
        migrate_user_role_aud(migration_pks, missing_entity_date_prefix);
        migrate_user_roles_aud(migration_pks, missing_entity_date_prefix);
        -- END migrate primary keys to new format

    END migrate_multitenancy;

END MIGRATE_42_TO_50;
/