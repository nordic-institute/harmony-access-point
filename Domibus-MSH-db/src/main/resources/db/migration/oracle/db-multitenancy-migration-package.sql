-- ********************************************************************************************************
-- Domibus 4.2.7 to 5.0 data migration package
--
-- Main entry point is the procedure 'migrate'. To be executed into a begin/end; block
--
-- Parameters to be adjusted:
-- BATCH_SIZE - size of the batch for data migration on each migrated table after which there is a commit;
--              default value is 100
-- BULK_COLLECT_LIMIT - limit to avoid reading a high number of records into memory; default value is 100
-- VERBOSE_LOGS - more information into the logs; default to false
-- ********************************************************************************************************

DECLARE
    table_does_not_exist exception;
    PRAGMA EXCEPTION_INIT(table_does_not_exist, -942);
BEGIN
    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_ACTION_AUDIT';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_ACTION_AUDIT: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_ALERT';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_ALERT: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_AUTH_ENTRY';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_AUTH_ENTRY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_BACKEND_FILTER';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_BACKEND_FILTER: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_CERTIFICATE';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_CERTIFICATE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_COMMAND';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_COMMAND: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_EVENT';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_EVENT: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_CONF_RAW';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_CONF_RAW: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_CONFIGURATION';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_CONFIGURATION: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_PARTY';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_PARTY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_PARTY_ID_TYPE';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_PARTY_ID_TYPE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_REV_INFO';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_REV_INFO: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_ROUTING_CRITERIA';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_ROUTING_CRITERIA: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_TIMEZONE_OFFSET';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_TIMEZONE_OFFSET: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_USER';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_USER: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_USER_ROLE';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_USER_ROLE: table does not exist');
    END;
