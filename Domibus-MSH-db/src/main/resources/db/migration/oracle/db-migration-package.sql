-- ********************************************************************************************************
-- Domibus 4.2 to 5.0 data migration package
--
-- Main entry point is the procedure 'migrate'. To be executed into a begin/end; block
--
-- Parameters to be adjusted:
-- BATCH_SIZE - size of the batch for data migration on each migrated table after which there is a commit;
--              default value is 100
-- BULK_COLLECT_LIMIT - limit to avoid reading a high number of records into memory; default value is 100
-- VERBOSE_LOGS - more information into the logs; default to false
-- ********************************************************************************************************

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_TIMEZONE_OFFSET (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_TIMEZONE_OFFSET PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_USER_MESSAGE (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_USER_MESSAGE PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_MESSAGE_INFO (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_MESSAGE_INFO PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_MESSAGE_GROUP (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_MESSAGE_GROUP PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PART_INFO (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PART_INFO PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_MESSAGE_ACKNW (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_MESSAGE_ACKNW PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_SEND_ATTEMPT (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_SEND_ATTEMPT PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_ACTION_AUDIT (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_ACTION_AUDIT PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_ALERT (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_ALERT PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_EVENT (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_EVENT PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_AUTH_ENTRY (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_AUTH_ENTRY PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_BACKEND_FILTER (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_BACKEND_FILTER PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_ROUTING_CRITERIA (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_ROUTING_CRITERIA PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_CERTIFICATE (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_CERTIFICATE PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_COMMAND (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_COMMAND PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_BUSINESS_PROC (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_BUSINESS_PROC PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_ACTION (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_ACTION PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_AGREEMENT (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_AGREEMENT PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_ERROR_HANDLING (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_ERROR_HANDLING PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_MEP (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_MEP PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_MEP_BINDING (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_MEP_BINDING PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_MESSAGE_PROP (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_MESSAGE_PROP PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_MSG_PROP_SET (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_MSG_PROP_SET PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_PARTY (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_PARTY PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_CONFIGURATION (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_CONFIGURATION PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_MPC (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_MPC PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_PARTY_ID_TYPE (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_PARTY_ID_TYPE PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_PARTY_ID (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_PARTY_ID PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_PAYLOAD (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_PAYLOAD PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_PAYLOAD_PROF (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_PAYLOAD_PROF PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_RECEPTN_AWARNS (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_RECEPTN_AWARNS PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_RELIABILITY (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_RELIABILITY PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_ROLE (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_ROLE PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_SECURITY (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_SECURITY PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_SERVICE (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_SERVICE PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_SPLITTING (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_SPLITTING PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_LEG (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_LEG PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_PROCESS (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_PROCESS PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PM_CONF_RAW (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PM_CONF_RAW PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_USER (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_USER PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_USER_ROLE (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_USER_ROLE PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_REV_INFO (OLD_ID NUMBER NOT NULL, NEW_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_REV_INFO PRIMARY KEY (OLD_ID)) ON COMMIT PRESERVE ROWS;

CREATE OR REPLACE PACKAGE MIGRATE_42_TO_50 IS
    -- batch size for commit of the migrated records
    BATCH_SIZE CONSTANT NUMBER := 100;

    -- limit loading a high number of records into memory
    BULK_COLLECT_LIMIT CONSTANT NUMBER := 100;

    -- enable more verbose logs
    VERBOSE_LOGS CONSTANT BOOLEAN := FALSE;

    -- entry point for running the single tenancy or multitenancy non-general schema migration - to be executed in a BEGIN/END; block
    PROCEDURE migrate;

END MIGRATE_42_TO_50;
/

CREATE OR REPLACE PACKAGE BODY MIGRATE_42_TO_50 IS

    /** -- FORALL exception handling -*/
    failure_in_forall EXCEPTION;
    PRAGMA EXCEPTION_INIT (failure_in_forall, -24381);

    /** -- Dummy TB_USER_MESSAGE.ID_PK (because of the new NOT NULL constraints introduced with partitioning) -*/
    DUMMY_USER_MESSAGE_ID_PK CONSTANT NUMBER := 19700101;

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

    FUNCTION lookup_migration_pk_tz_offset RETURN NUMBER IS
        new_id NUMBER;
    BEGIN
        SELECT MPKSTO.NEW_ID
            INTO new_id
            FROM MIGR_TB_PKS_TIMEZONE_OFFSET MPKSTO
            WHERE MPKSTO.OLD_ID = 1;
        RETURN new_id;
    END lookup_migration_pk_tz_offset;

    FUNCTION get_tb_d_mpc_rec(mpc_value VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF mpc_value IS NULL THEN
            DBMS_OUTPUT.PUT_LINE('No record added into TB_D_MPC');
            RETURN v_id_pk;
        END IF;
        BEGIN
            SELECT ID_PK INTO v_id_pk FROM TB_D_MPC WHERE VALUE = mpc_value;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_MPC: ' || mpc_value);
                v_id_pk := generate_id();
                INSERT INTO TB_D_MPC (ID_PK, VALUE) VALUES (v_id_pk, mpc_value);
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_mpc_rec;

    FUNCTION get_tb_d_role_rec(v_role VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF v_role IS NULL THEN
            log_verbose('No record added into TB_D_ROLE');
            RETURN v_id_pk;
        END IF;
        BEGIN
            SELECT ID_PK INTO v_id_pk FROM TB_D_ROLE WHERE ROLE = v_role;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_ROLE: ' || v_role);
                v_id_pk := generate_id();
                INSERT INTO TB_D_ROLE (ID_PK, ROLE) VALUES (v_id_pk, v_role);
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_role_rec;

    FUNCTION get_tb_d_msh_role_rec(v_role VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF v_role IS NULL THEN
            log_verbose('No record added into TB_D_MSH_ROLE');
            RETURN v_id_pk;
        END IF;
        BEGIN
            SELECT ID_PK INTO v_id_pk FROM TB_D_MSH_ROLE WHERE ROLE = v_role;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_MSH_ROLE: ' || v_role);
                v_id_pk := generate_id();
                INSERT INTO TB_D_MSH_ROLE (ID_PK, ROLE) VALUES (v_id_pk, v_role);
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
                SELECT ID_PK INTO v_id_pk FROM TB_D_SERVICE WHERE TYPE IS NULL AND VALUE = service_value;
            ELSIF service_value IS NULL THEN
                SELECT ID_PK INTO v_id_pk FROM TB_D_SERVICE WHERE TYPE = service_type AND VALUE IS NULL;
            ELSE
                SELECT ID_PK INTO v_id_pk FROM TB_D_SERVICE WHERE TYPE = service_type AND VALUE = service_value;
            END IF;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_SERVICE: ' || service_type || ' , ' || service_value);
                v_id_pk := generate_id();
                INSERT INTO TB_D_SERVICE (ID_PK, TYPE, VALUE) VALUES (v_id_pk, service_type, service_value);
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
            SELECT ID_PK INTO v_id_pk FROM TB_D_MESSAGE_STATUS WHERE STATUS = message_status;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_MESSAGE_STATUS: ' || message_status);
                v_id_pk := generate_id();
                INSERT INTO TB_D_MESSAGE_STATUS (ID_PK, STATUS) VALUES (v_id_pk, message_status);
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
            IF agreement_type IS NULL THEN
                SELECT ID_PK INTO v_id_pk FROM TB_D_AGREEMENT WHERE TYPE IS NULL AND VALUE = agreement_value;
            ELSIF agreement_value IS NULL THEN
                SELECT ID_PK INTO v_id_pk FROM TB_D_AGREEMENT WHERE TYPE = agreement_type AND VALUE IS NULL;
            ELSE
                SELECT ID_PK INTO v_id_pk FROM TB_D_AGREEMENT WHERE TYPE = agreement_type AND VALUE = agreement_value;
            END IF;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE(
                            'Add new record into TB_D_AGREEMENT: ' || agreement_type || ' , ' || agreement_value);
                v_id_pk := generate_id();
                INSERT INTO TB_D_AGREEMENT (ID_PK, TYPE, VALUE) VALUES (v_id_pk, agreement_type, agreement_value);
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_agreement_rec;

    FUNCTION get_tb_d_action_rec(v_action VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF v_action IS NULL THEN
            log_verbose('No record added into TB_D_ACTION');
            RETURN v_id_pk;
        END IF;
        BEGIN
            SELECT ID_PK INTO v_id_pk FROM TB_D_ACTION WHERE ACTION = v_action;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_ACTION: ' || v_action);
                v_id_pk := generate_id();
                INSERT INTO TB_D_ACTION (ID_PK, ACTION) VALUES (v_id_pk, v_action);
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
            SELECT ID_PK INTO v_id_pk FROM TB_D_PARTY WHERE TYPE = party_type AND VALUE = party_value;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_PARTY: ' || party_type || ' , ' || party_value);
                v_id_pk := generate_id();
                INSERT INTO TB_D_PARTY (ID_PK, TYPE, VALUE) VALUES (v_id_pk, party_type, party_value);
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

    FUNCTION get_tb_d_notif_status_rec(v_status VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF v_status IS NULL THEN
            log_verbose('No record added into TB_D_NOTIFICATION_STATUS');
            RETURN v_id_pk;
        END IF;
        BEGIN
            SELECT ID_PK INTO v_id_pk FROM TB_D_NOTIFICATION_STATUS WHERE STATUS = v_status;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_NOTIFICATION_STATUS: ' || v_status);
                v_id_pk := generate_id();
                INSERT INTO TB_D_NOTIFICATION_STATUS (ID_PK, STATUS) VALUES (v_id_pk, v_status);
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_notif_status_rec;

    FUNCTION get_tb_user_message_rec(v_message_id VARCHAR2) RETURN NUMBER IS
        v_id_pk   NUMBER;
        v_tab_new VARCHAR2(30) := 'MIGR_TB_USER_MESSAGE';
    BEGIN
        IF v_message_id IS NULL THEN
            log_verbose('No record to look into ' || v_tab_new);
            RETURN v_id_pk;
        END IF;
        BEGIN
            SELECT ID_PK INTO v_id_pk FROM MIGR_TB_USER_MESSAGE WHERE MESSAGE_ID = v_message_id;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                DBMS_OUTPUT.PUT_LINE('No record found into ' || v_tab_new || ' for MESSAGE_ID = ' || v_message_id);
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
            SELECT ID_PK INTO v_id_pk FROM MIGR_TB_SIGNAL_MESSAGE WHERE SIGNAL_MESSAGE_ID = message_id;
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
            SELECT ID_PK INTO v_id_pk FROM TB_D_MESSAGE_PROPERTY
            WHERE (NAME = prop_name AND TYPE = prop_type AND VALUE = prop_value)
               OR (NAME = prop_name AND TYPE IS NULL AND VALUE = prop_value);
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE(
                            'Add new record into TB_D_MESSAGE_PROPERTY: ' || prop_name || ' , ' || prop_value|| ' , ' || prop_type);
                v_id_pk := generate_id();
                INSERT INTO TB_D_MESSAGE_PROPERTY (ID_PK, NAME, VALUE, TYPE) VALUES (v_id_pk, prop_name, prop_value, prop_type);
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
            SELECT ID_PK INTO v_id_pk  FROM TB_D_PART_PROPERTY
            WHERE (NAME = prop_name AND VALUE = prop_value AND TYPE = prop_type)
               OR (NAME = prop_name AND VALUE = prop_value AND TYPE IS NULL);
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE(
                            'Add new record into TB_D_PART_PROPERTY: ' || prop_name || ' , ' || prop_value|| ' , ' || prop_type);
                v_id_pk := generate_id();
                INSERT INTO TB_D_PART_PROPERTY (ID_PK, NAME, VALUE, TYPE) VALUES (v_id_pk, prop_name, prop_value, prop_type);
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_part_property_rec;

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

    /**-- TB_USER_MESSAGE migration --*/
    PROCEDURE prepare_user_message IS
        v_user_message_exists INT;
    BEGIN
        BEGIN
            SELECT COUNT(*) INTO v_user_message_exists FROM TB_USER_MESSAGE WHERE ID_PK = DUMMY_USER_MESSAGE_ID_PK;

            IF v_user_message_exists > 0 THEN
                RAISE_APPLICATION_ERROR(-20001, 'TB_USER_MESSAGE entry having ID_PK = ' || DUMMY_USER_MESSAGE_ID_PK
                    || ' already exists in your old user schema. This has a special meaning in the new user schema: please either remove it or update its value.');
            ELSE
                INSERT INTO MIGR_TB_USER_MESSAGE (ID_PK)
                VALUES (DUMMY_USER_MESSAGE_ID_PK);

                COMMIT;
            END IF;
        END;
    END;

    PROCEDURE migrate_user_message IS
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

        TYPE T_MIGR_USER_MESSAGE IS TABLE OF MIGR_TB_USER_MESSAGE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_user_message T_MIGR_USER_MESSAGE;

        TYPE T_MIGR_PKS_USER_MESSAGE IS TABLE OF MIGR_TB_PKS_USER_MESSAGE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_user_message T_MIGR_PKS_USER_MESSAGE;

        TYPE T_MIGR_PKS_MESSAGE_INFO IS TABLE OF MIGR_TB_PKS_MESSAGE_INFO%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_message_info T_MIGR_PKS_MESSAGE_INFO;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;

        v_count_user_message INT;
        v_count_migr_user_message INT;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_user_message;
        LOOP
            FETCH c_user_message BULK COLLECT INTO user_message LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_message.COUNT = 0;

            migr_user_message := T_MIGR_USER_MESSAGE();
            migr_pks_user_message := T_MIGR_PKS_USER_MESSAGE();
            migr_pks_message_info := T_MIGR_PKS_MESSAGE_INFO();

            FOR i IN user_message.FIRST .. user_message.LAST
                LOOP
                    IF user_message(i).ID_PK = DUMMY_USER_MESSAGE_ID_PK THEN
                        -- migrate the dummy entry as-is (its ID_PK doesn't need to change)
                        v_id_pk := DUMMY_USER_MESSAGE_ID_PK;
                    ELSE
                        v_id_pk := generate_scalable_seq(user_message(i).ID_PK, user_message(i).CREATION_TIME);
                    END IF;

                    migr_pks_user_message(i).OLD_ID := user_message(i).ID_PK;
                    migr_pks_user_message(i).NEW_ID := v_id_pk;

                    migr_pks_message_info(i).OLD_ID := user_message(i).ID_PK;
                    migr_pks_message_info(i).NEW_ID := v_id_pk;

                    migr_user_message(i).ID_PK := v_id_pk;
                    migr_user_message(i).MESSAGE_ID := user_message(i).MESSAGE_ID;
                    migr_user_message(i).REF_TO_MESSAGE_ID := user_message(i).REF_TO_MESSAGE_ID;
                    migr_user_message(i).CONVERSATION_ID := user_message(i).CONVERSATION_ID;
                    migr_user_message(i).SOURCE_MESSAGE := user_message(i).SOURCE_MESSAGE;
                    migr_user_message(i).MESSAGE_FRAGMENT := user_message(i).MESSAGE_FRAGMENT;
                    migr_user_message(i).EBMS3_TIMESTAMP := user_message(i).EBMS3_TIMESTAMP;
                    migr_user_message(i).MPC_ID_FK := get_tb_d_mpc_rec(user_message(i).MPC);
                    migr_user_message(i).FROM_ROLE_ID_FK := get_tb_d_role_rec(user_message(i).FROM_ROLE);
                    migr_user_message(i).TO_ROLE_ID_FK := get_tb_d_role_rec(user_message(i).TO_ROLE);
                    migr_user_message(i).SERVICE_ID_FK := get_tb_d_service_rec(user_message(i).SERVICE_TYPE, user_message(i).SERVICE_VALUE);
                    migr_user_message(i).AGREEMENT_ID_FK := get_tb_d_agreement_rec(user_message(i).AGREEMENT_REF_TYPE, user_message(i).AGREEMENT_REF_VALUE);
                    migr_user_message(i).ACTION_ID_FK := get_tb_d_action_rec(user_message(i).ACTION);
                    migr_user_message(i).FROM_PARTY_ID_FK := get_tb_d_party_rec(user_message(i).FROM_PARTY_TYPE, user_message(i).FROM_PARTY_VALUE);
                    migr_user_message(i).TO_PARTY_ID_FK := get_tb_d_party_rec(user_message(i).TO_PARTY_TYPE, user_message(i).TO_PARTY_VALUE);
                    migr_user_message(i).TEST_MESSAGE := get_msg_subtype(user_message(i).MESSAGE_SUBTYPE);
                END LOOP;

            v_start := 1;
            v_last := migr_user_message.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_user_message -> update user message lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_USER_MESSAGE (OLD_ID, NEW_ID)
                    VALUES (migr_pks_user_message(i).OLD_ID,
                            migr_pks_user_message(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                log_verbose('migrate_user_message -> update message info lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_MESSAGE_INFO (OLD_ID, NEW_ID)
                    VALUES (migr_pks_message_info(i).OLD_ID,
                            migr_pks_message_info(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_user_message -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_USER_MESSAGE (ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, CONVERSATION_ID,
                                                          SOURCE_MESSAGE, MESSAGE_FRAGMENT, EBMS3_TIMESTAMP, MPC_ID_FK,
                                                          FROM_ROLE_ID_FK, TO_ROLE_ID_FK, SERVICE_ID_FK, AGREEMENT_ID_FK,
                                                          ACTION_ID_FK, FROM_PARTY_ID_FK, TO_PARTY_ID_FK, TEST_MESSAGE)
                        VALUES (migr_user_message(i).ID_PK,
                                migr_user_message(i).MESSAGE_ID,
                                migr_user_message(i).REF_TO_MESSAGE_ID,
                                migr_user_message(i).CONVERSATION_ID,
                                migr_user_message(i).SOURCE_MESSAGE,
                                migr_user_message(i).MESSAGE_FRAGMENT,
                                migr_user_message(i).EBMS3_TIMESTAMP,
                                migr_user_message(i).MPC_ID_FK,
                                migr_user_message(i).FROM_ROLE_ID_FK,
                                migr_user_message(i).TO_ROLE_ID_FK,
                                migr_user_message(i).SERVICE_ID_FK,
                                migr_user_message(i).AGREEMENT_ID_FK,
                                migr_user_message(i).ACTION_ID_FK,
                                migr_user_message(i).FROM_PARTY_ID_FK,
                                migr_user_message(i).TO_PARTY_ID_FK,
                                migr_user_message(i).TEST_MESSAGE);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_user_message -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                                || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                                || '  with error code '
                                                || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                                || '  for migration entry having ID_PK '
                                                || migr_user_message(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || user_message.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_user_message;

        -- check counts
        DBMS_OUTPUT.PUT_LINE('The count of TB_USER_MESSAGE should be equal to the count value for MIGR_TB_USER_MESSAGE minus 1 for the dummy user message record');
        SELECT COUNT(*) INTO v_count_user_message FROM TB_USER_MESSAGE;
        SELECT COUNT(*) INTO v_count_migr_user_message FROM MIGR_TB_USER_MESSAGE;
        IF v_count_user_message = v_count_migr_user_message - 1 THEN
            DBMS_OUTPUT.PUT_LINE('TB_USER_MESSAGE migration is done');
        ELSE
            DBMS_OUTPUT.PUT_LINE('Table TB_USER_MESSAGE has different number of records - ' || v_count_user_message ||
                    ' (should be one less) - than table MIGR_TB_USER_MESSAGE - ' || v_count_migr_user_message || ' -');
        END IF;

    END migrate_user_message;

    /**-- TB_MESSAGE_FRAGMENT migration --*/
    PROCEDURE migrate_message_fragment IS
        v_tab              VARCHAR2(30) := 'TB_MESSAGE_FRAGMENT';
        v_tab_new          VARCHAR2(30) := 'MIGR_TB_SJ_MESSAGE_FRAGMENT';
        v_tab_user_message VARCHAR2(30) := 'TB_USER_MESSAGE';

        CURSOR c_message_fragment IS
            SELECT (SELECT MPKSUM.NEW_ID
                    FROM MIGR_TB_PKS_USER_MESSAGE MPKSUM
                    WHERE MPKSUM.OLD_ID = UM.ID_PK) AS ID_PK, -- 1:1 ID_PK implementation
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

        TYPE T_MIGR_MESSAGE_FRAGMENT IS TABLE OF MIGR_TB_SJ_MESSAGE_FRAGMENT%ROWTYPE INDEX BY PLS_INTEGER;
        migr_message_fragment T_MIGR_MESSAGE_FRAGMENT;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        IF NOT check_table_exists(v_tab_user_message) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab_user_message || ' should exists before starting ' || v_tab || ' migration');
        END IF;

        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_message_fragment;
        LOOP
            FETCH c_message_fragment BULK COLLECT INTO message_fragment LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_fragment.COUNT = 0;

            migr_message_fragment := T_MIGR_MESSAGE_FRAGMENT();

            FOR i IN message_fragment.FIRST .. message_fragment.LAST
                LOOP
                    migr_message_fragment(i).ID_PK := message_fragment(i).ID_PK;
                    migr_message_fragment(i).GROUP_ID_FK := message_fragment(i).GROUP_ID_FK;
                    migr_message_fragment(i).FRAGMENT_NUMBER := message_fragment(i).FRAGMENT_NUMBER;
                    migr_message_fragment(i).CREATION_TIME := message_fragment(i).CREATION_TIME;
                    migr_message_fragment(i).CREATED_BY := message_fragment(i).CREATED_BY;
                    migr_message_fragment(i).MODIFICATION_TIME := message_fragment(i).MODIFICATION_TIME;
                    migr_message_fragment(i).MODIFIED_BY := message_fragment(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_message_fragment.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_message_fragment -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_SJ_MESSAGE_FRAGMENT (ID_PK, GROUP_ID_FK, FRAGMENT_NUMBER, CREATION_TIME,
                                                                 CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_message_fragment(i).ID_PK,
                                migr_message_fragment(i).GROUP_ID_FK,
                                migr_message_fragment(i).FRAGMENT_NUMBER,
                                migr_message_fragment(i).CREATION_TIME,
                                migr_message_fragment(i).CREATED_BY,
                                migr_message_fragment(i).MODIFICATION_TIME,
                                migr_message_fragment(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_message_fragment -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_message_fragment(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose(v_tab_new || ': Migrated ' || message_fragment.COUNT || ' records');
        END LOOP;

        COMMIT;
        CLOSE c_message_fragment;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;

    END migrate_message_fragment;

    /**-- TB_MESSAGE_GROUP migration --*/
    PROCEDURE migrate_message_group IS
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
        message_group T_MESSAGE_GROUP;

        TYPE T_MIGR_MESSAGE_GROUP IS TABLE OF MIGR_TB_SJ_MESSAGE_GROUP%ROWTYPE INDEX BY PLS_INTEGER;
        migr_message_group T_MIGR_MESSAGE_GROUP;

        TYPE T_MIGR_PKS_MESSAGE_GROUP IS TABLE OF MIGR_TB_PKS_MESSAGE_GROUP%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_message_group T_MIGR_PKS_MESSAGE_GROUP;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        IF NOT check_table_exists(v_tab_user_message_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab_user_message_new || ' should exists before starting ' || v_tab || ' migration');
        END IF;

        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_message_group;
        LOOP
            FETCH c_message_group BULK COLLECT INTO message_group LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_group.COUNT = 0;

            migr_message_group := T_MIGR_MESSAGE_GROUP();
            migr_pks_message_group := T_MIGR_PKS_MESSAGE_GROUP();

            FOR i IN message_group.FIRST .. message_group.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(message_group(i).ID_PK, message_group(i).CREATION_TIME);

                    migr_pks_message_group(i).OLD_ID := message_group(i).ID_PK;
                    migr_pks_message_group(i).NEW_ID := v_id_pk;

                    migr_message_group(i).ID_PK := v_id_pk;
                    migr_message_group(i).GROUP_ID := message_group(i).GROUP_ID;
                    migr_message_group(i).MESSAGE_SIZE := message_group(i).MESSAGE_SIZE;
                    migr_message_group(i).FRAGMENT_COUNT := message_group(i).FRAGMENT_COUNT;
                    migr_message_group(i).SENT_FRAGMENTS := message_group(i).SENT_FRAGMENTS;
                    migr_message_group(i).RECEIVED_FRAGMENTS := message_group(i).RECEIVED_FRAGMENTS;
                    migr_message_group(i).COMPRESSION_ALGORITHM := message_group(i).COMPRESSION_ALGORITHM;
                    migr_message_group(i).COMPRESSED_MESSAGE_SIZE := message_group(i).COMPRESSED_MESSAGE_SIZE;
                    migr_message_group(i).SOAP_ACTION := message_group(i).SOAP_ACTION;
                    migr_message_group(i).REJECTED := message_group(i).REJECTED;
                    migr_message_group(i).EXPIRED := message_group(i).EXPIRED;
                    migr_message_group(i).MSH_ROLE_ID_FK := get_tb_d_msh_role_rec(message_group(i).MSH_ROLE);
                    migr_message_group(i).CREATION_TIME := message_group(i).CREATION_TIME;
                    migr_message_group(i).CREATED_BY := message_group(i).CREATED_BY;
                    migr_message_group(i).MODIFICATION_TIME := message_group(i).MODIFICATION_TIME;
                    migr_message_group(i).MODIFIED_BY := message_group(i).MODIFIED_BY;
                    migr_message_group(i).SOURCE_MESSAGE_ID_FK := get_tb_user_message_rec(message_group(i).SOURCE_MESSAGE_ID);

                    IF migr_message_group(i).SOURCE_MESSAGE_ID_FK IS NULL THEN
                        log_verbose('Encountered NULL value for mandatory user message FK value: setting its value to the dummy user message ID_PK 19700101');
                        migr_message_group(i).SOURCE_MESSAGE_ID_FK := DUMMY_USER_MESSAGE_ID_PK;
                    END IF;
                END LOOP;

            v_start := 1;
            v_last := migr_message_group.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_message_group -> update message group lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_MESSAGE_GROUP (OLD_ID, NEW_ID)
                    VALUES (migr_pks_message_group(i).OLD_ID,
                            migr_pks_message_group(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_message_group -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_SJ_MESSAGE_GROUP (ID_PK, GROUP_ID, MESSAGE_SIZE, FRAGMENT_COUNT,
                                                              SENT_FRAGMENTS, RECEIVED_FRAGMENTS, COMPRESSION_ALGORITHM,
                                                              COMPRESSED_MESSAGE_SIZE, SOAP_ACTION, REJECTED, EXPIRED,
                                                              MSH_ROLE_ID_FK, CREATION_TIME, CREATED_BY,
                                                              MODIFICATION_TIME, MODIFIED_BY, SOURCE_MESSAGE_ID_FK)
                        VALUES (migr_message_group(i).ID_PK,
                                migr_message_group(i).GROUP_ID,
                                migr_message_group(i).MESSAGE_SIZE,
                                migr_message_group(i).FRAGMENT_COUNT,
                                migr_message_group(i).SENT_FRAGMENTS,
                                migr_message_group(i).RECEIVED_FRAGMENTS,
                                migr_message_group(i).COMPRESSION_ALGORITHM,
                                migr_message_group(i).COMPRESSED_MESSAGE_SIZE,
                                migr_message_group(i).SOAP_ACTION,
                                migr_message_group(i).REJECTED,
                                migr_message_group(i).EXPIRED,
                                migr_message_group(i).MSH_ROLE_ID_FK,
                                migr_message_group(i).CREATION_TIME,
                                migr_message_group(i).CREATED_BY,
                                migr_message_group(i).MODIFICATION_TIME,
                                migr_message_group(i).MODIFIED_BY,
                                migr_message_group(i).SOURCE_MESSAGE_ID_FK); -- we look into migrated table here
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_message_group -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_message_group(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose(v_tab_new || ': Migrated ' || message_group.COUNT || ' records');
        END LOOP;

        COMMIT;
        CLOSE c_message_group;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;

    END migrate_message_group;

    /**-- TB_MESSAGE_GROUP migration --*/
    PROCEDURE migrate_message_header IS
        v_tab               VARCHAR2(30) := 'TB_MESSAGE_HEADER';
        v_tab_new           VARCHAR2(30) := 'MIGR_TB_SJ_MESSAGE_HEADER';
        v_tab_message_group VARCHAR2(30) := 'TB_MESSAGE_GROUP';

        CURSOR c_message_header IS
            SELECT (SELECT MPKSMG.NEW_ID
                    FROM MIGR_TB_PKS_MESSAGE_GROUP MPKSMG
                    WHERE MPKSMG.OLD_ID = MG.ID_PK) AS ID_PK, -- 1:1 ID_PK implementation
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
        message_header T_MESSAGE_HEADER;

        TYPE T_MIGR_MESSAGE_HEADER IS TABLE OF MIGR_TB_SJ_MESSAGE_HEADER%ROWTYPE INDEX BY PLS_INTEGER;
        migr_message_header T_MIGR_MESSAGE_HEADER;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        IF NOT check_table_exists(v_tab_message_group) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab_message_group || ' should exists before starting ' || v_tab || ' migration');
        END IF;

        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_message_header;
        LOOP
            FETCH c_message_header BULK COLLECT INTO message_header LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_header.COUNT = 0;

            migr_message_header := T_MIGR_MESSAGE_HEADER();

            FOR i IN message_header.FIRST .. message_header.LAST
                LOOP
                    migr_message_header(i).ID_PK := message_header(i).ID_PK;
                    migr_message_header(i).BOUNDARY := message_header(i).BOUNDARY;
                    migr_message_header(i).START_MULTIPART := message_header(i)."START";
                    migr_message_header(i).CREATION_TIME := message_header(i).CREATION_TIME;
                    migr_message_header(i).CREATED_BY := message_header(i).CREATED_BY;
                    migr_message_header(i).MODIFICATION_TIME := message_header(i).MODIFICATION_TIME;
                    migr_message_header(i).MODIFIED_BY := message_header(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_message_header.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_message_header -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_SJ_MESSAGE_HEADER (ID_PK, BOUNDARY, START_MULTIPART, CREATION_TIME,
                                                               CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_message_header(i).ID_PK,
                                migr_message_header(i).BOUNDARY,
                                migr_message_header(i).START_MULTIPART,
                                migr_message_header(i).CREATION_TIME,
                                migr_message_header(i).CREATED_BY,
                                migr_message_header(i).MODIFICATION_TIME,
                                migr_message_header(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_message_header -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_message_header(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose(v_tab_new || ': Migrated ' || message_header.COUNT || ' records');
        END LOOP;

        COMMIT;
        CLOSE c_message_header;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;

    END migrate_message_header;

    /**-- TB_SIGNAL_MESSAGE, TB_RECEIPT and TB_RECEIPT_DATA migration --*/
    PROCEDURE migrate_signal_receipt IS
        v_tab_signal           VARCHAR2(30) := 'TB_SIGNAL_MESSAGE';
        v_tab_signal_new       VARCHAR2(30) := 'MIGR_TB_SIGNAL_MESSAGE';
        v_tab_messaging        VARCHAR2(30) := 'TB_MESSAGING';
        v_tab_user_message     VARCHAR2(30) := 'TB_USER_MESSAGE';
        v_tab_receipt          VARCHAR2(30) := 'TB_RECEIPT';
        v_tab_receipt_data     VARCHAR2(30) := 'TB_RECEIPT_DATA';
        v_tab_receipt_new      VARCHAR2(30) := 'MIGR_TB_RECEIPT';

        CURSOR c_signal_message_receipt IS
            SELECT (SELECT MPKSUM.NEW_ID
                    FROM MIGR_TB_PKS_USER_MESSAGE MPKSUM
                    WHERE MPKSUM.OLD_ID = UM.ID_PK) AS ID_PK, -- 1:1 ID_PK implementation
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

        TYPE T_MIGR_SIGNAL_MESSAGE IS TABLE OF MIGR_TB_SIGNAL_MESSAGE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_signal_message T_MIGR_SIGNAL_MESSAGE;

        TYPE T_MIGR_RECEIPT IS TABLE OF MIGR_TB_RECEIPT%ROWTYPE INDEX BY PLS_INTEGER;
        migr_receipt T_MIGR_RECEIPT;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
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
            FETCH c_signal_message_receipt BULK COLLECT INTO signal_message_receipt LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN signal_message_receipt.COUNT = 0;

            migr_signal_message := T_MIGR_SIGNAL_MESSAGE();
            migr_receipt := T_MIGR_RECEIPT();

            FOR i IN signal_message_receipt.FIRST .. signal_message_receipt.LAST
                LOOP
                    migr_signal_message(i).ID_PK := signal_message_receipt(i).ID_PK;
                    migr_signal_message(i).SIGNAL_MESSAGE_ID := signal_message_receipt(i).SIGNAL_MESSAGE_ID;
                    migr_signal_message(i).REF_TO_MESSAGE_ID := signal_message_receipt(i).REF_TO_MESSAGE_ID;
                    migr_signal_message(i).EBMS3_TIMESTAMP := signal_message_receipt(i).EBMS3_TIMESTAMP;
                    migr_signal_message(i).CREATION_TIME := signal_message_receipt(i).CREATION_TIME;
                    migr_signal_message(i).CREATED_BY := signal_message_receipt(i).CREATED_BY;
                    migr_signal_message(i).MODIFICATION_TIME := signal_message_receipt(i).MODIFICATION_TIME;
                    migr_signal_message(i).MODIFIED_BY := signal_message_receipt(i).MODIFIED_BY;

                    migr_receipt(i).ID_PK := signal_message_receipt(i).ID_PK;
                    migr_receipt(i).RAW_XML := clob_to_blob(signal_message_receipt(i).RAW_XML);
                    migr_receipt(i).CREATION_TIME := signal_message_receipt(i).R_CREATION_TIME;
                    migr_receipt(i).CREATED_BY := signal_message_receipt(i).R_CREATED_BY;
                    migr_receipt(i).MODIFICATION_TIME := signal_message_receipt(i).R_MODIFICATION_TIME;
                    migr_receipt(i).MODIFIED_BY := signal_message_receipt(i).R_MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_signal_message.COUNT; -- both have same length

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                -- new tb_signal_message table
                BEGIN
                    log_verbose('migrate_signal_receipt - signal message -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_SIGNAL_MESSAGE (ID_PK, SIGNAL_MESSAGE_ID, REF_TO_MESSAGE_ID,
                                                            EBMS3_TIMESTAMP, CREATION_TIME, CREATED_BY,
                                                            MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_signal_message(i).ID_PK,
                                migr_signal_message(i).SIGNAL_MESSAGE_ID,
                                migr_signal_message(i).REF_TO_MESSAGE_ID,
                                migr_signal_message(i).EBMS3_TIMESTAMP,
                                migr_signal_message(i).CREATION_TIME,
                                migr_signal_message(i).CREATED_BY,
                                migr_signal_message(i).MODIFICATION_TIME,
                                migr_signal_message(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_signal_receipt - signal message -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_signal_message(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                DBMS_OUTPUT.PUT_LINE(v_tab_signal_new || ': Committing...');
                COMMIT;

                -- new tb_receipt table
                BEGIN
                    log_verbose('migrate_signal_receipt - receipt -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_RECEIPT (ID_PK, RAW_XML, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                     MODIFIED_BY)
                        VALUES (migr_receipt(i).ID_PK,
                                migr_receipt(i).RAW_XML,
                                migr_receipt(i).CREATION_TIME,
                                migr_receipt(i).CREATED_BY,
                                migr_receipt(i).MODIFICATION_TIME,
                                migr_receipt(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_signal_receipt - receipt -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_receipt(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                DBMS_OUTPUT.PUT_LINE(v_tab_receipt_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            DBMS_OUTPUT.PUT_LINE(
                        'Migrated ' || signal_message_receipt.COUNT || ' records into both ' || v_tab_signal_new ||
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
    PROCEDURE migrate_raw_envelope_log IS
        v_tab              VARCHAR2(30) := 'TB_RAWENVELOPE_LOG';
        v_tab_user_new     VARCHAR2(30) := 'MIGR_TB_USER_MESSAGE_RAW';
        v_tab_signal_new   VARCHAR2(30) := 'MIGR_TB_SIGNAL_MESSAGE_RAW';
        v_tab_user_message VARCHAR2(30) := 'TB_USER_MESSAGE';
        v_tab_messaging    VARCHAR2(30) := 'TB_MESSAGING';
        v_tab_migrated     VARCHAR2(30) := v_tab_signal_new;

        calculated_raw_xml BLOB;

        CURSOR c_raw_envelope IS
            SELECT (SELECT MPKSUM.NEW_ID
                    FROM MIGR_TB_PKS_USER_MESSAGE MPKSUM
                    WHERE MPKSUM.OLD_ID = UM.ID_PK) AS ID_PK, -- 1:1 ID_PK implementation
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
            SELECT (SELECT MPKSUM.NEW_ID
                    FROM MIGR_TB_PKS_USER_MESSAGE MPKSUM
                    WHERE MPKSUM.OLD_ID = UM.ID_PK) AS ID_PK, -- 1:1 ID_PK implementation
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
        raw_envelope T_RAW_ENVELOPE;

        TYPE T_MIGR_USER_MESSAGE_RAW IS TABLE OF MIGR_TB_USER_MESSAGE_RAW%ROWTYPE INDEX BY PLS_INTEGER;
        migr_user_message_raw T_MIGR_USER_MESSAGE_RAW;

        TYPE T_MIGR_SIGNAL_MESSAGE_RAW IS TABLE OF MIGR_TB_SIGNAL_MESSAGE_RAW%ROWTYPE INDEX BY PLS_INTEGER;
        migr_signal_message_raw T_MIGR_SIGNAL_MESSAGE_RAW;

        v_current PLS_INTEGER;
        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        IF NOT check_table_exists(v_tab_messaging) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab_messaging || ' should exists before starting ' || v_tab || ' migration');
        END IF;
        IF NOT check_table_exists(v_tab_user_message) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab_user_message || ' should exists before starting ' || v_tab || ' migration');
        END IF;

        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');

        OPEN c_raw_envelope;
        LOOP
            FETCH c_raw_envelope BULK COLLECT INTO raw_envelope LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN raw_envelope.COUNT = 0;

            migr_user_message_raw := T_MIGR_USER_MESSAGE_RAW();
            migr_signal_message_raw := T_MIGR_SIGNAL_MESSAGE_RAW();

            FOR i IN raw_envelope.FIRST .. raw_envelope.LAST
                LOOP
                    calculated_raw_xml := clob_to_blob(raw_envelope(i).RAW_XML);

                    IF raw_envelope(i)."TYPE" = 'USER' THEN
                        v_current := migr_user_message_raw.COUNT + 1;
                        migr_user_message_raw(v_current).ID_PK := raw_envelope(i).ID_PK;
                        migr_user_message_raw(v_current).RAW_XML := calculated_raw_xml;
                        migr_user_message_raw(v_current).CREATION_TIME := raw_envelope(i).CREATION_TIME;
                        migr_user_message_raw(v_current).CREATED_BY := raw_envelope(i).CREATED_BY;
                        migr_user_message_raw(v_current).MODIFICATION_TIME := raw_envelope(i).MODIFICATION_TIME;
                        migr_user_message_raw(v_current).MODIFIED_BY := raw_envelope(i).MODIFIED_BY;
                    ELSE
                        v_current := migr_signal_message_raw.COUNT + 1;
                        migr_signal_message_raw(v_current).ID_PK := raw_envelope(i).ID_PK;
                        migr_signal_message_raw(v_current).RAW_XML := calculated_raw_xml;
                        migr_signal_message_raw(v_current).CREATION_TIME := raw_envelope(i).CREATION_TIME;
                        migr_signal_message_raw(v_current).CREATED_BY := raw_envelope(i).CREATED_BY;
                        migr_signal_message_raw(v_current).MODIFICATION_TIME := raw_envelope(i).MODIFICATION_TIME;
                        migr_signal_message_raw(v_current).MODIFIED_BY := raw_envelope(i).MODIFIED_BY;
                    END IF;
                END LOOP;

            -- new tb_user_message_raw table
            v_start := 1;
            v_last := migr_user_message_raw.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_raw_envelope_log - user message raw -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_USER_MESSAGE_RAW (ID_PK, RAW_XML, CREATION_TIME, CREATED_BY,
                                                              MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_user_message_raw(i).ID_PK,
                                migr_user_message_raw(i).RAW_XML,
                                migr_user_message_raw(i).CREATION_TIME,
                                migr_user_message_raw(i).CREATED_BY,
                                migr_user_message_raw(i).MODIFICATION_TIME,
                                migr_user_message_raw(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_raw_envelope_log - user message raw -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_user_message_raw(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                DBMS_OUTPUT.PUT_LINE(v_tab_user_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;

            -- new tb_signal_message_raw table
            v_start := 1;
            v_last := migr_signal_message_raw.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_raw_envelope_log - signal message raw -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_SIGNAL_MESSAGE_RAW (ID_PK, RAW_XML, CREATION_TIME, CREATED_BY,
                                                                MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_signal_message_raw(i).ID_PK,
                                migr_signal_message_raw(i).RAW_XML,
                                migr_signal_message_raw(i).CREATION_TIME,
                                migr_signal_message_raw(i).CREATED_BY,
                                migr_signal_message_raw(i).MODIFICATION_TIME,
                                migr_signal_message_raw(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_raw_envelope_log - signal message raw -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_signal_message_raw(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                DBMS_OUTPUT.PUT_LINE(v_tab_signal_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;

            log_verbose('Migrated ' || raw_envelope.COUNT || ' records into ' || v_tab_migrated);
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

        calculated_message_status_id_fk NUMBER;
        calculated_msh_role_id_fk NUMBER;

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
        message_log T_MESSAGE_LOG;

        TYPE T_MIGR_USER_MESSAGE_LOG IS TABLE OF MIGR_TB_USER_MESSAGE_LOG%ROWTYPE INDEX BY PLS_INTEGER;
        migr_user_message_log T_MIGR_USER_MESSAGE_LOG;

        TYPE T_MIGR_SIGNAL_MESSAGE_LOG IS TABLE OF MIGR_TB_SIGNAL_MESSAGE_LOG%ROWTYPE INDEX BY PLS_INTEGER;
        migr_signal_message_log T_MIGR_SIGNAL_MESSAGE_LOG;

        v_current PLS_INTEGER;
        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_message_log;
        LOOP
            FETCH c_message_log BULK COLLECT INTO message_log LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_log.COUNT = 0;

            migr_user_message_log := T_MIGR_USER_MESSAGE_LOG();
            migr_signal_message_log := T_MIGR_SIGNAL_MESSAGE_LOG();

            FOR i IN message_log.FIRST .. message_log.LAST
                LOOP
                    calculated_message_status_id_fk := get_tb_d_msg_status_rec(message_log(i).MESSAGE_STATUS);
                    calculated_msh_role_id_fk := get_tb_d_msh_role_rec(message_log(i).MSH_ROLE);

                    IF message_log(i).MESSAGE_TYPE = 'USER_MESSAGE' THEN
                        v_current := migr_user_message_log.COUNT + 1;
                        migr_user_message_log(v_current).ID_PK := get_tb_user_message_rec(message_log(i).MESSAGE_ID);
                        migr_user_message_log(v_current).BACKEND := message_log(i).BACKEND;
                        migr_user_message_log(v_current).RECEIVED := message_log(i).RECEIVED;
                        migr_user_message_log(v_current).DOWNLOADED := message_log(i).DOWNLOADED;
                        migr_user_message_log(v_current).FAILED := message_log(i).FAILED;
                        migr_user_message_log(v_current).RESTORED := message_log(i).RESTORED;
                        migr_user_message_log(v_current).DELETED := message_log(i).DELETED;
                        migr_user_message_log(v_current).NEXT_ATTEMPT := message_log(i).NEXT_ATTEMPT;
                        migr_user_message_log(v_current).SEND_ATTEMPTS := message_log(i).SEND_ATTEMPTS;
                        migr_user_message_log(v_current).SEND_ATTEMPTS_MAX := message_log(i).SEND_ATTEMPTS_MAX;
                        migr_user_message_log(v_current).SCHEDULED := message_log(i).SCHEDULED;
                        migr_user_message_log(v_current).VERSION := message_log(i).VERSION;
                        migr_user_message_log(v_current).MESSAGE_STATUS_ID_FK := calculated_message_status_id_fk;
                        migr_user_message_log(v_current).MSH_ROLE_ID_FK := calculated_msh_role_id_fk;
                        migr_user_message_log(v_current).NOTIFICATION_STATUS_ID_FK := get_tb_d_notif_status_rec(message_log(i).NOTIFICATION_STATUS);
                        migr_user_message_log(v_current).CREATION_TIME := message_log(i).CREATION_TIME;
                        migr_user_message_log(v_current).CREATED_BY := message_log(i).CREATED_BY;
                        migr_user_message_log(v_current).MODIFICATION_TIME := message_log(i).MODIFICATION_TIME;
                        migr_user_message_log(v_current).MODIFIED_BY := message_log(i).MODIFIED_BY;
                    ELSE
                        v_current := migr_signal_message_log.COUNT + 1;
                        migr_signal_message_log(v_current).ID_PK := get_tb_signal_message_rec(message_log(i).MESSAGE_ID);
                        migr_signal_message_log(v_current).RECEIVED := message_log(i).RECEIVED;
                        migr_signal_message_log(v_current).DELETED := message_log(i).DELETED;
                        migr_signal_message_log(v_current).MESSAGE_STATUS_ID_FK := calculated_message_status_id_fk;
                        migr_signal_message_log(v_current).MSH_ROLE_ID_FK := calculated_msh_role_id_fk;
                        migr_signal_message_log(v_current).CREATION_TIME := message_log(i).CREATION_TIME;
                        migr_signal_message_log(v_current).CREATED_BY := message_log(i).CREATED_BY;
                        migr_signal_message_log(v_current).MODIFICATION_TIME := message_log(i).MODIFICATION_TIME;
                        migr_signal_message_log(v_current).MODIFIED_BY := message_log(i).MODIFIED_BY;
                    END IF;
                END LOOP;

            -- new tb_user_message_log table
            v_start := 1;
            v_last := migr_user_message_log.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_message_log - user message log -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_USER_MESSAGE_LOG (ID_PK, BACKEND, RECEIVED, DOWNLOADED, FAILED,
                                                              RESTORED, DELETED, NEXT_ATTEMPT, SEND_ATTEMPTS,
                                                              SEND_ATTEMPTS_MAX, SCHEDULED, VERSION,
                                                              MESSAGE_STATUS_ID_FK, MSH_ROLE_ID_FK,
                                                              NOTIFICATION_STATUS_ID_FK, CREATION_TIME,
                                                              CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_user_message_log(i).ID_PK,
                                migr_user_message_log(i).BACKEND,
                                migr_user_message_log(i).RECEIVED,
                                migr_user_message_log(i).DOWNLOADED,
                                migr_user_message_log(i).FAILED,
                                migr_user_message_log(i).RESTORED,
                                migr_user_message_log(i).DELETED,
                                migr_user_message_log(i).NEXT_ATTEMPT,
                                migr_user_message_log(i).SEND_ATTEMPTS,
                                migr_user_message_log(i).SEND_ATTEMPTS_MAX,
                                migr_user_message_log(i).SCHEDULED,
                                migr_user_message_log(i).VERSION,
                                migr_user_message_log(i).MESSAGE_STATUS_ID_FK,
                                migr_user_message_log(i).MSH_ROLE_ID_FK,
                                migr_user_message_log(i).NOTIFICATION_STATUS_ID_FK,
                                migr_user_message_log(i).CREATION_TIME,
                                migr_user_message_log(i).CREATED_BY,
                                migr_user_message_log(i).MODIFICATION_TIME,
                                migr_user_message_log(i).MODIFIED_BY);
                    v_count_user := v_count_user + v_end - v_start + 1;
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_message_log - user message log -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');
                            v_count_user := v_count_user + SQL%ROWCOUNT;

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_user_message_log(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                DBMS_OUTPUT.PUT_LINE(v_tab_user_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;

            -- new tb_signal_message_log table
            v_start := 1;
            v_last := migr_signal_message_log.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_message_log - signal message log -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_SIGNAL_MESSAGE_LOG (ID_PK, RECEIVED, DELETED, MESSAGE_STATUS_ID_FK,
                                                                MSH_ROLE_ID_FK, CREATION_TIME, CREATED_BY,
                                                                MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_signal_message_log(i).ID_PK,
                                migr_signal_message_log(i).RECEIVED,
                                migr_signal_message_log(i).DELETED,
                                migr_signal_message_log(i).MESSAGE_STATUS_ID_FK,
                                migr_signal_message_log(i).MSH_ROLE_ID_FK,
                                migr_signal_message_log(i).CREATION_TIME,
                                migr_signal_message_log(i).CREATED_BY,
                                migr_signal_message_log(i).MODIFICATION_TIME,
                                migr_signal_message_log(i).MODIFIED_BY);
                    v_count_signal := v_count_signal + v_end - v_start + 1;
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_message_log - signal message log -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');
                            v_count_signal := v_count_signal + SQL%ROWCOUNT;

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_signal_message_log(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                DBMS_OUTPUT.PUT_LINE(v_tab_signal_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;

            v_count := v_count + message_log.COUNT;
            DBMS_OUTPUT.PUT_LINE(
                        'Migrated ' || message_log.COUNT || ' records: ' || v_count_user || ' into ' || v_tab_user_new ||
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
    PROCEDURE migrate_property IS
        v_tab              VARCHAR2(30) := 'TB_PROPERTY';
        v_tab_message_new  VARCHAR2(30) := 'MIGR_TB_MESSAGE_PROPERTIES';

        CURSOR c_property IS
            SELECT (SELECT MPKSUM.NEW_ID
                    FROM MIGR_TB_PKS_USER_MESSAGE MPKSUM
                    WHERE MPKSUM.OLD_ID = UM.ID_PK) AS USER_MESSAGE_ID_FK, -- 1:1 ID_PK implementation
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
        property T_PROPERTY;

        TYPE T_MESSAGE_PROPERTIES IS TABLE OF MIGR_TB_MESSAGE_PROPERTIES%ROWTYPE INDEX BY PLS_INTEGER;
        migr_message_properties T_MESSAGE_PROPERTIES;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_property;
        LOOP
            FETCH c_property BULK COLLECT INTO property LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN property.COUNT = 0;

            migr_message_properties := T_MESSAGE_PROPERTIES();

            FOR i IN property.FIRST .. property.LAST
                LOOP
                    migr_message_properties(i).USER_MESSAGE_ID_FK := property(i).USER_MESSAGE_ID_FK;
                    migr_message_properties(i).MESSAGE_PROPERTY_FK := get_tb_d_msg_property_rec(property(i).NAME, property(i).VALUE, property(i).TYPE);
                    migr_message_properties(i).CREATION_TIME := property(i).CREATION_TIME;
                    migr_message_properties(i).CREATED_BY := property(i).CREATED_BY;
                    migr_message_properties(i).MODIFICATION_TIME := property(i).MODIFICATION_TIME;
                    migr_message_properties(i).MODIFIED_BY := property(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_message_properties.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_property -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_MESSAGE_PROPERTIES (USER_MESSAGE_ID_FK, MESSAGE_PROPERTY_FK, CREATION_TIME,
                                                                CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_message_properties(i).USER_MESSAGE_ID_FK,
                                migr_message_properties(i).MESSAGE_PROPERTY_FK,
                                migr_message_properties(i).CREATION_TIME,
                                migr_message_properties(i).CREATED_BY,
                                migr_message_properties(i).MODIFICATION_TIME,
                                migr_message_properties(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_property -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having USER_MESSAGE_ID_FK '
                                        || migr_message_properties(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).USER_MESSAGE_ID_FK);
                                END LOOP;
                END;

                DBMS_OUTPUT.PUT_LINE(v_tab_message_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;

            log_verbose('Migrated ' || property.COUNT || ' records into ' || v_tab_message_new);
        END LOOP;

        COMMIT;
        CLOSE c_property;
    END migrate_property;

    /**- TB_PART_INFO, TB_USER_MESSAGE data migration --*/
    PROCEDURE migrate_part_info_user IS
        v_tab              VARCHAR2(30) := 'TB_PART_INFO';
        v_tab_new          VARCHAR2(30) := 'MIGR_TB_PART_INFO';
        v_tab_user_message VARCHAR2(30) := 'TB_USER_MESSAGE';

        v_id_pk NUMBER;

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
                   (SELECT MPKSUM.NEW_ID
                    FROM MIGR_TB_PKS_USER_MESSAGE MPKSUM
                    WHERE MPKSUM.OLD_ID = UM.ID_PK) AS USER_MESSAGE_ID_FK -- 1:1 ID_PK implementation
            FROM TB_USER_MESSAGE UM,
                 TB_PART_INFO PI
            WHERE PI.PAYLOADINFO_ID = UM.ID_PK;

        TYPE T_PART_INFO IS TABLE OF c_part_info%ROWTYPE;
        part_info T_PART_INFO;

        TYPE T_MIGR_PART_INFO IS TABLE OF MIGR_TB_PART_INFO%ROWTYPE INDEX BY PLS_INTEGER;
        migr_part_info T_MIGR_PART_INFO;

        TYPE T_MIGR_PKS_PART_INFO IS TABLE OF MIGR_TB_PKS_PART_INFO%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_part_info T_MIGR_PKS_PART_INFO;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_part_info;
        LOOP
            FETCH c_part_info BULK COLLECT INTO part_info LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN part_info.COUNT = 0;

            migr_part_info := T_MIGR_PART_INFO();
            migr_pks_part_info := T_MIGR_PKS_PART_INFO();

            FOR i IN part_info.FIRST .. part_info.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(part_info(i).ID_PK, part_info(i).CREATION_TIME);

                    migr_pks_part_info(i).OLD_ID := part_info(i).ID_PK;
                    migr_pks_part_info(i).NEW_ID := v_id_pk;

                    migr_part_info(i).ID_PK := v_id_pk;
                    migr_part_info(i).BINARY_DATA := part_info(i).BINARY_DATA;
                    migr_part_info(i).DESCRIPTION_LANG := part_info(i).DESCRIPTION_LANG;
                    migr_part_info(i).DESCRIPTION_VALUE := part_info(i).DESCRIPTION_VALUE;
                    migr_part_info(i).HREF := part_info(i).HREF;
                    migr_part_info(i).IN_BODY := part_info(i).IN_BODY;
                    migr_part_info(i).FILENAME := part_info(i).FILENAME;
                    migr_part_info(i).MIME := part_info(i).MIME;
                    migr_part_info(i).PART_ORDER := part_info(i).PART_ORDER;
                    migr_part_info(i).ENCRYPTED := part_info(i).ENCRYPTED;
                    migr_part_info(i).USER_MESSAGE_ID_FK := part_info(i).USER_MESSAGE_ID_FK;
                    migr_part_info(i).CREATION_TIME := part_info(i).CREATION_TIME;
                    migr_part_info(i).CREATED_BY := part_info(i).CREATED_BY;
                    migr_part_info(i).MODIFICATION_TIME := part_info(i).MODIFICATION_TIME;
                    migr_part_info(i).MODIFIED_BY := part_info(i).MODIFIED_BY;

                    IF migr_part_info(i).USER_MESSAGE_ID_FK IS NULL THEN
                        log_verbose('Encountered NULL value for mandatory user message FK value: setting its value to the dummy user message ID_PK 19700101');
                        migr_part_info(i).USER_MESSAGE_ID_FK := DUMMY_USER_MESSAGE_ID_PK;
                    END IF;
                END LOOP;

            v_start := 1;
            v_last := migr_part_info.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_part_info_user -> update part info lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PART_INFO (OLD_ID, NEW_ID)
                    VALUES (migr_pks_part_info(i).OLD_ID,
                            migr_pks_part_info(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_part_info_user -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PART_INFO (ID_PK, BINARY_DATA, DESCRIPTION_LANG, DESCRIPTION_VALUE, HREF,
                                                       IN_BODY, FILENAME, MIME, PART_ORDER, ENCRYPTED,
                                                       USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                       MODIFIED_BY)
                        VALUES (migr_part_info(i).ID_PK,
                                migr_part_info(i).BINARY_DATA,
                                migr_part_info(i).DESCRIPTION_LANG,
                                migr_part_info(i).DESCRIPTION_VALUE,
                                migr_part_info(i).HREF,
                                migr_part_info(i).IN_BODY,
                                migr_part_info(i).FILENAME,
                                migr_part_info(i).MIME,
                                migr_part_info(i).PART_ORDER,
                                migr_part_info(i).ENCRYPTED,
                                migr_part_info(i).USER_MESSAGE_ID_FK,
                                migr_part_info(i).CREATION_TIME,
                                migr_part_info(i).CREATED_BY,
                                migr_part_info(i).MODIFICATION_TIME,
                                migr_part_info(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_part_info_user -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_part_info(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;

            log_verbose('Migrated ' || part_info.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_part_info;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' and '|| v_tab_user_message ||' migration is done');
        END IF;
    END migrate_part_info_user;

    /**- TB_PART_INFO, TB_PROPERTY data migration --*/
    PROCEDURE migrate_part_info_property IS
        v_tab_info     VARCHAR2(30) := 'TB_PART_INFO';
        v_tab_property VARCHAR2(30) := 'TB_PROPERTY';
        v_tab_new      VARCHAR2(30) := 'MIGR_TB_PART_PROPERTIES';

        CURSOR c_part_prop IS
            SELECT PR.NAME,
                   PR.VALUE,
                   PR.TYPE,
                   (SELECT MPKSPI.NEW_ID
                    FROM MIGR_TB_PKS_PART_INFO MPKSPI
                    WHERE MPKSPI.OLD_ID = PI.ID_PK) AS PART_INFO_ID_FK,
                   PR.CREATION_TIME,
                   PR.CREATED_BY,
                   PR.MODIFICATION_TIME,
                   PR.MODIFIED_BY
            FROM TB_PART_INFO PI,
                 TB_PROPERTY PR
            WHERE PR.PARTPROPERTIES_ID = PI.ID_PK;

        TYPE T_PART_PROP IS TABLE OF c_part_prop%ROWTYPE;
        part_prop T_PART_PROP;

        TYPE T_MIGR_PART_PROPERTIES IS TABLE OF MIGR_TB_PART_PROPERTIES%ROWTYPE INDEX BY PLS_INTEGER;
        migr_part_properties T_MIGR_PART_PROPERTIES;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;

        v_count_property INT;
        v_count_migr_msg_property INT;
        v_count_migr_part_property INT;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab_info || ' and ' || v_tab_property || ' migration started...');
        OPEN c_part_prop;
        LOOP
            FETCH c_part_prop BULK COLLECT INTO part_prop LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN part_prop.COUNT = 0;

            migr_part_properties := T_MIGR_PART_PROPERTIES();

            FOR i IN part_prop.FIRST .. part_prop.LAST
                LOOP
                    migr_part_properties(i).PART_INFO_ID_FK := part_prop(i).PART_INFO_ID_FK;
                    migr_part_properties(i).PART_INFO_PROPERTY_FK := get_tb_d_part_property_rec(part_prop(i).NAME,part_prop(i).VALUE, part_prop(i).TYPE);
                    migr_part_properties(i).CREATION_TIME := part_prop(i).CREATION_TIME;
                    migr_part_properties(i).CREATED_BY := part_prop(i).CREATED_BY;
                    migr_part_properties(i).MODIFICATION_TIME := part_prop(i).MODIFICATION_TIME;
                    migr_part_properties(i).MODIFIED_BY := part_prop(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_part_properties.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_part_info_property -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PART_PROPERTIES (PART_INFO_ID_FK, PART_INFO_PROPERTY_FK, CREATION_TIME,
                                                             CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_part_properties(i).PART_INFO_ID_FK,
                                migr_part_properties(i).PART_INFO_PROPERTY_FK,
                                migr_part_properties(i).CREATION_TIME,
                                migr_part_properties(i).CREATED_BY,
                                migr_part_properties(i).MODIFICATION_TIME,
                                migr_part_properties(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_part_info_property -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having PART_INFO_ID_FK '
                                        || migr_part_properties(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).PART_INFO_ID_FK
                                        || '  and PART_INFO_PROPERTY_FK '
                                        || migr_part_properties(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).PART_INFO_PROPERTY_FK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || part_prop.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_part_prop;

        -- check counts
        DBMS_OUTPUT.PUT_LINE('The count of TB_PROPERTY should be equal to the sum of count values for MIGR_TB_MESSAGE_PROPERTIES and MIGR_TB_PART_PROPERTIES');
        SELECT COUNT(*) INTO v_count_property FROM TB_PROPERTY;
        SELECT COUNT(*) INTO v_count_migr_msg_property FROM MIGR_TB_MESSAGE_PROPERTIES;
        SELECT COUNT(*) INTO v_count_migr_part_property FROM MIGR_TB_PART_PROPERTIES;
        IF v_count_property = v_count_migr_msg_property + v_count_migr_part_property THEN
             DBMS_OUTPUT.PUT_LINE('TB_PROPERTY migration between the MIGR_TB_MESSAGE_PROPERTIES and MIGR_TB_PART_PROPERTIES tables is done');
        ELSE
             DBMS_OUTPUT.PUT_LINE('Table TB_PROPERTY has different number of records - ' || v_count_property
                    || ' - than tables MIGR_TB_MESSAGE_PROPERTIES - ' || v_count_migr_msg_property
                    || ' - and MIGR_TB_PART_PROPERTIES - ' || v_count_migr_part_property || ' - together');
        END IF;
    END migrate_part_info_property;

    /**- TB_ERROR_LOG data migration --*/
    PROCEDURE migrate_error_log IS
        v_tab      VARCHAR2(30) := 'TB_ERROR_LOG';
        v_tab_new  VARCHAR2(30) := 'MIGR_TB_ERROR_LOG';

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
                   (SELECT MPKSMI.NEW_ID
                    FROM MIGR_TB_PKS_MESSAGE_INFO MPKSMI
                    WHERE MPKSMI.OLD_ID = UMMI.ID_PK) AS USER_MESSAGE_ID_FK
            FROM TB_ERROR_LOG EL
                     LEFT JOIN
                 (SELECT MI.MESSAGE_ID, UM.ID_PK
                  FROM TB_MESSAGE_INFO MI,
                       TB_USER_MESSAGE UM
                  WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK) UMMI
                 ON EL.MESSAGE_IN_ERROR_ID = UMMI.MESSAGE_ID;

        TYPE T_ERROR_LOG IS TABLE OF c_error_log%ROWTYPE;
        error_log T_ERROR_LOG;

        TYPE T_MIGR_ERROR_LOG IS TABLE OF MIGR_TB_ERROR_LOG%ROWTYPE INDEX BY PLS_INTEGER;
        migr_error_log T_MIGR_ERROR_LOG;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_error_log;
        LOOP
            FETCH c_error_log BULK COLLECT INTO error_log LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN error_log.COUNT = 0;

            migr_error_log := T_MIGR_ERROR_LOG();

            FOR i IN error_log.FIRST .. error_log.LAST
                LOOP
                    migr_error_log(i).ID_PK := generate_scalable_seq(error_log(i).ID_PK, error_log(i).CREATION_TIME);
                    migr_error_log(i).ERROR_CODE := error_log(i).ERROR_CODE;
                    migr_error_log(i).ERROR_DETAIL := error_log(i).ERROR_DETAIL;
                    migr_error_log(i).ERROR_SIGNAL_MESSAGE_ID := error_log(i).ERROR_SIGNAL_MESSAGE_ID;
                    migr_error_log(i).MESSAGE_IN_ERROR_ID := error_log(i).MESSAGE_IN_ERROR_ID;
                    migr_error_log(i).MSH_ROLE_ID_FK := get_tb_d_msh_role_rec(error_log(i).MSH_ROLE);
                    migr_error_log(i).NOTIFIED := error_log(i).NOTIFIED;
                    migr_error_log(i).TIME_STAMP := error_log(i).TIME_STAMP;
                    migr_error_log(i).USER_MESSAGE_ID_FK := error_log(i).USER_MESSAGE_ID_FK;
                    migr_error_log(i).CREATION_TIME := error_log(i).CREATION_TIME;
                    migr_error_log(i).CREATED_BY := error_log(i).CREATED_BY;
                    migr_error_log(i).MODIFICATION_TIME := error_log(i).MODIFICATION_TIME;
                    migr_error_log(i).MODIFIED_BY := error_log(i).MODIFIED_BY;

                    IF migr_error_log(i).USER_MESSAGE_ID_FK IS NULL THEN
                        log_verbose('Encountered NULL value for mandatory user message FK value: setting its value to the dummy user message ID_PK 19700101');
                        migr_error_log(i).USER_MESSAGE_ID_FK := DUMMY_USER_MESSAGE_ID_PK;
                    END IF;
                END LOOP;

            v_start := 1;
            v_last := migr_error_log.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_error_log -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_ERROR_LOG (ID_PK, ERROR_CODE, ERROR_DETAIL, ERROR_SIGNAL_MESSAGE_ID,
                                                       MESSAGE_IN_ERROR_ID, MSH_ROLE_ID_FK, NOTIFIED, TIME_STAMP,
                                                       USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                       MODIFIED_BY)
                        VALUES (migr_error_log(i).ID_PK,
                                migr_error_log(i).ERROR_CODE,
                                migr_error_log(i).ERROR_DETAIL,
                                migr_error_log(i).ERROR_SIGNAL_MESSAGE_ID,
                                migr_error_log(i).MESSAGE_IN_ERROR_ID,
                                migr_error_log(i).MSH_ROLE_ID_FK,
                                migr_error_log(i).NOTIFIED,
                                migr_error_log(i).TIME_STAMP,
                                migr_error_log(i).USER_MESSAGE_ID_FK,
                                migr_error_log(i).CREATION_TIME,
                                migr_error_log(i).CREATED_BY,
                                migr_error_log(i).MODIFICATION_TIME,
                                migr_error_log(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_error_log -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_error_log(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || error_log.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_error_log;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_error_log;

    /**- TB_MESSAGE_ACKNW data migration --*/
    PROCEDURE migrate_message_acknw IS
        v_tab      VARCHAR2(30) := 'TB_MESSAGE_ACKNW';
        v_tab_new  VARCHAR2(30) := 'MIGR_TB_MESSAGE_ACKNW';

        v_id_pk NUMBER;

        CURSOR c_message_acknw IS
            SELECT MA.ID_PK,
                   MA.FROM_VALUE,
                   MA.TO_VALUE,
                   MA.ACKNOWLEDGE_DATE,
                   MA.CREATION_TIME,
                   MA.CREATED_BY,
                   MA.MODIFICATION_TIME,
                   MA.MODIFIED_BY,
                   (SELECT MPKSUM.NEW_ID
                    FROM MIGR_TB_PKS_USER_MESSAGE MPKSUM
                    WHERE MPKSUM.OLD_ID = UM.ID_PK) AS USER_MESSAGE_ID_FK
            FROM
                TB_MESSAGE_ACKNW MA,
                TB_MESSAGE_INFO MI,
                TB_USER_MESSAGE UM
            WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK
              AND MI.MESSAGE_ID = MA.MESSAGE_ID;

        TYPE T_MESSAGE_ACKNW IS TABLE OF c_message_acknw%ROWTYPE;
        message_acknw T_MESSAGE_ACKNW;

        TYPE T_MIGR_MESSAGE_ACKNW IS TABLE OF MIGR_TB_MESSAGE_ACKNW%ROWTYPE INDEX BY PLS_INTEGER;
        migr_message_acknw T_MIGR_MESSAGE_ACKNW;

        TYPE T_MIGR_PKS_MESSAGE_ACKNW IS TABLE OF MIGR_TB_PKS_MESSAGE_ACKNW%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_message_acknw T_MIGR_PKS_MESSAGE_ACKNW;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_message_acknw;
        LOOP
            FETCH c_message_acknw BULK COLLECT INTO message_acknw LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_acknw.COUNT = 0;

            migr_message_acknw := T_MIGR_MESSAGE_ACKNW();
            migr_pks_message_acknw := T_MIGR_PKS_MESSAGE_ACKNW();

            FOR i IN message_acknw.FIRST .. message_acknw.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(message_acknw(i).ID_PK, message_acknw(i).CREATION_TIME);

                    migr_pks_message_acknw(i).OLD_ID := message_acknw(i).ID_PK;
                    migr_pks_message_acknw(i).NEW_ID := v_id_pk;

                    migr_message_acknw(i).ID_PK := v_id_pk;
                    migr_message_acknw(i).FROM_VALUE := message_acknw(i).FROM_VALUE;
                    migr_message_acknw(i).TO_VALUE := message_acknw(i).TO_VALUE;
                    migr_message_acknw(i).ACKNOWLEDGE_DATE := message_acknw(i).ACKNOWLEDGE_DATE;
                    migr_message_acknw(i).USER_MESSAGE_ID_FK := message_acknw(i).USER_MESSAGE_ID_FK;
                    migr_message_acknw(i).CREATION_TIME := message_acknw(i).CREATION_TIME;
                    migr_message_acknw(i).CREATED_BY := message_acknw(i).CREATED_BY;
                    migr_message_acknw(i).MODIFICATION_TIME := message_acknw(i).MODIFICATION_TIME;
                    migr_message_acknw(i).MODIFIED_BY := message_acknw(i).MODIFIED_BY;

                    IF migr_message_acknw(i).USER_MESSAGE_ID_FK IS NULL THEN
                        log_verbose('Encountered NULL value for mandatory user message FK value: setting its value to the dummy user message ID_PK 19700101');
                        migr_message_acknw(i).USER_MESSAGE_ID_FK := DUMMY_USER_MESSAGE_ID_PK;
                    END IF;
                END LOOP;

            v_start := 1;
            v_last := migr_message_acknw.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_message_acknw -> update message acknowledge lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_MESSAGE_ACKNW (OLD_ID, NEW_ID)
                    VALUES (migr_pks_message_acknw(i).OLD_ID,
                            migr_pks_message_acknw(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_message_acknw -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_MESSAGE_ACKNW (ID_PK, FROM_VALUE, TO_VALUE, ACKNOWLEDGE_DATE,
                                                           USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY,
                                                           MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_message_acknw(i).ID_PK,
                                migr_message_acknw(i).FROM_VALUE,
                                migr_message_acknw(i).TO_VALUE,
                                migr_message_acknw(i).ACKNOWLEDGE_DATE,
                                migr_message_acknw(i).USER_MESSAGE_ID_FK,
                                migr_message_acknw(i).CREATION_TIME,
                                migr_message_acknw(i).CREATED_BY,
                                migr_message_acknw(i).MODIFICATION_TIME,
                                migr_message_acknw(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_message_acknw -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_message_acknw(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || message_acknw.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_message_acknw;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_message_acknw;

    /**- TB_SEND_ATTEMPT data migration --*/
    PROCEDURE migrate_send_attempt IS
        v_tab        VARCHAR2(30) := 'TB_SEND_ATTEMPT';
        v_tab_new    VARCHAR2(30) := 'MIGR_TB_SEND_ATTEMPT';

        v_id_pk NUMBER;

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
                   (SELECT MPKSUM.NEW_ID
                    FROM MIGR_TB_PKS_USER_MESSAGE MPKSUM
                    WHERE MPKSUM.OLD_ID = UM.ID_PK) AS USER_MESSAGE_ID_FK
            FROM TB_SEND_ATTEMPT SA,
                 TB_MESSAGE_INFO MI,
                 TB_USER_MESSAGE UM
            WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK
              AND MI.MESSAGE_ID = SA.MESSAGE_ID;

        TYPE T_SEND_ATTEMPT IS TABLE OF c_send_attempt%ROWTYPE;
        send_attempt T_SEND_ATTEMPT;

        TYPE T_MIGR_SEND_ATTEMPT IS TABLE OF MIGR_TB_SEND_ATTEMPT%ROWTYPE INDEX BY PLS_INTEGER;
        migr_send_attempt T_MIGR_SEND_ATTEMPT;

        TYPE T_MIGR_PKS_SEND_ATTEMPT IS TABLE OF MIGR_TB_PKS_SEND_ATTEMPT%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_send_attempt T_MIGR_PKS_SEND_ATTEMPT;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_send_attempt;
        LOOP
            FETCH c_send_attempt BULK COLLECT INTO send_attempt LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN send_attempt.COUNT = 0;

            migr_send_attempt := T_MIGR_SEND_ATTEMPT();
            migr_pks_send_attempt := T_MIGR_PKS_SEND_ATTEMPT();

            FOR i IN send_attempt.FIRST .. send_attempt.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(send_attempt(i).ID_PK, send_attempt(i).CREATION_TIME);

                    migr_pks_send_attempt(i).OLD_ID := send_attempt(i).ID_PK;
                    migr_pks_send_attempt(i).NEW_ID := v_id_pk;

                    migr_send_attempt(i).ID_PK := v_id_pk;
                    migr_send_attempt(i).START_DATE := send_attempt(i).START_DATE;
                    migr_send_attempt(i).END_DATE := send_attempt(i).END_DATE;
                    migr_send_attempt(i).STATUS := send_attempt(i).STATUS;
                    migr_send_attempt(i).ERROR := send_attempt(i).ERROR;
                    migr_send_attempt(i).USER_MESSAGE_ID_FK := send_attempt(i).USER_MESSAGE_ID_FK;
                    migr_send_attempt(i).CREATION_TIME := send_attempt(i).CREATION_TIME;
                    migr_send_attempt(i).CREATED_BY := send_attempt(i).CREATED_BY;
                    migr_send_attempt(i).MODIFICATION_TIME := send_attempt(i).MODIFICATION_TIME;
                    migr_send_attempt(i).MODIFIED_BY := send_attempt(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_send_attempt.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_send_attempt -> update send attempt lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_SEND_ATTEMPT (OLD_ID, NEW_ID)
                    VALUES (migr_pks_send_attempt(i).OLD_ID,
                            migr_pks_send_attempt(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_send_attempt -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_SEND_ATTEMPT (ID_PK, START_DATE, END_DATE, STATUS, ERROR, USER_MESSAGE_ID_FK,
                                                          CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_send_attempt(i).ID_PK,
                                migr_send_attempt(i).START_DATE,
                                migr_send_attempt(i).END_DATE,
                                migr_send_attempt(i).STATUS,
                                migr_send_attempt(i).ERROR,
                                migr_send_attempt(i).USER_MESSAGE_ID_FK,
                                migr_send_attempt(i).CREATION_TIME,
                                migr_send_attempt(i).CREATED_BY,
                                migr_send_attempt(i).MODIFICATION_TIME,
                                migr_send_attempt(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_send_attempt -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_send_attempt(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || send_attempt.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_send_attempt;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_send_attempt;

    PROCEDURE migrate_action_audit IS
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

        TYPE T_MIGR_ACTION_AUDIT IS TABLE OF MIGR_TB_ACTION_AUDIT%ROWTYPE INDEX BY PLS_INTEGER;
        migr_action_audit T_MIGR_ACTION_AUDIT;

        TYPE T_MIGR_PKS_ACTION_AUDIT IS TABLE OF MIGR_TB_PKS_ACTION_AUDIT%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_action_audit T_MIGR_PKS_ACTION_AUDIT;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_action_audit;
        LOOP
            FETCH c_action_audit BULK COLLECT INTO action_audit LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN action_audit.COUNT = 0;

            migr_action_audit := T_MIGR_ACTION_AUDIT();
            migr_pks_action_audit := T_MIGR_PKS_ACTION_AUDIT();

            FOR i IN action_audit.FIRST .. action_audit.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(action_audit(i).ID_PK, action_audit(i).CREATION_TIME);

                    migr_pks_action_audit(i).OLD_ID := action_audit(i).ID_PK;
                    migr_pks_action_audit(i).NEW_ID := v_id_pk;

                    migr_action_audit(i).ID_PK := v_id_pk;
                    migr_action_audit(i).AUDIT_TYPE := action_audit(i).AUDIT_TYPE;
                    migr_action_audit(i).ENTITY_ID := action_audit(i).ENTITY_ID;
                    migr_action_audit(i).MODIFICATION_TYPE := action_audit(i).MODIFICATION_TYPE;
                    migr_action_audit(i).REVISION_DATE := action_audit(i).REVISION_DATE;
                    migr_action_audit(i).USER_NAME := action_audit(i).USER_NAME;
                    migr_action_audit(i).FROM_QUEUE := action_audit(i).FROM_QUEUE;
                    migr_action_audit(i).TO_QUEUE := action_audit(i).TO_QUEUE;
                    migr_action_audit(i).CREATION_TIME := action_audit(i).CREATION_TIME;
                    migr_action_audit(i).CREATED_BY := action_audit(i).CREATED_BY;
                    migr_action_audit(i).MODIFICATION_TIME := action_audit(i).MODIFICATION_TIME;
                    migr_action_audit(i).MODIFIED_BY := action_audit(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_action_audit.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_action_audit -> update action audit lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_ACTION_AUDIT (OLD_ID, NEW_ID)
                    VALUES (migr_pks_action_audit(i).OLD_ID,
                            migr_pks_action_audit(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_action_audit -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_ACTION_AUDIT (ID_PK, AUDIT_TYPE, ENTITY_ID, MODIFICATION_TYPE,
                                                          REVISION_DATE, USER_NAME, FROM_QUEUE, TO_QUEUE, CREATION_TIME,
                                                          CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_action_audit(i).ID_PK,
                                migr_action_audit(i).AUDIT_TYPE,
                                migr_action_audit(i).ENTITY_ID,
                                migr_action_audit(i).MODIFICATION_TYPE,
                                migr_action_audit(i).REVISION_DATE,
                                migr_action_audit(i).USER_NAME,
                                migr_action_audit(i).FROM_QUEUE,
                                migr_action_audit(i).TO_QUEUE,
                                migr_action_audit(i).CREATION_TIME,
                                migr_action_audit(i).CREATED_BY,
                                migr_action_audit(i).MODIFICATION_TIME,
                                migr_action_audit(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_action_audit -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_action_audit(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || action_audit.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_action_audit;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_action_audit;

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

        v_fk_timezone_offset := lookup_migration_pk_tz_offset();

        OPEN c_alert;
        LOOP
            FETCH c_alert BULK COLLECT INTO alert LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN alert.COUNT = 0;

            migr_alert := T_MIGR_ALERT();
            migr_pks_alert := T_MIGR_PKS_ALERT();

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

    PROCEDURE migrate_authentication_entry IS
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

        TYPE T_MIGR_AUTHENTICATION_ENTRY IS TABLE OF MIGR_TB_AUTHENTICATION_ENTRY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_authentication_entry T_MIGR_AUTHENTICATION_ENTRY;

        TYPE T_MIGR_PKS_AUTH_ENTRY IS TABLE OF MIGR_TB_PKS_AUTH_ENTRY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_auth_entry T_MIGR_PKS_AUTH_ENTRY;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_authentication_entry;
        LOOP
            FETCH c_authentication_entry BULK COLLECT INTO authentication_entry LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN authentication_entry.COUNT = 0;

            migr_authentication_entry := T_MIGR_AUTHENTICATION_ENTRY();
            migr_pks_auth_entry := T_MIGR_PKS_AUTH_ENTRY();

            FOR i IN authentication_entry.FIRST .. authentication_entry.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(authentication_entry(i).ID_PK, authentication_entry(i).CREATION_TIME);

                    migr_pks_auth_entry(i).OLD_ID := authentication_entry(i).ID_PK;
                    migr_pks_auth_entry(i).NEW_ID := v_id_pk;

                    migr_authentication_entry(i).ID_PK := v_id_pk;
                    migr_authentication_entry(i).CERTIFICATE_ID := authentication_entry(i).CERTIFICATE_ID;
                    migr_authentication_entry(i).USERNAME := authentication_entry(i).USERNAME;
                    migr_authentication_entry(i).PASSWD := authentication_entry(i).PASSWD;
                    migr_authentication_entry(i).AUTH_ROLES := authentication_entry(i).AUTH_ROLES;
                    migr_authentication_entry(i).ORIGINAL_USER := authentication_entry(i).ORIGINAL_USER;
                    migr_authentication_entry(i).BACKEND := authentication_entry(i).BACKEND;
                    migr_authentication_entry(i).PASSWORD_CHANGE_DATE := authentication_entry(i).PASSWORD_CHANGE_DATE;
                    migr_authentication_entry(i).DEFAULT_PASSWORD := authentication_entry(i).DEFAULT_PASSWORD;
                    migr_authentication_entry(i).ATTEMPT_COUNT := authentication_entry(i).ATTEMPT_COUNT;
                    migr_authentication_entry(i).SUSPENSION_DATE := authentication_entry(i).SUSPENSION_DATE;
                    migr_authentication_entry(i).USER_ENABLED := authentication_entry(i).USER_ENABLED;
                    migr_authentication_entry(i).CREATION_TIME := authentication_entry(i).CREATION_TIME;
                    migr_authentication_entry(i).CREATED_BY := authentication_entry(i).CREATED_BY;
                    migr_authentication_entry(i).MODIFICATION_TIME := authentication_entry(i).MODIFICATION_TIME;
                    migr_authentication_entry(i).MODIFIED_BY := authentication_entry(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_authentication_entry.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_authentication_entry -> update authentication entry lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_AUTH_ENTRY (OLD_ID, NEW_ID)
                    VALUES (migr_pks_auth_entry(i).OLD_ID,
                            migr_pks_auth_entry(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_authentication_entry -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_AUTHENTICATION_ENTRY (ID_PK, CERTIFICATE_ID, USERNAME, PASSWD, AUTH_ROLES,
                                                                  ORIGINAL_USER, BACKEND, PASSWORD_CHANGE_DATE,
                                                                  DEFAULT_PASSWORD, ATTEMPT_COUNT, SUSPENSION_DATE,
                                                                  USER_ENABLED, CREATION_TIME, CREATED_BY,
                                                                  MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_authentication_entry(i).ID_PK,
                                migr_authentication_entry(i).CERTIFICATE_ID,
                                migr_authentication_entry(i).USERNAME,
                                migr_authentication_entry(i).PASSWD,
                                migr_authentication_entry(i).AUTH_ROLES,
                                migr_authentication_entry(i).ORIGINAL_USER,
                                migr_authentication_entry(i).BACKEND,
                                migr_authentication_entry(i).PASSWORD_CHANGE_DATE,
                                migr_authentication_entry(i).DEFAULT_PASSWORD,
                                migr_authentication_entry(i).ATTEMPT_COUNT,
                                migr_authentication_entry(i).SUSPENSION_DATE,
                                migr_authentication_entry(i).USER_ENABLED,
                                migr_authentication_entry(i).CREATION_TIME,
                                migr_authentication_entry(i).CREATED_BY,
                                migr_authentication_entry(i).MODIFICATION_TIME,
                                migr_authentication_entry(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_authentication_entry -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_authentication_entry(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || authentication_entry.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_authentication_entry;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;

    END migrate_authentication_entry;

    PROCEDURE migrate_plugin_user_passwd_history IS
        v_tab VARCHAR2(30) := 'TB_PLUGIN_USER_PASSWD_HISTORY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PLUGIN_USR_PASSWD_HIST';

        CURSOR c_plugin_user_passwd_history IS
            SELECT PUPH.ID_PK,
                   (SELECT MPKSAE.NEW_ID
                    FROM MIGR_TB_PKS_AUTH_ENTRY MPKSAE
                    WHERE MPKSAE.OLD_ID = PUPH.USER_ID) AS USER_ID,
                   PUPH.USER_PASSWORD,
                   PUPH.PASSWORD_CHANGE_DATE,
                   PUPH.CREATION_TIME,
                   PUPH.CREATED_BY,
                   PUPH.MODIFICATION_TIME,
                   PUPH.MODIFIED_BY
            FROM TB_PLUGIN_USER_PASSWD_HISTORY PUPH;

        TYPE T_PLUGIN_USER_PASSWD_HISTORY IS TABLE OF c_plugin_user_passwd_history%rowtype;
        plugin_user_passwd_history T_PLUGIN_USER_PASSWD_HISTORY;

        TYPE T_MIGR_PLUGIN_USR_PASSWD_HIST IS TABLE OF MIGR_TB_PLUGIN_USR_PASSWD_HIST%ROWTYPE INDEX BY PLS_INTEGER;
        migr_plugin_usr_passwd_hist T_MIGR_PLUGIN_USR_PASSWD_HIST;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_plugin_user_passwd_history;
        LOOP
            FETCH c_plugin_user_passwd_history BULK COLLECT INTO plugin_user_passwd_history LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN plugin_user_passwd_history.COUNT = 0;

            migr_plugin_usr_passwd_hist := T_MIGR_PLUGIN_USR_PASSWD_HIST();

            FOR i IN plugin_user_passwd_history.FIRST .. plugin_user_passwd_history.LAST
                LOOP
                    migr_plugin_usr_passwd_hist(i).ID_PK := generate_scalable_seq(plugin_user_passwd_history(i).ID_PK, plugin_user_passwd_history(i).CREATION_TIME);
                    migr_plugin_usr_passwd_hist(i).USER_ID := plugin_user_passwd_history(i).USER_ID;
                    migr_plugin_usr_passwd_hist(i).USER_PASSWORD := plugin_user_passwd_history(i).USER_PASSWORD;
                    migr_plugin_usr_passwd_hist(i).PASSWORD_CHANGE_DATE := plugin_user_passwd_history(i).PASSWORD_CHANGE_DATE;
                    migr_plugin_usr_passwd_hist(i).CREATION_TIME := plugin_user_passwd_history(i).CREATION_TIME;
                    migr_plugin_usr_passwd_hist(i).CREATED_BY := plugin_user_passwd_history(i).CREATED_BY;
                    migr_plugin_usr_passwd_hist(i).MODIFICATION_TIME := plugin_user_passwd_history(i).MODIFICATION_TIME;
                    migr_plugin_usr_passwd_hist(i).MODIFIED_BY := plugin_user_passwd_history(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_plugin_usr_passwd_hist.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_plugin_user_passwd_history -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PLUGIN_USR_PASSWD_HIST (ID_PK, USER_ID, USER_PASSWORD, PASSWORD_CHANGE_DATE,
                                                                    CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                                    MODIFIED_BY)
                        VALUES (migr_plugin_usr_passwd_hist(i).ID_PK,
                                migr_plugin_usr_passwd_hist(i).USER_ID,
                                migr_plugin_usr_passwd_hist(i).USER_PASSWORD,
                                migr_plugin_usr_passwd_hist(i).PASSWORD_CHANGE_DATE,
                                migr_plugin_usr_passwd_hist(i).CREATION_TIME,
                                migr_plugin_usr_passwd_hist(i).CREATED_BY,
                                migr_plugin_usr_passwd_hist(i).MODIFICATION_TIME,
                                migr_plugin_usr_passwd_hist(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_plugin_user_passwd_history -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_plugin_usr_passwd_hist(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || plugin_user_passwd_history.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_plugin_user_passwd_history;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_plugin_user_passwd_history;

    PROCEDURE migrate_backend_filter IS
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

        TYPE T_MIGR_BACKEND_FILTER IS TABLE OF MIGR_TB_BACKEND_FILTER%ROWTYPE INDEX BY PLS_INTEGER;
        migr_backend_filter T_MIGR_BACKEND_FILTER;

        TYPE T_MIGR_PKS_BACKEND_FILTER IS TABLE OF MIGR_TB_PKS_BACKEND_FILTER%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_backend_filter T_MIGR_PKS_BACKEND_FILTER;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_backend_filter;
        LOOP
            FETCH c_backend_filter BULK COLLECT INTO backend_filter LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN backend_filter.COUNT = 0;

            migr_backend_filter := T_MIGR_BACKEND_FILTER();
            migr_pks_backend_filter := T_MIGR_PKS_BACKEND_FILTER();

            FOR i IN backend_filter.FIRST .. backend_filter.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(backend_filter(i).ID_PK, backend_filter(i).CREATION_TIME);

                    migr_pks_backend_filter(i).OLD_ID := backend_filter(i).ID_PK;
                    migr_pks_backend_filter(i).NEW_ID := v_id_pk;

                    migr_backend_filter(i).ID_PK := v_id_pk;
                    migr_backend_filter(i).BACKEND_NAME := backend_filter(i).BACKEND_NAME;
                    migr_backend_filter(i).PRIORITY := backend_filter(i).PRIORITY;
                    migr_backend_filter(i).CREATION_TIME := backend_filter(i).CREATION_TIME;
                    migr_backend_filter(i).CREATED_BY := backend_filter(i).CREATED_BY;
                    migr_backend_filter(i).MODIFICATION_TIME := backend_filter(i).MODIFICATION_TIME;
                    migr_backend_filter(i).MODIFIED_BY := backend_filter(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_backend_filter.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_backend_filter -> update backend filter lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_BACKEND_FILTER (OLD_ID, NEW_ID)
                    VALUES (migr_pks_backend_filter(i).OLD_ID,
                            migr_pks_backend_filter(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_backend_filter -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_BACKEND_FILTER (ID_PK, BACKEND_NAME, PRIORITY, CREATION_TIME, CREATED_BY,
                                                            MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_backend_filter(i).ID_PK,
                                migr_backend_filter(i).BACKEND_NAME,
                                migr_backend_filter(i).PRIORITY,
                                migr_backend_filter(i).CREATION_TIME,
                                migr_backend_filter(i).CREATED_BY,
                                migr_backend_filter(i).MODIFICATION_TIME,
                                migr_backend_filter(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_backend_filter -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_backend_filter(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || backend_filter.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_backend_filter;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_backend_filter;

    PROCEDURE migrate_routing_criteria IS
        v_tab VARCHAR2(30) := 'TB_ROUTING_CRITERIA';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_ROUTING_CRITERIA';

        v_id_pk NUMBER;

        CURSOR c_routing_criteria IS
            SELECT RC.ID_PK,
                   RC.EXPRESSION,
                   RC.NAME,
                   (SELECT MPKSBF.NEW_ID
                    FROM MIGR_TB_PKS_BACKEND_FILTER MPKSBF
                    WHERE MPKSBF.OLD_ID = RC.FK_BACKEND_FILTER) AS FK_BACKEND_FILTER,
                   RC.PRIORITY,
                   RC.CREATION_TIME,
                   RC.CREATED_BY,
                   RC.MODIFICATION_TIME,
                   RC.MODIFIED_BY
            FROM TB_ROUTING_CRITERIA RC;

        TYPE T_ROUTING_CRITERIA IS TABLE OF c_routing_criteria%rowtype;
        routing_criteria T_ROUTING_CRITERIA;

        TYPE T_MIGR_ROUTING_CRITERIA IS TABLE OF MIGR_TB_ROUTING_CRITERIA%ROWTYPE INDEX BY PLS_INTEGER;
        migr_routing_criteria T_MIGR_ROUTING_CRITERIA;

        TYPE T_MIGR_PKS_ROUTING_CRITERIA IS TABLE OF MIGR_TB_PKS_ROUTING_CRITERIA%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_routing_criteria T_MIGR_PKS_ROUTING_CRITERIA;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_routing_criteria;
        LOOP
            FETCH c_routing_criteria BULK COLLECT INTO routing_criteria LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN routing_criteria.COUNT = 0;

            migr_routing_criteria := T_MIGR_ROUTING_CRITERIA();
            migr_pks_routing_criteria := T_MIGR_PKS_ROUTING_CRITERIA();

            FOR i IN routing_criteria.FIRST .. routing_criteria.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(routing_criteria(i).ID_PK, routing_criteria(i).CREATION_TIME);

                    migr_pks_routing_criteria(i).OLD_ID := routing_criteria(i).ID_PK;
                    migr_pks_routing_criteria(i).NEW_ID := v_id_pk;

                    migr_routing_criteria(i).ID_PK := v_id_pk;
                    migr_routing_criteria(i).EXPRESSION := routing_criteria(i).EXPRESSION;
                    migr_routing_criteria(i).NAME := routing_criteria(i).NAME;
                    migr_routing_criteria(i).FK_BACKEND_FILTER := routing_criteria(i).FK_BACKEND_FILTER;
                    migr_routing_criteria(i).PRIORITY := routing_criteria(i).PRIORITY;
                    migr_routing_criteria(i).CREATION_TIME := routing_criteria(i).CREATION_TIME;
                    migr_routing_criteria(i).CREATED_BY := routing_criteria(i).CREATED_BY;
                    migr_routing_criteria(i).MODIFICATION_TIME := routing_criteria(i).MODIFICATION_TIME;
                    migr_routing_criteria(i).MODIFIED_BY := routing_criteria(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_routing_criteria.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_routing_criteria -> update routing criteria lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_ROUTING_CRITERIA (OLD_ID, NEW_ID)
                    VALUES (migr_pks_routing_criteria(i).OLD_ID,
                            migr_pks_routing_criteria(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_routing_criteria -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_ROUTING_CRITERIA (ID_PK, EXPRESSION, NAME, FK_BACKEND_FILTER, PRIORITY,
                                                              CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                              MODIFIED_BY)
                        VALUES (migr_routing_criteria(i).ID_PK,
                                migr_routing_criteria(i).EXPRESSION,
                                migr_routing_criteria(i).NAME,
                                migr_routing_criteria(i).FK_BACKEND_FILTER,
                                migr_routing_criteria(i).PRIORITY,
                                migr_routing_criteria(i).CREATION_TIME,
                                migr_routing_criteria(i).CREATED_BY,
                                migr_routing_criteria(i).MODIFICATION_TIME,
                                migr_routing_criteria(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_routing_criteria -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_routing_criteria(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || routing_criteria.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_routing_criteria;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_routing_criteria;

    PROCEDURE migrate_certificate IS
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

        TYPE T_MIGR_CERTIFICATE IS TABLE OF MIGR_TB_CERTIFICATE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_certificate T_MIGR_CERTIFICATE;

        TYPE T_MIGR_PKS_CERTIFICATE IS TABLE OF MIGR_TB_PKS_CERTIFICATE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_certificate T_MIGR_PKS_CERTIFICATE;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_certificate;
        LOOP
            FETCH c_certificate BULK COLLECT INTO certificate LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN certificate.COUNT = 0;

            migr_certificate := T_MIGR_CERTIFICATE();
            migr_pks_certificate := T_MIGR_PKS_CERTIFICATE();

            FOR i IN certificate.FIRST .. certificate.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(certificate(i).ID_PK, certificate(i).CREATION_TIME);

                    migr_pks_certificate(i).OLD_ID := certificate(i).ID_PK;
                    migr_pks_certificate(i).NEW_ID := v_id_pk;

                    migr_certificate(i).ID_PK := v_id_pk;
                    migr_certificate(i).CERTIFICATE_ALIAS := certificate(i).CERTIFICATE_ALIAS;
                    migr_certificate(i).NOT_VALID_BEFORE_DATE := certificate(i).NOT_VALID_BEFORE_DATE;
                    migr_certificate(i).NOT_VALID_AFTER_DATE := certificate(i).NOT_VALID_AFTER_DATE;
                    migr_certificate(i).REVOKE_NOTIFICATION_DATE := certificate(i).REVOKE_NOTIFICATION_DATE;
                    migr_certificate(i).ALERT_IMM_NOTIFICATION_DATE := certificate(i).ALERT_IMM_NOTIFICATION_DATE;
                    migr_certificate(i).ALERT_EXP_NOTIFICATION_DATE := certificate(i).ALERT_EXP_NOTIFICATION_DATE;
                    migr_certificate(i).CERTIFICATE_STATUS := certificate(i).CERTIFICATE_STATUS;
                    migr_certificate(i).CERTIFICATE_TYPE := certificate(i).CERTIFICATE_TYPE;
                    migr_certificate(i).CREATION_TIME := certificate(i).CREATION_TIME;
                    migr_certificate(i).CREATED_BY := certificate(i).CREATED_BY;
                    migr_certificate(i).MODIFICATION_TIME := certificate(i).MODIFICATION_TIME;
                    migr_certificate(i).MODIFIED_BY := certificate(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_certificate.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_certificate -> update certificate lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_CERTIFICATE (OLD_ID, NEW_ID)
                    VALUES (migr_pks_certificate(i).OLD_ID,
                            migr_pks_certificate(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_certificate -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_CERTIFICATE (ID_PK, CERTIFICATE_ALIAS, NOT_VALID_BEFORE_DATE,
                                                         NOT_VALID_AFTER_DATE, REVOKE_NOTIFICATION_DATE,
                                                         ALERT_IMM_NOTIFICATION_DATE, ALERT_EXP_NOTIFICATION_DATE,
                                                         CERTIFICATE_STATUS, CERTIFICATE_TYPE, CREATION_TIME,
                                                         CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_certificate(i).ID_PK,
                                migr_certificate(i).CERTIFICATE_ALIAS,
                                migr_certificate(i).NOT_VALID_BEFORE_DATE,
                                migr_certificate(i).NOT_VALID_AFTER_DATE,
                                migr_certificate(i).REVOKE_NOTIFICATION_DATE,
                                migr_certificate(i).ALERT_IMM_NOTIFICATION_DATE,
                                migr_certificate(i).ALERT_EXP_NOTIFICATION_DATE,
                                migr_certificate(i).CERTIFICATE_STATUS,
                                migr_certificate(i).CERTIFICATE_TYPE,
                                migr_certificate(i).CREATION_TIME,
                                migr_certificate(i).CREATED_BY,
                                migr_certificate(i).MODIFICATION_TIME,
                                migr_certificate(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_certificate -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_certificate(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || certificate.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_certificate;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_certificate;

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

    PROCEDURE migrate_encryption_key IS
        v_tab VARCHAR2(30) := 'TB_ENCRYPTION_KEY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_ENCRYPTION_KEY';

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

        TYPE T_MIGR_ENCRYPTION_KEY IS TABLE OF MIGR_TB_ENCRYPTION_KEY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_encryption_key T_MIGR_ENCRYPTION_KEY;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_encryption_key;
        LOOP
            FETCH c_encryption_key BULK COLLECT INTO encryption_key LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN encryption_key.COUNT = 0;

            migr_encryption_key := T_MIGR_ENCRYPTION_KEY();

            FOR i IN encryption_key.FIRST .. encryption_key.LAST
                LOOP
                    migr_encryption_key(i).ID_PK := generate_scalable_seq(encryption_key(i).ID_PK, encryption_key(i).CREATION_TIME);
                    migr_encryption_key(i).KEY_USAGE := encryption_key(i).KEY_USAGE;
                    migr_encryption_key(i).SECRET_KEY := encryption_key(i).SECRET_KEY;
                    migr_encryption_key(i).INIT_VECTOR := encryption_key(i).INIT_VECTOR;
                    migr_encryption_key(i).CREATION_TIME := encryption_key(i).CREATION_TIME;
                    migr_encryption_key(i).CREATED_BY := encryption_key(i).CREATED_BY;
                    migr_encryption_key(i).MODIFICATION_TIME := encryption_key(i).MODIFICATION_TIME;
                    migr_encryption_key(i).MODIFIED_BY := encryption_key(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_encryption_key.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_encryption_key -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_ENCRYPTION_KEY (ID_PK, KEY_USAGE, SECRET_KEY, INIT_VECTOR, CREATION_TIME,
                                                            CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_encryption_key(i).ID_PK,
                                migr_encryption_key(i).KEY_USAGE,
                                migr_encryption_key(i).SECRET_KEY,
                                migr_encryption_key(i).INIT_VECTOR,
                                migr_encryption_key(i).CREATION_TIME,
                                migr_encryption_key(i).CREATED_BY,
                                migr_encryption_key(i).MODIFICATION_TIME,
                                migr_encryption_key(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_encryption_key -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_encryption_key(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || migr_encryption_key.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_encryption_key;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_encryption_key;

    PROCEDURE migrate_message_acknw_prop IS
        v_tab VARCHAR2(30) := 'TB_MESSAGE_ACKNW_PROP';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_MESSAGE_ACKNW_PROP';

        CURSOR c_message_acknw_prop IS
            SELECT MAP.ID_PK,
                   MAP.PROPERTY_NAME,
                   MAP.PROPERTY_VALUE,
                   (SELECT MPKSMA.NEW_ID
                    FROM MIGR_TB_PKS_MESSAGE_ACKNW MPKSMA
                    WHERE MPKSMA.OLD_ID = MAP.FK_MSG_ACKNOWLEDGE) AS FK_MSG_ACKNOWLEDGE,
                   MAP.CREATION_TIME,
                   MAP.CREATED_BY,
                   MAP.MODIFICATION_TIME,
                   MAP.MODIFIED_BY
            FROM TB_MESSAGE_ACKNW_PROP MAP;

        TYPE T_MESSAGE_ACKNW_PROP IS TABLE OF c_message_acknw_prop%rowtype;
        message_acknw_prop T_MESSAGE_ACKNW_PROP;

        TYPE T_MIGR_MESSAGE_ACKNW_PROP IS TABLE OF MIGR_TB_MESSAGE_ACKNW_PROP%ROWTYPE INDEX BY PLS_INTEGER;
        migr_message_acknw_prop T_MIGR_MESSAGE_ACKNW_PROP;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_message_acknw_prop;
        LOOP
            FETCH c_message_acknw_prop BULK COLLECT INTO message_acknw_prop LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_acknw_prop.COUNT = 0;

            migr_message_acknw_prop := T_MIGR_MESSAGE_ACKNW_PROP();

            FOR i IN message_acknw_prop.FIRST .. message_acknw_prop.LAST
                LOOP
                    migr_message_acknw_prop(i).ID_PK := generate_scalable_seq(message_acknw_prop(i).ID_PK, message_acknw_prop(i).CREATION_TIME);
                    migr_message_acknw_prop(i).PROPERTY_NAME := message_acknw_prop(i).PROPERTY_NAME;
                    migr_message_acknw_prop(i).PROPERTY_VALUE := message_acknw_prop(i).PROPERTY_VALUE;
                    migr_message_acknw_prop(i).FK_MSG_ACKNOWLEDGE := message_acknw_prop(i).FK_MSG_ACKNOWLEDGE;
                    migr_message_acknw_prop(i).CREATION_TIME := message_acknw_prop(i).CREATION_TIME;
                    migr_message_acknw_prop(i).CREATED_BY := message_acknw_prop(i).CREATED_BY;
                    migr_message_acknw_prop(i).MODIFICATION_TIME := message_acknw_prop(i).MODIFICATION_TIME;
                    migr_message_acknw_prop(i).MODIFIED_BY := message_acknw_prop(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_message_acknw_prop.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_message_acknw_prop -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_MESSAGE_ACKNW_PROP (ID_PK, PROPERTY_NAME, PROPERTY_VALUE,
                                                                FK_MSG_ACKNOWLEDGE, CREATION_TIME, CREATED_BY,
                                                                MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_message_acknw_prop(i).ID_PK,
                                migr_message_acknw_prop(i).PROPERTY_NAME,
                                migr_message_acknw_prop(i).PROPERTY_VALUE,
                                migr_message_acknw_prop(i).FK_MSG_ACKNOWLEDGE,
                                migr_message_acknw_prop(i).CREATION_TIME,
                                migr_message_acknw_prop(i).CREATED_BY,
                                migr_message_acknw_prop(i).MODIFICATION_TIME,
                                migr_message_acknw_prop(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_message_acknw_prop -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_message_acknw_prop(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || message_acknw_prop.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_message_acknw_prop;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_message_acknw_prop;

    PROCEDURE migrate_messaging_lock IS
        v_tab VARCHAR2(30) := 'TB_MESSAGING_LOCK';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_MESSAGING_LOCK';

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

        TYPE T_MIGR_MESSAGING_LOCK IS TABLE OF MIGR_TB_MESSAGING_LOCK%ROWTYPE INDEX BY PLS_INTEGER;
        migr_messaging_lock T_MIGR_MESSAGING_LOCK;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');

        v_fk_timezone_offset := lookup_migration_pk_tz_offset();

        OPEN c_messaging_lock;
        LOOP
            FETCH c_messaging_lock BULK COLLECT INTO messaging_lock LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN messaging_lock.COUNT = 0;

            migr_messaging_lock := T_MIGR_MESSAGING_LOCK();

            FOR i IN messaging_lock.FIRST .. messaging_lock.LAST
                LOOP
                    migr_messaging_lock(i).ID_PK := generate_scalable_seq(messaging_lock(i).ID_PK, messaging_lock(i).CREATION_TIME);
                    migr_messaging_lock(i).MESSAGE_TYPE := messaging_lock(i).MESSAGE_TYPE;
                    migr_messaging_lock(i).MESSAGE_RECEIVED := messaging_lock(i).MESSAGE_RECEIVED;
                    migr_messaging_lock(i).MESSAGE_STATE := messaging_lock(i).MESSAGE_STATE;
                    migr_messaging_lock(i).MESSAGE_ID := messaging_lock(i).MESSAGE_ID;
                    migr_messaging_lock(i).INITIATOR := messaging_lock(i).INITIATOR;
                    migr_messaging_lock(i).MPC := messaging_lock(i).MPC;
                    migr_messaging_lock(i).SEND_ATTEMPTS := messaging_lock(i).SEND_ATTEMPTS;
                    migr_messaging_lock(i).SEND_ATTEMPTS_MAX := messaging_lock(i).SEND_ATTEMPTS_MAX;
                    migr_messaging_lock(i).NEXT_ATTEMPT := messaging_lock(i).NEXT_ATTEMPT;
                    migr_messaging_lock(i).FK_TIMEZONE_OFFSET := v_fk_timezone_offset;
                    migr_messaging_lock(i).MESSAGE_STALED := messaging_lock(i).MESSAGE_STALED;
                    migr_messaging_lock(i).CREATION_TIME := messaging_lock(i).CREATION_TIME;
                    migr_messaging_lock(i).CREATED_BY := messaging_lock(i).CREATED_BY;
                    migr_messaging_lock(i).MODIFICATION_TIME := messaging_lock(i).MODIFICATION_TIME;
                    migr_messaging_lock(i).MODIFIED_BY := messaging_lock(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_messaging_lock.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_messaging_lock -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_MESSAGING_LOCK (ID_PK, MESSAGE_TYPE, MESSAGE_RECEIVED, MESSAGE_STATE,
                                                            MESSAGE_ID, INITIATOR, MPC, SEND_ATTEMPTS,
                                                            SEND_ATTEMPTS_MAX, NEXT_ATTEMPT, FK_TIMEZONE_OFFSET,
                                                            MESSAGE_STALED, CREATION_TIME, CREATED_BY,
                                                            MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_messaging_lock(i).ID_PK,
                                migr_messaging_lock(i).MESSAGE_TYPE,
                                migr_messaging_lock(i).MESSAGE_RECEIVED,
                                migr_messaging_lock(i).MESSAGE_STATE,
                                migr_messaging_lock(i).MESSAGE_ID,
                                migr_messaging_lock(i).INITIATOR,
                                migr_messaging_lock(i).MPC,
                                migr_messaging_lock(i).SEND_ATTEMPTS,
                                migr_messaging_lock(i).SEND_ATTEMPTS_MAX,
                                migr_messaging_lock(i).NEXT_ATTEMPT,
                                migr_messaging_lock(i).FK_TIMEZONE_OFFSET,
                                migr_messaging_lock(i).MESSAGE_STALED,
                                migr_messaging_lock(i).CREATION_TIME,
                                migr_messaging_lock(i).CREATED_BY,
                                migr_messaging_lock(i).MODIFICATION_TIME,
                                migr_messaging_lock(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_messaging_lock -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_messaging_lock(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || messaging_lock.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_messaging_lock;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_messaging_lock;

    PROCEDURE migrate_pm_business_process IS
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

        TYPE T_MIGR_PM_BUSINESS_PROCESS IS TABLE OF MIGR_TB_PM_BUSINESS_PROCESS%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_business_process T_MIGR_PM_BUSINESS_PROCESS;

        TYPE T_MIGR_PKS_PM_BUSINESS_PROC IS TABLE OF MIGR_TB_PKS_PM_BUSINESS_PROC%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_business_proc T_MIGR_PKS_PM_BUSINESS_PROC;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_business_process;
        LOOP
            FETCH c_pm_business_process BULK COLLECT INTO pm_business_process LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_business_process.COUNT = 0;

            migr_pm_business_process := T_MIGR_PM_BUSINESS_PROCESS();
            migr_pks_pm_business_proc := T_MIGR_PKS_PM_BUSINESS_PROC();

            FOR i IN pm_business_process.FIRST .. pm_business_process.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_business_process(i).ID_PK, pm_business_process(i).CREATION_TIME);

                    migr_pks_pm_business_proc(i).OLD_ID := pm_business_process(i).ID_PK;
                    migr_pks_pm_business_proc(i).NEW_ID := v_id_pk;

                    migr_pm_business_process(i).ID_PK := v_id_pk;
                    migr_pm_business_process(i).CREATION_TIME := pm_business_process(i).CREATION_TIME;
                    migr_pm_business_process(i).CREATED_BY := pm_business_process(i).CREATED_BY;
                    migr_pm_business_process(i).MODIFICATION_TIME := pm_business_process(i).MODIFICATION_TIME;
                    migr_pm_business_process(i).MODIFIED_BY := pm_business_process(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_business_process.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_business_process -> update PMode business process lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_BUSINESS_PROC (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_business_proc(i).OLD_ID,
                            migr_pks_pm_business_proc(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_business_process -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_BUSINESS_PROCESS (ID_PK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                                 MODIFIED_BY)
                        VALUES (migr_pm_business_process(i).ID_PK,
                                migr_pm_business_process(i).CREATION_TIME,
                                migr_pm_business_process(i).CREATED_BY,
                                migr_pm_business_process(i).MODIFICATION_TIME,
                                migr_pm_business_process(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_business_process -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_business_process(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_business_process.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_business_process;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_business_process;

    PROCEDURE migrate_pm_action IS
        v_tab VARCHAR2(30) := 'TB_PM_ACTION';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_ACTION';

        v_id_pk NUMBER;

        CURSOR c_pm_action IS
            SELECT PA.ID_PK,
                   PA.NAME,
                   PA.VALUE,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PA.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PA.CREATION_TIME,
                   PA.CREATED_BY,
                   PA.MODIFICATION_TIME,
                   PA.MODIFIED_BY
            FROM TB_PM_ACTION PA;

        TYPE T_PM_ACTION IS TABLE OF c_pm_action%rowtype;
        pm_action T_PM_ACTION;

        TYPE T_MIGR_PM_ACTION IS TABLE OF MIGR_TB_PM_ACTION%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_action T_MIGR_PM_ACTION;

        TYPE T_MIGR_PKS_PM_ACTION IS TABLE OF MIGR_TB_PKS_PM_ACTION%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_action T_MIGR_PKS_PM_ACTION;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_action;
        LOOP
            FETCH c_pm_action BULK COLLECT INTO pm_action LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_action.COUNT = 0;

            migr_pm_action := T_MIGR_PM_ACTION();
            migr_pks_pm_action := T_MIGR_PKS_PM_ACTION();

            FOR i IN pm_action.FIRST .. pm_action.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_action(i).ID_PK, pm_action(i).CREATION_TIME);

                    migr_pks_pm_action(i).OLD_ID := pm_action(i).ID_PK;
                    migr_pks_pm_action(i).NEW_ID := v_id_pk;

                    migr_pm_action(i).ID_PK := v_id_pk;
                    migr_pm_action(i).NAME := pm_action(i).NAME;
                    migr_pm_action(i).VALUE := pm_action(i).VALUE;
                    migr_pm_action(i).FK_BUSINESSPROCESS := pm_action(i).FK_BUSINESSPROCESS;
                    migr_pm_action(i).CREATION_TIME := pm_action(i).CREATION_TIME;
                    migr_pm_action(i).CREATED_BY := pm_action(i).CREATED_BY;
                    migr_pm_action(i).MODIFICATION_TIME := pm_action(i).MODIFICATION_TIME;
                    migr_pm_action(i).MODIFIED_BY := pm_action(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_action.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_action -> update PMode action lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_ACTION (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_action(i).OLD_ID,
                            migr_pks_pm_action(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_action -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_ACTION (ID_PK, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME,
                                                       CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_action(i).ID_PK,
                                migr_pm_action(i).NAME,
                                migr_pm_action(i).VALUE,
                                migr_pm_action(i).FK_BUSINESSPROCESS,
                                migr_pm_action(i).CREATION_TIME,
                                migr_pm_action(i).CREATED_BY,
                                migr_pm_action(i).MODIFICATION_TIME,
                                migr_pm_action(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_action -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_action(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_action.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_action;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_action;

    PROCEDURE migrate_pm_agreement IS
        v_tab VARCHAR2(30) := 'TB_PM_AGREEMENT';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_AGREEMENT';

        v_id_pk NUMBER;

        CURSOR c_pm_agreement IS
            SELECT PA.ID_PK,
                   PA.NAME,
                   PA.TYPE,
                   PA.VALUE,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PA.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PA.CREATION_TIME,
                   PA.CREATED_BY,
                   PA.MODIFICATION_TIME,
                   PA.MODIFIED_BY
            FROM TB_PM_AGREEMENT PA;

        TYPE T_PM_AGREEMENT IS TABLE OF c_pm_agreement%rowtype;
        pm_agreement T_PM_AGREEMENT;

        TYPE T_MIGR_PM_AGREEMENT IS TABLE OF MIGR_TB_PM_AGREEMENT%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_agreement T_MIGR_PM_AGREEMENT;

        TYPE T_MIGR_PKS_PM_AGREEMENT IS TABLE OF MIGR_TB_PKS_PM_AGREEMENT%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_agreement T_MIGR_PKS_PM_AGREEMENT;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_agreement;
        LOOP
            FETCH c_pm_agreement BULK COLLECT INTO pm_agreement LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_agreement.COUNT = 0;

            migr_pm_agreement := T_MIGR_PM_AGREEMENT();
            migr_pks_pm_agreement := T_MIGR_PKS_PM_AGREEMENT();

            FOR i IN pm_agreement.FIRST .. pm_agreement.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_agreement(i).ID_PK, pm_agreement(i).CREATION_TIME);

                    migr_pks_pm_agreement(i).OLD_ID := pm_agreement(i).ID_PK;
                    migr_pks_pm_agreement(i).NEW_ID := v_id_pk;

                    migr_pm_agreement(i).ID_PK := v_id_pk;
                    migr_pm_agreement(i).NAME := pm_agreement(i).NAME;
                    migr_pm_agreement(i).TYPE := pm_agreement(i).TYPE;
                    migr_pm_agreement(i).VALUE := pm_agreement(i).VALUE;
                    migr_pm_agreement(i).FK_BUSINESSPROCESS := pm_agreement(i).FK_BUSINESSPROCESS;
                    migr_pm_agreement(i).CREATION_TIME := pm_agreement(i).CREATION_TIME;
                    migr_pm_agreement(i).CREATED_BY := pm_agreement(i).CREATED_BY;
                    migr_pm_agreement(i).MODIFICATION_TIME := pm_agreement(i).MODIFICATION_TIME;
                    migr_pm_agreement(i).MODIFIED_BY := pm_agreement(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_agreement.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_business_process -> update PMode agreement lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_AGREEMENT (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_agreement(i).OLD_ID,
                            migr_pks_pm_agreement(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_agreement -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_AGREEMENT (ID_PK, NAME, TYPE, VALUE, FK_BUSINESSPROCESS, CREATION_TIME,
                                                          CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_agreement(i).ID_PK,
                                migr_pm_agreement(i).NAME,
                                migr_pm_agreement(i).TYPE,
                                migr_pm_agreement(i).VALUE,
                                migr_pm_agreement(i).FK_BUSINESSPROCESS,
                                migr_pm_agreement(i).CREATION_TIME,
                                migr_pm_agreement(i).CREATED_BY,
                                migr_pm_agreement(i).MODIFICATION_TIME,
                                migr_pm_agreement(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_agreement -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_agreement(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_agreement.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_agreement;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_agreement;

    PROCEDURE migrate_pm_error_handling IS
        v_tab VARCHAR2(30) := 'TB_PM_ERROR_HANDLING';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_ERROR_HANDLING';

        v_id_pk NUMBER;

        CURSOR c_pm_error_handling IS
            SELECT PEH.ID_PK,
                   PEH.BUSINESS_ERROR_NOTIFY_CONSUMER,
                   PEH.BUSINESS_ERROR_NOTIFY_PRODUCER,
                   PEH.DELIVERY_FAIL_NOTIFY_PRODUCER,
                   PEH.ERROR_AS_RESPONSE,
                   PEH.NAME,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PEH.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PEH.CREATION_TIME,
                   PEH.CREATED_BY,
                   PEH.MODIFICATION_TIME,
                   PEH.MODIFIED_BY
            FROM TB_PM_ERROR_HANDLING PEH;

        TYPE T_PM_ERROR_HANDLING IS TABLE OF c_pm_error_handling%rowtype;
        pm_error_handling T_PM_ERROR_HANDLING;

        TYPE T_MIGR_PM_ERROR_HANDLING IS TABLE OF MIGR_TB_PM_ERROR_HANDLING%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_error_handling T_MIGR_PM_ERROR_HANDLING;

        TYPE T_MIGR_PKS_PM_ERROR_HANDLING IS TABLE OF MIGR_TB_PKS_PM_ERROR_HANDLING%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_error_handling T_MIGR_PKS_PM_ERROR_HANDLING;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_error_handling;
        LOOP
            FETCH c_pm_error_handling BULK COLLECT INTO pm_error_handling LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_error_handling.COUNT = 0;

            migr_pm_error_handling := T_MIGR_PM_ERROR_HANDLING();
            migr_pks_pm_error_handling := T_MIGR_PKS_PM_ERROR_HANDLING();

            FOR i IN pm_error_handling.FIRST .. pm_error_handling.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_error_handling(i).ID_PK, pm_error_handling(i).CREATION_TIME);

                    migr_pks_pm_error_handling(i).OLD_ID := pm_error_handling(i).ID_PK;
                    migr_pks_pm_error_handling(i).NEW_ID := v_id_pk;

                    migr_pm_error_handling(i).ID_PK := v_id_pk;
                    migr_pm_error_handling(i).BUSINESS_ERROR_NOTIFY_CONSUMER := pm_error_handling(i).BUSINESS_ERROR_NOTIFY_CONSUMER;
                    migr_pm_error_handling(i).BUSINESS_ERROR_NOTIFY_PRODUCER := pm_error_handling(i).BUSINESS_ERROR_NOTIFY_PRODUCER;
                    migr_pm_error_handling(i).DELIVERY_FAIL_NOTIFY_PRODUCER := pm_error_handling(i).DELIVERY_FAIL_NOTIFY_PRODUCER;
                    migr_pm_error_handling(i).ERROR_AS_RESPONSE := pm_error_handling(i).ERROR_AS_RESPONSE;
                    migr_pm_error_handling(i).NAME := pm_error_handling(i).NAME;
                    migr_pm_error_handling(i).FK_BUSINESSPROCESS := pm_error_handling(i).FK_BUSINESSPROCESS;
                    migr_pm_error_handling(i).CREATION_TIME := pm_error_handling(i).CREATION_TIME;
                    migr_pm_error_handling(i).CREATED_BY := pm_error_handling(i).CREATED_BY;
                    migr_pm_error_handling(i).MODIFICATION_TIME := pm_error_handling(i).MODIFICATION_TIME;
                    migr_pm_error_handling(i).MODIFIED_BY := pm_error_handling(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_error_handling.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_error_handling -> update PMode error handling lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_ERROR_HANDLING (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_error_handling(i).OLD_ID,
                            migr_pks_pm_error_handling(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_error_handling -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_ERROR_HANDLING (ID_PK, BUSINESS_ERROR_NOTIFY_CONSUMER,
                                                               BUSINESS_ERROR_NOTIFY_PRODUCER,
                                                               DELIVERY_FAIL_NOTIFY_PRODUCER, ERROR_AS_RESPONSE, NAME,
                                                               FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY,
                                                               MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_error_handling(i).ID_PK,
                                migr_pm_error_handling(i).BUSINESS_ERROR_NOTIFY_CONSUMER,
                                migr_pm_error_handling(i).BUSINESS_ERROR_NOTIFY_PRODUCER,
                                migr_pm_error_handling(i).DELIVERY_FAIL_NOTIFY_PRODUCER,
                                migr_pm_error_handling(i).ERROR_AS_RESPONSE,
                                migr_pm_error_handling(i).NAME,
                                migr_pm_error_handling(i).FK_BUSINESSPROCESS,
                                migr_pm_error_handling(i).CREATION_TIME,
                                migr_pm_error_handling(i).CREATED_BY,
                                migr_pm_error_handling(i).MODIFICATION_TIME,
                                migr_pm_error_handling(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_error_handling -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_error_handling(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_error_handling.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_error_handling;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_error_handling;

    PROCEDURE migrate_pm_mep IS
        v_tab VARCHAR2(30) := 'TB_PM_MEP';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_MEP';

        v_id_pk NUMBER;

        CURSOR c_pm_mep IS
            SELECT PM.ID_PK,
                   PM.LEG_COUNT,
                   PM.NAME,
                   PM.VALUE,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PM.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PM.CREATION_TIME,
                   PM.CREATED_BY,
                   PM.MODIFICATION_TIME,
                   PM.MODIFIED_BY
            FROM TB_PM_MEP PM;

        TYPE T_PM_MEP IS TABLE OF c_pm_mep%rowtype;
        pm_mep T_PM_MEP;

        TYPE T_MIGR_PM_MEP IS TABLE OF MIGR_TB_PM_MEP%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_mep T_MIGR_PM_MEP;

        TYPE T_MIGR_PKS_PM_MEP IS TABLE OF MIGR_TB_PKS_PM_MEP%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_mep T_MIGR_PKS_PM_MEP;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_mep;
        LOOP
            FETCH c_pm_mep BULK COLLECT INTO pm_mep LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_mep.COUNT = 0;

            migr_pm_mep := T_MIGR_PM_MEP();
            migr_pks_pm_mep := T_MIGR_PKS_PM_MEP();

            FOR i IN pm_mep.FIRST .. pm_mep.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_mep(i).ID_PK, pm_mep(i).CREATION_TIME);

                    migr_pks_pm_mep(i).OLD_ID := pm_mep(i).ID_PK;
                    migr_pks_pm_mep(i).NEW_ID := v_id_pk;

                    migr_pm_mep(i).ID_PK := v_id_pk;
                    migr_pm_mep(i).LEG_COUNT := pm_mep(i).LEG_COUNT;
                    migr_pm_mep(i).NAME := pm_mep(i).NAME;
                    migr_pm_mep(i).VALUE := pm_mep(i).VALUE;
                    migr_pm_mep(i).FK_BUSINESSPROCESS := pm_mep(i).FK_BUSINESSPROCESS;
                    migr_pm_mep(i).CREATION_TIME := pm_mep(i).CREATION_TIME;
                    migr_pm_mep(i).CREATED_BY := pm_mep(i).CREATED_BY;
                    migr_pm_mep(i).MODIFICATION_TIME := pm_mep(i).MODIFICATION_TIME;
                    migr_pm_mep(i).MODIFIED_BY := pm_mep(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_mep.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_mep -> update PMode mep lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_MEP (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_mep(i).OLD_ID,
                            migr_pks_pm_mep(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_mep -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_MEP (ID_PK, LEG_COUNT, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME,
                                                    CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_mep(i).ID_PK,
                                migr_pm_mep(i).LEG_COUNT,
                                migr_pm_mep(i).NAME,
                                migr_pm_mep(i).VALUE,
                                migr_pm_mep(i).FK_BUSINESSPROCESS,
                                migr_pm_mep(i).CREATION_TIME,
                                migr_pm_mep(i).CREATED_BY,
                                migr_pm_mep(i).MODIFICATION_TIME,
                                migr_pm_mep(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_mep -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_mep(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_mep.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_mep;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_mep;

    PROCEDURE migrate_pm_mep_binding IS
        v_tab VARCHAR2(30) := 'TB_PM_MEP_BINDING';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_MEP_BINDING';

        v_id_pk NUMBER;

        CURSOR c_pm_mep_binding IS
            SELECT PMB.ID_PK,
                   PMB.NAME,
                   PMB.VALUE,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PMB.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PMB.CREATION_TIME,
                   PMB.CREATED_BY,
                   PMB.MODIFICATION_TIME,
                   PMB.MODIFIED_BY
            FROM TB_PM_MEP_BINDING PMB;

        TYPE T_PM_MEP_BINDING IS TABLE OF c_pm_mep_binding%rowtype;
        pm_mep_binding T_PM_MEP_BINDING;

        TYPE T_MIGR_PM_MEP_BINDING IS TABLE OF MIGR_TB_PM_MEP_BINDING%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_mep_binding T_MIGR_PM_MEP_BINDING;

        TYPE T_MIGR_PKS_PM_MEP_BINDING IS TABLE OF MIGR_TB_PKS_PM_MEP_BINDING%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_mep_binding T_MIGR_PKS_PM_MEP_BINDING;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_mep_binding;
        LOOP
            FETCH c_pm_mep_binding BULK COLLECT INTO pm_mep_binding LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_mep_binding.COUNT = 0;

            migr_pm_mep_binding := T_MIGR_PM_MEP_BINDING();
            migr_pks_pm_mep_binding := T_MIGR_PKS_PM_MEP_BINDING();

            FOR i IN pm_mep_binding.FIRST .. pm_mep_binding.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_mep_binding(i).ID_PK, pm_mep_binding(i).CREATION_TIME);

                    migr_pks_pm_mep_binding(i).OLD_ID := pm_mep_binding(i).ID_PK;
                    migr_pks_pm_mep_binding(i).NEW_ID := v_id_pk;

                    migr_pm_mep_binding(i).ID_PK := v_id_pk;
                    migr_pm_mep_binding(i).NAME := pm_mep_binding(i).NAME;
                    migr_pm_mep_binding(i).VALUE := pm_mep_binding(i).VALUE;
                    migr_pm_mep_binding(i).FK_BUSINESSPROCESS := pm_mep_binding(i).FK_BUSINESSPROCESS;
                    migr_pm_mep_binding(i).CREATION_TIME := pm_mep_binding(i).CREATION_TIME;
                    migr_pm_mep_binding(i).CREATED_BY := pm_mep_binding(i).CREATED_BY;
                    migr_pm_mep_binding(i).MODIFICATION_TIME := pm_mep_binding(i).MODIFICATION_TIME;
                    migr_pm_mep_binding(i).MODIFIED_BY := pm_mep_binding(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_mep_binding.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_mep_binding -> update PMode mep binding lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_MEP_BINDING (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_mep_binding(i).OLD_ID,
                            migr_pks_pm_mep_binding(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_mep_binding -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_MEP_BINDING (ID_PK, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME,
                                                            CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_mep_binding(i).ID_PK,
                                migr_pm_mep_binding(i).NAME,
                                migr_pm_mep_binding(i).VALUE,
                                migr_pm_mep_binding(i).FK_BUSINESSPROCESS,
                                migr_pm_mep_binding(i).CREATION_TIME,
                                migr_pm_mep_binding(i).CREATED_BY,
                                migr_pm_mep_binding(i).MODIFICATION_TIME,
                                migr_pm_mep_binding(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_mep_binding -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_mep_binding(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_mep_binding.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_mep_binding;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_mep_binding;

    PROCEDURE migrate_pm_message_property IS
        v_tab VARCHAR2(30) := 'TB_PM_MESSAGE_PROPERTY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_MESSAGE_PROPERTY';

        v_id_pk NUMBER;

        CURSOR c_pm_message_property IS
            SELECT PMP.ID_PK,
                   PMP.DATATYPE,
                   PMP.KEY_,
                   PMP.NAME,
                   PMP.REQUIRED_,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PMP.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PMP.CREATION_TIME,
                   PMP.CREATED_BY,
                   PMP.MODIFICATION_TIME,
                   PMP.MODIFIED_BY
            FROM TB_PM_MESSAGE_PROPERTY PMP;

        TYPE T_PM_MESSAGE_PROPERTY IS TABLE OF c_pm_message_property%rowtype;
        pm_message_property T_PM_MESSAGE_PROPERTY;

        TYPE T_MIGR_PM_MESSAGE_PROPERTY IS TABLE OF MIGR_TB_PM_MESSAGE_PROPERTY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_message_property T_MIGR_PM_MESSAGE_PROPERTY;

        TYPE T_MIGR_PKS_PM_MESSAGE_PROP IS TABLE OF MIGR_TB_PKS_PM_MESSAGE_PROP%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_message_prop T_MIGR_PKS_PM_MESSAGE_PROP;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_message_property;
        LOOP
            FETCH c_pm_message_property BULK COLLECT INTO pm_message_property LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_message_property.COUNT = 0;

            migr_pm_message_property := T_MIGR_PM_MESSAGE_PROPERTY();
            migr_pks_pm_message_prop := T_MIGR_PKS_PM_MESSAGE_PROP();

            FOR i IN pm_message_property.FIRST .. pm_message_property.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_message_property(i).ID_PK, pm_message_property(i).CREATION_TIME);

                    migr_pks_pm_message_prop(i).OLD_ID := pm_message_property(i).ID_PK;
                    migr_pks_pm_message_prop(i).NEW_ID := v_id_pk;

                    migr_pm_message_property(i).ID_PK := v_id_pk;
                    migr_pm_message_property(i).DATATYPE := pm_message_property(i).DATATYPE;
                    migr_pm_message_property(i).KEY_ := pm_message_property(i).KEY_;
                    migr_pm_message_property(i).NAME := pm_message_property(i).NAME;
                    migr_pm_message_property(i).REQUIRED_ := pm_message_property(i).REQUIRED_;
                    migr_pm_message_property(i).FK_BUSINESSPROCESS := pm_message_property(i).FK_BUSINESSPROCESS;
                    migr_pm_message_property(i).CREATION_TIME := pm_message_property(i).CREATION_TIME;
                    migr_pm_message_property(i).CREATED_BY := pm_message_property(i).CREATED_BY;
                    migr_pm_message_property(i).MODIFICATION_TIME := pm_message_property(i).MODIFICATION_TIME;
                    migr_pm_message_property(i).MODIFIED_BY := pm_message_property(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_message_property.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_message_property -> update PMode message property lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_MESSAGE_PROP (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_message_prop(i).OLD_ID,
                            migr_pks_pm_message_prop(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_message_property -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_MESSAGE_PROPERTY (ID_PK, DATATYPE, KEY_, NAME, REQUIRED_,
                                                                 FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY,
                                                                 MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_message_property(i).ID_PK,
                                migr_pm_message_property(i).DATATYPE,
                                migr_pm_message_property(i).KEY_,
                                migr_pm_message_property(i).NAME,
                                migr_pm_message_property(i).REQUIRED_,
                                migr_pm_message_property(i).FK_BUSINESSPROCESS,
                                migr_pm_message_property(i).CREATION_TIME,
                                migr_pm_message_property(i).CREATED_BY,
                                migr_pm_message_property(i).MODIFICATION_TIME,
                                migr_pm_message_property(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_message_property -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_message_property(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_message_property.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_message_property;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_message_property;

    PROCEDURE migrate_pm_message_property_set IS
        v_tab VARCHAR2(30) := 'TB_PM_MESSAGE_PROPERTY_SET';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_MSG_PROPERTY_SET';

        v_id_pk NUMBER;

        CURSOR c_pm_message_property_set IS
            SELECT PMPS.ID_PK,
                   PMPS.NAME,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PMPS.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PMPS.CREATION_TIME,
                   PMPS.CREATED_BY,
                   PMPS.MODIFICATION_TIME,
                   PMPS.MODIFIED_BY
            FROM TB_PM_MESSAGE_PROPERTY_SET PMPS;

        TYPE T_PM_MESSAGE_PROPERTY_SET IS TABLE OF c_pm_message_property_set%rowtype;
        pm_message_property_set T_PM_MESSAGE_PROPERTY_SET;

        TYPE T_MIGR_PM_MSG_PROPERTY_SET IS TABLE OF MIGR_TB_PM_MSG_PROPERTY_SET%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_msg_property_set T_MIGR_PM_MSG_PROPERTY_SET;

        TYPE T_MIGR_PKS_PM_MSG_PROP_SET IS TABLE OF MIGR_TB_PKS_PM_MSG_PROP_SET%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_msg_prop_set T_MIGR_PKS_PM_MSG_PROP_SET;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_message_property_set;
        LOOP
            FETCH c_pm_message_property_set BULK COLLECT INTO pm_message_property_set LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_message_property_set.COUNT = 0;

            migr_pm_msg_property_set := T_MIGR_PM_MSG_PROPERTY_SET();
            migr_pks_pm_msg_prop_set := T_MIGR_PKS_PM_MSG_PROP_SET();

            FOR i IN pm_message_property_set.FIRST .. pm_message_property_set.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_message_property_set(i).ID_PK, pm_message_property_set(i).CREATION_TIME);

                    migr_pks_pm_msg_prop_set(i).OLD_ID := pm_message_property_set(i).ID_PK;
                    migr_pks_pm_msg_prop_set(i).NEW_ID := v_id_pk;

                    migr_pm_msg_property_set(i).ID_PK := v_id_pk;
                    migr_pm_msg_property_set(i).NAME := pm_message_property_set(i).NAME;
                    migr_pm_msg_property_set(i).FK_BUSINESSPROCESS := pm_message_property_set(i).FK_BUSINESSPROCESS;
                    migr_pm_msg_property_set(i).CREATION_TIME := pm_message_property_set(i).CREATION_TIME;
                    migr_pm_msg_property_set(i).CREATED_BY := pm_message_property_set(i).CREATED_BY;
                    migr_pm_msg_property_set(i).MODIFICATION_TIME := pm_message_property_set(i).MODIFICATION_TIME;
                    migr_pm_msg_property_set(i).MODIFIED_BY := pm_message_property_set(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_msg_property_set.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_message_property_set -> update PMode property set lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_MSG_PROP_SET (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_msg_prop_set(i).OLD_ID,
                            migr_pks_pm_msg_prop_set(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_message_property_set -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_MSG_PROPERTY_SET (ID_PK, NAME, FK_BUSINESSPROCESS, CREATION_TIME,
                                                                 CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_msg_property_set(i).ID_PK,
                                migr_pm_msg_property_set(i).NAME,
                                migr_pm_msg_property_set(i).FK_BUSINESSPROCESS,
                                migr_pm_msg_property_set(i).CREATION_TIME,
                                migr_pm_msg_property_set(i).CREATED_BY,
                                migr_pm_msg_property_set(i).MODIFICATION_TIME,
                                migr_pm_msg_property_set(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_message_property_set -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_msg_property_set(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_message_property_set.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_message_property_set;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_message_property_set;

    PROCEDURE migrate_pm_join_property_set IS
        v_tab VARCHAR2(30) := 'TB_PM_JOIN_PROPERTY_SET';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_JOIN_PROPERTY_SET';

        -- FKs are inverse here so we need to pass the other FK value when doing the lookups
        -- (i.e PROPERTY_FK for pm_message_property_set and SET_FK for pm_message_property)
        CURSOR c_pm_join_property_set IS
            SELECT (SELECT MPKSPMPS.NEW_ID
                    FROM MIGR_TB_PKS_PM_MSG_PROP_SET MPKSPMPS
                    WHERE MPKSPMPS.OLD_ID = PJPS.PROPERTY_FK) AS PROPERTY_FK,
                   (SELECT MPKSPMP.NEW_ID
                    FROM MIGR_TB_PKS_PM_MESSAGE_PROP MPKSPMP
                    WHERE MPKSPMP.OLD_ID = PJPS.SET_FK) AS SET_FK,
                   PJPS.CREATION_TIME,
                   PJPS.CREATED_BY,
                   PJPS.MODIFICATION_TIME,
                   PJPS.MODIFIED_BY
            FROM TB_PM_JOIN_PROPERTY_SET PJPS;

        TYPE T_PM_JOIN_PROPERTY_SET IS TABLE OF c_pm_join_property_set%rowtype;
        pm_join_property_set T_PM_JOIN_PROPERTY_SET;

        TYPE T_MIGR_PM_JOIN_PROPERTY_SET IS TABLE OF MIGR_TB_PM_JOIN_PROPERTY_SET%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_join_property_set T_MIGR_PM_JOIN_PROPERTY_SET;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_join_property_set;
        LOOP
            FETCH c_pm_join_property_set BULK COLLECT INTO pm_join_property_set LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_join_property_set.COUNT = 0;

            migr_pm_join_property_set := T_MIGR_PM_JOIN_PROPERTY_SET();

            FOR i IN pm_join_property_set.FIRST .. pm_join_property_set.LAST
                LOOP
                    migr_pm_join_property_set(i).PROPERTY_FK := pm_join_property_set(i).PROPERTY_FK;
                    migr_pm_join_property_set(i).SET_FK := pm_join_property_set(i).SET_FK;
                    migr_pm_join_property_set(i).CREATION_TIME := pm_join_property_set(i).CREATION_TIME;
                    migr_pm_join_property_set(i).CREATED_BY := pm_join_property_set(i).CREATED_BY;
                    migr_pm_join_property_set(i).MODIFICATION_TIME := pm_join_property_set(i).MODIFICATION_TIME;
                    migr_pm_join_property_set(i).MODIFIED_BY := pm_join_property_set(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_join_property_set.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_pm_join_property_set -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_JOIN_PROPERTY_SET (PROPERTY_FK, SET_FK, CREATION_TIME, CREATED_BY,
                                                                  MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_join_property_set(i).PROPERTY_FK,
                                migr_pm_join_property_set(i).SET_FK,
                                migr_pm_join_property_set(i).CREATION_TIME,
                                migr_pm_join_property_set(i).CREATED_BY,
                                migr_pm_join_property_set(i).MODIFICATION_TIME,
                                migr_pm_join_property_set(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_join_property_set -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having PROPERTY_FK '
                                        || migr_pm_join_property_set(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).PROPERTY_FK
                                        || '  and SET_FK '
                                        || migr_pm_join_property_set(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).SET_FK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_join_property_set.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_join_property_set;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_join_property_set;

    PROCEDURE migrate_pm_party IS
        v_tab VARCHAR2(30) := 'TB_PM_PARTY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PARTY';

        v_id_pk NUMBER;

        CURSOR c_pm_party IS
            SELECT PP.ID_PK,
                   PP.ENDPOINT,
                   PP.NAME,
                   PP.PASSWORD,
                   PP.USERNAME,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PP.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PP.CREATION_TIME,
                   PP.CREATED_BY,
                   PP.MODIFICATION_TIME,
                   PP.MODIFIED_BY
            FROM TB_PM_PARTY PP;

        TYPE T_PM_PARTY IS TABLE OF c_pm_party%rowtype;
        pm_party T_PM_PARTY;

        TYPE T_MIGR_PM_PARTY IS TABLE OF MIGR_TB_PM_PARTY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_party T_MIGR_PM_PARTY;

        TYPE T_MIGR_PKS_PM_PARTY IS TABLE OF MIGR_TB_PKS_PM_PARTY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_party T_MIGR_PKS_PM_PARTY;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_party;
        LOOP
            FETCH c_pm_party BULK COLLECT INTO pm_party LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_party.COUNT = 0;

            migr_pm_party := T_MIGR_PM_PARTY();
            migr_pks_pm_party := T_MIGR_PKS_PM_PARTY();

            FOR i IN pm_party.FIRST .. pm_party.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_party(i).ID_PK, pm_party(i).CREATION_TIME);

                    migr_pks_pm_party(i).OLD_ID := pm_party(i).ID_PK;
                    migr_pks_pm_party(i).NEW_ID := v_id_pk;

                    migr_pm_party(i).ID_PK := v_id_pk;
                    migr_pm_party(i).ENDPOINT := pm_party(i).ENDPOINT;
                    migr_pm_party(i).NAME := pm_party(i).NAME;
                    migr_pm_party(i).PASSWORD := pm_party(i).PASSWORD;
                    migr_pm_party(i).USERNAME := pm_party(i).USERNAME;
                    migr_pm_party(i).FK_BUSINESSPROCESS := pm_party(i).FK_BUSINESSPROCESS;
                    migr_pm_party(i).CREATION_TIME := pm_party(i).CREATION_TIME;
                    migr_pm_party(i).CREATED_BY := pm_party(i).CREATED_BY;
                    migr_pm_party(i).MODIFICATION_TIME := pm_party(i).MODIFICATION_TIME;
                    migr_pm_party(i).MODIFIED_BY := pm_party(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_party.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_party -> update PMode party lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_PARTY (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_party(i).OLD_ID,
                            migr_pks_pm_party(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_party -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_PARTY (ID_PK, ENDPOINT, NAME, PASSWORD, USERNAME, FK_BUSINESSPROCESS,
                                                      CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_party(i).ID_PK,
                                migr_pm_party(i).ENDPOINT,
                                migr_pm_party(i).NAME,
                                migr_pm_party(i).PASSWORD,
                                migr_pm_party(i).USERNAME,
                                migr_pm_party(i).FK_BUSINESSPROCESS,
                                migr_pm_party(i).CREATION_TIME,
                                migr_pm_party(i).CREATED_BY,
                                migr_pm_party(i).MODIFICATION_TIME,
                                migr_pm_party(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_party -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_party(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_party.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_party;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_party;

    PROCEDURE migrate_pm_configuration IS
        v_tab VARCHAR2(30) := 'TB_PM_CONFIGURATION';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_CONFIGURATION';

        v_id_pk NUMBER;

        CURSOR c_pm_configuration IS
            SELECT PC.ID_PK,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PC.FK_BUSINESSPROCESSES) AS FK_BUSINESSPROCESSES,
                   (SELECT MPKSPP.NEW_ID
                    FROM MIGR_TB_PKS_PM_PARTY MPKSPP
                    WHERE MPKSPP.OLD_ID = PC.FK_PARTY) AS FK_PARTY,
                   PC.CREATION_TIME,
                   PC.CREATED_BY,
                   PC.MODIFICATION_TIME,
                   PC.MODIFIED_BY
            FROM TB_PM_CONFIGURATION PC;

        TYPE T_PM_CONFIGURATION IS TABLE OF c_pm_configuration%rowtype;
        pm_configuration T_PM_CONFIGURATION;

        TYPE T_MIGR_PM_CONFIGURATION IS TABLE OF MIGR_TB_PM_CONFIGURATION%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_configuration T_MIGR_PM_CONFIGURATION;

        TYPE T_MIGR_PKS_PM_CONFIGURATION IS TABLE OF MIGR_TB_PKS_PM_CONFIGURATION%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_configuration T_MIGR_PKS_PM_CONFIGURATION;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_configuration;
        LOOP
            FETCH c_pm_configuration BULK COLLECT INTO pm_configuration LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_configuration.COUNT = 0;

            migr_pm_configuration := T_MIGR_PM_CONFIGURATION();
            migr_pks_pm_configuration := T_MIGR_PKS_PM_CONFIGURATION();

            FOR i IN pm_configuration.FIRST .. pm_configuration.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_configuration(i).ID_PK, pm_configuration(i).CREATION_TIME);

                    migr_pks_pm_configuration(i).OLD_ID := pm_configuration(i).ID_PK;
                    migr_pks_pm_configuration(i).NEW_ID := v_id_pk;

                    migr_pm_configuration(i).ID_PK := v_id_pk;
                    migr_pm_configuration(i).FK_BUSINESSPROCESSES := pm_configuration(i).FK_BUSINESSPROCESSES;
                    migr_pm_configuration(i).FK_PARTY := pm_configuration(i).FK_PARTY;
                    migr_pm_configuration(i).CREATION_TIME := pm_configuration(i).CREATION_TIME;
                    migr_pm_configuration(i).CREATED_BY := pm_configuration(i).CREATED_BY;
                    migr_pm_configuration(i).MODIFICATION_TIME := pm_configuration(i).MODIFICATION_TIME;
                    migr_pm_configuration(i).MODIFIED_BY := pm_configuration(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_configuration.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_configuration -> update PMode configuration lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_CONFIGURATION (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_configuration(i).OLD_ID,
                            migr_pks_pm_configuration(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_configuration -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_CONFIGURATION (ID_PK, FK_BUSINESSPROCESSES, FK_PARTY, CREATION_TIME,
                                                              CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_configuration(i).ID_PK,
                                migr_pm_configuration(i).FK_BUSINESSPROCESSES,
                                migr_pm_configuration(i).FK_PARTY,
                                migr_pm_configuration(i).CREATION_TIME,
                                migr_pm_configuration(i).CREATED_BY,
                                migr_pm_configuration(i).MODIFICATION_TIME,
                                migr_pm_configuration(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_configuration -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_configuration(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_configuration.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_configuration;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_configuration;

    PROCEDURE migrate_pm_mpc IS
        v_tab VARCHAR2(30) := 'TB_PM_MPC';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_MPC';

        v_id_pk NUMBER;

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
                   (SELECT MPKSPC.NEW_ID
                    FROM MIGR_TB_PKS_PM_CONFIGURATION MPKSPC
                    WHERE MPKSPC.OLD_ID = PM.FK_CONFIGURATION) AS FK_CONFIGURATION,
                   PM.CREATION_TIME,
                   PM.CREATED_BY,
                   PM.MODIFICATION_TIME,
                   PM.MODIFIED_BY
            FROM TB_PM_MPC PM;

        TYPE T_PM_MPC IS TABLE OF c_pm_mpc%rowtype;
        pm_mpc T_PM_MPC;

        TYPE T_MIGR_PM_MPC IS TABLE OF MIGR_TB_PM_MPC%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_mpc T_MIGR_PM_MPC;

        TYPE T_MIGR_PKS_PM_MPC IS TABLE OF MIGR_TB_PKS_PM_MPC%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_mpc T_MIGR_PKS_PM_MPC;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_mpc;
        LOOP
            FETCH c_pm_mpc BULK COLLECT INTO pm_mpc LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_mpc.COUNT = 0;

            migr_pm_mpc := T_MIGR_PM_MPC();
            migr_pks_pm_mpc := T_MIGR_PKS_PM_MPC();

            FOR i IN pm_mpc.FIRST .. pm_mpc.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_mpc(i).ID_PK, pm_mpc(i).CREATION_TIME);

                    migr_pks_pm_mpc(i).OLD_ID := pm_mpc(i).ID_PK;
                    migr_pks_pm_mpc(i).NEW_ID := v_id_pk;

                    migr_pm_mpc(i).ID_PK := v_id_pk;
                    migr_pm_mpc(i).DEFAULT_MPC := pm_mpc(i).DEFAULT_MPC;
                    migr_pm_mpc(i).IS_ENABLED := pm_mpc(i).IS_ENABLED;
                    migr_pm_mpc(i).NAME := pm_mpc(i).NAME;
                    migr_pm_mpc(i).QUALIFIED_NAME := pm_mpc(i).QUALIFIED_NAME;
                    migr_pm_mpc(i).RETENTION_DOWNLOADED := pm_mpc(i).RETENTION_DOWNLOADED;
                    migr_pm_mpc(i).RETENTION_UNDOWNLOADED := pm_mpc(i).RETENTION_UNDOWNLOADED;
                    migr_pm_mpc(i).RETENTION_SENT := pm_mpc(i).RETENTION_SENT;
                    migr_pm_mpc(i).DELETE_MESSAGE_METADATA := pm_mpc(i).DELETE_MESSAGE_METADATA;
                    migr_pm_mpc(i).MAX_BATCH_DELETE := pm_mpc(i).MAX_BATCH_DELETE;
                    migr_pm_mpc(i).FK_CONFIGURATION := pm_mpc(i).FK_CONFIGURATION;
                    migr_pm_mpc(i).CREATION_TIME := pm_mpc(i).CREATION_TIME;
                    migr_pm_mpc(i).CREATED_BY := pm_mpc(i).CREATED_BY;
                    migr_pm_mpc(i).MODIFICATION_TIME := pm_mpc(i).MODIFICATION_TIME;
                    migr_pm_mpc(i).MODIFIED_BY := pm_mpc(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_mpc.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_mpc -> update PMode mpc lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_MPC (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_mpc(i).OLD_ID,
                            migr_pks_pm_mpc(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_mpc -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_MPC (ID_PK, DEFAULT_MPC, IS_ENABLED, NAME, QUALIFIED_NAME,
                                                    RETENTION_DOWNLOADED, RETENTION_UNDOWNLOADED, RETENTION_SENT,
                                                    DELETE_MESSAGE_METADATA, MAX_BATCH_DELETE, FK_CONFIGURATION,
                                                    CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_mpc(i).ID_PK,
                                migr_pm_mpc(i).DEFAULT_MPC,
                                migr_pm_mpc(i).IS_ENABLED,
                                migr_pm_mpc(i).NAME,
                                migr_pm_mpc(i).QUALIFIED_NAME,
                                migr_pm_mpc(i).RETENTION_DOWNLOADED,
                                migr_pm_mpc(i).RETENTION_UNDOWNLOADED,
                                migr_pm_mpc(i).RETENTION_SENT,
                                migr_pm_mpc(i).DELETE_MESSAGE_METADATA,
                                migr_pm_mpc(i).MAX_BATCH_DELETE,
                                migr_pm_mpc(i).FK_CONFIGURATION,
                                migr_pm_mpc(i).CREATION_TIME,
                                migr_pm_mpc(i).CREATED_BY,
                                migr_pm_mpc(i).MODIFICATION_TIME,
                                migr_pm_mpc(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_mpc -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_mpc(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_mpc.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_mpc;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_mpc;

    PROCEDURE migrate_pm_party_id_type IS
        v_tab VARCHAR2(30) := 'TB_PM_PARTY_ID_TYPE';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PARTY_ID_TYPE';

        v_id_pk NUMBER;

        CURSOR c_pm_party_id_type IS
            SELECT PPIT.ID_PK,
                   PPIT.NAME,
                   PPIT.VALUE,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PPIT.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PPIT.CREATION_TIME,
                   PPIT.CREATED_BY,
                   PPIT.MODIFICATION_TIME,
                   PPIT.MODIFIED_BY
            FROM TB_PM_PARTY_ID_TYPE PPIT;

        TYPE T_PM_PARTY_ID_TYPE IS TABLE OF c_pm_party_id_type%rowtype;
        pm_party_id_type T_PM_PARTY_ID_TYPE;

        TYPE T_MIGR_PM_PARTY_ID_TYPE IS TABLE OF MIGR_TB_PM_PARTY_ID_TYPE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_party_id_type T_MIGR_PM_PARTY_ID_TYPE;

        TYPE T_MIGR_PKS_PM_PARTY_ID_TYPE IS TABLE OF MIGR_TB_PKS_PM_PARTY_ID_TYPE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_party_id_type T_MIGR_PKS_PM_PARTY_ID_TYPE;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_party_id_type;
        LOOP
            FETCH c_pm_party_id_type BULK COLLECT INTO pm_party_id_type LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_party_id_type.COUNT = 0;

            migr_pm_party_id_type := T_MIGR_PM_PARTY_ID_TYPE();
            migr_pks_pm_party_id_type := T_MIGR_PKS_PM_PARTY_ID_TYPE();

            FOR i IN pm_party_id_type.FIRST .. pm_party_id_type.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_party_id_type(i).ID_PK, pm_party_id_type(i).CREATION_TIME);

                    migr_pks_pm_party_id_type(i).OLD_ID := pm_party_id_type(i).ID_PK;
                    migr_pks_pm_party_id_type(i).NEW_ID := v_id_pk;

                    migr_pm_party_id_type(i).ID_PK := v_id_pk;
                    migr_pm_party_id_type(i).NAME := pm_party_id_type(i).NAME;
                    migr_pm_party_id_type(i).VALUE := pm_party_id_type(i).VALUE;
                    migr_pm_party_id_type(i).FK_BUSINESSPROCESS := pm_party_id_type(i).FK_BUSINESSPROCESS;
                    migr_pm_party_id_type(i).CREATION_TIME := pm_party_id_type(i).CREATION_TIME;
                    migr_pm_party_id_type(i).CREATED_BY := pm_party_id_type(i).CREATED_BY;
                    migr_pm_party_id_type(i).MODIFICATION_TIME := pm_party_id_type(i).MODIFICATION_TIME;
                    migr_pm_party_id_type(i).MODIFIED_BY := pm_party_id_type(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_party_id_type.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_party_id_type -> update PMode party ID type lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_PARTY_ID_TYPE (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_party_id_type(i).OLD_ID,
                            migr_pks_pm_party_id_type(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_party_id_type -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_PARTY_ID_TYPE (ID_PK, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME,
                                                              CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_party_id_type(i).ID_PK,
                                migr_pm_party_id_type(i).NAME,
                                migr_pm_party_id_type(i).VALUE,
                                migr_pm_party_id_type(i).FK_BUSINESSPROCESS,
                                migr_pm_party_id_type(i).CREATION_TIME,
                                migr_pm_party_id_type(i).CREATED_BY,
                                migr_pm_party_id_type(i).MODIFICATION_TIME,
                                migr_pm_party_id_type(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_party_id_type -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_party_id_type(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_party_id_type.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_party_id_type;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_party_id_type;

    PROCEDURE migrate_pm_party_identifier IS
        v_tab VARCHAR2(30) := 'TB_PM_PARTY_IDENTIFIER';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PARTY_IDENTIFIER';

        v_id_pk NUMBER;

        CURSOR c_pm_party_identifier IS
            SELECT PPI.ID_PK,
                   PPI.PARTY_ID,
                   (SELECT MPKSPPIT.NEW_ID
                    FROM MIGR_TB_PKS_PM_PARTY_ID_TYPE MPKSPPIT
                    WHERE MPKSPPIT.OLD_ID = PPI.FK_PARTY_ID_TYPE) AS FK_PARTY_ID_TYPE,
                   (SELECT MPKSPP.NEW_ID
                    FROM MIGR_TB_PKS_PM_PARTY MPKSPP
                    WHERE MPKSPP.OLD_ID = PPI.FK_PARTY) AS FK_PARTY,
                   PPI.CREATION_TIME,
                   PPI.CREATED_BY,
                   PPI.MODIFICATION_TIME,
                   PPI.MODIFIED_BY
            FROM TB_PM_PARTY_IDENTIFIER PPI;

        TYPE T_PM_PARTY_IDENTIFIER IS TABLE OF c_pm_party_identifier%rowtype;
        pm_party_identifier T_PM_PARTY_IDENTIFIER;

        TYPE T_MIGR_PM_PARTY_IDENTIFIER IS TABLE OF MIGR_TB_PM_PARTY_IDENTIFIER%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_party_identifier T_MIGR_PM_PARTY_IDENTIFIER;

        TYPE T_MIGR_PKS_PM_PARTY_ID IS TABLE OF MIGR_TB_PKS_PM_PARTY_ID%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_party_id T_MIGR_PKS_PM_PARTY_ID;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_party_identifier;
        LOOP
            FETCH c_pm_party_identifier BULK COLLECT INTO pm_party_identifier LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_party_identifier.COUNT = 0;

            migr_pm_party_identifier := T_MIGR_PM_PARTY_IDENTIFIER();
            migr_pks_pm_party_id := T_MIGR_PKS_PM_PARTY_ID();

            FOR i IN pm_party_identifier.FIRST .. pm_party_identifier.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_party_identifier(i).ID_PK, pm_party_identifier(i).CREATION_TIME);

                    migr_pks_pm_party_id(i).OLD_ID := pm_party_identifier(i).ID_PK;
                    migr_pks_pm_party_id(i).NEW_ID := v_id_pk;

                    migr_pm_party_identifier(i).ID_PK := v_id_pk;
                    migr_pm_party_identifier(i).PARTY_ID := pm_party_identifier(i).PARTY_ID;
                    migr_pm_party_identifier(i).FK_PARTY_ID_TYPE := pm_party_identifier(i).FK_PARTY_ID_TYPE;
                    migr_pm_party_identifier(i).FK_PARTY := pm_party_identifier(i).FK_PARTY;
                    migr_pm_party_identifier(i).CREATION_TIME := pm_party_identifier(i).CREATION_TIME;
                    migr_pm_party_identifier(i).CREATED_BY := pm_party_identifier(i).CREATED_BY;
                    migr_pm_party_identifier(i).MODIFICATION_TIME := pm_party_identifier(i).MODIFICATION_TIME;
                    migr_pm_party_identifier(i).MODIFIED_BY := pm_party_identifier(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_party_identifier.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_party_identifier -> update PMode party ID lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_PARTY_ID (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_party_id(i).OLD_ID,
                            migr_pks_pm_party_id(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_party_identifier -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_PARTY_IDENTIFIER (ID_PK, PARTY_ID, FK_PARTY_ID_TYPE, FK_PARTY,
                                                                 CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                                 MODIFIED_BY)
                        VALUES (migr_pm_party_identifier(i).ID_PK,
                                migr_pm_party_identifier(i).PARTY_ID,
                                migr_pm_party_identifier(i).FK_PARTY_ID_TYPE,
                                migr_pm_party_identifier(i).FK_PARTY,
                                migr_pm_party_identifier(i).CREATION_TIME,
                                migr_pm_party_identifier(i).CREATED_BY,
                                migr_pm_party_identifier(i).MODIFICATION_TIME,
                                migr_pm_party_identifier(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_party_identifier -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_party_identifier(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_party_identifier.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_party_identifier;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_party_identifier;

    PROCEDURE migrate_pm_payload IS
        v_tab VARCHAR2(30) := 'TB_PM_PAYLOAD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PAYLOAD';

        v_id_pk NUMBER;

        CURSOR c_pm_payload IS
            SELECT PP.ID_PK,
                   PP.CID,
                   PP.IN_BODY,
                   PP.MAX_SIZE,
                   PP.MIME_TYPE,
                   PP.NAME,
                   PP.REQUIRED_,
                   PP.SCHEMA_FILE,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PP.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PP.CREATION_TIME,
                   PP.CREATED_BY,
                   PP.MODIFICATION_TIME,
                   PP.MODIFIED_BY
            FROM TB_PM_PAYLOAD PP;

        TYPE T_PM_PAYLOAD IS TABLE OF c_pm_payload%rowtype;
        pm_payload T_PM_PAYLOAD;

        TYPE T_MIGR_PM_PAYLOAD IS TABLE OF MIGR_TB_PM_PAYLOAD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_payload T_MIGR_PM_PAYLOAD;

        TYPE T_MIGR_PKS_PM_PAYLOAD IS TABLE OF MIGR_TB_PKS_PM_PAYLOAD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_payload T_MIGR_PKS_PM_PAYLOAD;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_payload;
        LOOP
            FETCH c_pm_payload BULK COLLECT INTO pm_payload LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_payload.COUNT = 0;

            migr_pm_payload := T_MIGR_PM_PAYLOAD();
            migr_pks_pm_payload := T_MIGR_PKS_PM_PAYLOAD();

            FOR i IN pm_payload.FIRST .. pm_payload.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_payload(i).ID_PK, pm_payload(i).CREATION_TIME);

                    migr_pks_pm_payload(i).OLD_ID := pm_payload(i).ID_PK;
                    migr_pks_pm_payload(i).NEW_ID := v_id_pk;

                    migr_pm_payload(i).ID_PK := v_id_pk;
                    migr_pm_payload(i).CID := pm_payload(i).CID;
                    migr_pm_payload(i).IN_BODY := pm_payload(i).IN_BODY;
                    migr_pm_payload(i).MAX_SIZE := pm_payload(i).MAX_SIZE;
                    migr_pm_payload(i).MIME_TYPE := pm_payload(i).MIME_TYPE;
                    migr_pm_payload(i).NAME := pm_payload(i).NAME;
                    migr_pm_payload(i).REQUIRED_ := pm_payload(i).REQUIRED_;
                    migr_pm_payload(i).SCHEMA_FILE := pm_payload(i).SCHEMA_FILE;
                    migr_pm_payload(i).FK_BUSINESSPROCESS := pm_payload(i).FK_BUSINESSPROCESS;
                    migr_pm_payload(i).CREATION_TIME := pm_payload(i).CREATION_TIME;
                    migr_pm_payload(i).CREATED_BY := pm_payload(i).CREATED_BY;
                    migr_pm_payload(i).MODIFICATION_TIME := pm_payload(i).MODIFICATION_TIME;
                    migr_pm_payload(i).MODIFIED_BY := pm_payload(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_payload.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_payload -> update PMode payload lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_PAYLOAD (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_payload(i).OLD_ID,
                            migr_pks_pm_payload(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_payload -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_PAYLOAD (ID_PK, CID, IN_BODY, MAX_SIZE, MIME_TYPE, NAME, REQUIRED_,
                                                        SCHEMA_FILE, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY,
                                                        MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_payload(i).ID_PK,
                                migr_pm_payload(i).CID,
                                migr_pm_payload(i).IN_BODY,
                                migr_pm_payload(i).MAX_SIZE,
                                migr_pm_payload(i).MIME_TYPE,
                                migr_pm_payload(i).NAME,
                                migr_pm_payload(i).REQUIRED_,
                                migr_pm_payload(i).SCHEMA_FILE,
                                migr_pm_payload(i).FK_BUSINESSPROCESS,
                                migr_pm_payload(i).CREATION_TIME,
                                migr_pm_payload(i).CREATED_BY,
                                migr_pm_payload(i).MODIFICATION_TIME,
                                migr_pm_payload(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_payload -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_payload(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_payload.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_payload;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_payload;

    PROCEDURE migrate_pm_payload_profile IS
        v_tab VARCHAR2(30) := 'TB_PM_PAYLOAD_PROFILE';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PAYLOAD_PROFILE';

        v_id_pk NUMBER;

        CURSOR c_pm_payload_profile IS
            SELECT PPP.ID_PK,
                   PPP.MAX_SIZE,
                   PPP.NAME,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PPP.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PPP.CREATION_TIME,
                   PPP.CREATED_BY,
                   PPP.MODIFICATION_TIME,
                   PPP.MODIFIED_BY
            FROM TB_PM_PAYLOAD_PROFILE PPP;

        TYPE T_PM_PAYLOAD_PROFILE IS TABLE OF c_pm_payload_profile%rowtype;
        pm_payload_profile T_PM_PAYLOAD_PROFILE;

        TYPE T_MIGR_PM_PAYLOAD_PROFILE IS TABLE OF MIGR_TB_PM_PAYLOAD_PROFILE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_payload_profile T_MIGR_PM_PAYLOAD_PROFILE;

        TYPE T_MIGR_PKS_PM_PAYLOAD_PROF IS TABLE OF MIGR_TB_PKS_PM_PAYLOAD_PROF%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_payload_prof T_MIGR_PKS_PM_PAYLOAD_PROF;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_payload_profile;
        LOOP
            FETCH c_pm_payload_profile BULK COLLECT INTO pm_payload_profile LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_payload_profile.COUNT = 0;

            migr_pm_payload_profile := T_MIGR_PM_PAYLOAD_PROFILE();
            migr_pks_pm_payload_prof := T_MIGR_PKS_PM_PAYLOAD_PROF();

            FOR i IN pm_payload_profile.FIRST .. pm_payload_profile.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_payload_profile(i).ID_PK, pm_payload_profile(i).CREATION_TIME);

                    migr_pks_pm_payload_prof(i).OLD_ID := pm_payload_profile(i).ID_PK;
                    migr_pks_pm_payload_prof(i).NEW_ID := v_id_pk;

                    migr_pm_payload_profile(i).ID_PK := v_id_pk;
                    migr_pm_payload_profile(i).MAX_SIZE := pm_payload_profile(i).MAX_SIZE;
                    migr_pm_payload_profile(i).NAME := pm_payload_profile(i).NAME;
                    migr_pm_payload_profile(i).FK_BUSINESSPROCESS := pm_payload_profile(i).FK_BUSINESSPROCESS;
                    migr_pm_payload_profile(i).CREATION_TIME := pm_payload_profile(i).CREATION_TIME;
                    migr_pm_payload_profile(i).CREATED_BY := pm_payload_profile(i).CREATED_BY;
                    migr_pm_payload_profile(i).MODIFICATION_TIME := pm_payload_profile(i).MODIFICATION_TIME;
                    migr_pm_payload_profile(i).MODIFIED_BY := pm_payload_profile(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_payload_profile.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_payload_profile -> update PMode payload profile lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_PAYLOAD_PROF (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_payload_prof(i).OLD_ID,
                            migr_pks_pm_payload_prof(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_payload_profile -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_PAYLOAD_PROFILE (ID_PK, MAX_SIZE, NAME, FK_BUSINESSPROCESS,
                                                                CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                                MODIFIED_BY)
                        VALUES (migr_pm_payload_profile(i).ID_PK,
                                migr_pm_payload_profile(i).MAX_SIZE,
                                migr_pm_payload_profile(i).NAME,
                                migr_pm_payload_profile(i).FK_BUSINESSPROCESS,
                                migr_pm_payload_profile(i).CREATION_TIME,
                                migr_pm_payload_profile(i).CREATED_BY,
                                migr_pm_payload_profile(i).MODIFICATION_TIME,
                                migr_pm_payload_profile(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_payload_profile -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_payload_profile(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_payload_profile.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_payload_profile;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_payload_profile;

    PROCEDURE migrate_pm_join_payload_profile IS
        v_tab VARCHAR2(30) := 'TB_PM_JOIN_PAYLOAD_PROFILE';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_JOIN_PAYLD_PROFILE';

        -- FKs are inverse here so we need to pass the other FK value when doing the lookups
        -- (i.e FK_PAYLOAD for pm_payload_profile and FK_PROFILE for pm_payload)
        CURSOR c_pm_join_payload_profile IS
            SELECT (SELECT MPKSPPP.NEW_ID
                    FROM MIGR_TB_PKS_PM_PAYLOAD_PROF MPKSPPP
                    WHERE MPKSPPP.OLD_ID = PJPP.FK_PAYLOAD) AS FK_PAYLOAD,
                    (SELECT MPKSPP.NEW_ID
                     FROM MIGR_TB_PKS_PM_PAYLOAD MPKSPP
                     WHERE MPKSPP.OLD_ID = PJPP.FK_PROFILE) AS FK_PROFILE,
                   PJPP.CREATION_TIME,
                   PJPP.CREATED_BY,
                   PJPP.MODIFICATION_TIME,
                   PJPP.MODIFIED_BY
            FROM TB_PM_JOIN_PAYLOAD_PROFILE PJPP;

        TYPE T_PM_JOIN_PAYLOAD_PROFILE IS TABLE OF c_pm_join_payload_profile%rowtype;
        pm_join_payload_profile T_PM_JOIN_PAYLOAD_PROFILE;

        TYPE T_MIGR_PM_JOIN_PAYLD_PROFILE IS TABLE OF MIGR_TB_PM_JOIN_PAYLD_PROFILE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_join_payld_profile T_MIGR_PM_JOIN_PAYLD_PROFILE;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_join_payload_profile;
        LOOP
            FETCH c_pm_join_payload_profile BULK COLLECT INTO pm_join_payload_profile LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_join_payload_profile.COUNT = 0;

            migr_pm_join_payld_profile := T_MIGR_PM_JOIN_PAYLD_PROFILE();

            FOR i IN pm_join_payload_profile.FIRST .. pm_join_payload_profile.LAST
                LOOP
                    migr_pm_join_payld_profile(i).FK_PAYLOAD := pm_join_payload_profile(i).FK_PAYLOAD;
                    migr_pm_join_payld_profile(i).FK_PROFILE := pm_join_payload_profile(i).FK_PROFILE;
                    migr_pm_join_payld_profile(i).CREATION_TIME := pm_join_payload_profile(i).CREATION_TIME;
                    migr_pm_join_payld_profile(i).CREATED_BY := pm_join_payload_profile(i).CREATED_BY;
                    migr_pm_join_payld_profile(i).MODIFICATION_TIME := pm_join_payload_profile(i).MODIFICATION_TIME;
                    migr_pm_join_payld_profile(i).MODIFIED_BY := pm_join_payload_profile(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_join_payld_profile.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_pm_join_payload_profile -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_JOIN_PAYLD_PROFILE (FK_PAYLOAD, FK_PROFILE, CREATION_TIME, CREATED_BY,
                                                                   MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_join_payld_profile(i).FK_PAYLOAD,
                                migr_pm_join_payld_profile(i).FK_PROFILE,
                                migr_pm_join_payld_profile(i).CREATION_TIME,
                                migr_pm_join_payld_profile(i).CREATED_BY,
                                migr_pm_join_payld_profile(i).MODIFICATION_TIME,
                                migr_pm_join_payld_profile(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_join_payload_profile -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having FK_PAYLOAD '
                                        || migr_pm_join_payld_profile(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).FK_PAYLOAD
                                        || '  and FK_PROFILE '
                                        || migr_pm_join_payld_profile(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).FK_PROFILE);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_join_payload_profile.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_join_payload_profile;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_join_payload_profile;

    PROCEDURE migrate_pm_reception_awareness IS
        v_tab VARCHAR2(30) := 'TB_PM_RECEPTION_AWARENESS';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_RECEPTION_AWARENESS';

        v_id_pk NUMBER;

        CURSOR c_pm_reception_awareness IS
            SELECT PRA.ID_PK,
                   PRA.DUPLICATE_DETECTION,
                   PRA.NAME,
                   PRA.RETRY_COUNT,
                   PRA.RETRY_TIMEOUT,
                   PRA.STRATEGY,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PRA.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PRA.CREATION_TIME,
                   PRA.CREATED_BY,
                   PRA.MODIFICATION_TIME,
                   PRA.MODIFIED_BY
            FROM TB_PM_RECEPTION_AWARENESS PRA;

        TYPE T_PM_RECEPTION_AWARENESS IS TABLE OF c_pm_reception_awareness%rowtype;
        pm_reception_awareness T_PM_RECEPTION_AWARENESS;

        TYPE T_MIGR_PM_RECEPTION_AWARENESS IS TABLE OF MIGR_TB_PM_RECEPTION_AWARENESS%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_reception_awareness T_MIGR_PM_RECEPTION_AWARENESS;

        TYPE T_MIGR_PKS_PM_RECEPTN_AWARNS IS TABLE OF MIGR_TB_PKS_PM_RECEPTN_AWARNS%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_receptn_awarns T_MIGR_PKS_PM_RECEPTN_AWARNS;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_reception_awareness;
        LOOP
            FETCH c_pm_reception_awareness BULK COLLECT INTO pm_reception_awareness LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_reception_awareness.COUNT = 0;

            migr_pm_reception_awareness := T_MIGR_PM_RECEPTION_AWARENESS();
            migr_pks_pm_receptn_awarns := T_MIGR_PKS_PM_RECEPTN_AWARNS();

            FOR i IN pm_reception_awareness.FIRST .. pm_reception_awareness.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_reception_awareness(i).ID_PK, pm_reception_awareness(i).CREATION_TIME);

                    migr_pks_pm_receptn_awarns(i).OLD_ID := pm_reception_awareness(i).ID_PK;
                    migr_pks_pm_receptn_awarns(i).NEW_ID := v_id_pk;

                    migr_pm_reception_awareness(i).ID_PK := v_id_pk;
                    migr_pm_reception_awareness(i).DUPLICATE_DETECTION := pm_reception_awareness(i).DUPLICATE_DETECTION;
                    migr_pm_reception_awareness(i).NAME := pm_reception_awareness(i).NAME;
                    migr_pm_reception_awareness(i).RETRY_COUNT := pm_reception_awareness(i).RETRY_COUNT;
                    migr_pm_reception_awareness(i).RETRY_TIMEOUT := pm_reception_awareness(i).RETRY_TIMEOUT;
                    migr_pm_reception_awareness(i).STRATEGY := pm_reception_awareness(i).STRATEGY;
                    migr_pm_reception_awareness(i).FK_BUSINESSPROCESS := pm_reception_awareness(i).FK_BUSINESSPROCESS;
                    migr_pm_reception_awareness(i).CREATION_TIME := pm_reception_awareness(i).CREATION_TIME;
                    migr_pm_reception_awareness(i).CREATED_BY := pm_reception_awareness(i).CREATED_BY;
                    migr_pm_reception_awareness(i).MODIFICATION_TIME := pm_reception_awareness(i).MODIFICATION_TIME;
                    migr_pm_reception_awareness(i).MODIFIED_BY := pm_reception_awareness(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_reception_awareness.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_reception_awareness -> update PMode reception awareness lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_RECEPTN_AWARNS (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_receptn_awarns(i).OLD_ID,
                            migr_pks_pm_receptn_awarns(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_reception_awareness -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_RECEPTION_AWARENESS (ID_PK, DUPLICATE_DETECTION, NAME, RETRY_COUNT,
                                                                    RETRY_TIMEOUT, STRATEGY, FK_BUSINESSPROCESS,
                                                                    CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                                    MODIFIED_BY)
                        VALUES (migr_pm_reception_awareness(i).ID_PK,
                                migr_pm_reception_awareness(i).DUPLICATE_DETECTION,
                                migr_pm_reception_awareness(i).NAME,
                                migr_pm_reception_awareness(i).RETRY_COUNT,
                                migr_pm_reception_awareness(i).RETRY_TIMEOUT,
                                migr_pm_reception_awareness(i).STRATEGY,
                                migr_pm_reception_awareness(i).FK_BUSINESSPROCESS,
                                migr_pm_reception_awareness(i).CREATION_TIME,
                                migr_pm_reception_awareness(i).CREATED_BY,
                                migr_pm_reception_awareness(i).MODIFICATION_TIME,
                                migr_pm_reception_awareness(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_reception_awareness -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_reception_awareness(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_reception_awareness.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_reception_awareness;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_reception_awareness;

    PROCEDURE migrate_pm_reliability IS
        v_tab VARCHAR2(30) := 'TB_PM_RELIABILITY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_RELIABILITY';

        v_id_pk NUMBER;

        CURSOR c_pm_reliability IS
            SELECT PR.ID_PK,
                   PR.NAME,
                   PR.NON_REPUDIATION,
                   PR.REPLY_PATTERN,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PR.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PR.CREATION_TIME,
                   PR.CREATED_BY,
                   PR.MODIFICATION_TIME,
                   PR.MODIFIED_BY
            FROM TB_PM_RELIABILITY PR;

        TYPE T_PM_RELIABILITY IS TABLE OF c_pm_reliability%rowtype;
        pm_reliability T_PM_RELIABILITY;

        TYPE T_MIGR_PM_RELIABILITY IS TABLE OF MIGR_TB_PM_RELIABILITY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_reliability T_MIGR_PM_RELIABILITY;

        TYPE T_MIGR_PKS_PM_RELIABILITY IS TABLE OF MIGR_TB_PKS_PM_RELIABILITY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_reliability T_MIGR_PKS_PM_RELIABILITY;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_reliability;
        LOOP
            FETCH c_pm_reliability BULK COLLECT INTO pm_reliability LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_reliability.COUNT = 0;

            migr_pm_reliability := T_MIGR_PM_RELIABILITY();
            migr_pks_pm_reliability := T_MIGR_PKS_PM_RELIABILITY();

            FOR i IN pm_reliability.FIRST .. pm_reliability.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_reliability(i).ID_PK, pm_reliability(i).CREATION_TIME);

                    migr_pks_pm_reliability(i).OLD_ID := pm_reliability(i).ID_PK;
                    migr_pks_pm_reliability(i).NEW_ID := v_id_pk;

                    migr_pm_reliability(i).ID_PK := v_id_pk;
                    migr_pm_reliability(i).NAME := pm_reliability(i).NAME;
                    migr_pm_reliability(i).NON_REPUDIATION := pm_reliability(i).NON_REPUDIATION;
                    migr_pm_reliability(i).REPLY_PATTERN := pm_reliability(i).REPLY_PATTERN;
                    migr_pm_reliability(i).FK_BUSINESSPROCESS := pm_reliability(i).FK_BUSINESSPROCESS;
                    migr_pm_reliability(i).CREATION_TIME := pm_reliability(i).CREATION_TIME;
                    migr_pm_reliability(i).CREATED_BY := pm_reliability(i).CREATED_BY;
                    migr_pm_reliability(i).MODIFICATION_TIME := pm_reliability(i).MODIFICATION_TIME;
                    migr_pm_reliability(i).MODIFIED_BY := pm_reliability(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_reliability.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_reliability -> update PMode reliability lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_RELIABILITY (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_reliability(i).OLD_ID,
                            migr_pks_pm_reliability(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_reliability -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_RELIABILITY (ID_PK, NAME, NON_REPUDIATION, REPLY_PATTERN,
                                                            FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY,
                                                            MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_reliability(i).ID_PK,
                                migr_pm_reliability(i).NAME,
                                migr_pm_reliability(i).NON_REPUDIATION,
                                migr_pm_reliability(i).REPLY_PATTERN,
                                migr_pm_reliability(i).FK_BUSINESSPROCESS,
                                migr_pm_reliability(i).CREATION_TIME,
                                migr_pm_reliability(i).CREATED_BY,
                                migr_pm_reliability(i).MODIFICATION_TIME,
                                migr_pm_reliability(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_reliability -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_reliability(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_reliability.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_reliability;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_reliability;

    PROCEDURE migrate_pm_role IS
        v_tab VARCHAR2(30) := 'TB_PM_ROLE';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_ROLE';

        v_id_pk NUMBER;

        CURSOR c_pm_role IS
            SELECT PR.ID_PK,
                   PR.NAME,
                   PR.VALUE,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PR.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PR.CREATION_TIME,
                   PR.CREATED_BY,
                   PR.MODIFICATION_TIME,
                   PR.MODIFIED_BY
            FROM TB_PM_ROLE PR;

        TYPE T_PM_ROLE IS TABLE OF c_pm_role%rowtype;
        pm_role T_PM_ROLE;

        TYPE T_MIGR_PM_ROLE IS TABLE OF MIGR_TB_PM_ROLE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_role T_MIGR_PM_ROLE;

        TYPE T_MIGR_PKS_PM_ROLE IS TABLE OF MIGR_TB_PKS_PM_ROLE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_role T_MIGR_PKS_PM_ROLE;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_role;
        LOOP
            FETCH c_pm_role BULK COLLECT INTO pm_role LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_role.COUNT = 0;

            migr_pm_role := T_MIGR_PM_ROLE();
            migr_pks_pm_role := T_MIGR_PKS_PM_ROLE();

            FOR i IN pm_role.FIRST .. pm_role.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_role(i).ID_PK, pm_role(i).CREATION_TIME);

                    migr_pks_pm_role(i).OLD_ID := pm_role(i).ID_PK;
                    migr_pks_pm_role(i).NEW_ID := v_id_pk;

                    migr_pm_role(i).ID_PK := v_id_pk;
                    migr_pm_role(i).NAME := pm_role(i).NAME;
                    migr_pm_role(i).VALUE := pm_role(i).VALUE;
                    migr_pm_role(i).FK_BUSINESSPROCESS := pm_role(i).FK_BUSINESSPROCESS;
                    migr_pm_role(i).CREATION_TIME := pm_role(i).CREATION_TIME;
                    migr_pm_role(i).CREATED_BY := pm_role(i).CREATED_BY;
                    migr_pm_role(i).MODIFICATION_TIME := pm_role(i).MODIFICATION_TIME;
                    migr_pm_role(i).MODIFIED_BY := pm_role(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_role.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_role -> update PMode role lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_ROLE (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_role(i).OLD_ID,
                            migr_pks_pm_role(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_role -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_ROLE (ID_PK, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY,
                                                     MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_role(i).ID_PK,
                                migr_pm_role(i).NAME,
                                migr_pm_role(i).VALUE,
                                migr_pm_role(i).FK_BUSINESSPROCESS,
                                migr_pm_role(i).CREATION_TIME,
                                migr_pm_role(i).CREATED_BY,
                                migr_pm_role(i).MODIFICATION_TIME,
                                migr_pm_role(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_role -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_role(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_role.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_role;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_role;

    PROCEDURE migrate_pm_security IS
        v_tab VARCHAR2(30) := 'TB_PM_SECURITY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_SECURITY';

        v_id_pk NUMBER;

        CURSOR c_pm_security IS
            SELECT PS.ID_PK,
                   PS.NAME,
                   PS.POLICY,
                   PS.SIGNATURE_METHOD,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PS.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PS.CREATION_TIME,
                   PS.CREATED_BY,
                   PS.MODIFICATION_TIME,
                   PS.MODIFIED_BY
            FROM TB_PM_SECURITY PS;

        TYPE T_PM_SECURITY IS TABLE OF c_pm_security%rowtype;
        pm_security T_PM_SECURITY;

        TYPE T_MIGR_PM_SECURITY IS TABLE OF MIGR_TB_PM_SECURITY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_security T_MIGR_PM_SECURITY;

        TYPE T_MIGR_PKS_PM_SECURITY IS TABLE OF MIGR_TB_PKS_PM_SECURITY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_security T_MIGR_PKS_PM_SECURITY;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_security;
        LOOP
            FETCH c_pm_security BULK COLLECT INTO pm_security LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_security.COUNT = 0;

            migr_pm_security := T_MIGR_PM_SECURITY();
            migr_pks_pm_security := T_MIGR_PKS_PM_SECURITY();

            FOR i IN pm_security.FIRST .. pm_security.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_security(i).ID_PK, pm_security(i).CREATION_TIME);

                    migr_pks_pm_security(i).OLD_ID := pm_security(i).ID_PK;
                    migr_pks_pm_security(i).NEW_ID := v_id_pk;

                    migr_pm_security(i).ID_PK := v_id_pk;
                    migr_pm_security(i).NAME := pm_security(i).NAME;
                    migr_pm_security(i).POLICY := pm_security(i).POLICY;
                    migr_pm_security(i).SIGNATURE_METHOD := pm_security(i).SIGNATURE_METHOD;
                    migr_pm_security(i).FK_BUSINESSPROCESS := pm_security(i).FK_BUSINESSPROCESS;
                    migr_pm_security(i).CREATION_TIME := pm_security(i).CREATION_TIME;
                    migr_pm_security(i).CREATED_BY := pm_security(i).CREATED_BY;
                    migr_pm_security(i).MODIFICATION_TIME := pm_security(i).MODIFICATION_TIME;
                    migr_pm_security(i).MODIFIED_BY := pm_security(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_security.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_security -> update PMode security lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_SECURITY (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_security(i).OLD_ID,
                            migr_pks_pm_security(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_security -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_SECURITY (ID_PK, NAME, POLICY, SIGNATURE_METHOD, FK_BUSINESSPROCESS,
                                                         CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_security(i).ID_PK,
                                migr_pm_security(i).NAME,
                                migr_pm_security(i).POLICY,
                                migr_pm_security(i).SIGNATURE_METHOD,
                                migr_pm_security(i).FK_BUSINESSPROCESS,
                                migr_pm_security(i).CREATION_TIME,
                                migr_pm_security(i).CREATED_BY,
                                migr_pm_security(i).MODIFICATION_TIME,
                                migr_pm_security(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_security -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_security(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_security.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_security;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_security;

    PROCEDURE migrate_pm_service IS
        v_tab VARCHAR2(30) := 'TB_PM_SERVICE';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_SERVICE';

        v_id_pk NUMBER;

        CURSOR c_pm_service IS
            SELECT PS.ID_PK,
                   PS.NAME,
                   PS.SERVICE_TYPE,
                   PS.VALUE,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PS.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PS.CREATION_TIME,
                   PS.CREATED_BY,
                   PS.MODIFICATION_TIME,
                   PS.MODIFIED_BY
            FROM TB_PM_SERVICE PS;

        TYPE T_PM_SERVICE IS TABLE OF c_pm_service%rowtype;
        pm_service T_PM_SERVICE;

        TYPE T_MIGR_PM_SERVICE IS TABLE OF MIGR_TB_PM_SERVICE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_service T_MIGR_PM_SERVICE;

        TYPE T_MIGR_PKS_PM_SERVICE IS TABLE OF MIGR_TB_PKS_PM_SERVICE%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_service T_MIGR_PKS_PM_SERVICE;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_service;
        LOOP
            FETCH c_pm_service BULK COLLECT INTO pm_service LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_service.COUNT = 0;

            migr_pm_service := T_MIGR_PM_SERVICE();
            migr_pks_pm_service := T_MIGR_PKS_PM_SERVICE();

            FOR i IN pm_service.FIRST .. pm_service.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_service(i).ID_PK, pm_service(i).CREATION_TIME);

                    migr_pks_pm_service(i).OLD_ID := pm_service(i).ID_PK;
                    migr_pks_pm_service(i).NEW_ID := v_id_pk;

                    migr_pm_service(i).ID_PK := v_id_pk;
                    migr_pm_service(i).NAME := pm_service(i).NAME;
                    migr_pm_service(i).SERVICE_TYPE := pm_service(i).SERVICE_TYPE;
                    migr_pm_service(i).VALUE := pm_service(i).VALUE;
                    migr_pm_service(i).FK_BUSINESSPROCESS := pm_service(i).FK_BUSINESSPROCESS;
                    migr_pm_service(i).CREATION_TIME := pm_service(i).CREATION_TIME;
                    migr_pm_service(i).CREATED_BY := pm_service(i).CREATED_BY;
                    migr_pm_service(i).MODIFICATION_TIME := pm_service(i).MODIFICATION_TIME;
                    migr_pm_service(i).MODIFIED_BY := pm_service(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_service.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_service -> update PMode service lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_SERVICE (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_service(i).OLD_ID,
                            migr_pks_pm_service(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_service -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_SERVICE (ID_PK, NAME, SERVICE_TYPE, VALUE, FK_BUSINESSPROCESS,
                                                        CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_service(i).ID_PK,
                                migr_pm_service(i).NAME,
                                migr_pm_service(i).SERVICE_TYPE,
                                migr_pm_service(i).VALUE,
                                migr_pm_service(i).FK_BUSINESSPROCESS,
                                migr_pm_service(i).CREATION_TIME,
                                migr_pm_service(i).CREATED_BY,
                                migr_pm_service(i).MODIFICATION_TIME,
                                migr_pm_service(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_service -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_service(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_service.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_service;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_service;

    PROCEDURE migrate_pm_splitting IS
        v_tab VARCHAR2(30) := 'TB_PM_SPLITTING';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_SPLITTING';

        v_id_pk NUMBER;

        CURSOR c_pm_splitting IS
            SELECT PS.ID_PK,
                   PS.NAME,
                   PS.FRAGMENT_SIZE,
                   PS.COMPRESSION,
                   PS.COMPRESSION_ALGORITHM,
                   PS.JOIN_INTERVAL,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PS.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PS.CREATION_TIME,
                   PS.CREATED_BY,
                   PS.MODIFICATION_TIME,
                   PS.MODIFIED_BY
            FROM TB_PM_SPLITTING PS;

        TYPE T_PM_SPLITTING IS TABLE OF c_pm_splitting%rowtype;
        pm_splitting T_PM_SPLITTING;

        TYPE T_MIGR_PM_SPLITTING IS TABLE OF MIGR_TB_PM_SPLITTING%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_splitting T_MIGR_PM_SPLITTING;

        TYPE T_MIGR_PKS_PM_SPLITTING IS TABLE OF MIGR_TB_PKS_PM_SPLITTING%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_splitting T_MIGR_PKS_PM_SPLITTING;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_splitting;
        LOOP
            FETCH c_pm_splitting BULK COLLECT INTO pm_splitting LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_splitting.COUNT = 0;

            migr_pm_splitting := T_MIGR_PM_SPLITTING();
            migr_pks_pm_splitting := T_MIGR_PKS_PM_SPLITTING();

            FOR i IN pm_splitting.FIRST .. pm_splitting.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_splitting(i).ID_PK, pm_splitting(i).CREATION_TIME);

                    migr_pks_pm_splitting(i).OLD_ID := pm_splitting(i).ID_PK;
                    migr_pks_pm_splitting(i).NEW_ID := v_id_pk;

                    migr_pm_splitting(i).ID_PK := v_id_pk;
                    migr_pm_splitting(i).NAME := pm_splitting(i).NAME;
                    migr_pm_splitting(i).FRAGMENT_SIZE := pm_splitting(i).FRAGMENT_SIZE;
                    migr_pm_splitting(i).COMPRESSION := pm_splitting(i).COMPRESSION;
                    migr_pm_splitting(i).COMPRESSION_ALGORITHM := pm_splitting(i).COMPRESSION_ALGORITHM;
                    migr_pm_splitting(i).JOIN_INTERVAL := pm_splitting(i).JOIN_INTERVAL;
                    migr_pm_splitting(i).FK_BUSINESSPROCESS := pm_splitting(i).FK_BUSINESSPROCESS;
                    migr_pm_splitting(i).CREATION_TIME := pm_splitting(i).CREATION_TIME;
                    migr_pm_splitting(i).CREATED_BY := pm_splitting(i).CREATED_BY;
                    migr_pm_splitting(i).MODIFICATION_TIME := pm_splitting(i).MODIFICATION_TIME;
                    migr_pm_splitting(i).MODIFIED_BY := pm_splitting(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_splitting.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_splitting -> update PMode splitting lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_SPLITTING (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_splitting(i).OLD_ID,
                            migr_pks_pm_splitting(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_splitting -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_SPLITTING (ID_PK, NAME, FRAGMENT_SIZE, COMPRESSION,
                                                          COMPRESSION_ALGORITHM, JOIN_INTERVAL, FK_BUSINESSPROCESS,
                                                          CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_splitting(i).ID_PK,
                                migr_pm_splitting(i).NAME,
                                migr_pm_splitting(i).FRAGMENT_SIZE,
                                migr_pm_splitting(i).COMPRESSION,
                                migr_pm_splitting(i).COMPRESSION_ALGORITHM,
                                migr_pm_splitting(i).JOIN_INTERVAL,
                                migr_pm_splitting(i).FK_BUSINESSPROCESS,
                                migr_pm_splitting(i).CREATION_TIME,
                                migr_pm_splitting(i).CREATED_BY,
                                migr_pm_splitting(i).MODIFICATION_TIME,
                                migr_pm_splitting(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_splitting -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_splitting(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_splitting.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_splitting;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_splitting;

    PROCEDURE migrate_pm_leg IS
        v_tab VARCHAR2(30) := 'TB_PM_LEG';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_LEG';

        v_id_pk NUMBER;

        CURSOR c_pm_leg IS
            SELECT PL.ID_PK,
                   PL.COMPRESS_PAYLOADS,
                   PL.NAME,
                   (SELECT MPKSPA.NEW_ID
                    FROM MIGR_TB_PKS_PM_ACTION MPKSPA
                    WHERE MPKSPA.OLD_ID = PL.FK_ACTION) AS FK_ACTION,
                   (SELECT MPKSPM.NEW_ID
                    FROM MIGR_TB_PKS_PM_MPC MPKSPM
                    WHERE MPKSPM.OLD_ID = PL.FK_MPC) AS FK_MPC,
                   (SELECT MPKSPEH.NEW_ID
                    FROM MIGR_TB_PKS_PM_ERROR_HANDLING MPKSPEH
                    WHERE MPKSPEH.OLD_ID = PL.FK_ERROR_HANDLING) AS FK_ERROR_HANDLING,
                   (SELECT MPKSPPP.NEW_ID
                    FROM MIGR_TB_PKS_PM_PAYLOAD_PROF MPKSPPP
                    WHERE MPKSPPP.OLD_ID = PL.FK_PAYLOAD_PROFILE) AS FK_PAYLOAD_PROFILE,
                   (SELECT MPKSPMPS.NEW_ID
                    FROM MIGR_TB_PKS_PM_MSG_PROP_SET MPKSPMPS
                    WHERE MPKSPMPS.OLD_ID = PL.FK_PROPERTY_SET) AS FK_PROPERTY_SET,
                   (SELECT MPKSPRA.NEW_ID
                    FROM MIGR_TB_PKS_PM_RECEPTN_AWARNS MPKSPRA
                    WHERE MPKSPRA.OLD_ID = PL.FK_RECEPTION_AWARENESS) AS FK_RECEPTION_AWARENESS,
                   (SELECT MPKSPR.NEW_ID
                    FROM MIGR_TB_PKS_PM_RELIABILITY MPKSPR
                    WHERE MPKSPR.OLD_ID = PL.FK_RELIABILITY) AS FK_RELIABILITY,
                   (SELECT MPKSPSEC.NEW_ID
                    FROM MIGR_TB_PKS_PM_SECURITY MPKSPSEC
                    WHERE MPKSPSEC.OLD_ID = PL.FK_SECURITY) AS FK_SECURITY,
                   (SELECT MPKSPSER.NEW_ID
                    FROM MIGR_TB_PKS_PM_SERVICE MPKSPSER
                    WHERE MPKSPSER.OLD_ID = PL.FK_SERVICE) AS FK_SERVICE,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PL.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   (SELECT MPKSPS.NEW_ID
                    FROM MIGR_TB_PKS_PM_SPLITTING MPKSPS
                    WHERE MPKSPS.OLD_ID = PL.FK_SPLITTING) AS FK_SPLITTING,
                   PL.CREATION_TIME,
                   PL.CREATED_BY,
                   PL.MODIFICATION_TIME,
                   PL.MODIFIED_BY
            FROM TB_PM_LEG PL;

        TYPE T_PM_LEG IS TABLE OF c_pm_leg%rowtype;
        pm_leg T_PM_LEG;

        TYPE T_MIGR_PM_LEG IS TABLE OF MIGR_TB_PM_LEG%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_leg T_MIGR_PM_LEG;

        TYPE T_MIGR_PKS_PM_LEG IS TABLE OF MIGR_TB_PKS_PM_LEG%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_leg T_MIGR_PKS_PM_LEG;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_leg;
        LOOP
            FETCH c_pm_leg BULK COLLECT INTO pm_leg LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_leg.COUNT = 0;

            migr_pm_leg := T_MIGR_PM_LEG();
            migr_pks_pm_leg := T_MIGR_PKS_PM_LEG();

            FOR i IN pm_leg.FIRST .. pm_leg.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_leg(i).ID_PK, pm_leg(i).CREATION_TIME);

                    migr_pks_pm_leg(i).OLD_ID := pm_leg(i).ID_PK;
                    migr_pks_pm_leg(i).NEW_ID := v_id_pk;

                    migr_pm_leg(i).ID_PK := v_id_pk;
                    migr_pm_leg(i).COMPRESS_PAYLOADS := pm_leg(i).COMPRESS_PAYLOADS;
                    migr_pm_leg(i).NAME := pm_leg(i).NAME;
                    migr_pm_leg(i).FK_ACTION := pm_leg(i).FK_ACTION;
                    migr_pm_leg(i).FK_MPC := pm_leg(i).FK_MPC;
                    migr_pm_leg(i).FK_ERROR_HANDLING := pm_leg(i).FK_ERROR_HANDLING;
                    migr_pm_leg(i).FK_PAYLOAD_PROFILE := pm_leg(i).FK_PAYLOAD_PROFILE;
                    migr_pm_leg(i).FK_PROPERTY_SET := pm_leg(i).FK_PROPERTY_SET;
                    migr_pm_leg(i).FK_RECEPTION_AWARENESS := pm_leg(i).FK_RECEPTION_AWARENESS;
                    migr_pm_leg(i).FK_RELIABILITY := pm_leg(i).FK_RELIABILITY;
                    migr_pm_leg(i).FK_SECURITY := pm_leg(i).FK_SECURITY;
                    migr_pm_leg(i).FK_SERVICE := pm_leg(i).FK_SERVICE;
                    migr_pm_leg(i).FK_BUSINESSPROCESS := pm_leg(i).FK_BUSINESSPROCESS;
                    migr_pm_leg(i).FK_SPLITTING := pm_leg(i).FK_SPLITTING;
                    migr_pm_leg(i).CREATION_TIME := pm_leg(i).CREATION_TIME;
                    migr_pm_leg(i).CREATED_BY := pm_leg(i).CREATED_BY;
                    migr_pm_leg(i).MODIFICATION_TIME := pm_leg(i).MODIFICATION_TIME;
                    migr_pm_leg(i).MODIFIED_BY := pm_leg(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_leg.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_leg -> update PMode leg lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_LEG (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_leg(i).OLD_ID,
                            migr_pks_pm_leg(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_leg -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_LEG (ID_PK, COMPRESS_PAYLOADS, NAME, FK_ACTION, FK_MPC,
                                                    FK_ERROR_HANDLING, FK_PAYLOAD_PROFILE, FK_PROPERTY_SET,
                                                    FK_RECEPTION_AWARENESS, FK_RELIABILITY, FK_SECURITY, FK_SERVICE,
                                                    FK_BUSINESSPROCESS, FK_SPLITTING, CREATION_TIME, CREATED_BY,
                                                    MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_leg(i).ID_PK,
                                migr_pm_leg(i).COMPRESS_PAYLOADS,
                                migr_pm_leg(i).NAME,
                                migr_pm_leg(i).FK_ACTION,
                                migr_pm_leg(i).FK_MPC,
                                migr_pm_leg(i).FK_ERROR_HANDLING,
                                migr_pm_leg(i).FK_PAYLOAD_PROFILE,
                                migr_pm_leg(i).FK_PROPERTY_SET,
                                migr_pm_leg(i).FK_RECEPTION_AWARENESS,
                                migr_pm_leg(i).FK_RELIABILITY,
                                migr_pm_leg(i).FK_SECURITY,
                                migr_pm_leg(i).FK_SERVICE,
                                migr_pm_leg(i).FK_BUSINESSPROCESS,
                                migr_pm_leg(i).FK_SPLITTING,
                                migr_pm_leg(i).CREATION_TIME,
                                migr_pm_leg(i).CREATED_BY,
                                migr_pm_leg(i).MODIFICATION_TIME,
                                migr_pm_leg(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_leg -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_leg(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_leg.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_leg;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_leg;

    PROCEDURE migrate_pm_leg_mpc IS
        v_tab VARCHAR2(30) := 'TB_PM_LEG_MPC';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_LEG_MPC';

        CURSOR c_pm_leg_mpc IS
            SELECT (SELECT MPKSPL.NEW_ID
                    FROM MIGR_TB_PKS_PM_LEG MPKSPL
                    WHERE MPKSPL.OLD_ID = PLM.LEGCONFIGURATION_ID_PK) AS LEGCONFIGURATION_ID_PK,
                   (SELECT MPKSPM.NEW_ID
                    FROM MIGR_TB_PKS_PM_MPC MPKSPM
                    WHERE MPKSPM.OLD_ID = PLM.PARTYMPCMAP_ID_PK) AS PARTYMPCMAP_ID_PK,
                   (SELECT MPKSPP.NEW_ID
                    FROM MIGR_TB_PKS_PM_PARTY MPKSPP
                    WHERE MPKSPP.OLD_ID = PLM.PARTYMPCMAP_KEY) AS PARTYMPCMAP_KEY,
                   PLM.CREATION_TIME,
                   PLM.CREATED_BY,
                   PLM.MODIFICATION_TIME,
                   PLM.MODIFIED_BY
            FROM TB_PM_LEG_MPC PLM;

        TYPE T_PM_LEG_MPC IS TABLE OF c_pm_leg_mpc%rowtype;
        pm_leg_mpc T_PM_LEG_MPC;

        TYPE T_MIGR_PM_LEG_MPC IS TABLE OF MIGR_TB_PM_LEG_MPC%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_leg_mpc T_MIGR_PM_LEG_MPC;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_leg_mpc;
        LOOP
            FETCH c_pm_leg_mpc BULK COLLECT INTO pm_leg_mpc LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_leg_mpc.COUNT = 0;

            migr_pm_leg_mpc := T_MIGR_PM_LEG_MPC();

            FOR i IN pm_leg_mpc.FIRST .. pm_leg_mpc.LAST
                LOOP
                    migr_pm_leg_mpc(i).LEGCONFIGURATION_ID_PK := pm_leg_mpc(i).LEGCONFIGURATION_ID_PK;
                    migr_pm_leg_mpc(i).PARTYMPCMAP_ID_PK := pm_leg_mpc(i).PARTYMPCMAP_ID_PK;
                    migr_pm_leg_mpc(i).PARTYMPCMAP_KEY := pm_leg_mpc(i).PARTYMPCMAP_KEY;
                    migr_pm_leg_mpc(i).CREATION_TIME := pm_leg_mpc(i).CREATION_TIME;
                    migr_pm_leg_mpc(i).CREATED_BY := pm_leg_mpc(i).CREATED_BY;
                    migr_pm_leg_mpc(i).MODIFICATION_TIME := pm_leg_mpc(i).MODIFICATION_TIME;
                    migr_pm_leg_mpc(i).MODIFIED_BY := pm_leg_mpc(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_leg_mpc.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_pm_leg_mpc -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_LEG_MPC (LEGCONFIGURATION_ID_PK, PARTYMPCMAP_ID_PK, PARTYMPCMAP_KEY,
                                                        CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_leg_mpc(i).LEGCONFIGURATION_ID_PK,
                                migr_pm_leg_mpc(i).PARTYMPCMAP_ID_PK,
                                migr_pm_leg_mpc(i).PARTYMPCMAP_KEY,
                                migr_pm_leg_mpc(i).CREATION_TIME,
                                migr_pm_leg_mpc(i).CREATED_BY,
                                migr_pm_leg_mpc(i).MODIFICATION_TIME,
                                migr_pm_leg_mpc(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_leg_mpc -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having LEGCONFIGURATION_ID_PK '
                                        || migr_pm_leg_mpc(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).LEGCONFIGURATION_ID_PK
                                        || '  and PARTYMPCMAP_ID_PK '
                                        || migr_pm_leg_mpc(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).PARTYMPCMAP_ID_PK
                                        || '  and PARTYMPCMAP_KEY '
                                        || migr_pm_leg_mpc(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).PARTYMPCMAP_KEY);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_leg_mpc.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_leg_mpc;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_leg_mpc;

    PROCEDURE migrate_pm_process IS
        v_tab VARCHAR2(30) := 'TB_PM_PROCESS';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PROCESS';

        v_id_pk NUMBER;

        CURSOR c_pm_process IS
            SELECT PP.ID_PK,
                   PP.NAME,
                   (SELECT MPKSPA.NEW_ID
                    FROM MIGR_TB_PKS_PM_AGREEMENT MPKSPA
                    WHERE MPKSPA.OLD_ID = PP.FK_AGREEMENT) AS FK_AGREEMENT,
                   (SELECT MPKSPIR.NEW_ID
                    FROM MIGR_TB_PKS_PM_ROLE MPKSPIR
                    WHERE MPKSPIR.OLD_ID = PP.FK_INITIATOR_ROLE) AS FK_INITIATOR_ROLE,
                   (SELECT MPKSPM.NEW_ID
                    FROM MIGR_TB_PKS_PM_MEP MPKSPM
                    WHERE MPKSPM.OLD_ID = PP.FK_MEP) AS FK_MEP,
                   (SELECT MPKSPMB.NEW_ID
                    FROM MIGR_TB_PKS_PM_MEP_BINDING MPKSPMB
                    WHERE MPKSPMB.OLD_ID = PP.FK_MEP_BINDING) AS FK_MEP_BINDING,
                   (SELECT MPKSPRR.NEW_ID
                    FROM MIGR_TB_PKS_PM_ROLE MPKSPRR
                    WHERE MPKSPRR.OLD_ID = PP.FK_RESPONDER_ROLE) AS FK_RESPONDER_ROLE,
                   (SELECT MPKSPBP.NEW_ID
                    FROM MIGR_TB_PKS_PM_BUSINESS_PROC MPKSPBP
                    WHERE MPKSPBP.OLD_ID = PP.FK_BUSINESSPROCESS) AS FK_BUSINESSPROCESS,
                   PP.USE_DYNAMIC_INITIATOR,
                   PP.USE_DYNAMIC_RESPONDER,
                   PP.CREATION_TIME,
                   PP.CREATED_BY,
                   PP.MODIFICATION_TIME,
                   PP.MODIFIED_BY
            FROM TB_PM_PROCESS PP;

        TYPE T_PM_PROCESS IS TABLE OF c_pm_process%rowtype;
        pm_process T_PM_PROCESS;

        TYPE T_MIGR_PM_PROCESS IS TABLE OF MIGR_TB_PM_PROCESS%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_process T_MIGR_PM_PROCESS;

        TYPE T_MIGR_PKS_PM_PROCESS IS TABLE OF MIGR_TB_PKS_PM_PROCESS%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_process T_MIGR_PKS_PM_PROCESS;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_process;
        LOOP
            FETCH c_pm_process BULK COLLECT INTO pm_process LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_process.COUNT = 0;

            migr_pm_process := T_MIGR_PM_PROCESS();
            migr_pks_pm_process := T_MIGR_PKS_PM_PROCESS();

            FOR i IN pm_process.FIRST .. pm_process.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_process(i).ID_PK, pm_process(i).CREATION_TIME);

                    migr_pks_pm_process(i).OLD_ID := pm_process(i).ID_PK;
                    migr_pks_pm_process(i).NEW_ID := v_id_pk;

                    migr_pm_process(i).ID_PK := v_id_pk;
                    migr_pm_process(i).NAME := pm_process(i).NAME;
                    migr_pm_process(i).FK_AGREEMENT := pm_process(i).FK_AGREEMENT;
                    migr_pm_process(i).FK_INITIATOR_ROLE := pm_process(i).FK_INITIATOR_ROLE;
                    migr_pm_process(i).FK_MEP := pm_process(i).FK_MEP;
                    migr_pm_process(i).FK_MEP_BINDING := pm_process(i).FK_MEP_BINDING;
                    migr_pm_process(i).FK_RESPONDER_ROLE := pm_process(i).FK_RESPONDER_ROLE;
                    migr_pm_process(i).FK_BUSINESSPROCESS := pm_process(i).FK_BUSINESSPROCESS;
                    migr_pm_process(i).USE_DYNAMIC_INITIATOR := pm_process(i).USE_DYNAMIC_INITIATOR;
                    migr_pm_process(i).USE_DYNAMIC_RESPONDER := pm_process(i).USE_DYNAMIC_RESPONDER;
                    migr_pm_process(i).CREATION_TIME := pm_process(i).CREATION_TIME;
                    migr_pm_process(i).CREATED_BY := pm_process(i).CREATED_BY;
                    migr_pm_process(i).MODIFICATION_TIME := pm_process(i).MODIFICATION_TIME;
                    migr_pm_process(i).MODIFIED_BY := pm_process(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_process.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_process -> update PMode process lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_PROCESS (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_process(i).OLD_ID,
                            migr_pks_pm_process(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_process -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_PROCESS (ID_PK, NAME, FK_AGREEMENT, FK_INITIATOR_ROLE, FK_MEP,
                                                        FK_MEP_BINDING, FK_RESPONDER_ROLE, FK_BUSINESSPROCESS,
                                                        USE_DYNAMIC_INITIATOR, USE_DYNAMIC_RESPONDER, CREATION_TIME,
                                                        CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_process(i).ID_PK,
                                migr_pm_process(i).NAME,
                                migr_pm_process(i).FK_AGREEMENT,
                                migr_pm_process(i).FK_INITIATOR_ROLE,
                                migr_pm_process(i).FK_MEP,
                                migr_pm_process(i).FK_MEP_BINDING,
                                migr_pm_process(i).FK_RESPONDER_ROLE,
                                migr_pm_process(i).FK_BUSINESSPROCESS,
                                migr_pm_process(i).USE_DYNAMIC_INITIATOR,
                                migr_pm_process(i).USE_DYNAMIC_RESPONDER,
                                migr_pm_process(i).CREATION_TIME,
                                migr_pm_process(i).CREATED_BY,
                                migr_pm_process(i).MODIFICATION_TIME,
                                migr_pm_process(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_process -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_process(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_process.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_process;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_process;

    PROCEDURE migrate_pm_join_process_init_party IS
        v_tab VARCHAR2(30) := 'TB_PM_JOIN_PROCESS_INIT_PARTY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_JOIN_PROC_INI_PARTY';

        CURSOR c_pm_join_process_init_party IS
            SELECT (SELECT MPKSPPR.NEW_ID
                    FROM MIGR_TB_PKS_PM_PROCESS MPKSPPR
                    WHERE MPKSPPR.OLD_ID = PJPIP.PROCESS_FK) AS PROCESS_FK,
                   (SELECT MPKSPPA.NEW_ID
                    FROM MIGR_TB_PKS_PM_PARTY MPKSPPA
                    WHERE MPKSPPA.OLD_ID = PJPIP.PARTY_FK) AS PARTY_FK,
                   PJPIP.CREATION_TIME,
                   PJPIP.CREATED_BY,
                   PJPIP.MODIFICATION_TIME,
                   PJPIP.MODIFIED_BY
            FROM TB_PM_JOIN_PROCESS_INIT_PARTY PJPIP;

        TYPE T_PM_JOIN_PROCESS_INIT_PARTY IS TABLE OF c_pm_join_process_init_party%rowtype;
        pm_join_process_init_party T_PM_JOIN_PROCESS_INIT_PARTY;

        TYPE T_MIGR_PM_JOIN_PROC_INI_PARTY IS TABLE OF MIGR_TB_PM_JOIN_PROC_INI_PARTY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_join_proc_ini_party T_MIGR_PM_JOIN_PROC_INI_PARTY;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_join_process_init_party;
        LOOP
            FETCH c_pm_join_process_init_party BULK COLLECT INTO pm_join_process_init_party LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_join_process_init_party.COUNT = 0;

            migr_pm_join_proc_ini_party := T_MIGR_PM_JOIN_PROC_INI_PARTY();

            FOR i IN pm_join_process_init_party.FIRST .. pm_join_process_init_party.LAST
                LOOP
                    migr_pm_join_proc_ini_party(i).PROCESS_FK := pm_join_process_init_party(i).PROCESS_FK;
                    migr_pm_join_proc_ini_party(i).PARTY_FK := pm_join_process_init_party(i).PARTY_FK;
                    migr_pm_join_proc_ini_party(i).CREATION_TIME := pm_join_process_init_party(i).CREATION_TIME;
                    migr_pm_join_proc_ini_party(i).CREATED_BY := pm_join_process_init_party(i).CREATED_BY;
                    migr_pm_join_proc_ini_party(i).MODIFICATION_TIME := pm_join_process_init_party(i).MODIFICATION_TIME;
                    migr_pm_join_proc_ini_party(i).MODIFIED_BY := pm_join_process_init_party(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_join_proc_ini_party.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_pm_join_process_init_party -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_JOIN_PROC_INI_PARTY (PROCESS_FK, PARTY_FK, CREATION_TIME, CREATED_BY,
                                                                    MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_join_proc_ini_party(i).PROCESS_FK,
                                migr_pm_join_proc_ini_party(i).PARTY_FK,
                                migr_pm_join_proc_ini_party(i).CREATION_TIME,
                                migr_pm_join_proc_ini_party(i).CREATED_BY,
                                migr_pm_join_proc_ini_party(i).MODIFICATION_TIME,
                                migr_pm_join_proc_ini_party(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_join_process_init_party -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having PROCESS_FK '
                                        || migr_pm_join_proc_ini_party(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).PROCESS_FK
                                        || '  and PARTY_FK '
                                        || migr_pm_join_proc_ini_party(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).PARTY_FK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_join_process_init_party.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_join_process_init_party;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_join_process_init_party;

    PROCEDURE migrate_pm_join_process_leg IS
        v_tab VARCHAR2(30) := 'TB_PM_JOIN_PROCESS_LEG';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_JOIN_PROCESS_LEG';

        CURSOR c_pm_join_process_leg IS
            SELECT (SELECT MPKSPP.NEW_ID
                    FROM MIGR_TB_PKS_PM_PROCESS MPKSPP
                    WHERE MPKSPP.OLD_ID = PJPL.PROCESS_FK) AS PROCESS_FK,
                   (SELECT MPKSPL.NEW_ID
                    FROM MIGR_TB_PKS_PM_LEG MPKSPL
                    WHERE MPKSPL.OLD_ID = PJPL.LEG_FK) AS LEG_FK,
                   PJPL.CREATION_TIME,
                   PJPL.CREATED_BY,
                   PJPL.MODIFICATION_TIME,
                   PJPL.MODIFIED_BY
            FROM TB_PM_JOIN_PROCESS_LEG PJPL;

        TYPE T_PM_JOIN_PROCESS_LEG IS TABLE OF c_pm_join_process_leg%rowtype;
        pm_join_process_leg T_PM_JOIN_PROCESS_LEG;

        TYPE T_MIGR_PM_JOIN_PROCESS_LEG IS TABLE OF MIGR_TB_PM_JOIN_PROCESS_LEG%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_join_process_leg T_MIGR_PM_JOIN_PROCESS_LEG;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_join_process_leg;
        LOOP
            FETCH c_pm_join_process_leg BULK COLLECT INTO pm_join_process_leg LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_join_process_leg.COUNT = 0;

            migr_pm_join_process_leg := T_MIGR_PM_JOIN_PROCESS_LEG();

            FOR i IN pm_join_process_leg.FIRST .. pm_join_process_leg.LAST
                LOOP
                    migr_pm_join_process_leg(i).PROCESS_FK := pm_join_process_leg(i).PROCESS_FK;
                    migr_pm_join_process_leg(i).LEG_FK := pm_join_process_leg(i).LEG_FK;
                    migr_pm_join_process_leg(i).CREATION_TIME := pm_join_process_leg(i).CREATION_TIME;
                    migr_pm_join_process_leg(i).CREATED_BY := pm_join_process_leg(i).CREATED_BY;
                    migr_pm_join_process_leg(i).MODIFICATION_TIME := pm_join_process_leg(i).MODIFICATION_TIME;
                    migr_pm_join_process_leg(i).MODIFIED_BY := pm_join_process_leg(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_join_process_leg.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_pm_join_process_leg -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_JOIN_PROCESS_LEG (PROCESS_FK, LEG_FK, CREATION_TIME, CREATED_BY,
                                                                 MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_join_process_leg(i).PROCESS_FK,
                                migr_pm_join_process_leg(i).LEG_FK,
                                migr_pm_join_process_leg(i).CREATION_TIME,
                                migr_pm_join_process_leg(i).CREATED_BY,
                                migr_pm_join_process_leg(i).MODIFICATION_TIME,
                                migr_pm_join_process_leg(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_join_process_leg -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having PROCESS_FK '
                                        || migr_pm_join_process_leg(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).PROCESS_FK
                                        || '  and LEG_FK '
                                        || migr_pm_join_process_leg(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).LEG_FK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_join_process_leg.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_join_process_leg;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_join_process_leg;

    PROCEDURE migrate_pm_join_process_resp_party IS
        v_tab VARCHAR2(30) := 'TB_PM_JOIN_PROCESS_RESP_PARTY';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_JOIN_PROC_RSP_PARTY';

        CURSOR c_pm_join_process_resp_party IS
            SELECT (SELECT MPKSPPR.NEW_ID
                    FROM MIGR_TB_PKS_PM_PROCESS MPKSPPR
                    WHERE MPKSPPR.OLD_ID = PJPRP.PROCESS_FK) AS PROCESS_FK,
                   (SELECT MPKSPPA.NEW_ID
                    FROM MIGR_TB_PKS_PM_PARTY MPKSPPA
                    WHERE MPKSPPA.OLD_ID = PJPRP.PARTY_FK) AS PARTY_FK,
                   PJPRP.CREATION_TIME,
                   PJPRP.CREATED_BY,
                   PJPRP.MODIFICATION_TIME,
                   PJPRP.MODIFIED_BY
            FROM TB_PM_JOIN_PROCESS_RESP_PARTY PJPRP;

        TYPE T_PM_JOIN_PROCESS_RESP_PARTY IS TABLE OF c_pm_join_process_resp_party%rowtype;
        pm_join_process_resp_party T_PM_JOIN_PROCESS_RESP_PARTY;

        TYPE T_MIGR_PM_JOIN_PROC_RSP_PARTY IS TABLE OF MIGR_TB_PM_JOIN_PROC_RSP_PARTY%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_join_proc_rsp_party T_MIGR_PM_JOIN_PROC_RSP_PARTY;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_join_process_resp_party;
        LOOP
            FETCH c_pm_join_process_resp_party BULK COLLECT INTO pm_join_process_resp_party LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_join_process_resp_party.COUNT = 0;

            migr_pm_join_proc_rsp_party := T_MIGR_PM_JOIN_PROC_RSP_PARTY();

            FOR i IN pm_join_process_resp_party.FIRST .. pm_join_process_resp_party.LAST
                LOOP
                    migr_pm_join_proc_rsp_party(i).PROCESS_FK := pm_join_process_resp_party(i).PROCESS_FK;
                    migr_pm_join_proc_rsp_party(i).PARTY_FK := pm_join_process_resp_party(i).PARTY_FK;
                    migr_pm_join_proc_rsp_party(i).CREATION_TIME := pm_join_process_resp_party(i).CREATION_TIME;
                    migr_pm_join_proc_rsp_party(i).CREATED_BY := pm_join_process_resp_party(i).CREATED_BY;
                    migr_pm_join_proc_rsp_party(i).MODIFICATION_TIME := pm_join_process_resp_party(i).MODIFICATION_TIME;
                    migr_pm_join_proc_rsp_party(i).MODIFIED_BY := pm_join_process_resp_party(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_join_proc_rsp_party.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                EXIT WHEN v_start > v_last;


                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_pm_join_process_resp_party -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_JOIN_PROC_RSP_PARTY (PROCESS_FK, PARTY_FK, CREATION_TIME, CREATED_BY,
                                                                    MODIFICATION_TIME, MODIFIED_BY)
                        VALUES (migr_pm_join_proc_rsp_party(i).PROCESS_FK,
                                migr_pm_join_proc_rsp_party(i).PARTY_FK,
                                migr_pm_join_proc_rsp_party(i).CREATION_TIME,
                                migr_pm_join_proc_rsp_party(i).CREATED_BY,
                                migr_pm_join_proc_rsp_party(i).MODIFICATION_TIME,
                                migr_pm_join_proc_rsp_party(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_join_process_resp_party -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having PROCESS_FK '
                                        || migr_pm_join_proc_rsp_party(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).PROCESS_FK
                                        || '  and PARTY_FK '
                                        || migr_pm_join_proc_rsp_party(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).PARTY_FK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_join_process_resp_party.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_join_process_resp_party;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_join_process_resp_party;

    PROCEDURE migrate_pm_configuration_raw IS
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

        TYPE T_MIGR_PM_CONFIGURATION_RAW IS TABLE OF MIGR_TB_PM_CONFIGURATION_RAW%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_configuration_raw T_MIGR_PM_CONFIGURATION_RAW;

        TYPE T_MIGR_PKS_PM_CONF_RAW IS TABLE OF MIGR_TB_PKS_PM_CONF_RAW%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pks_pm_conf_raw T_MIGR_PKS_PM_CONF_RAW;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_configuration_raw;
        LOOP
            FETCH c_pm_configuration_raw BULK COLLECT INTO pm_configuration_raw LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_configuration_raw.COUNT = 0;

            migr_pm_configuration_raw := T_MIGR_PM_CONFIGURATION_RAW();
            migr_pks_pm_conf_raw := T_MIGR_PKS_PM_CONF_RAW();

            FOR i IN pm_configuration_raw.FIRST .. pm_configuration_raw.LAST
                LOOP
                    v_id_pk := generate_scalable_seq(pm_configuration_raw(i).ID_PK, pm_configuration_raw(i).CREATION_TIME);

                    migr_pks_pm_conf_raw(i).OLD_ID := pm_configuration_raw(i).ID_PK;
                    migr_pks_pm_conf_raw(i).NEW_ID := v_id_pk;

                    migr_pm_configuration_raw(i).ID_PK := v_id_pk;
                    migr_pm_configuration_raw(i).CONFIGURATION_DATE := pm_configuration_raw(i).CONFIGURATION_DATE;
                    migr_pm_configuration_raw(i).XML := pm_configuration_raw(i).XML;
                    migr_pm_configuration_raw(i).DESCRIPTION := pm_configuration_raw(i).DESCRIPTION;
                    migr_pm_configuration_raw(i).CREATION_TIME := pm_configuration_raw(i).CREATION_TIME;
                    migr_pm_configuration_raw(i).CREATED_BY := pm_configuration_raw(i).CREATED_BY;
                    migr_pm_configuration_raw(i).MODIFICATION_TIME := pm_configuration_raw(i).MODIFICATION_TIME;
                    migr_pm_configuration_raw(i).MODIFIED_BY := pm_configuration_raw(i).MODIFIED_BY;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_configuration_raw.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                log_verbose('migrate_pm_configuration_raw -> update PMode configuration raw lookup table: ' || v_start || '-' || v_end);
                FORALL i IN v_start .. v_end
                    INSERT INTO MIGR_TB_PKS_PM_CONF_RAW (OLD_ID, NEW_ID)
                    VALUES (migr_pks_pm_conf_raw(i).OLD_ID,
                            migr_pks_pm_conf_raw(i).NEW_ID);
                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                BEGIN
                    log_verbose('migrate_pm_configuration_raw -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_CONFIGURATION_RAW (ID_PK, CONFIGURATION_DATE, XML, DESCRIPTION,
                                                                  CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                                  MODIFIED_BY)
                        VALUES (migr_pm_configuration_raw(i).ID_PK,
                                migr_pm_configuration_raw(i).CONFIGURATION_DATE,
                                migr_pm_configuration_raw(i).XML,
                                migr_pm_configuration_raw(i).DESCRIPTION,
                                migr_pm_configuration_raw(i).CREATION_TIME,
                                migr_pm_configuration_raw(i).CREATED_BY,
                                migr_pm_configuration_raw(i).MODIFICATION_TIME,
                                migr_pm_configuration_raw(i).MODIFIED_BY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_configuration_raw -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_pm_configuration_raw(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_configuration_raw.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_configuration_raw;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_configuration_raw;

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

    PROCEDURE migrate_ws_plugin_tb_message_log IS
        v_tab VARCHAR2(30) := 'WS_PLUGIN_TB_MESSAGE_LOG';
        v_tab_new VARCHAR2(30) := 'MIGR_WS_PLUGIN_TB_MESSAGE_LOG';

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

        TYPE T_MIGR_WS_PLUGIN_MESSAGE_LOG IS TABLE OF MIGR_WS_PLUGIN_TB_MESSAGE_LOG%ROWTYPE INDEX BY PLS_INTEGER;
        migr_ws_plugin_message_log T_MIGR_WS_PLUGIN_MESSAGE_LOG;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_ws_plugin_message_log;
        LOOP
            FETCH c_ws_plugin_message_log BULK COLLECT INTO ws_plugin_message_log LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN ws_plugin_message_log.COUNT = 0;

            migr_ws_plugin_message_log := T_MIGR_WS_PLUGIN_MESSAGE_LOG();

            FOR i IN ws_plugin_message_log.FIRST .. ws_plugin_message_log.LAST
                LOOP
                    migr_ws_plugin_message_log(i).ID_PK := generate_scalable_seq(ws_plugin_message_log(i).ID_PK, SYSDATE);
                    migr_ws_plugin_message_log(i).MESSAGE_ID := ws_plugin_message_log(i).MESSAGE_ID;
                    migr_ws_plugin_message_log(i).CONVERSATION_ID := ws_plugin_message_log(i).CONVERSATION_ID;
                    migr_ws_plugin_message_log(i).REF_TO_MESSAGE_ID := ws_plugin_message_log(i).REF_TO_MESSAGE_ID;
                    migr_ws_plugin_message_log(i).FROM_PARTY_ID := ws_plugin_message_log(i).FROM_PARTY_ID;
                    migr_ws_plugin_message_log(i).FINAL_RECIPIENT := ws_plugin_message_log(i).FINAL_RECIPIENT;
                    migr_ws_plugin_message_log(i).ORIGINAL_SENDER := ws_plugin_message_log(i).ORIGINAL_SENDER;
                    migr_ws_plugin_message_log(i).RECEIVED := ws_plugin_message_log(i).RECEIVED;
                END LOOP;

            v_start := 1;
            v_last := migr_ws_plugin_message_log.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_ws_plugin_message_log -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_WS_PLUGIN_TB_MESSAGE_LOG (ID_PK, MESSAGE_ID, CONVERSATION_ID,
                                                                   REF_TO_MESSAGE_ID, FROM_PARTY_ID, FINAL_RECIPIENT,
                                                                   ORIGINAL_SENDER, RECEIVED)
                        VALUES (migr_ws_plugin_message_log(i).ID_PK,
                                migr_ws_plugin_message_log(i).MESSAGE_ID,
                                migr_ws_plugin_message_log(i).CONVERSATION_ID,
                                migr_ws_plugin_message_log(i).REF_TO_MESSAGE_ID,
                                migr_ws_plugin_message_log(i).FROM_PARTY_ID,
                                migr_ws_plugin_message_log(i).FINAL_RECIPIENT,
                                migr_ws_plugin_message_log(i).ORIGINAL_SENDER,
                                migr_ws_plugin_message_log(i).RECEIVED);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_ws_plugin_message_log -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration entry having ID_PK '
                                        || migr_ws_plugin_message_log(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || ws_plugin_message_log.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_ws_plugin_message_log;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_ws_plugin_tb_message_log;

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

    PROCEDURE migrate_authentication_entry_aud(missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_AUTHENTICATION_ENTRY_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_AUTH_ENTRY_AUD';

        CURSOR c_authentication_entry_aud IS
            SELECT (SELECT MPKSAE.NEW_ID
                    FROM MIGR_TB_PKS_AUTH_ENTRY MPKSAE
                    WHERE MPKSAE.OLD_ID = AEA.ID_PK) AS ID_PK,
                   AEA.ID_PK AS ORIGINAL_ID_PK,
                   (SELECT MPKSRI.NEW_ID
                    FROM MIGR_TB_PKS_REV_INFO MPKSRI
                    WHERE MPKSRI.OLD_ID = AEA.REV) AS REV,
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

        TYPE T_MIGR_AUTH_ENTRY_AUD IS TABLE OF MIGR_TB_AUTH_ENTRY_AUD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_auth_entry_aud T_MIGR_AUTH_ENTRY_AUD;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_authentication_entry_aud;
        LOOP
            FETCH c_authentication_entry_aud BULK COLLECT INTO authentication_entry_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN authentication_entry_aud.COUNT = 0;

            migr_auth_entry_aud := T_MIGR_AUTH_ENTRY_AUD();

            FOR i IN authentication_entry_aud.FIRST .. authentication_entry_aud.LAST
                LOOP
                    migr_auth_entry_aud(i).ID_PK := NVL(authentication_entry_aud(i).ID_PK, generate_scalable_seq(authentication_entry_aud(i).ORIGINAL_ID_PK, missing_entity_date_prefix));
                    migr_auth_entry_aud(i).REV := authentication_entry_aud(i).REV;
                    migr_auth_entry_aud(i).REVTYPE := authentication_entry_aud(i).REVTYPE;
                    migr_auth_entry_aud(i).CERTIFICATE_ID := authentication_entry_aud(i).CERTIFICATE_ID;
                    migr_auth_entry_aud(i).CERTIFICATEID_MOD := authentication_entry_aud(i).CERTIFICATEID_MOD;
                    migr_auth_entry_aud(i).USERNAME := authentication_entry_aud(i).USERNAME;
                    migr_auth_entry_aud(i).USERNAME_MOD := authentication_entry_aud(i).USERNAME_MOD;
                    migr_auth_entry_aud(i).PASSWD := authentication_entry_aud(i).PASSWD;
                    migr_auth_entry_aud(i).PASSWORD_MOD := authentication_entry_aud(i).PASSWORD_MOD;
                    migr_auth_entry_aud(i).AUTH_ROLES := authentication_entry_aud(i).AUTH_ROLES;
                    migr_auth_entry_aud(i).AUTHROLES_MOD := authentication_entry_aud(i).AUTHROLES_MOD;
                    migr_auth_entry_aud(i).ORIGINAL_USER := authentication_entry_aud(i).ORIGINAL_USER;
                    migr_auth_entry_aud(i).ORIGINALUSER_MOD := authentication_entry_aud(i).ORIGINALUSER_MOD;
                    migr_auth_entry_aud(i).BACKEND := authentication_entry_aud(i).BACKEND;
                    migr_auth_entry_aud(i).BACKEND_MOD := authentication_entry_aud(i).BACKEND_MOD;
                    migr_auth_entry_aud(i).USER_ENABLED := authentication_entry_aud(i).USER_ENABLED;
                    migr_auth_entry_aud(i).ACTIVE_MOD := authentication_entry_aud(i).ACTIVE_MOD;
                    migr_auth_entry_aud(i).PASSWORD_CHANGE_DATE := authentication_entry_aud(i).PASSWORD_CHANGE_DATE;
                    migr_auth_entry_aud(i).PASSWORDCHANGEDATE_MOD := authentication_entry_aud(i).PASSWORDCHANGEDATE_MOD;
                    migr_auth_entry_aud(i).DEFAULT_PASSWORD := authentication_entry_aud(i).DEFAULT_PASSWORD;
                    migr_auth_entry_aud(i).DEFAULTPASSWORD_MOD := authentication_entry_aud(i).DEFAULTPASSWORD_MOD;
                END LOOP;

            v_start := 1;
            v_last := migr_auth_entry_aud.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_authentication_entry_aud -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_AUTH_ENTRY_AUD (ID_PK, REV, REVTYPE, CERTIFICATE_ID, CERTIFICATEID_MOD,
                                                            USERNAME, USERNAME_MOD, PASSWD, PASSWORD_MOD, AUTH_ROLES,
                                                            AUTHROLES_MOD, ORIGINAL_USER, ORIGINALUSER_MOD, BACKEND,
                                                            BACKEND_MOD, USER_ENABLED, ACTIVE_MOD, PASSWORD_CHANGE_DATE,
                                                            PASSWORDCHANGEDATE_MOD, DEFAULT_PASSWORD,
                                                            DEFAULTPASSWORD_MOD)
                        VALUES (migr_auth_entry_aud(i).ID_PK,
                                migr_auth_entry_aud(i).REV,
                                migr_auth_entry_aud(i).REVTYPE,
                                migr_auth_entry_aud(i).CERTIFICATE_ID,
                                migr_auth_entry_aud(i).CERTIFICATEID_MOD,
                                migr_auth_entry_aud(i).USERNAME,
                                migr_auth_entry_aud(i).USERNAME_MOD,
                                migr_auth_entry_aud(i).PASSWD,
                                migr_auth_entry_aud(i).PASSWORD_MOD,
                                migr_auth_entry_aud(i).AUTH_ROLES,
                                migr_auth_entry_aud(i).AUTHROLES_MOD,
                                migr_auth_entry_aud(i).ORIGINAL_USER,
                                migr_auth_entry_aud(i).ORIGINALUSER_MOD,
                                migr_auth_entry_aud(i).BACKEND,
                                migr_auth_entry_aud(i).BACKEND_MOD,
                                migr_auth_entry_aud(i).USER_ENABLED,
                                migr_auth_entry_aud(i).ACTIVE_MOD,
                                migr_auth_entry_aud(i).PASSWORD_CHANGE_DATE,
                                migr_auth_entry_aud(i).PASSWORDCHANGEDATE_MOD,
                                migr_auth_entry_aud(i).DEFAULT_PASSWORD,
                                migr_auth_entry_aud(i).DEFAULTPASSWORD_MOD);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_authentication_entry_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having ID_PK '
                                        || migr_auth_entry_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK
                                        || '  and REV '
                                        || migr_auth_entry_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).REV);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || authentication_entry_aud.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_authentication_entry_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_authentication_entry_aud;

    PROCEDURE migrate_back_rcriteria_aud(missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_BACK_RCRITERIA_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_BACK_RCRITERIA_AUD';

        CURSOR c_back_rcriteria_aud IS
            SELECT (SELECT MPKSRC.NEW_ID
                    FROM MIGR_TB_PKS_ROUTING_CRITERIA MPKSRC
                    WHERE MPKSRC.OLD_ID = BRA.ID_PK) AS ID_PK,
                   BRA.ID_PK AS ORIGINAL_ID_PK,
                   (SELECT MPKSRI.NEW_ID
                    FROM MIGR_TB_PKS_REV_INFO MPKSRI
                    WHERE MPKSRI.OLD_ID = BRA.REV) AS REV,
                   BRA.REVTYPE,
                   (SELECT MPKSBF.NEW_ID
                    FROM MIGR_TB_PKS_BACKEND_FILTER MPKSBF
                    WHERE MPKSBF.OLD_ID = BRA.FK_BACKEND_FILTER) AS FK_BACKEND_FILTER,
                   BRA.FK_BACKEND_FILTER AS ORIGINAL_FK_BACKEND_FILTER,
                   BRA.PRIORITY
            FROM TB_BACK_RCRITERIA_AUD BRA;

        TYPE T_BACK_RCRITERIA_AUD IS TABLE OF c_back_rcriteria_aud%rowtype;
        back_rcriteria_aud T_BACK_RCRITERIA_AUD;

        TYPE T_MIGR_BACK_RCRITERIA_AUD IS TABLE OF MIGR_TB_BACK_RCRITERIA_AUD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_back_rcriteria_aud T_MIGR_BACK_RCRITERIA_AUD;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_back_rcriteria_aud;
        LOOP
            FETCH c_back_rcriteria_aud BULK COLLECT INTO back_rcriteria_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN back_rcriteria_aud.COUNT = 0;

            migr_back_rcriteria_aud := T_MIGR_BACK_RCRITERIA_AUD();

            FOR i IN back_rcriteria_aud.FIRST .. back_rcriteria_aud.LAST
                LOOP
                    migr_back_rcriteria_aud(i).ID_PK := NVL(back_rcriteria_aud(i).ID_PK, generate_scalable_seq(back_rcriteria_aud(i).ORIGINAL_ID_PK, missing_entity_date_prefix));
                    migr_back_rcriteria_aud(i).REV := back_rcriteria_aud(i).REV;
                    migr_back_rcriteria_aud(i).REVTYPE := back_rcriteria_aud(i).REVTYPE;
                    migr_back_rcriteria_aud(i).FK_BACKEND_FILTER := NVL(back_rcriteria_aud(i).FK_BACKEND_FILTER, generate_scalable_seq(back_rcriteria_aud(i).ORIGINAL_FK_BACKEND_FILTER, missing_entity_date_prefix));
                    migr_back_rcriteria_aud(i).PRIORITY := back_rcriteria_aud(i).PRIORITY;
                END LOOP;

            v_start := 1;
            v_last := migr_back_rcriteria_aud.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_back_rcriteria_aud -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_BACK_RCRITERIA_AUD (ID_PK, REV, REVTYPE, FK_BACKEND_FILTER, PRIORITY)
                        VALUES (migr_back_rcriteria_aud(i).ID_PK,
                                migr_back_rcriteria_aud(i).REV,
                                migr_back_rcriteria_aud(i).REVTYPE,
                                migr_back_rcriteria_aud(i).FK_BACKEND_FILTER,
                                migr_back_rcriteria_aud(i).PRIORITY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_back_rcriteria_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having ID_PK '
                                        || migr_back_rcriteria_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK
                                        || '  and REV '
                                        || migr_back_rcriteria_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).REV);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || back_rcriteria_aud.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_back_rcriteria_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_back_rcriteria_aud;

    PROCEDURE migrate_backend_filter_aud(missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_BACKEND_FILTER_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_BACKEND_FILTER_AUD';

        CURSOR c_backend_filter_aud IS
            SELECT (SELECT MPKSBF.NEW_ID
                    FROM MIGR_TB_PKS_BACKEND_FILTER MPKSBF
                    WHERE MPKSBF.OLD_ID = BFA.ID_PK) AS ID_PK,
                   BFA.ID_PK AS ORIGINAL_ID_PK,
                   (SELECT MPKSRI.NEW_ID
                    FROM MIGR_TB_PKS_REV_INFO MPKSRI
                    WHERE MPKSRI.OLD_ID = BFA.REV) AS REV,
                   BFA.REVTYPE,
                   BFA.BACKEND_NAME,
                   BFA.BACKENDNAME_MOD,
                   BFA.PRIORITY,
                   BFA.INDEX_MOD,
                   BFA.ROUTINGCRITERIAS_MOD
            FROM TB_BACKEND_FILTER_AUD BFA;

        TYPE T_BACKEND_FILTER_AUD IS TABLE OF c_backend_filter_aud%rowtype;
        backend_filter_aud T_BACKEND_FILTER_AUD;

        TYPE T_MIGR_BACKEND_FILTER_AUD IS TABLE OF MIGR_TB_BACKEND_FILTER_AUD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_backend_filter_aud T_MIGR_BACKEND_FILTER_AUD;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_backend_filter_aud;
        LOOP
            FETCH c_backend_filter_aud BULK COLLECT INTO backend_filter_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN backend_filter_aud.COUNT = 0;

            migr_backend_filter_aud := T_MIGR_BACKEND_FILTER_AUD();

            FOR i IN backend_filter_aud.FIRST .. backend_filter_aud.LAST
                LOOP
                    migr_backend_filter_aud(i).ID_PK := NVL(backend_filter_aud(i).ID_PK, generate_scalable_seq(backend_filter_aud(i).ORIGINAL_ID_PK, missing_entity_date_prefix));
                    migr_backend_filter_aud(i).REV := backend_filter_aud(i).REV;
                    migr_backend_filter_aud(i).REVTYPE := backend_filter_aud(i).REVTYPE;
                    migr_backend_filter_aud(i).BACKEND_NAME := backend_filter_aud(i).BACKEND_NAME;
                    migr_backend_filter_aud(i).BACKENDNAME_MOD := backend_filter_aud(i).BACKENDNAME_MOD;
                    migr_backend_filter_aud(i).PRIORITY := backend_filter_aud(i).PRIORITY;
                    migr_backend_filter_aud(i).INDEX_MOD := backend_filter_aud(i).INDEX_MOD;
                    migr_backend_filter_aud(i).ROUTINGCRITERIAS_MOD := backend_filter_aud(i).ROUTINGCRITERIAS_MOD;
                END LOOP;

            v_start := 1;
            v_last := migr_backend_filter_aud.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_backend_filter_aud -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_BACKEND_FILTER_AUD (ID_PK, REV, REVTYPE, BACKEND_NAME, BACKENDNAME_MOD,
                                                                PRIORITY, INDEX_MOD, ROUTINGCRITERIAS_MOD)
                        VALUES (migr_backend_filter_aud(i).ID_PK,
                                migr_backend_filter_aud(i).REV,
                                migr_backend_filter_aud(i).REVTYPE,
                                migr_backend_filter_aud(i).BACKEND_NAME,
                                migr_backend_filter_aud(i).BACKENDNAME_MOD,
                                migr_backend_filter_aud(i).PRIORITY,
                                migr_backend_filter_aud(i).INDEX_MOD,
                                migr_backend_filter_aud(i).ROUTINGCRITERIAS_MOD);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_backend_filter_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having ID_PK '
                                        || migr_backend_filter_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK
                                        || '  and REV '
                                        || migr_backend_filter_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).REV);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || backend_filter_aud.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_backend_filter_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;

    END migrate_backend_filter_aud;

    PROCEDURE migrate_certificate_aud(missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_CERTIFICATE_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_CERTIFICATE_AUD';

        CURSOR c_certificate_aud IS
            SELECT (SELECT MPKSC.NEW_ID
                    FROM MIGR_TB_PKS_CERTIFICATE MPKSC
                    WHERE MPKSC.OLD_ID = CA.ID_PK) AS ID_PK,
                   CA.ID_PK AS ORIGINAL_ID_PK,
                   (SELECT MPKSRI.NEW_ID
                    FROM MIGR_TB_PKS_REV_INFO MPKSRI
                    WHERE MPKSRI.OLD_ID = CA.REV) AS REV,
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

        TYPE T_MIGR_CERTIFICATE_AUD IS TABLE OF MIGR_TB_CERTIFICATE_AUD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_certificate_aud T_MIGR_CERTIFICATE_AUD;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_certificate_aud;
        LOOP
            FETCH c_certificate_aud BULK COLLECT INTO certificate_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN certificate_aud.COUNT = 0;

            migr_certificate_aud := T_MIGR_CERTIFICATE_AUD();

            FOR i IN certificate_aud.FIRST .. certificate_aud.LAST
                LOOP
                    migr_certificate_aud(i).ID_PK := NVL(certificate_aud(i).ID_PK, generate_scalable_seq(certificate_aud(i).ORIGINAL_ID_PK, missing_entity_date_prefix));
                    migr_certificate_aud(i).REV := certificate_aud(i).REV;
                    migr_certificate_aud(i).REVTYPE := certificate_aud(i).REVTYPE;
                    migr_certificate_aud(i).CERTIFICATE_ALIAS := certificate_aud(i).CERTIFICATE_ALIAS;
                    migr_certificate_aud(i).NOT_VALID_BEFORE_DATE := certificate_aud(i).NOT_VALID_BEFORE_DATE;
                    migr_certificate_aud(i).NOT_VALID_AFTER_DATE := certificate_aud(i).NOT_VALID_AFTER_DATE;
                    migr_certificate_aud(i).REVOKE_NOTIFICATION_DATE := certificate_aud(i).REVOKE_NOTIFICATION_DATE;
                    migr_certificate_aud(i).ALERT_IMM_NOTIFICATION_DATE := certificate_aud(i).ALERT_IMM_NOTIFICATION_DATE;
                    migr_certificate_aud(i).ALERT_EXP_NOTIFICATION_DATE := certificate_aud(i).ALERT_EXP_NOTIFICATION_DATE;
                    migr_certificate_aud(i).CERTIFICATE_STATUS := certificate_aud(i).CERTIFICATE_STATUS;
                    migr_certificate_aud(i).CERTIFICATE_TYPE := certificate_aud(i).CERTIFICATE_TYPE;
                END LOOP;

            v_start := 1;
            v_last := migr_certificate_aud.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_certificate_aud -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_CERTIFICATE_AUD (ID_PK, REV, REVTYPE, CERTIFICATE_ALIAS,
                                                             NOT_VALID_BEFORE_DATE, NOT_VALID_AFTER_DATE,
                                                             REVOKE_NOTIFICATION_DATE, ALERT_IMM_NOTIFICATION_DATE,
                                                             ALERT_EXP_NOTIFICATION_DATE, CERTIFICATE_STATUS,
                                                             CERTIFICATE_TYPE)
                        VALUES (migr_certificate_aud(i).ID_PK,
                                migr_certificate_aud(i).REV,
                                migr_certificate_aud(i).REVTYPE,
                                migr_certificate_aud(i).CERTIFICATE_ALIAS,
                                migr_certificate_aud(i).NOT_VALID_BEFORE_DATE,
                                migr_certificate_aud(i).NOT_VALID_AFTER_DATE,
                                migr_certificate_aud(i).REVOKE_NOTIFICATION_DATE,
                                migr_certificate_aud(i).ALERT_IMM_NOTIFICATION_DATE,
                                migr_certificate_aud(i).ALERT_EXP_NOTIFICATION_DATE,
                                migr_certificate_aud(i).CERTIFICATE_STATUS,
                                migr_certificate_aud(i).CERTIFICATE_TYPE);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_certificate_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having ID_PK '
                                        || migr_certificate_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK
                                        || '  and REV '
                                        || migr_certificate_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).REV);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || certificate_aud.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_certificate_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_certificate_aud;

    PROCEDURE migrate_pm_configuration_aud(missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_PM_CONFIGURATION_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_CONFIGURATION_AUD';

        CURSOR c_pm_configuration_aud IS
            SELECT (SELECT MPKSPC.NEW_ID
                    FROM MIGR_TB_PKS_PM_CONFIGURATION MPKSPC
                    WHERE MPKSPC.OLD_ID = PCA.ID_PK) AS ID_PK,
                   PCA.ID_PK AS ORIGINAL_ID_PK,
                   (SELECT MPKSRI.NEW_ID
                    FROM MIGR_TB_PKS_REV_INFO MPKSRI
                    WHERE MPKSRI.OLD_ID = PCA.REV) AS REV,
                   PCA.REVTYPE,
                   PCA.EXPRESSION,
                   PCA.EXPRESSION_MOD,
                   PCA.NAME,
                   PCA.NAME_MOD
            FROM TB_PM_CONFIGURATION_AUD PCA;

        TYPE T_PM_CONFIGURATION_AUD IS TABLE OF c_pm_configuration_aud%rowtype;
        pm_configuration_aud T_PM_CONFIGURATION_AUD;

        TYPE T_MIGR_PM_CONFIGURATION_AUD IS TABLE OF MIGR_TB_PM_CONFIGURATION_AUD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_configuration_aud T_MIGR_PM_CONFIGURATION_AUD;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_configuration_aud;
        LOOP
            FETCH c_pm_configuration_aud BULK COLLECT INTO pm_configuration_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_configuration_aud.COUNT = 0;

            migr_pm_configuration_aud := T_MIGR_PM_CONFIGURATION_AUD();

            FOR i IN pm_configuration_aud.FIRST .. pm_configuration_aud.LAST
                LOOP
                    migr_pm_configuration_aud(i).ID_PK := NVL(pm_configuration_aud(i).ID_PK, generate_scalable_seq(pm_configuration_aud(i).ORIGINAL_ID_PK, missing_entity_date_prefix));
                    migr_pm_configuration_aud(i).REV := pm_configuration_aud(i).REV;
                    migr_pm_configuration_aud(i).REVTYPE := pm_configuration_aud(i).REVTYPE;
                    migr_pm_configuration_aud(i).EXPRESSION := pm_configuration_aud(i).EXPRESSION;
                    migr_pm_configuration_aud(i).EXPRESSION_MOD := pm_configuration_aud(i).EXPRESSION_MOD;
                    migr_pm_configuration_aud(i).NAME := pm_configuration_aud(i).NAME;
                    migr_pm_configuration_aud(i).NAME_MOD := pm_configuration_aud(i).NAME_MOD;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_configuration_aud.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_pm_configuration_aud -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_CONFIGURATION_AUD (ID_PK, REV, REVTYPE, EXPRESSION, EXPRESSION_MOD,
                                                                  NAME, NAME_MOD)
                        VALUES (migr_pm_configuration_aud(i).ID_PK,
                                migr_pm_configuration_aud(i).REV,
                                migr_pm_configuration_aud(i).REVTYPE,
                                migr_pm_configuration_aud(i).EXPRESSION,
                                migr_pm_configuration_aud(i).EXPRESSION_MOD,
                                migr_pm_configuration_aud(i).NAME,
                                migr_pm_configuration_aud(i).NAME_MOD);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_configuration_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having ID_PK '
                                        || migr_pm_configuration_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK
                                        || '  and REV '
                                        || migr_pm_configuration_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).REV);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_configuration_aud.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_configuration_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_configuration_aud;

    PROCEDURE migrate_pm_configuration_raw_aud(missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_PM_CONFIGURATION_RAW_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_CONF_RAW_AUD';

        CURSOR c_pm_configuration_raw_aud IS
            SELECT (SELECT MPKSPCR.NEW_ID
                    FROM MIGR_TB_PKS_PM_CONF_RAW MPKSPCR
                    WHERE MPKSPCR.OLD_ID = PCRA.ID_PK) AS ID_PK,
                   PCRA.ID_PK AS ORIGINAL_ID_PK,
                   (SELECT MPKSRI.NEW_ID
                    FROM MIGR_TB_PKS_REV_INFO MPKSRI
                    WHERE MPKSRI.OLD_ID = PCRA.REV) AS REV,
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

        TYPE T_MIGR_PM_CONF_RAW_AUD IS TABLE OF MIGR_TB_PM_CONF_RAW_AUD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_conf_raw_aud T_MIGR_PM_CONF_RAW_AUD;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_configuration_raw_aud;
        LOOP
            FETCH c_pm_configuration_raw_aud BULK COLLECT INTO pm_configuration_raw_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_configuration_raw_aud.COUNT = 0;

            migr_pm_conf_raw_aud := T_MIGR_PM_CONF_RAW_AUD();

            FOR i IN pm_configuration_raw_aud.FIRST .. pm_configuration_raw_aud.LAST
                LOOP
                    migr_pm_conf_raw_aud(i).ID_PK := NVL(pm_configuration_raw_aud(i).ID_PK, generate_scalable_seq(pm_configuration_raw_aud(i).ORIGINAL_ID_PK, missing_entity_date_prefix));
                    migr_pm_conf_raw_aud(i).REV := pm_configuration_raw_aud(i).REV;
                    migr_pm_conf_raw_aud(i).REVTYPE := pm_configuration_raw_aud(i).REVTYPE;
                    migr_pm_conf_raw_aud(i).CONFIGURATION_DATE := pm_configuration_raw_aud(i).CONFIGURATION_DATE;
                    migr_pm_conf_raw_aud(i).CONFIGURATIONDATE_MOD := pm_configuration_raw_aud(i).CONFIGURATIONDATE_MOD;
                    migr_pm_conf_raw_aud(i).DESCRIPTION := pm_configuration_raw_aud(i).DESCRIPTION;
                    migr_pm_conf_raw_aud(i).DESCRIPTION_MOD := pm_configuration_raw_aud(i).DESCRIPTION_MOD;
                    migr_pm_conf_raw_aud(i).XML := pm_configuration_raw_aud(i).XML;
                    migr_pm_conf_raw_aud(i).XML_MOD := pm_configuration_raw_aud(i).XML_MOD;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_conf_raw_aud.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_pm_configuration_raw_aud -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_CONF_RAW_AUD (ID_PK, REV, REVTYPE, CONFIGURATION_DATE,
                                                             CONFIGURATIONDATE_MOD, DESCRIPTION, DESCRIPTION_MOD,
                                                             XML, XML_MOD)
                        VALUES (migr_pm_conf_raw_aud(i).ID_PK,
                                migr_pm_conf_raw_aud(i).REV,
                                migr_pm_conf_raw_aud(i).REVTYPE,
                                migr_pm_conf_raw_aud(i).CONFIGURATION_DATE,
                                migr_pm_conf_raw_aud(i).CONFIGURATIONDATE_MOD,
                                migr_pm_conf_raw_aud(i).DESCRIPTION,
                                migr_pm_conf_raw_aud(i).DESCRIPTION_MOD,
                                migr_pm_conf_raw_aud(i).XML,
                                migr_pm_conf_raw_aud(i).XML_MOD);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_configuration_raw_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having ID_PK '
                                        || migr_pm_conf_raw_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK
                                        || '  and REV '
                                        || migr_pm_conf_raw_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).REV);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_configuration_raw_aud.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_configuration_raw_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_configuration_raw_aud;

    PROCEDURE migrate_pm_party_aud(missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_PM_PARTY_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PARTY_AUD';

        CURSOR c_pm_party_aud IS
            SELECT (SELECT MPKSPP.NEW_ID
                    FROM MIGR_TB_PKS_PM_PARTY MPKSPP
                    WHERE MPKSPP.OLD_ID = PPA.ID_PK) AS ID_PK,
                   PPA.ID_PK AS ORIGINAL_ID_PK,
                   (SELECT MPKSRI.NEW_ID
                    FROM MIGR_TB_PKS_REV_INFO MPKSRI
                    WHERE MPKSRI.OLD_ID = PPA.REV) AS REV,
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

        TYPE T_MIGR_PM_PARTY_AUD IS TABLE OF MIGR_TB_PM_PARTY_AUD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_party_aud T_MIGR_PM_PARTY_AUD;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_party_aud;
        LOOP
            FETCH c_pm_party_aud BULK COLLECT INTO pm_party_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_party_aud.COUNT = 0;

            migr_pm_party_aud := T_MIGR_PM_PARTY_AUD();

            FOR i IN pm_party_aud.FIRST .. pm_party_aud.LAST
                LOOP
                    migr_pm_party_aud(i).ID_PK := NVL(pm_party_aud(i).ID_PK, generate_scalable_seq(pm_party_aud(i).ORIGINAL_ID_PK, missing_entity_date_prefix));
                    migr_pm_party_aud(i).REV := pm_party_aud(i).REV;
                    migr_pm_party_aud(i).REVTYPE := pm_party_aud(i).REVTYPE;
                    migr_pm_party_aud(i).ENDPOINT := pm_party_aud(i).ENDPOINT;
                    migr_pm_party_aud(i).ENDPOINT_MOD := pm_party_aud(i).ENDPOINT_MOD;
                    migr_pm_party_aud(i).NAME := pm_party_aud(i).NAME;
                    migr_pm_party_aud(i).NAME_MOD := pm_party_aud(i).NAME_MOD;
                    migr_pm_party_aud(i).PASSWORD := pm_party_aud(i).PASSWORD;
                    migr_pm_party_aud(i).PASSWORD_MOD := pm_party_aud(i).PASSWORD_MOD;
                    migr_pm_party_aud(i).USERNAME := pm_party_aud(i).USERNAME;
                    migr_pm_party_aud(i).USERNAME_MOD := pm_party_aud(i).USERNAME_MOD;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_party_aud.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_pm_party_aud -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_PARTY_AUD (ID_PK, REV, REVTYPE, ENDPOINT, ENDPOINT_MOD, NAME,
                                                          NAME_MOD, PASSWORD, PASSWORD_MOD, USERNAME, USERNAME_MOD)
                        VALUES (migr_pm_party_aud(i).ID_PK,
                                migr_pm_party_aud(i).REV,
                                migr_pm_party_aud(i).REVTYPE,
                                migr_pm_party_aud(i).ENDPOINT,
                                migr_pm_party_aud(i).ENDPOINT_MOD,
                                migr_pm_party_aud(i).NAME,
                                migr_pm_party_aud(i).NAME_MOD,
                                migr_pm_party_aud(i).PASSWORD,
                                migr_pm_party_aud(i).PASSWORD_MOD,
                                migr_pm_party_aud(i).USERNAME,
                                migr_pm_party_aud(i).USERNAME_MOD);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_party_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having ID_PK '
                                        || migr_pm_party_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK
                                        || '  and REV '
                                        || migr_pm_party_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).REV);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_party_aud.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_party_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_party_aud;

    PROCEDURE migrate_pm_party_id_type_aud(missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_PM_PARTY_ID_TYPE_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PARTY_ID_TYPE_AUD';

        CURSOR c_pm_party_id_type_aud IS
            SELECT (SELECT MPKSPPIT.NEW_ID
                    FROM MIGR_TB_PKS_PM_PARTY_ID_TYPE MPKSPPIT
                    WHERE MPKSPPIT.OLD_ID = PPITA.ID_PK) AS ID_PK,
                   PPITA.ID_PK AS ORIGINAL_ID_PK,
                   (SELECT MPKSRI.NEW_ID
                    FROM MIGR_TB_PKS_REV_INFO MPKSRI
                    WHERE MPKSRI.OLD_ID = PPITA.REV) AS REV,
                   PPITA.REVTYPE,
                   PPITA.NAME,
                   PPITA.NAME_MOD,
                   PPITA.VALUE,
                   PPITA.VALUE_MOD
            FROM TB_PM_PARTY_ID_TYPE_AUD PPITA;

        TYPE T_PM_PARTY_ID_TYPE_AUD IS TABLE OF c_pm_party_id_type_aud%rowtype;
        pm_party_id_type_aud T_PM_PARTY_ID_TYPE_AUD;

        TYPE T_MIGR_PM_PARTY_ID_TYPE_AUD IS TABLE OF MIGR_TB_PM_PARTY_ID_TYPE_AUD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_party_id_type_aud T_MIGR_PM_PARTY_ID_TYPE_AUD;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_party_id_type_aud;
        LOOP
            FETCH c_pm_party_id_type_aud BULK COLLECT INTO pm_party_id_type_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_party_id_type_aud.COUNT = 0;

            migr_pm_party_id_type_aud := T_MIGR_PM_PARTY_ID_TYPE_AUD();

            FOR i IN pm_party_id_type_aud.FIRST .. pm_party_id_type_aud.LAST
                LOOP
                    migr_pm_party_id_type_aud(i).ID_PK := NVL(pm_party_id_type_aud(i).ID_PK, generate_scalable_seq(pm_party_id_type_aud(i).ORIGINAL_ID_PK, missing_entity_date_prefix));
                    migr_pm_party_id_type_aud(i).REV := pm_party_id_type_aud(i).REV;
                    migr_pm_party_id_type_aud(i).REVTYPE := pm_party_id_type_aud(i).REVTYPE;
                    migr_pm_party_id_type_aud(i).NAME := pm_party_id_type_aud(i).NAME;
                    migr_pm_party_id_type_aud(i).NAME_MOD := pm_party_id_type_aud(i).NAME_MOD;
                    migr_pm_party_id_type_aud(i).VALUE := pm_party_id_type_aud(i).VALUE;
                    migr_pm_party_id_type_aud(i).VALUE_MOD := pm_party_id_type_aud(i).VALUE_MOD;
                END LOOP;

            v_start := 1;
            v_last := migr_pm_party_id_type_aud.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_pm_party_id_type_aud -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_PARTY_ID_TYPE_AUD (ID_PK, REV, REVTYPE, NAME, NAME_MOD, VALUE,
                                                                  VALUE_MOD)
                        VALUES (migr_pm_party_id_type_aud(i).ID_PK,
                                migr_pm_party_id_type_aud(i).REV,
                                migr_pm_party_id_type_aud(i).REVTYPE,
                                migr_pm_party_id_type_aud(i).NAME,
                                migr_pm_party_id_type_aud(i).NAME_MOD,
                                migr_pm_party_id_type_aud(i).VALUE,
                                migr_pm_party_id_type_aud(i).VALUE_MOD);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_party_id_type_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having ID_PK '
                                        || migr_pm_party_id_type_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK
                                        || '  and REV '
                                        || migr_pm_party_id_type_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).REV);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_party_id_type_aud.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_party_id_type_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_party_id_type_aud;

    PROCEDURE migrate_pm_party_identifier_aud(missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_PM_PARTY_IDENTIFIER_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_PM_PARTY_IDENTIF_AUD';

        CURSOR c_pm_party_identifier_aud IS
            SELECT (SELECT MPKSPPI.NEW_ID
                    FROM MIGR_TB_PKS_PM_PARTY_ID MPKSPPI
                    WHERE MPKSPPI.OLD_ID = PPIA.ID_PK) AS ID_PK,
                   PPIA.ID_PK AS ORIGINAL_ID_PK,
                   (SELECT MPKSRI.NEW_ID
                    FROM MIGR_TB_PKS_REV_INFO MPKSRI
                    WHERE MPKSRI.OLD_ID = PPIA.REV) AS REV,
                   PPIA.REVTYPE,
                   (SELECT MPKSPP.NEW_ID
                    FROM MIGR_TB_PKS_PM_PARTY MPKSPP
                    WHERE MPKSPP.OLD_ID = PPIA.FK_PARTY) AS FK_PARTY,
                   PPIA.FK_PARTY AS ORIGINAL_FK_PARTY
            FROM TB_PM_PARTY_IDENTIFIER_AUD PPIA;

        TYPE T_PM_PARTY_IDENTIFIER_AUD IS TABLE OF c_pm_party_identifier_aud%rowtype;
        pm_party_identifier_aud T_PM_PARTY_IDENTIFIER_AUD;

        TYPE T_MIGR_PM_PARTY_IDENTIF_AUD IS TABLE OF MIGR_TB_PM_PARTY_IDENTIF_AUD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_pm_party_identif_aud T_MIGR_PM_PARTY_IDENTIF_AUD;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_pm_party_identifier_aud;
        LOOP
            FETCH c_pm_party_identifier_aud BULK COLLECT INTO pm_party_identifier_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN pm_party_identifier_aud.COUNT = 0;

            migr_pm_party_identif_aud := T_MIGR_PM_PARTY_IDENTIF_AUD();

            FOR i IN pm_party_identifier_aud.FIRST .. pm_party_identifier_aud.LAST
                LOOP
                    migr_pm_party_identif_aud(i).ID_PK := NVL(pm_party_identifier_aud(i).ID_PK, generate_scalable_seq(pm_party_identifier_aud(i).ORIGINAL_ID_PK, missing_entity_date_prefix));
                    migr_pm_party_identif_aud(i).REV := pm_party_identifier_aud(i).REV;
                    migr_pm_party_identif_aud(i).REVTYPE := pm_party_identifier_aud(i).REVTYPE;
                    migr_pm_party_identif_aud(i).FK_PARTY := NVL(pm_party_identifier_aud(i).FK_PARTY, generate_scalable_seq(pm_party_identifier_aud(i).ORIGINAL_FK_PARTY, missing_entity_date_prefix));
                END LOOP;

            v_start := 1;
            v_last := migr_pm_party_identif_aud.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_pm_party_identifier_aud -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_PM_PARTY_IDENTIF_AUD (ID_PK, REV, REVTYPE, FK_PARTY)
                        VALUES (migr_pm_party_identif_aud(i).ID_PK,
                                migr_pm_party_identif_aud(i).REV,
                                migr_pm_party_identif_aud(i).REVTYPE,
                                migr_pm_party_identif_aud(i).FK_PARTY);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_pm_party_identifier_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having ID_PK '
                                        || migr_pm_party_identif_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK
                                        || '  and REV '
                                        || migr_pm_party_identif_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).REV);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || pm_party_identifier_aud.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_pm_party_identifier_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_pm_party_identifier_aud;

    PROCEDURE migrate_routing_criteria_aud(missing_entity_date_prefix DATE) IS
        v_tab VARCHAR2(30) := 'TB_ROUTING_CRITERIA_AUD';
        v_tab_new VARCHAR2(30) := 'MIGR_TB_ROUTING_CRITERIA_AUD';

        CURSOR c_routing_criteria_aud IS
            SELECT (SELECT MPKSRC.NEW_ID
                    FROM MIGR_TB_PKS_ROUTING_CRITERIA MPKSRC
                    WHERE MPKSRC.OLD_ID = RCA.ID_PK) AS ID_PK,
                   RCA.ID_PK AS ORIGINAL_ID_PK,
                   (SELECT MPKSRI.NEW_ID
                    FROM MIGR_TB_PKS_REV_INFO MPKSRI
                    WHERE MPKSRI.OLD_ID = RCA.REV) AS REV,
                   RCA.REVTYPE,
                   RCA.EXPRESSION,
                   RCA.EXPRESSION_MOD,
                   RCA.NAME,
                   RCA.NAME_MOD
            FROM TB_ROUTING_CRITERIA_AUD RCA;

        TYPE T_ROUTING_CRITERIA_AUD IS TABLE OF c_routing_criteria_aud%rowtype;
        routing_criteria_aud T_ROUTING_CRITERIA_AUD;

        TYPE T_MIGR_ROUTING_CRITERIA_AUD IS TABLE OF MIGR_TB_ROUTING_CRITERIA_AUD%ROWTYPE INDEX BY PLS_INTEGER;
        migr_routing_criteria_aud T_MIGR_ROUTING_CRITERIA_AUD;

        v_last PLS_INTEGER;
        v_start PLS_INTEGER;
        v_end PLS_INTEGER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE(v_tab || ' migration started...');
        OPEN c_routing_criteria_aud;
        LOOP
            FETCH c_routing_criteria_aud BULK COLLECT INTO routing_criteria_aud LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN routing_criteria_aud.COUNT = 0;

            migr_routing_criteria_aud := T_MIGR_ROUTING_CRITERIA_AUD();

            FOR i IN routing_criteria_aud.FIRST .. routing_criteria_aud.LAST
                LOOP
                    migr_routing_criteria_aud(i).ID_PK := NVL(routing_criteria_aud(i).ID_PK, generate_scalable_seq(routing_criteria_aud(i).ORIGINAL_ID_PK, missing_entity_date_prefix));
                    migr_routing_criteria_aud(i).REV := routing_criteria_aud(i).REV;
                    migr_routing_criteria_aud(i).REVTYPE := routing_criteria_aud(i).REVTYPE;
                    migr_routing_criteria_aud(i).EXPRESSION := routing_criteria_aud(i).EXPRESSION;
                    migr_routing_criteria_aud(i).EXPRESSION_MOD := routing_criteria_aud(i).EXPRESSION_MOD;
                    migr_routing_criteria_aud(i).NAME := routing_criteria_aud(i).NAME;
                    migr_routing_criteria_aud(i).NAME_MOD := routing_criteria_aud(i).NAME_MOD;
                END LOOP;

            v_start := 1;
            v_last := migr_routing_criteria_aud.COUNT;

            LOOP
                EXIT WHEN v_start > v_last;

                v_end := LEAST(v_start + BATCH_SIZE - 1, v_last);

                BEGIN
                    log_verbose('migrate_routing_criteria_aud -> start-end: ' || v_start || '-' || v_end);
                    FORALL i IN v_start .. v_end SAVE EXCEPTIONS
                        INSERT INTO MIGR_TB_ROUTING_CRITERIA_AUD (ID_PK, REV, REVTYPE, EXPRESSION, EXPRESSION_MOD,
                                                                  NAME, NAME_MOD)
                        VALUES (migr_routing_criteria_aud(i).ID_PK,
                                migr_routing_criteria_aud(i).REV,
                                migr_routing_criteria_aud(i).REVTYPE,
                                migr_routing_criteria_aud(i).EXPRESSION,
                                migr_routing_criteria_aud(i).EXPRESSION_MOD,
                                migr_routing_criteria_aud(i).NAME,
                                migr_routing_criteria_aud(i).NAME_MOD);
                EXCEPTION
                    WHEN failure_in_forall
                        THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_routing_criteria_aud -> insert error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                            DBMS_OUTPUT.PUT_LINE('Updated ' || SQL%ROWCOUNT || ' rows.');

                            FOR i IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
                                LOOP
                                    DBMS_OUTPUT.PUT_LINE('Error ' || i || ' occurred on index '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                                        || '  with error code '
                                        || SQL%BULK_EXCEPTIONS(i).ERROR_CODE
                                        || '  for migration audit entry having ID_PK '
                                        || migr_routing_criteria_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).ID_PK
                                        || '  and REV '
                                        || migr_routing_criteria_aud(SQL%BULK_EXCEPTIONS(i).ERROR_INDEX).REV);
                                END LOOP;
                END;

                log_verbose(v_tab_new || ': Committing...');
                COMMIT;

                v_start := v_end + 1;
            END LOOP;
            log_verbose('Migrated ' || routing_criteria_aud.COUNT || ' records into ' || v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_routing_criteria_aud;

        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab || ' migration is done');
        END IF;
    END migrate_routing_criteria_aud;

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

    /**-- main entry point for running the single tenancy or the multitenancy non-general schema migration --*/
    PROCEDURE migrate IS
        missing_entity_date_prefix DATE;
    BEGIN
        -- keep it in this order
        prepare_timezone_offset;
        prepare_user_message;

        -- START migrate to the new schema (including primary keys to the new format)
        migrate_user_message;
        migrate_message_fragment;
        migrate_message_group;
        migrate_message_header;

        migrate_signal_receipt;
        migrate_message_log;

        migrate_raw_envelope_log;

        migrate_property;
        migrate_part_info_user;
        migrate_part_info_property;

        migrate_error_log;
        migrate_message_acknw;
        migrate_send_attempt;
        -- END migrate to the new schema (including primary keys to the new format)

        -- START migrate primary keys to new format
        migrate_action_audit;
        migrate_alert;
        migrate_event;
        migrate_event_alert;
        migrate_event_property;
        --
        migrate_authentication_entry;
        migrate_plugin_user_passwd_history;
        --
        migrate_backend_filter;
        migrate_routing_criteria;
        --
        migrate_certificate;
        --
        migrate_command;
        migrate_command_property;
        --
        migrate_encryption_key; -- SECRET_KEY & INIT_VECTOR (BLOB > LONGBLOB)
        --
        migrate_message_acknw_prop;
        --
        --
        migrate_messaging_lock;
        --
        migrate_pm_business_process;
        migrate_pm_action;
        migrate_pm_agreement;
        migrate_pm_error_handling;
        migrate_pm_mep;
        migrate_pm_mep_binding;
        migrate_pm_message_property;
        migrate_pm_message_property_set;
        migrate_pm_join_property_set;
        migrate_pm_party;
        migrate_pm_configuration;
        migrate_pm_mpc;
        migrate_pm_party_id_type;
        migrate_pm_party_identifier;
        migrate_pm_payload;
        migrate_pm_payload_profile;
        migrate_pm_join_payload_profile;
        migrate_pm_reception_awareness;
        migrate_pm_reliability;
        migrate_pm_role;
        migrate_pm_security;
        migrate_pm_service;
        migrate_pm_splitting;
        migrate_pm_leg;
        migrate_pm_leg_mpc;
        migrate_pm_process;
        migrate_pm_join_process_init_party;
        migrate_pm_join_process_leg;
        migrate_pm_join_process_resp_party;
        --
        migrate_pm_configuration_raw;
        --
        migrate_user;
        migrate_user_password_history;
        migrate_user_role;
        migrate_user_roles;
        --
        migrate_ws_plugin_tb_message_log;
        --
        missing_entity_date_prefix := SYSDATE;
        migrate_rev_info;
        migrate_rev_changes(missing_entity_date_prefix);
        migrate_authentication_entry_aud(missing_entity_date_prefix);
        migrate_back_rcriteria_aud(missing_entity_date_prefix);
        migrate_backend_filter_aud(missing_entity_date_prefix);
        migrate_certificate_aud(missing_entity_date_prefix);
        migrate_pm_configuration_aud(missing_entity_date_prefix);
        migrate_pm_configuration_raw_aud(missing_entity_date_prefix);
        migrate_pm_party_aud(missing_entity_date_prefix);
        migrate_pm_party_id_type_aud(missing_entity_date_prefix);
        migrate_pm_party_identifier_aud(missing_entity_date_prefix);
        migrate_routing_criteria_aud(missing_entity_date_prefix);
        migrate_user_aud(missing_entity_date_prefix);
        migrate_user_role_aud(missing_entity_date_prefix);
        migrate_user_roles_aud(missing_entity_date_prefix);
        -- END migrate primary keys to new format

    END migrate;

END MIGRATE_42_TO_50;
/