END;
/

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_ACTION_AUDIT (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_ACTION_AUDIT PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_ALERT (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_ALERT PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_AUTH_ENTRY (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_AUTH_ENTRY PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_BACKEND_FILTER (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_BACKEND_FILTER PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_CERTIFICATE (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_CERTIFICATE PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_COMMAND (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_COMMAND PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_EVENT (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_EVENT PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_CONF_RAW (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_CONF_RAW PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_CONFIGURATION (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_CONFIGURATION PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_PARTY (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_PARTY PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_PARTY_ID_TYPE (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_PARTY_ID_TYPE PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_REV_INFO (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_REV_INFO PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_ROUTING_CRITERIA (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_ROUTING_CRITERIA PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_TIMEZONE_OFFSET (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_TIMEZONE_OFFSET PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_USER (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_USER PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_USER_ROLE (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_USER_ROLE PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE OR REPLACE PACKAGE MIGRATE_42_TO_50 IS
    -- batch size for commit of the migrated records
    BATCH_SIZE CONSTANT NUMBER := 100;

    -- limit loading a high number of records into memory
    BULK_COLLECT_LIMIT CONSTANT NUMBER := 100;

    -- enable more verbose logs
    VERBOSE_LOGS CONSTANT BOOLEAN := FALSE;

    -- entry point for running the multitenancy general schema migration - to be executed in a BEGIN/END; block
    PROCEDURE migrate_multitenancy;

END MIGRATE_42_TO_50;
/

CREATE OR REPLACE PACKAGE BODY MIGRATE_42_TO_50 IS

    /** -- FORALL exception handling -*/
    failure_in_forall EXCEPTION;
    PRAGMA EXCEPTION_INIT (failure_in_forall, -24381);

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
            DBMS_OUTPUT.PUT_LINE('Table ' || tab_name1 || ' has different number of records - ' || v_count_tab1 ||
                                 ' - than table '|| tab_name2 || ' - ' || v_count_tab2 || ' -');
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

    /**-- TB_D_TIMEZONE_OFFSET migration --*/
    PROCEDURE prepare_timezone_offset IS
        v_id_pk NUMBER;
    BEGIN
        BEGIN
            SELECT ID_PK INTO v_id_pk FROM TB_D_TIMEZONE_OFFSET WHERE NEXT_ATTEMPT_TIMEZONE_ID='UTC';
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                v_id_pk := generate_id();

                INSERT INTO MIGR_TB_PKS_TIMEZONE_OFFSET (OLD_ID, NEW_ID)
                VALUES(1, v_id_pk);

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

    PROCEDURE migrate_alert IS
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

        TYPE T_MIGR_ALERT IS TABLE OF MIGR_TB_ALERT%ROWTYPE INDEX BY PLS_INTEGER;
        migr_alert T_MIGR_ALERT;

        TYPE T_MIGR_PKS_ALERT IS TABLE OF MIGR_TB_PKS_ALERT%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_alert T_MIGR_PKS_ALERT;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_alert;
        LOOP
            FETCH c_alert BULK COLLECT INTO alert LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN alert.COUNT = 0;

            migr_alert := T_MIGR_ALERT();
            migr_pks_alert := T_MIGR_PKS_ALERT();

            SELECT MPKSTO.NEW_ID
            INTO v_fk_timezone_offset
            FROM MIGR_TB_PKS_TIMEZONE_OFFSET MPKSTO
            WHERE MPKSTO.OLD_ID = 1;

            FOR i IN alert.FIRST .. alert.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(alert(i).ID_PK, alert(i).CREATION_TIME);

                    migr_pks_alert(i).OLD_ID := alert(i).ID_PK;
                    migr_pks_alert(i).NEW_ID := v_id_pk;

                    migr_alert(i).ID_PK := v_id_pk;
                    migr_alert(i).ALERT_TYPE := alert(i).ALERT_TYPE;
                    migr_alert(i).ATTEMPTS_NUMBER := alert(i).ATTEMPTS_NUMBER;
                    migr_alert(i).MAX_ATTEMPTS_NUMBER := alert(i).MAX_ATTEMPTS_NUMBER;
                    migr_alert(i).PROCESSED := alert(i).PROCESSED;
                    migr_alert(i).PROCESSED_TIME := alert(i).PROCESSED_TIME;
                    migr_alert(i).REPORTING_TIME := alert(i).REPORTING_TIME;
                    migr_alert(i).REPORTING_TIME_FAILURE := alert(i).REPORTING_TIME_FAILURE;
                    migr_alert(i).NEXT_ATTEMPT := alert(i).NEXT_ATTEMPT;
                    migr_alert(i).FK_TIMEZONE_OFFSET := v_fk_timezone_offset;
                    migr_alert(i).ALERT_STATUS := alert(i).ALERT_STATUS;
                    migr_alert(i).ALERT_LEVEL := alert(i).ALERT_LEVEL;
                    migr_alert(i).CREATION_TIME := alert(i).CREATION_TIME;
                    migr_alert(i).CREATED_BY := alert(i).CREATED_BY;
                    migr_alert(i).MODIFICATION_TIME := alert(i).MODIFICATION_TIME;
                    migr_alert(i).MODIFIED_BY := alert(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_alert.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_alert -> update alert lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_ALERT (OLD_ID, NEW_ID)
                    VALUES (migr_pks_alert(i).OLD_ID,
                            migr_pks_alert(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_alert -> start-end: ' || v_start || '-' || v_end);

                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_ALERT (ID_PK, ALERT_TYPE, ATTEMPTS_NUMBER, MAX_ATTEMPTS_NUMBER, PROCESSED,
                                                   PROCESSED_TIME, REPORTING_TIME, REPORTING_TIME_FAILURE, NEXT_ATTEMPT,
                                                   FK_TIMEZONE_OFFSET, ALERT_STATUS, ALERT_LEVEL, CREATION_TIME,
                                                   CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_alert(i).ID_PK,
                                migr_alert(i).ALERT_TYPE,
                                migr_alert(i).ATTEMPTS_NUMBER,
                                migr_alert(i).MAX_ATTEMPTS_NUMBER,
                                migr_alert(i).PROCESSED,
                                migr_alert(i).PROCESSED_TIME,
                                migr_alert(i).REPORTING_TIME,
                                migr_alert(i).REPORTING_TIME_FAILURE,
                                migr_alert(i).NEXT_ATTEMPT,
                                migr_alert(i).FK_TIMEZONE_OFFSET,
                                migr_alert(i).ALERT_STATUS,
                                migr_alert(i).ALERT_LEVEL,
                                migr_alert(i).CREATION_TIME,
                                migr_alert(i).CREATED_BY,
                                migr_alert(i).MODIFICATION_TIME,
                                migr_alert(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_alert -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_alert(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || alert.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_alert;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_alert;

    PROCEDURE migrate_event IS
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

        TYPE T_MIGR_EVENT IS TABLE OF MIGR_TB_EVENT%ROWTYPE INDEX BY PLS_INTEGER;
        migr_event T_MIGR_EVENT;

        TYPE T_MIGR_PKS_EVENT IS TABLE OF MIGR_TB_PKS_EVENT%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_event T_MIGR_PKS_EVENT;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_event;
        LOOP
            FETCH c_event BULK COLLECT INTO event LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN event.COUNT = 0;

            migr_event := T_MIGR_EVENT();
            migr_pks_event := T_MIGR_PKS_EVENT();

            FOR i IN event.FIRST .. event.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(event(i).ID_PK, event(i).CREATION_TIME);

                    migr_pks_event(i).OLD_ID := event(i).ID_PK;
                    migr_pks_event(i).NEW_ID := v_id_pk;

                    migr_event(i).ID_PK := v_id_pk;
                    migr_event(i).EVENT_TYPE := event(i).EVENT_TYPE;
                    migr_event(i).REPORTING_TIME := event(i).REPORTING_TIME;
                    migr_event(i).LAST_ALERT_DATE := event(i).LAST_ALERT_DATE;
                    migr_event(i).CREATION_TIME := event(i).CREATION_TIME;
                    migr_event(i).CREATED_BY := event(i).CREATED_BY;
                    migr_event(i).MODIFICATION_TIME := event(i).MODIFICATION_TIME;
                    migr_event(i).MODIFIED_BY := event(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_event.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_event -> update event lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_EVENT (OLD_ID, NEW_ID)
                    VALUES (migr_pks_event(i).OLD_ID,
                            migr_pks_event(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_event -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_EVENT (ID_PK, EVENT_TYPE, REPORTING_TIME, LAST_ALERT_DATE, CREATION_TIME,
                                                   CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_event(i).ID_PK,
                                migr_event(i).EVENT_TYPE,
                                migr_event(i).REPORTING_TIME,
                                migr_event(i).LAST_ALERT_DATE,
                                migr_event(i).CREATION_TIME,
                                migr_event(i).CREATED_BY,
                                migr_event(i).MODIFICATION_TIME,
                                migr_event(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_event -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_event(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || event.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_event;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_event;

    PROCEDURE migrate_event_alert IS
        v_tab VARCHAR2(30) := 'TB_EVENT_ALERT';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_EVENT_ALERT';

        CURSOR c_event_alert IS
            SELECT (SELECT MPKSE.NEW_ID
                    FROM MIGR_TB_PKS_EVENT MPKSE
                    WHERE MPKSE.OLD_ID = EA.FK_EVENT) AS FK_EVENT,
                   (SELECT MPKSA.NEW_ID
                    FROM MIGR_TB_PKS_ALERT MPKSA
                    WHERE MPKSA.OLD_ID = EA.FK_ALERT) AS FK_ALERT,
                   EA.CREATION_TIME,
                   EA.CREATED_BY,
                   EA.MODIFICATION_TIME,
                   EA.MODIFIED_BY
            FROM TB_EVENT_ALERT EA;

        TYPE T_EVENT_ALERT IS TABLE OF c_event_alert%rowtype;
        event_alert T_EVENT_ALERT;

        TYPE T_MIGR_EVENT_ALERT IS TABLE OF MIGR_TB_EVENT_ALERT%ROWTYPE INDEX BY PLS_INTEGER;
        migr_event_alert T_MIGR_EVENT_ALERT;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_event_alert;
        LOOP
            FETCH c_event_alert BULK COLLECT INTO event_alert LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN event_alert.COUNT = 0;

            migr_event_alert := T_MIGR_EVENT_ALERT();

            FOR i IN event_alert.FIRST .. event_alert.LAST
                LOOP
                    migr_event_alert(i).FK_EVENT := event_alert(i).FK_EVENT;
                    migr_event_alert(i).FK_ALERT := event_alert(i).FK_ALERT;
                    migr_event_alert(i).CREATION_TIME := event_alert(i).CREATION_TIME;
                    migr_event_alert(i).CREATED_BY := event_alert(i).CREATED_BY;
                    migr_event_alert(i).MODIFICATION_TIME := event_alert(i).MODIFICATION_TIME;
                    migr_event_alert(i).MODIFIED_BY := event_alert(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_event_alert.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_event_alert -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_EVENT_ALERT (FK_EVENT, FK_ALERT, CREATION_TIME, CREATED_BY,
                                                         MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_event_alert(i).FK_EVENT,
                                migr_event_alert(i).FK_ALERT,
                                migr_event_alert(i).CREATION_TIME,
                                migr_event_alert(i).CREATED_BY,
                                migr_event_alert(i).MODIFICATION_TIME,
                                migr_event_alert(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_event_alert -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having FK_EVENT '
                                        || migr_event_alert(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).FK_EVENT
                                        || '  and FK_ALERT '
                                        || migr_event_alert(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).FK_ALERT);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || event_alert.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_event_alert;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_event_alert;

    PROCEDURE migrate_event_property IS
        v_tab VARCHAR2(30) := 'TB_EVENT_PROPERTY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_EVENT_PROPERTY';

        CURSOR c_event_property IS
            SELECT EP.ID_PK,
                   EP.PROPERTY_TYPE,
                   (SELECT MPKSE.NEW_ID
                    FROM MIGR_TB_PKS_EVENT MPKSE
                    WHERE MPKSE.OLD_ID = EP.FK_EVENT) AS FK_EVENT,
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

        TYPE T_MIGR_EVENT_PROPERTY IS TABLE OF MIGR_TB_EVENT_PROPERTY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_event_property T_MIGR_EVENT_PROPERTY;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_event_property;
        LOOP
            FETCH c_event_property BULK COLLECT INTO event_property LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN event_property.COUNT = 0;

            migr_event_property := T_MIGR_EVENT_PROPERTY();

            FOR i IN event_property.FIRST .. event_property.LAST
                LOOP
                    migr_event_property(i).ID_PK := generate_scalable_seq(event_property(i).ID_PK, event_property(i).CREATION_TIME);
                    migr_event_property(i).PROPERTY_TYPE := event_property(i).PROPERTY_TYPE;
                    migr_event_property(i).FK_EVENT := event_property(i).FK_EVENT;
                    migr_event_property(i).DTYPE := event_property(i).DTYPE;
                    migr_event_property(i).STRING_VALUE := event_property(i).STRING_VALUE;
                    migr_event_property(i).DATE_VALUE := event_property(i).DATE_VALUE;
                    migr_event_property(i).CREATION_TIME := event_property(i).CREATION_TIME;
                    migr_event_property(i).CREATED_BY := event_property(i).CREATED_BY;
                    migr_event_property(i).MODIFICATION_TIME := event_property(i).MODIFICATION_TIME;
                    migr_event_property(i).MODIFIED_BY := event_property(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_event_property.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_event_property -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_EVENT_PROPERTY (ID_PK, PROPERTY_TYPE, FK_EVENT, DTYPE, STRING_VALUE,
                                                            DATE_VALUE, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                            MODIFIED_BY)
                        VALUES (migr_event_property(i).ID_PK,
                                migr_event_property(i).PROPERTY_TYPE,
                                migr_event_property(i).FK_EVENT,
                                migr_event_property(i).DTYPE,
                                migr_event_property(i).STRING_VALUE,
                                migr_event_property(i).DATE_VALUE,
                                migr_event_property(i).CREATION_TIME,
                                migr_event_property(i).CREATED_BY,
                                migr_event_property(i).MODIFICATION_TIME,
                                migr_event_property(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_event_property -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_event_property(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || event_property.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_event_property;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_event_property;

    PROCEDURE migrate_command IS
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

        TYPE T_MIGR_COMMAND IS TABLE OF MIGR_TB_COMMAND%ROWTYPE INDEX BY PLS_INTEGER;
        migr_command T_MIGR_COMMAND;

        TYPE T_MIGR_PKS_COMMAND IS TABLE OF MIGR_TB_PKS_COMMAND%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_command T_MIGR_PKS_COMMAND;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_command;
        LOOP
            FETCH c_command BULK COLLECT INTO command LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN command.COUNT = 0;

            migr_command := T_MIGR_COMMAND();
            migr_pks_command := T_MIGR_PKS_COMMAND();

            FOR i IN command.FIRST .. command.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(command(i).ID_PK, command(i).CREATION_TIME);

                    migr_pks_command(i).OLD_ID := command(i).ID_PK;
                    migr_pks_command(i).NEW_ID := v_id_pk;

                    migr_command(i).ID_PK := v_id_pk;
                    migr_command(i).SERVER_NAME := command(i).SERVER_NAME;
                    migr_command(i).COMMAND_NAME := command(i).COMMAND_NAME;
                    migr_command(i).CREATION_TIME := command(i).CREATION_TIME;
                    migr_command(i).CREATED_BY := command(i).CREATED_BY;
                    migr_command(i).MODIFICATION_TIME := command(i).MODIFICATION_TIME;
                    migr_command(i).MODIFIED_BY := command(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_command.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_command -> update command lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_COMMAND (OLD_ID, NEW_ID)
                    VALUES (migr_pks_command(i).OLD_ID,
                            migr_pks_command(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_command -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_COMMAND (ID_PK, SERVER_NAME, COMMAND_NAME, CREATION_TIME, CREATED_BY,
                                                     MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_command(i).ID_PK,
                                migr_command(i).SERVER_NAME,
                                migr_command(i).COMMAND_NAME,
                                migr_command(i).CREATION_TIME,
                                migr_command(i).CREATED_BY,
                                migr_command(i).MODIFICATION_TIME,
                                migr_command(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_command -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_command(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || command.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_command;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_command;

    PROCEDURE migrate_command_property IS
        v_tab VARCHAR2(30) := 'TB_COMMAND_PROPERTY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_COMMAND_PROPERTY';

        CURSOR c_command_property IS
            SELECT CP.PROPERTY_NAME,
                   CP.PROPERTY_VALUE,
                   (SELECT MPKSC.NEW_ID
                    FROM MIGR_TB_PKS_COMMAND MPKSC
                    WHERE MPKSC.OLD_ID = CP.FK_COMMAND) AS FK_COMMAND,
                   CP.CREATION_TIME,
                   CP.CREATED_BY,
                   CP.MODIFICATION_TIME,
                   CP.MODIFIED_BY
            FROM TB_COMMAND_PROPERTY CP;

        TYPE T_COMMAND_PROPERTY IS TABLE OF c_command_property%rowtype;
        command_property T_COMMAND_PROPERTY;

        TYPE T_MIGR_COMMAND_PROPERTY IS TABLE OF MIGR_TB_COMMAND_PROPERTY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_command_property T_MIGR_COMMAND_PROPERTY;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_command_property;
        LOOP
            FETCH c_command_property BULK COLLECT INTO command_property LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN command_property.COUNT = 0;

            migr_command_property := T_MIGR_COMMAND_PROPERTY();

            FOR i IN command_property.FIRST .. command_property.LAST
                LOOP
                    migr_command_property(i).PROPERTY_NAME := command_property(i).PROPERTY_NAME;
                    migr_command_property(i).PROPERTY_VALUE := command_property(i).PROPERTY_VALUE;
                    migr_command_property(i).FK_COMMAND := command_property(i).FK_COMMAND;
                    migr_command_property(i).CREATION_TIME := command_property(i).CREATION_TIME;
                    migr_command_property(i).CREATED_BY := command_property(i).CREATED_BY;
                    migr_command_property(i).MODIFICATION_TIME := command_property(i).MODIFICATION_TIME;
                    migr_command_property(i).MODIFIED_BY := command_property(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_command_property.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_command_property -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_COMMAND_PROPERTY (PROPERTY_NAME, PROPERTY_VALUE, FK_COMMAND, CREATION_TIME,
                                                              CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_command_property(i).PROPERTY_NAME,
                                migr_command_property(i).PROPERTY_VALUE,
                                migr_command_property(i).FK_COMMAND,
                                migr_command_property(i).CREATION_TIME,
                                migr_command_property(i).CREATED_BY,
                                migr_command_property(i).MODIFICATION_TIME,
                                migr_command_property(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_command_property -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having PROPERTY_NAME '
                                        || migr_command_property(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).PROPERTY_NAME
                                        || '  and PROPERTY_VALUE '
                                        || migr_command_property(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).PROPERTY_VALUE
                                        || '  and FK_COMMAND '
                                        || migr_command_property(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).FK_COMMAND);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || command_property.COUNT || ' records into ' || v_tab_new);
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

        TYPE T_MIGR_USER_DOMAIN IS TABLE OF MIGR_TB_USER_DOMAIN%ROWTYPE INDEX BY PLS_INTEGER;
        migr_user_domain T_MIGR_USER_DOMAIN;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_domain;
        LOOP
            FETCH c_user_domain BULK COLLECT INTO user_domain LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_domain.COUNT = 0;

            migr_user_domain := T_MIGR_USER_DOMAIN();

            FOR i IN user_domain.FIRST .. user_domain.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(user_domain(i).ID_PK, user_domain(i).CREATION_TIME);

                    migr_user_domain(i).ID_PK := v_id_pk;
                    migr_user_domain(i).USER_NAME := user_domain(i).USER_NAME;
                    migr_user_domain(i).DOMAIN := user_domain(i).DOMAIN;
                    migr_user_domain(i).PREFERRED_DOMAIN := user_domain(i).PREFERRED_DOMAIN;
                    migr_user_domain(i).CREATION_TIME := user_domain(i).CREATION_TIME;
                    migr_user_domain(i).CREATED_BY := user_domain(i).CREATED_BY;
                    migr_user_domain(i).MODIFICATION_TIME := user_domain(i).MODIFICATION_TIME;
                    migr_user_domain(i).MODIFIED_BY := user_domain(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_user_domain.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    DBMS_OUTPUT.PUT_LINE('migrate_user_domain -> start-end: ' || v_start || '-' || v_end);

                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_USER_DOMAIN (ID_PK, USER_NAME, DOMAIN, PREFERRED_DOMAIN, CREATION_TIME,
                                                         CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_user_domain(i).ID_PK,
                                migr_user_domain(i).USER_NAME,
                                migr_user_domain(i).DOMAIN,
                                migr_user_domain(i).PREFERRED_DOMAIN,
                                migr_user_domain(i).CREATION_TIME,
                                migr_user_domain(i).CREATED_BY,
                                migr_user_domain(i).MODIFICATION_TIME,
                                migr_user_domain(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_domain -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_user_domain(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;
                DBMS_OUTPUT.PUT_LINE(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_domain.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_domain;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_domain;

    PROCEDURE migrate_user IS
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

        TYPE T_MIGR_USER IS TABLE OF MIGR_TB_USER%ROWTYPE INDEX BY PLS_INTEGER;
        migr_user T_MIGR_USER;

        TYPE T_MIGR_PKS_USER IS TABLE OF MIGR_TB_PKS_USER%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_user T_MIGR_PKS_USER;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user;
        LOOP
            FETCH c_user BULK COLLECT INTO v_user LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN v_user.COUNT = 0;

            migr_user := T_MIGR_USER();
            migr_pks_user := T_MIGR_PKS_USER();

            FOR i IN v_user.FIRST .. v_user.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(v_user(i).ID_PK, v_user(i).CREATION_TIME);

                    migr_pks_user(i).OLD_ID := v_user(i).ID_PK;
                    migr_pks_user(i).NEW_ID := v_id_pk;

                    migr_user(i).ID_PK := v_id_pk;
                    migr_user(i).USER_EMAIL := v_user(i).USER_EMAIL;
                    migr_user(i).USER_ENABLED := v_user(i).USER_ENABLED;
                    migr_user(i).USER_PASSWORD := v_user(i).USER_PASSWORD;
                    migr_user(i).USER_NAME := v_user(i).USER_NAME;
                    migr_user(i).OPTLOCK := v_user(i).OPTLOCK;
                    migr_user(i).ATTEMPT_COUNT := v_user(i).ATTEMPT_COUNT;
                    migr_user(i).SUSPENSION_DATE := v_user(i).SUSPENSION_DATE;
                    migr_user(i).USER_DELETED := v_user(i).USER_DELETED;
                    migr_user(i).PASSWORD_CHANGE_DATE := v_user(i).PASSWORD_CHANGE_DATE;
                    migr_user(i).DEFAULT_PASSWORD := v_user(i).DEFAULT_PASSWORD;
                    migr_user(i).CREATION_TIME := v_user(i).CREATION_TIME;
                    migr_user(i).CREATED_BY := v_user(i).CREATED_BY;
                    migr_user(i).MODIFICATION_TIME := v_user(i).MODIFICATION_TIME;
                    migr_user(i).MODIFIED_BY := v_user(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_user.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_user -> update user lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_USER (OLD_ID, NEW_ID)
                    VALUES (migr_pks_user(i).OLD_ID,
                            migr_pks_user(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_user -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_USER (ID_PK, USER_EMAIL, USER_ENABLED, USER_PASSWORD, USER_NAME, OPTLOCK,
                                                  ATTEMPT_COUNT, SUSPENSION_DATE, USER_DELETED, PASSWORD_CHANGE_DATE,
                                                  DEFAULT_PASSWORD, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                  MODIFIED_BY)
                        VALUES (migr_user(i).ID_PK,
                                migr_user(i).USER_EMAIL,
                                migr_user(i).USER_ENABLED,
                                migr_user(i).USER_PASSWORD,
                                migr_user(i).USER_NAME,
                                migr_user(i).OPTLOCK,
                                migr_user(i).ATTEMPT_COUNT,
                                migr_user(i).SUSPENSION_DATE,
                                migr_user(i).USER_DELETED,
                                migr_user(i).PASSWORD_CHANGE_DATE,
                                migr_user(i).DEFAULT_PASSWORD,
                                migr_user(i).CREATION_TIME,
                                migr_user(i).CREATED_BY,
                                migr_user(i).MODIFICATION_TIME,
                                migr_user(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_user(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || v_user.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user;

    PROCEDURE migrate_user_password_history IS
        v_tab VARCHAR2(30) := 'TB_USER_PASSWORD_HISTORY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_PASSWORD_HISTORY';

        CURSOR c_user_password_history IS
            SELECT UPH.ID_PK,
                   (SELECT MPKU.NEW_ID
                    FROM MIGR_TB_PKS_USER MPKU
                    WHERE MPKU.OLD_ID = UPH.USER_ID) AS USER_ID,
                   UPH.USER_PASSWORD,
                   UPH.PASSWORD_CHANGE_DATE,
                   UPH.CREATION_TIME,
                   UPH.CREATED_BY,
                   UPH.MODIFICATION_TIME,
                   UPH.MODIFIED_BY
            FROM TB_USER_PASSWORD_HISTORY UPH;

        TYPE T_USER_PASSWORD_HISTORY IS TABLE OF c_user_password_history%rowtype;
        user_password_history T_USER_PASSWORD_HISTORY;

        TYPE T_MIGR_USER_PASSWORD_HISTORY IS TABLE OF MIGR_TB_USER_PASSWORD_HISTORY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_user_password_history T_MIGR_USER_PASSWORD_HISTORY;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_password_history;
        LOOP
            FETCH c_user_password_history BULK COLLECT INTO user_password_history LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_password_history.COUNT = 0;

            migr_user_password_history := T_MIGR_USER_PASSWORD_HISTORY();

            FOR i IN user_password_history.FIRST .. user_password_history.LAST
                LOOP
                    migr_user_password_history(i).ID_PK := generate_scalable_seq(user_password_history(i).ID_PK, user_password_history(i).CREATION_TIME);
                    migr_user_password_history(i).USER_ID := user_password_history(i).USER_ID;
                    migr_user_password_history(i).USER_PASSWORD := user_password_history(i).USER_PASSWORD;
                    migr_user_password_history(i).PASSWORD_CHANGE_DATE := user_password_history(i).PASSWORD_CHANGE_DATE;
                    migr_user_password_history(i).CREATION_TIME := user_password_history(i).CREATION_TIME;
                    migr_user_password_history(i).CREATED_BY := user_password_history(i).CREATED_BY;
                    migr_user_password_history(i).MODIFICATION_TIME := user_password_history(i).MODIFICATION_TIME;
                    migr_user_password_history(i).MODIFIED_BY := user_password_history(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_user_password_history.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_user_password_history -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_USER_PASSWORD_HISTORY (ID_PK, USER_ID, USER_PASSWORD, PASSWORD_CHANGE_DATE,
                                                                   CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                                   MODIFIED_BY)
                        VALUES (migr_user_password_history(i).ID_PK,
                                migr_user_password_history(i).USER_ID,
                                migr_user_password_history(i).USER_PASSWORD,
                                migr_user_password_history(i).PASSWORD_CHANGE_DATE,
                                migr_user_password_history(i).CREATION_TIME,
                                migr_user_password_history(i).CREATED_BY,
                                migr_user_password_history(i).MODIFICATION_TIME,
                                migr_user_password_history(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_password_history -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_user_password_history(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || user_password_history.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_password_history;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_password_history;

    PROCEDURE migrate_user_role IS
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

        TYPE T_MIGR_USER_ROLE IS TABLE OF MIGR_TB_USER_ROLE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_user_role T_MIGR_USER_ROLE;

        TYPE T_MIGR_PKS_USER_ROLE IS TABLE OF MIGR_TB_PKS_USER_ROLE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_user_role T_MIGR_PKS_USER_ROLE;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_role;
        LOOP
            FETCH c_user_role BULK COLLECT INTO user_role LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_role.COUNT = 0;

            migr_user_role := T_MIGR_USER_ROLE();
            migr_pks_user_role := T_MIGR_PKS_USER_ROLE();

            FOR i IN user_role.FIRST .. user_role.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(user_role(i).ID_PK, user_role(i).CREATION_TIME);

                    migr_pks_user_role(i).OLD_ID := user_role(i).ID_PK;
                    migr_pks_user_role(i).NEW_ID := v_id_pk;

                    migr_user_role(i).ID_PK := v_id_pk;
                    migr_user_role(i).ROLE_NAME := user_role(i).ROLE_NAME;
                    migr_user_role(i).CREATION_TIME := user_role(i).CREATION_TIME;
                    migr_user_role(i).CREATED_BY := user_role(i).CREATED_BY;
                    migr_user_role(i).MODIFICATION_TIME := user_role(i).MODIFICATION_TIME;
                    migr_user_role(i).MODIFIED_BY := user_role(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_user_role.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_user_role -> update user role lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_USER_ROLE (OLD_ID, NEW_ID)
                    VALUES (migr_pks_user_role(i).OLD_ID,
                            migr_pks_user_role(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_user_role -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_USER_ROLE (ID_PK, ROLE_NAME, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                       MODIFIED_BY)
                        VALUES (migr_user_role(i).ID_PK,
                                migr_user_role(i).ROLE_NAME,
                                migr_user_role(i).CREATION_TIME,
                                migr_user_role(i).CREATED_BY,
                                migr_user_role(i).MODIFICATION_TIME,
                                migr_user_role(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_role -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_user_role(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || user_role.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_role;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_role;

    PROCEDURE migrate_user_roles IS
        v_tab VARCHAR2(30) := 'TB_USER_ROLES';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_ROLES';

        CURSOR c_user_roles IS
            SELECT (SELECT MPKSU.NEW_ID
                    FROM MIGR_TB_PKS_USER MPKSU
                    WHERE MPKSU.OLD_ID = UR.USER_ID) AS USER_ID,
                   (SELECT MPKSUR.NEW_ID
                    FROM MIGR_TB_PKS_USER_ROLE MPKSUR
                    WHERE MPKSUR.OLD_ID = UR.ROLE_ID) AS ROLE_ID,
                   UR.CREATION_TIME,
                   UR.CREATED_BY,
                   UR.MODIFICATION_TIME,
                   UR.MODIFIED_BY
            FROM TB_USER_ROLES UR;

        TYPE T_USER_ROLES IS TABLE OF c_user_roles%rowtype;
        user_roles T_USER_ROLES;

        TYPE T_MIGR_USER_ROLES IS TABLE OF MIGR_TB_USER_ROLES%ROWTYPE INDEX BY PLS_INTEGER;
        migr_user_roles T_MIGR_USER_ROLES;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_roles;
        LOOP
            FETCH c_user_roles BULK COLLECT INTO user_roles LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_roles.COUNT = 0;

            migr_user_roles := T_MIGR_USER_ROLES();

            FOR i IN user_roles.FIRST .. user_roles.LAST
                LOOP
                    migr_user_roles(i).USER_ID := user_roles(i).USER_ID;
                    migr_user_roles(i).ROLE_ID := user_roles(i).ROLE_ID;
                    migr_user_roles(i).CREATION_TIME := user_roles(i).CREATION_TIME;
                    migr_user_roles(i).CREATED_BY := user_roles(i).CREATED_BY;
                    migr_user_roles(i).MODIFICATION_TIME := user_roles(i).MODIFICATION_TIME;
                    migr_user_roles(i).MODIFIED_BY := user_roles(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_user_roles.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_user_roles -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_USER_ROLES (USER_ID, ROLE_ID, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                        MODIFIED_BY)
                        VALUES (migr_user_roles(i).USER_ID,
                                migr_user_roles(i).ROLE_ID,
                                migr_user_roles(i).CREATION_TIME,
                                migr_user_roles(i).CREATED_BY,
                                migr_user_roles(i).MODIFICATION_TIME,
                                migr_user_roles(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_roles -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having USER_ID '
                                        || migr_user_roles(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).USER_ID
                                        || '  and ROLE_ID '
                                        || migr_user_roles(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ROLE_ID);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || user_roles.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_roles;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_roles;

    PROCEDURE migrate_rev_info IS
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

        TYPE T_MIGR_REV_INFO IS TABLE OF MIGR_TB_REV_INFO%ROWTYPE INDEX BY PLS_INTEGER;
        migr_rev_info T_MIGR_REV_INFO;

        TYPE T_MIGR_PKS_REV_INFO IS TABLE OF MIGR_TB_PKS_REV_INFO%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_rev_info T_MIGR_PKS_REV_INFO;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_rev_info;
        LOOP
            FETCH c_rev_info BULK COLLECT INTO rev_info LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN rev_info.COUNT = 0;

            migr_rev_info := T_MIGR_REV_INFO();
            migr_pks_rev_info := T_MIGR_PKS_REV_INFO();

            FOR i IN rev_info.FIRST .. rev_info.LAST
                LOOP
                    v_id := generate_scalable_seq(rev_info(i).ID, rev_info(i).REVISION_DATE);

                    migr_pks_rev_info(i).OLD_ID := rev_info(i).ID;
                    migr_pks_rev_info(i).NEW_ID := v_id;

                    migr_rev_info(i).ID := v_id;
                    migr_rev_info(i).TIMESTAMP := rev_info(i).TIMESTAMP;
                    migr_rev_info(i).REVISION_DATE := rev_info(i).REVISION_DATE;
                    migr_rev_info(i).USER_NAME := rev_info(i).USER_NAME;
                END LOOP;

            v_start := 1;
            v_last := migr_rev_info.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_rev_info -> update rev info audit lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_REV_INFO (OLD_ID, NEW_ID)
                    VALUES (migr_pks_rev_info(i).OLD_ID,
                            migr_pks_rev_info(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_rev_info -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_REV_INFO (ID, TIMESTAMP, REVISION_DATE, USER_NAME)
                        VALUES (migr_rev_info(i).ID,
                                migr_rev_info(i).TIMESTAMP,
                                migr_rev_info(i).REVISION_DATE,
                                migr_rev_info(i).USER_NAME);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_rev_info -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having ID '
                                        || migr_rev_info(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || rev_info.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_rev_info;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_rev_info;

    PROCEDURE migrate_rev_changes(missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_REV_CHANGES';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_REV_CHANGES';

        CURSOR c_rev_changes IS
            SELECT RC.ID_PK,
                   (SELECT MPKSRI.NEW_ID
                    FROM MIGR_TB_PKS_REV_INFO MPKSRI
                    WHERE MPKSRI.OLD_ID = RC.REV) AS REV,
                   RC.AUDIT_ORDER,
                   CASE -- entity names have changed over time from 4.x to 5.0 so adapt to the new fully qualified names
                       WHEN RC.ENTITY_NAME = 'eu.domibus.core.security.AuthenticationEntity'
                           THEN 'eu.domibus.api.user.plugin.AuthenticationEntity'
                       WHEN RC.ENTITY_NAME = 'eu.domibus.core.user.plugin.AuthenticationEntity'
                           THEN 'eu.domibus.api.user.plugin.AuthenticationEntity'
                       WHEN RC.ENTITY_NAME = 'eu.domibus.plugin.routing.BackendFilterEntity'
                           THEN 'eu.domibus.core.plugin.routing.BackendFilterEntity'
                       WHEN RC.ENTITY_NAME = 'eu.domibus.plugin.routing.RoutingCriteriaEntity'
                           THEN 'eu.domibus.core.plugin.routing.RoutingCriteriaEntity'
                       WHEN RC.ENTITY_NAME = 'eu.domibus.common.model.security.User'
                           THEN 'eu.domibus.core.user.ui.User'
                       WHEN RC.ENTITY_NAME = 'eu.domibus.core.user.User'
                           THEN 'eu.domibus.core.user.ui.User'
                       WHEN RC.ENTITY_NAME = 'eu.domibus.common.model.security.UserRole'
                           THEN 'eu.domibus.core.user.ui.UserRole'
                       WHEN RC.ENTITY_NAME = 'eu.domibus.core.user.UserRole'
                           THEN 'eu.domibus.core.user.ui.UserRole'
                       WHEN RC.ENTITY_NAME = 'eu.domibus.common.model.audit.PModeAudit'
                           THEN 'eu.domibus.core.audit.model.PModeAudit'
                       WHEN RC.ENTITY_NAME = 'eu.domibus.common.model.audit.MessageAudit'
                           THEN 'eu.domibus.core.audit.model.MessageAudit'
                       WHEN RC.ENTITY_NAME = 'eu.domibus.common.model.audit.JmsMessageAudit'
                           THEN 'eu.domibus.core.audit.model.JmsMessageAudit'
                       ELSE RC.ENTITY_NAME -- else keep the existing name since it's up-to-date
                       END AS ENTITY_NAME,
                   RC.GROUP_NAME,
                   CASE
                       WHEN RC.ENTITY_NAME IN ('eu.domibus.core.user.plugin.AuthenticationEntity',
                                              'eu.domibus.core.user.plugin.AuthenticationEntity',
                                              'eu.domibus.api.user.plugin.AuthenticationEntity')
                           THEN (SELECT MPKSAE.NEW_ID
                                 FROM MIGR_TB_PKS_AUTH_ENTRY MPKSAE
                                 WHERE MPKSAE.OLD_ID = RC.ENTITY_ID) -- authentication_entry
                       WHEN RC.ENTITY_NAME IN ('eu.domibus.plugin.routing.BackendFilterEntity',
                                              'eu.domibus.core.plugin.routing.BackendFilterEntity')
                           THEN (SELECT MPKSBF.NEW_ID
                                 FROM MIGR_TB_PKS_BACKEND_FILTER MPKSBF
                                 WHERE MPKSBF.OLD_ID = RC.ENTITY_ID) -- backend_filter
                       WHEN RC.ENTITY_NAME IN ('eu.domibus.plugin.routing.RoutingCriteriaEntity',
                                              'eu.domibus.core.plugin.routing.RoutingCriteriaEntity')
                           THEN (SELECT MPKSRC.NEW_ID
                                 FROM MIGR_TB_PKS_ROUTING_CRITERIA MPKSRC
                                 WHERE MPKSRC.OLD_ID = RC.ENTITY_ID) -- routing_criteria
                       WHEN RC.ENTITY_NAME IN ('eu.domibus.common.model.security.User',
                                              'eu.domibus.core.user.User',
                                              'eu.domibus.core.user.ui.User')
                           THEN (SELECT MPKSU.NEW_ID
                                 FROM MIGR_TB_PKS_USER MPKSU
                                 WHERE MPKSU.OLD_ID = RC.ENTITY_ID) -- user
                       WHEN RC.ENTITY_NAME = 'eu.domibus.common.model.configuration.Configuration'
                           THEN (SELECT MPKSPC.NEW_ID
                                 FROM MIGR_TB_PKS_PM_CONFIGURATION MPKSPC
                                 WHERE MPKSPC.OLD_ID = RC.ENTITY_ID) -- pm_configuration
                       WHEN RC.ENTITY_NAME = 'eu.domibus.common.model.configuration.ConfigurationRaw'
                           THEN (SELECT MPKSPCR.NEW_ID
                                 FROM MIGR_TB_PKS_PM_CONF_RAW MPKSPCR
                                 WHERE MPKSPCR.OLD_ID = RC.ENTITY_ID) -- pm_configuration_raw
                       WHEN RC.ENTITY_NAME = 'eu.domibus.common.model.configuration.Party'
                           THEN (SELECT MPKSPP.NEW_ID
                                 FROM MIGR_TB_PKS_PM_PARTY MPKSPP
                                 WHERE MPKSPP.OLD_ID = RC.ENTITY_ID) -- pm_party
                       WHEN RC.ENTITY_NAME = 'eu.domibus.common.model.configuration.PartyIdType'
                           THEN (SELECT MPKSPPIT.NEW_ID
                                 FROM MIGR_TB_PKS_PM_PARTY_ID_TYPE MPKSPPIT
                                 WHERE MPKSPPIT.OLD_ID = RC.ENTITY_ID) -- pm_party_id_type
                       WHEN RC.ENTITY_NAME = 'eu.domibus.core.certificate.Certificate'
                           THEN (SELECT MPKSC.NEW_ID
                                 FROM MIGR_TB_PKS_CERTIFICATE MPKSC
                                 WHERE MPKSC.OLD_ID = RC.ENTITY_ID) -- certificate
                       WHEN RC.ENTITY_NAME IN ('eu.domibus.common.model.security.UserRole',
                                              'eu.domibus.core.user.UserRole',
                                              'eu.domibus.core.user.ui.UserRole')
                           THEN (SELECT MPKSUR.NEW_ID
                                 FROM MIGR_TB_PKS_USER_ROLE MPKSUR
                                 WHERE MPKSUR.OLD_ID = RC.ENTITY_ID) -- user_role
                       WHEN RC.ENTITY_NAME = 'eu.domibus.core.audit.model.TruststoreAudit'
                           THEN (SELECT MPKSTA.NEW_ID
                                 FROM MIGR_TB_PKS_ACTION_AUDIT MPKSTA
                                 WHERE MPKSTA.OLD_ID = RC.ENTITY_ID) -- action_audit
                       WHEN RC.ENTITY_NAME IN ('eu.domibus.common.model.audit.PModeAudit',
                                              'eu.domibus.core.audit.model.PModeAudit')
                           THEN (SELECT MPKSPA.NEW_ID
                                 FROM MIGR_TB_PKS_ACTION_AUDIT MPKSPA
                                 WHERE MPKSPA.OLD_ID = RC.ENTITY_ID) -- action_audit
                       WHEN RC.ENTITY_NAME = 'eu.domibus.core.audit.model.PModeArchiveAudit'
                           THEN (SELECT MPKSPAA.NEW_ID
                                 FROM MIGR_TB_PKS_ACTION_AUDIT MPKSPAA
                                 WHERE MPKSPAA.OLD_ID = RC.ENTITY_ID) -- action_audit
                       WHEN RC.ENTITY_NAME IN ('eu.domibus.common.model.audit.MessageAudit',
                                              'eu.domibus.core.audit.model.MessageAudit')
                           THEN (SELECT MPKSMA.NEW_ID
                                 FROM MIGR_TB_PKS_ACTION_AUDIT MPKSMA
                                 WHERE MPKSMA.OLD_ID = RC.ENTITY_ID) -- action_audit
                       WHEN RC.ENTITY_NAME IN ('eu.domibus.common.model.audit.JmsMessageAudit',
                                              'eu.domibus.core.audit.model.JmsMessageAudit')
                           THEN (SELECT MPKSJMA.NEW_ID
                                 FROM MIGR_TB_PKS_ACTION_AUDIT MPKSJMA
                                 WHERE MPKSJMA.OLD_ID = RC.ENTITY_ID) -- action_audit
                       END AS ENTITY_ID,
                   RC.ENTITY_ID AS ORIGINAL_ENTITY_ID,
                   RC.MODIFICATION_TYPE,
                   RC.CREATION_TIME,
                   RC.CREATED_BY,
                   RC.MODIFICATION_TIME,
                   RC.MODIFIED_BY
            FROM TB_REV_CHANGES RC;

        TYPE T_REV_CHANGES IS TABLE OF c_rev_changes%rowtype;
        rev_changes T_REV_CHANGES;

        TYPE T_MIGR_REV_CHANGES IS TABLE OF MIGR_TB_REV_CHANGES%ROWTYPE INDEX BY PLS_INTEGER;
        migr_rev_changes T_MIGR_REV_CHANGES;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_rev_changes;
        LOOP
            FETCH c_rev_changes BULK COLLECT INTO rev_changes LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN rev_changes.COUNT = 0;

            migr_rev_changes := T_MIGR_REV_CHANGES();

            FOR i IN rev_changes.FIRST .. rev_changes.LAST
                LOOP
                    IF rev_changes(i).ENTITY_NAME NOT IN ('eu.domibus.api.user.plugin.AuthenticationEntity',
                                                         'eu.domibus.core.plugin.routing.BackendFilterEntity',
                                                         'eu.domibus.core.plugin.routing.RoutingCriteriaEntity',
                                                         'eu.domibus.core.user.ui.User',
                                                         'eu.domibus.common.model.configuration.Configuration',
                                                         'eu.domibus.common.model.configuration.ConfigurationRaw',
                                                         'eu.domibus.common.model.configuration.Party',
                                                         'eu.domibus.common.model.configuration.PartyIdType',
                                                         'eu.domibus.core.certificate.Certificate',
                                                         'eu.domibus.core.user.ui.UserRole',
                                                         'eu.domibus.core.audit.model.TruststoreAudit',
                                                         'eu.domibus.core.audit.model.PModeAudit',
                                                         'eu.domibus.core.audit.model.PModeArchiveAudit',
                                                         'eu.domibus.core.audit.model.MessageAudit',
                                                         'eu.domibus.core.audit.model.JmsMessageAudit') THEN
                        DBMS_OUTPUT.PUT_LINE('Unknown entity name ' || rev_changes(i).ENTITY_NAME);
                    END IF;

                    migr_rev_changes(i).ID_PK := generate_scalable_seq(rev_changes(i).ID_PK, rev_changes(i).CREATION_TIME);
                    migr_rev_changes(i).REV := rev_changes(i).REV;
                    migr_rev_changes(i).AUDIT_ORDER := rev_changes(i).AUDIT_ORDER;
                    migr_rev_changes(i).ENTITY_NAME := rev_changes(i).ENTITY_NAME;
                    migr_rev_changes(i).GROUP_NAME := rev_changes(i).GROUP_NAME;
                    migr_rev_changes(i).ENTITY_ID := TO_CHAR(NVL(rev_changes(i).ENTITY_ID, generate_scalable_seq(rev_changes(i).ORIGINAL_ENTITY_ID, missing_entity_date_prefix)));
                    migr_rev_changes(i).MODIFICATION_TYPE := rev_changes(i).MODIFICATION_TYPE;
                    migr_rev_changes(i).CREATION_TIME := rev_changes(i).CREATION_TIME;
                    migr_rev_changes(i).CREATED_BY := rev_changes(i).CREATED_BY;
                    migr_rev_changes(i).MODIFICATION_TIME := rev_changes(i).MODIFICATION_TIME;
                    migr_rev_changes(i).MODIFIED_BY := rev_changes(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_rev_changes.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_rev_changes -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_REV_CHANGES (ID_PK, REV, AUDIT_ORDER, ENTITY_NAME, GROUP_NAME, ENTITY_ID,
                                                         MODIFICATION_TYPE, CREATION_TIME, CREATED_BY,
                                                         MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_rev_changes(i).ID_PK,
                                migr_rev_changes(i).REV,
                                migr_rev_changes(i).AUDIT_ORDER,
                                migr_rev_changes(i).ENTITY_NAME,
                                migr_rev_changes(i).GROUP_NAME,
                                migr_rev_changes(i).ENTITY_ID,
                                migr_rev_changes(i).MODIFICATION_TYPE,
                                migr_rev_changes(i).CREATION_TIME,
                                migr_rev_changes(i).CREATED_BY,
                                migr_rev_changes(i).MODIFICATION_TIME,
                                migr_rev_changes(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_rev_changes -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having ID_PK '
                                        || migr_rev_changes(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK
                                        || '  and REV '
                                        || migr_rev_changes(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).REV);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || rev_changes.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_rev_changes;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_rev_changes;

    PROCEDURE migrate_user_aud(missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_USER_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_AUD';

        CURSOR c_user_aud IS
            SELECT (SELECT MPKSU.NEW_ID
                    FROM MIGR_TB_PKS_USER MPKSU
                    WHERE MPKSU.OLD_ID = UA.ID_PK) AS ID_PK,
                   UA.ID_PK AS ORIGINAL_ID_PK,
                   (SELECT MPKSRI.NEW_ID
                    FROM MIGR_TB_PKS_REV_INFO MPKSRI
                    WHERE MPKSRI.OLD_ID = UA.REV) AS REV,
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

        TYPE T_MIGR_USER_AUD IS TABLE OF MIGR_TB_USER_AUD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_user_aud T_MIGR_USER_AUD;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_aud;
        LOOP
            FETCH c_user_aud BULK COLLECT INTO user_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_aud.COUNT = 0;

            migr_user_aud := T_MIGR_USER_AUD();

            FOR i IN user_aud.FIRST .. user_aud.LAST
                LOOP
                    migr_user_aud(i).ID_PK := NVL(user_aud(i).ID_PK, generate_scalable_seq(user_aud(i).ORIGINAL_ID_PK, missing_entity_date_prefix));
                    migr_user_aud(i).REV := user_aud(i).REV;
                    migr_user_aud(i).REVTYPE := user_aud(i).REVTYPE;
                    migr_user_aud(i).USER_ENABLED := user_aud(i).USER_ENABLED;
                    migr_user_aud(i).ACTIVE_MOD := user_aud(i).ACTIVE_MOD;
                    migr_user_aud(i).USER_DELETED := user_aud(i).USER_DELETED;
                    migr_user_aud(i).DELETED_MOD := user_aud(i).DELETED_MOD;
                    migr_user_aud(i).USER_EMAIL := user_aud(i).USER_EMAIL;
                    migr_user_aud(i).EMAIL_MOD := user_aud(i).EMAIL_MOD;
                    migr_user_aud(i).USER_PASSWORD := user_aud(i).USER_PASSWORD;
                    migr_user_aud(i).PASSWORD_MOD := user_aud(i).PASSWORD_MOD;
                    migr_user_aud(i).USER_NAME := user_aud(i).USER_NAME;
                    migr_user_aud(i).USERNAME_MOD := user_aud(i).USERNAME_MOD;
                    migr_user_aud(i).OPTLOCK := user_aud(i).OPTLOCK;
                    migr_user_aud(i).VERSION_MOD := user_aud(i).VERSION_MOD;
                    migr_user_aud(i).ROLES_MOD := user_aud(i).ROLES_MOD;
                    migr_user_aud(i).PASSWORD_CHANGE_DATE := user_aud(i).PASSWORD_CHANGE_DATE;
                    migr_user_aud(i).PASSWORDCHANGEDATE_MOD := user_aud(i).PASSWORDCHANGEDATE_MOD;
                    migr_user_aud(i).DEFAULT_PASSWORD := user_aud(i).DEFAULT_PASSWORD;
                    migr_user_aud(i).DEFAULTPASSWORD_MOD := user_aud(i).DEFAULTPASSWORD_MOD;
                END LOOP;

            v_start := 1;
            v_last := migr_user_aud.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_user_aud -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_USER_AUD (ID_PK, REV, REVTYPE, USER_ENABLED, ACTIVE_MOD, USER_DELETED,
                                                      DELETED_MOD, USER_EMAIL, EMAIL_MOD, USER_PASSWORD, PASSWORD_MOD,
                                                      USER_NAME, USERNAME_MOD, OPTLOCK, VERSION_MOD, ROLES_MOD,
                                                      PASSWORD_CHANGE_DATE, PASSWORDCHANGEDATE_MOD, DEFAULT_PASSWORD,
                                                      DEFAULTPASSWORD_MOD)
                        VALUES (migr_user_aud(i).ID_PK,
                                migr_user_aud(i).REV,
                                migr_user_aud(i).REVTYPE,
                                migr_user_aud(i).USER_ENABLED,
                                migr_user_aud(i).ACTIVE_MOD,
                                migr_user_aud(i).USER_DELETED,
                                migr_user_aud(i).DELETED_MOD,
                                migr_user_aud(i).USER_EMAIL,
                                migr_user_aud(i).EMAIL_MOD,
                                migr_user_aud(i).USER_PASSWORD,
                                migr_user_aud(i).PASSWORD_MOD,
                                migr_user_aud(i).USER_NAME,
                                migr_user_aud(i).USERNAME_MOD,
                                migr_user_aud(i).OPTLOCK,
                                migr_user_aud(i).VERSION_MOD,
                                migr_user_aud(i).ROLES_MOD,
                                migr_user_aud(i).PASSWORD_CHANGE_DATE,
                                migr_user_aud(i).PASSWORDCHANGEDATE_MOD,
                                migr_user_aud(i).DEFAULT_PASSWORD,
                                migr_user_aud(i).DEFAULTPASSWORD_MOD);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having ID_PK '
                                        || migr_user_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK
                                        || '  and REV '
                                        || migr_user_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).REV);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || user_aud.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_aud;

    PROCEDURE migrate_user_role_aud(missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_USER_ROLE_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_ROLE_AUD';

        CURSOR c_user_role_aud IS
            SELECT (SELECT MPKSUR.NEW_ID
                    FROM MIGR_TB_PKS_USER_ROLE MPKSUR
                    WHERE MPKSUR.OLD_ID = URA.ID_PK) AS ID_PK,
                   URA.ID_PK AS ORIGINAL_ID_PK,
                   (SELECT MPKSRI.NEW_ID
                    FROM MIGR_TB_PKS_REV_INFO MPKSRI
                    WHERE MPKSRI.OLD_ID = URA.REV) AS REV,
                   URA.REVTYPE,
                   URA.ROLE_NAME,
                   URA.NAME_MOD,
                   URA.USERS_MOD
            FROM TB_USER_ROLE_AUD URA;

        TYPE T_USER_ROLE_AUD IS TABLE OF c_user_role_aud%rowtype;
        user_role_aud T_USER_ROLE_AUD;

        TYPE T_MIGR_USER_ROLE_AUD IS TABLE OF MIGR_TB_USER_ROLE_AUD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_user_role_aud T_MIGR_USER_ROLE_AUD;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_role_aud;
        LOOP
            FETCH c_user_role_aud BULK COLLECT INTO user_role_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_role_aud.COUNT = 0;

            migr_user_role_aud := T_MIGR_USER_ROLE_AUD();

            FOR i IN user_role_aud.FIRST .. user_role_aud.LAST
                LOOP
                    migr_user_role_aud(i).ID_PK := NVL(user_role_aud(i).ID_PK, generate_scalable_seq(user_role_aud(i).ORIGINAL_ID_PK, missing_entity_date_prefix));
                    migr_user_role_aud(i).REV := user_role_aud(i).REV;
                    migr_user_role_aud(i).REVTYPE := user_role_aud(i).REVTYPE;
                    migr_user_role_aud(i).ROLE_NAME := user_role_aud(i).ROLE_NAME;
                    migr_user_role_aud(i).NAME_MOD := user_role_aud(i).NAME_MOD;
                    migr_user_role_aud(i).USERS_MOD := user_role_aud(i).USERS_MOD;
                END LOOP;

            v_start := 1;
            v_last := migr_user_role_aud.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_user_role_aud -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_USER_ROLE_AUD (ID_PK, REV, REVTYPE, ROLE_NAME, NAME_MOD, USERS_MOD)
                        VALUES (migr_user_role_aud(i).ID_PK,
                                migr_user_role_aud(i).REV,
                                migr_user_role_aud(i).REVTYPE,
                                migr_user_role_aud(i).ROLE_NAME,
                                migr_user_role_aud(i).NAME_MOD,
                                migr_user_role_aud(i).USERS_MOD);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_role_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having ID_PK '
                                        || migr_user_role_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK
                                        || '  and REV '
                                        || migr_user_role_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).REV);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || user_role_aud.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_role_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_role_aud;

    PROCEDURE migrate_user_roles_aud(missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_USER_ROLES_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_ROLES_AUD';

        CURSOR c_user_roles_aud IS
            SELECT (SELECT MPKSRI.NEW_ID
                    FROM MIGR_TB_PKS_REV_INFO MPKSRI
                    WHERE MPKSRI.OLD_ID = URA.REV) AS REV,
                   URA.REVTYPE,
                   (SELECT MPKSU.NEW_ID
                    FROM MIGR_TB_PKS_USER MPKSU
                    WHERE MPKSU.OLD_ID = URA.USER_ID) AS USER_ID,
                   URA.USER_ID AS ORIGINAL_USER_ID,
                   (SELECT MPKSUR.NEW_ID
                    FROM MIGR_TB_PKS_USER_ROLE MPKSUR
                    WHERE MPKSUR.OLD_ID = URA.ROLE_ID) AS ROLE_ID,
                   URA.ROLE_ID AS ORIGINAL_ROLE_ID
            FROM TB_USER_ROLES_AUD URA;

        TYPE T_USER_ROLES_AUD IS TABLE OF c_user_roles_aud%rowtype;
        user_roles_aud T_USER_ROLES_AUD;

        TYPE T_MIGR_USER_ROLES_AUD IS TABLE OF MIGR_TB_USER_ROLES_AUD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_user_roles_aud T_MIGR_USER_ROLES_AUD;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_roles_aud;
        LOOP
            FETCH c_user_roles_aud BULK COLLECT INTO user_roles_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_roles_aud.COUNT = 0;

            migr_user_roles_aud := T_MIGR_USER_ROLES_AUD();

            FOR i IN user_roles_aud.FIRST .. user_roles_aud.LAST
                LOOP
                    migr_user_roles_aud(i).REV := user_roles_aud(i).REV;
                    migr_user_roles_aud(i).REVTYPE := user_roles_aud(i).REVTYPE;
                    migr_user_roles_aud(i).USER_ID := NVL(user_roles_aud(i).USER_ID, generate_scalable_seq(user_roles_aud(i).ORIGINAL_USER_ID, missing_entity_date_prefix));
                    migr_user_roles_aud(i).ROLE_ID := NVL(user_roles_aud(i).ROLE_ID, generate_scalable_seq(user_roles_aud(i).ORIGINAL_ROLE_ID, missing_entity_date_prefix));
                END LOOP;

            v_start := 1;
            v_last := migr_user_roles_aud.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_user_roles_aud -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_USER_ROLES_AUD (REV, REVTYPE, USER_ID, ROLE_ID)
                        VALUES (migr_user_roles_aud(i).REV,
                                migr_user_roles_aud(i).REVTYPE,
                                migr_user_roles_aud(i).USER_ID,
                                migr_user_roles_aud(i).ROLE_ID);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_roles_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having USER_ID '
                                        || migr_user_roles_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).USER_ID
                                        || '  and ROLE_ID '
                                        || migr_user_roles_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ROLE_ID
                                        || '  and REV '
                                        || migr_user_roles_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).REV);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || user_roles_aud.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_roles_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_user_roles_aud;

    /**-- main entry point for running the multitenancy general schema migration --*/
    PROCEDURE migrate_multitenancy IS
        missing_entity_date_prefix DATE;
    BEGIN
        -- keep it in this order
        prepare_timezone_offset;

        -- START migrate to the new schema (including primary keys to the new format)
        migrate_alert;
        migrate_event;
        migrate_event_alert;
        migrate_event_property;
        --
        migrate_command;
        migrate_command_property;
        --
        migrate_user_domain;
        --
        migrate_user;
        migrate_user_password_history;
        migrate_user_role;
        migrate_user_roles;
        --
        missing_entity_date_prefix := SYSDATE;
        migrate_rev_info;
        migrate_rev_changes(missing_entity_date_prefix);
        migrate_user_aud(missing_entity_date_prefix);
        migrate_user_role_aud(missing_entity_date_prefix);
        migrate_user_roles_aud(missing_entity_date_prefix);
        -- END migrate primary keys to new format

    END migrate_multitenancy;

END MIGRATE_42_TO_50;
/