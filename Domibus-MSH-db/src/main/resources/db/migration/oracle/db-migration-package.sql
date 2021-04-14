-- *****************************************************************************************************
-- Domibus 4.2.1 to 5.0 data migration package
--
-- Main entry point is the procedure 'migrate'. To be executed into a begin/end; block
--
-- Parameters to be adjusted:
-- BATCH_SIZE - size of the batch for data migration on each migrated table after which there is a commit;
--              default value is 10000
-- VERBOSE_LOGS - more information into the logs; default to false
--
-- Tables which are migrated: TB_USER_MESSAGE, TB_MESSAGE_FRAGMENT, TB_MESSAGE_GROUP, TB_MESSAGE_HEADER,
-- TB_MESSAGE_LOG, TB_RECEIPT, TB_RECEIPT_DATA, TB_RAWENVELOPE_LOG, TB_PROPERTY, TB_PART_INFO
-- *****************************************************************************************************
CREATE OR REPLACE PACKAGE MIGRATE_42_TO_50 IS
    -- batch size for commit of the migrated records
    BATCH_SIZE CONSTANT NUMBER := 10000;

    -- enable more verbose logs
    VERBOSE_LOGS CONSTANT BOOLEAN := FALSE;

    -- entry point of migration - to be executed in a BEGIN/END; block
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
                v_id_pk := HIBERNATE_SEQUENCE.nextval;
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_MPC(ID_PK, VALUE) VALUES (' || v_id_pk || ', :1)' USING mpc_value;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_mpc_rec;

    FUNCTION get_tb_d_role_rec(role VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF role IS NULL THEN
            IF VERBOSE_LOGS THEN
                DBMS_OUTPUT.PUT_LINE('No record added into TB_D_ROLE');
            END IF;
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_ROLE WHERE ROLE = :1' INTO v_id_pk USING role;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_ROLE: ' || role);
                v_id_pk := HIBERNATE_SEQUENCE.nextval;
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_ROLE(ID_PK, ROLE) VALUES (' || v_id_pk || ', :1)' USING role;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_role_rec;

    FUNCTION get_tb_d_msh_role_rec(role VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF role IS NULL THEN
            IF VERBOSE_LOGS THEN
                DBMS_OUTPUT.PUT_LINE('No record added into TB_D_MSH_ROLE');
            END IF;
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MSH_ROLE WHERE ROLE = :1' INTO v_id_pk USING role;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_MSH_ROLE: ' || role);
                v_id_pk := HIBERNATE_SEQUENCE.nextval;
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_MSH_ROLE(ID_PK, ROLE) VALUES (' || v_id_pk || ', :1)' USING role;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_msh_role_rec;

    FUNCTION get_tb_d_service_rec(service_type VARCHAR2, service_value VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF service_type IS NULL AND service_value IS NULL THEN
            IF VERBOSE_LOGS THEN
                DBMS_OUTPUT.PUT_LINE('No record added into TB_D_SERVICE');
            END IF;
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_SERVICE WHERE TYPE = :1 AND VALUE = :2' INTO v_id_pk USING service_type, service_value;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_SERVICE: ' || service_type || ' , ' || service_value);
                v_id_pk := HIBERNATE_SEQUENCE.nextval;
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
            IF VERBOSE_LOGS THEN
                DBMS_OUTPUT.PUT_LINE('No record added into TB_D_MESSAGE_STATUS');
            END IF;
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MESSAGE_STATUS WHERE STATUS = :1' INTO v_id_pk USING message_status;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_MESSAGE_STATUS: ' || message_status);
                v_id_pk := HIBERNATE_SEQUENCE.nextval;
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
            IF VERBOSE_LOGS THEN
                DBMS_OUTPUT.PUT_LINE('No record added into TB_D_AGREEMENT');
            END IF;
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_AGREEMENT WHERE TYPE = :1 AND VALUE = :2' INTO v_id_pk USING agreement_type, agreement_value;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE(
                            'Add new record into TB_D_AGREEMENT: ' || agreement_type || ' , ' || agreement_value);
                v_id_pk := HIBERNATE_SEQUENCE.nextval;
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
            IF VERBOSE_LOGS THEN
                DBMS_OUTPUT.PUT_LINE('No record added into TB_D_ACTION');
            END IF;
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_ACTION WHERE ACTION = :1' INTO v_id_pk USING action;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_ACTION: ' || action);
                v_id_pk := HIBERNATE_SEQUENCE.nextval;
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_ACTION(ID_PK, ACTION) VALUES (' || v_id_pk || ', :1)' USING action;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_action_rec;

    FUNCTION get_tb_d_party_rec(party_type VARCHAR2, party_value VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF party_type IS NULL AND party_value IS NULL THEN
            IF VERBOSE_LOGS THEN
                DBMS_OUTPUT.PUT_LINE('No record added into TB_D_PARTY');
            END IF;
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_PARTY WHERE TYPE = :1 AND VALUE = :2' INTO v_id_pk USING party_type, party_value;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_PARTY: ' || party_type || ' , ' || party_value);
                v_id_pk := HIBERNATE_SEQUENCE.nextval;
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_PARTY(ID_PK, TYPE, VALUE) VALUES (' || v_id_pk ||
                                  ', :1, :2)' USING party_type, party_value;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_party_rec;

    FUNCTION get_tb_d_msg_subtype_rec(msg_subtype VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF msg_subtype IS NULL THEN
            IF VERBOSE_LOGS THEN
                DBMS_OUTPUT.PUT_LINE('No record added into TB_D_MESSAGE_SUBTYPE');
            END IF;
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_ACTION WHERE ACTION = :1' INTO v_id_pk USING msg_subtype;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_MESSAGE_SUBTYPE: ' || msg_subtype);
                v_id_pk := HIBERNATE_SEQUENCE.nextval;
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_MESSAGE_SUBTYPE(ID_PK, SUBTYPE) VALUES (' || v_id_pk ||
                                  ', :1)' USING msg_subtype;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_msg_subtype_rec;

    FUNCTION get_tb_d_notif_status_rec(status VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF status IS NULL THEN
            IF VERBOSE_LOGS THEN
                DBMS_OUTPUT.PUT_LINE('No record added into TB_D_NOTIFICATION_STATUS');
            END IF;
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_NOTIFICATION_STATUS WHERE STATUS = :1' INTO v_id_pk USING status;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_NOTIFICATION_STATUS: ' || status);
                v_id_pk := HIBERNATE_SEQUENCE.nextval;
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
            IF VERBOSE_LOGS THEN
                DBMS_OUTPUT.PUT_LINE('No record to look into ' || v_tab_new);
            END IF;
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
            IF VERBOSE_LOGS THEN
                DBMS_OUTPUT.PUT_LINE('No record to look into ' || v_tab_new);
            END IF;
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
            IF VERBOSE_LOGS THEN
                DBMS_OUTPUT.PUT_LINE('No record added into TB_D_MESSAGE_PROPERTY');
            END IF;
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MESSAGE_PROPERTY WHERE (NAME = :1 AND TYPE = :2 AND VALUE = :3) OR (NAME = :4 AND TYPE IS NULL AND VALUE = :5 )' INTO v_id_pk USING prop_name, prop_type, prop_value, prop_name, prop_value;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE(
                            'Add new record into TB_D_MESSAGE_PROPERTY: ' || prop_name || ' , ' || prop_value|| ' , ' || prop_type);
                v_id_pk := HIBERNATE_SEQUENCE.nextval;
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
            IF VERBOSE_LOGS THEN
                DBMS_OUTPUT.PUT_LINE('No record added into TB_D_PART_PROPERTY');
            END IF;
            RETURN v_id_pk;
        END IF;
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_PART_PROPERTY WHERE (NAME = :1 AND VALUE = :2 AND TYPE = :3) OR (NAME = :4 AND VALUE = :5 AND TYPE IS NULL)' INTO v_id_pk USING prop_name, prop_value, prop_type, prop_name, prop_value;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE(
                            'Add new record into TB_D_PART_PROPERTY: ' || prop_name || ' , ' || prop_value|| ' , ' || prop_type);
                v_id_pk := HIBERNATE_SEQUENCE.nextval;
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_PART_PROPERTY(ID_PK, NAME, VALUE, TYPE) VALUES (' || v_id_pk ||
                                  ', :1, :2, :3)' USING prop_name, prop_value, prop_type;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_part_property_rec;

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

    /** -- Pre migration necessary actions --*/
    PROCEDURE migrate_pre IS
        v_sql        VARCHAR2(1000);
        v_table VARCHAR2(30);
    BEGIN
        -- TODO check if needed to drop FKs or constraints

        -- drop any previously created table
        drop_table_if_exists('MIGR_TB_USER_MESSAGE');
        drop_table_if_exists('MIGR_TB_SJ_MESSAGE_FRAGMENT');
        drop_table_if_exists('MIGR_TB_SJ_MESSAGE_GROUP');
        drop_table_if_exists('MIGR_TB_SJ_MESSAGE_HEADER');
        drop_table_if_exists('MIGR_TB_SIGNAL_MESSAGE');
        drop_table_if_exists('MIGR_TB_USER_MESSAGE_LOG');
        drop_table_if_exists('MIGR_TB_SIGNAL_MESSAGE_LOG');
        drop_table_if_exists('MIGR_TB_RECEIPT');
        drop_table_if_exists('MIGR_TB_USER_MESSAGE_RAW');
        drop_table_if_exists('MIGR_TB_SIGNAL_MESSAGE_RAW');
        drop_table_if_exists('MIGR_TB_MESSAGE_PROPERTIES');
        drop_table_if_exists('MIGR_TB_PART_INFO');
        drop_table_if_exists('MIGR_TB_PART_PROPERTIES');

        drop_table_if_exists('TB_D_MPC');
        drop_table_if_exists('TB_D_ROLE');
        drop_table_if_exists('TB_D_MSH_ROLE');
        drop_table_if_exists('TB_D_SERVICE');
        drop_table_if_exists('TB_D_AGREEMENT');
        drop_table_if_exists('TB_D_ACTION');
        drop_table_if_exists('TB_D_PARTY');
        drop_table_if_exists('TB_D_MESSAGE_SUBTYPE');
        drop_table_if_exists('TB_D_MESSAGE_STATUS');
        drop_table_if_exists('TB_D_NOTIFICATION_STATUS');
        drop_table_if_exists('TB_D_MESSAGE_PROPERTY');
        drop_table_if_exists('TB_D_PART_PROPERTY');

        -- create them
        v_table := 'MIGR_TB_USER_MESSAGE';
        v_sql := 'CREATE TABLE MIGR_TB_USER_MESSAGE (ID_PK NUMBER(38, 0) NOT NULL, MESSAGE_ID VARCHAR2(255), REF_TO_MESSAGE_ID VARCHAR2(255), CONVERSATION_ID VARCHAR2(255), SOURCE_MESSAGE NUMBER(1), MESSAGE_FRAGMENT NUMBER(1), EBMS3_TIMESTAMP TIMESTAMP, ACTION_ID_FK NUMBER(38, 0), AGREEMENT_ID_FK NUMBER(38, 0), SERVICE_ID_FK NUMBER(38, 0), MPC_ID_FK NUMBER(38, 0), FROM_PARTY_ID_FK NUMBER(38, 0), FROM_ROLE_ID_FK NUMBER(38, 0), TO_PARTY_ID_FK NUMBER(38, 0), TO_ROLE_ID_FK NUMBER(38, 0), MESSAGE_SUBTYPE_ID_FK NUMBER(38, 0), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_USER_MESSAGE PRIMARY KEY (ID_PK))';
        create_table(v_table, v_sql);

        v_table := 'MIGR_TB_SJ_MESSAGE_FRAGMENT';
        v_sql :=
                'CREATE TABLE MIGR_TB_SJ_MESSAGE_FRAGMENT (ID_PK NUMBER(38, 0) NOT NULL, FRAGMENT_NUMBER INTEGER NOT NULL, GROUP_ID_FK NUMBER(38, 0) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_MESSAGE_FRAGMENT PRIMARY KEY (ID_PK))';
        create_table(v_table, v_sql);

        v_table := 'MIGR_TB_SJ_MESSAGE_GROUP';
        v_sql :=
                    'CREATE TABLE MIGR_TB_SJ_MESSAGE_GROUP (ID_PK NUMBER(38, 0) NOT NULL, GROUP_ID VARCHAR2(255) NOT NULL, MESSAGE_SIZE NUMBER(38, 0), FRAGMENT_COUNT INTEGER, ' ||
                    'SENT_FRAGMENTS INTEGER, RECEIVED_FRAGMENTS INTEGER, COMPRESSION_ALGORITHM VARCHAR2(255), COMPRESSED_MESSAGE_SIZE NUMBER(38, 0), SOAP_ACTION VARCHAR2(255), ' ||
                    'REJECTED NUMBER(1), EXPIRED NUMBER(1), MSH_ROLE_ID_FK NUMBER(38, 0) NOT NULL, SOURCE_MESSAGE_ID_FK NUMBER(38, 0) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, ' ||
                    'CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_SJ_MESSAGE_GROUP PRIMARY KEY (ID_PK))';
        create_table(v_table, v_sql);

        v_table := 'MIGR_TB_SJ_MESSAGE_HEADER';
        v_sql :=
                'CREATE TABLE MIGR_TB_SJ_MESSAGE_HEADER (ID_PK NUMBER(38, 0) NOT NULL, BOUNDARY VARCHAR2(255), "START" VARCHAR2(255), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_SJ_MESSAGE_HEADER PRIMARY KEY (ID_PK))';
        create_table(v_table, v_sql);

        v_table := 'MIGR_TB_SIGNAL_MESSAGE';
        v_sql := 'CREATE TABLE MIGR_TB_SIGNAL_MESSAGE (ID_PK NUMBER(38, 0) NOT NULL, SIGNAL_MESSAGE_ID VARCHAR2(255), REF_TO_MESSAGE_ID VARCHAR2(255), EBMS3_TIMESTAMP TIMESTAMP, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_SIGNAL_MESSAGE PRIMARY KEY (ID_PK))';
        create_table(v_table, v_sql);

        v_table := 'MIGR_TB_RECEIPT';
        v_sql := 'CREATE TABLE MIGR_TB_RECEIPT (ID_PK NUMBER(38, 0) NOT NULL, RAW_XML BLOB, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_RECEIPT PRIMARY KEY (ID_PK))';
        create_table(v_table, v_sql);

        v_table := 'MIGR_TB_USER_MESSAGE_LOG';
        v_sql := 'CREATE TABLE MIGR_TB_USER_MESSAGE_LOG (ID_PK NUMBER(38, 0) NOT NULL, BACKEND VARCHAR2(255), RECEIVED TIMESTAMP NOT NULL, DOWNLOADED TIMESTAMP, FAILED TIMESTAMP, RESTORED TIMESTAMP, DELETED TIMESTAMP, NEXT_ATTEMPT TIMESTAMP, SEND_ATTEMPTS INTEGER, SEND_ATTEMPTS_MAX INTEGER, SCHEDULED NUMBER(1), VERSION INTEGER DEFAULT 0 NOT NULL, MESSAGE_STATUS_ID_FK NUMBER(38, 0), MSH_ROLE_ID_FK NUMBER(38, 0) NOT NULL, NOTIFICATION_STATUS_ID_FK NUMBER(38, 0), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_MESSAGE_LOG PRIMARY KEY (ID_PK))';
        create_table(v_table, v_sql);

        v_table := 'MIGR_TB_SIGNAL_MESSAGE_LOG';
        v_sql := 'CREATE TABLE ' || v_table ||
                 ' (ID_PK NUMBER(38, 0) NOT NULL, RECEIVED TIMESTAMP NOT NULL, DELETED TIMESTAMP, MESSAGE_STATUS_ID_FK NUMBER(38, 0), MSH_ROLE_ID_FK NUMBER(38, 0) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_SIGNAL_MESSAGE_LOG PRIMARY KEY (ID_PK))';
        create_table(v_table, v_sql);

        v_table := 'MIGR_TB_USER_MESSAGE_RAW';
        v_sql :=
                'CREATE TABLE MIGR_TB_USER_MESSAGE_RAW (ID_PK NUMBER(38, 0) NOT NULL, RAW_XML BLOB, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_USER_MESSAGE_RAW PRIMARY KEY (ID_PK))';
        create_table(v_table, v_sql);

        v_table := 'MIGR_TB_SIGNAL_MESSAGE_RAW';
        v_sql := 'CREATE TABLE MIGR_TB_SIGNAL_MESSAGE_RAW (ID_PK NUMBER(38, 0) NOT NULL, RAW_XML BLOB, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_SIGNAL_MESSAGE_RAW PRIMARY KEY (ID_PK))';
        create_table(v_table, v_sql);

        v_table := 'MIGR_TB_MESSAGE_PROPERTIES';
        v_sql := 'CREATE TABLE MIGR_TB_MESSAGE_PROPERTIES (ID_PK NUMBER(38, 0) NOT NULL, USER_MESSAGE_ID_FK NUMBER(38, 0) NOT NULL, MESSAGE_PROPERTY_FK NUMBER(38, 0) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_MSG_PROPERTIES PRIMARY KEY (ID_PK))';
        create_table(v_table, v_sql);

        v_sql :=
                'CREATE TABLE MIGR_TB_PART_INFO (ID_PK NUMBER(38, 0) NOT NULL, BINARY_DATA BLOB, DESCRIPTION_LANG VARCHAR2(255), DESCRIPTION_VALUE VARCHAR2(255), HREF VARCHAR2(255), IN_BODY NUMBER(1), FILENAME VARCHAR2(255), MIME VARCHAR2(255) NOT NULL, USER_MESSAGE_ID_FK NUMBER(38, 0), PART_ORDER INTEGER DEFAULT 0 NOT NULL, ENCRYPTED NUMBER(1) DEFAULT 0, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_PART_INFO PRIMARY KEY (ID_PK))';
        v_table := 'MIGR_TB_PART_INFO';
        create_table(v_table, v_sql);

        v_sql := 'CREATE TABLE MIGR_TB_PART_PROPERTIES (ID_PK NUMBER(38, 0) NOT NULL, PART_INFO_ID_FK NUMBER(38, 0) NOT NULL, PART_INFO_PROPERTY_FK NUMBER(38, 0) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_PART_PROPERTIES PRIMARY KEY (ID_PK))';
        v_table := 'MIGR_TB_PART_PROPERTIES';
        create_table(v_table, v_sql);

        -- create dictionary tables
        v_sql :=
                'CREATE TABLE TB_D_MPC (ID_PK NUMBER(38, 0) NOT NULL, VALUE VARCHAR2(255) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_MPC PRIMARY KEY (ID_PK))';
        v_table := 'TB_D_MPC';
        create_table(v_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_ROLE (ID_PK NUMBER(38, 0) NOT NULL, ROLE VARCHAR2(255) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_ROLE PRIMARY KEY (ID_PK))';
        v_table := 'TB_D_ROLE';
        create_table(v_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_SERVICE (ID_PK NUMBER(38, 0) NOT NULL, VALUE VARCHAR2(255) NOT NULL, TYPE VARCHAR2(255), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_SERVICE PRIMARY KEY (ID_PK))';
        v_table := 'TB_D_SERVICE';
        create_table(v_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_AGREEMENT (ID_PK NUMBER(38, 0) NOT NULL, VALUE VARCHAR2(255) NOT NULL, TYPE VARCHAR2(255), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_AGREEMENT PRIMARY KEY (ID_PK))';
        v_table := 'TB_D_AGREEMENT';
        create_table(v_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_ACTION (ID_PK NUMBER(38, 0) NOT NULL, ACTION VARCHAR2(255) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_ACTION PRIMARY KEY (ID_PK))';
        v_table := 'TB_D_ACTION';
        create_table(v_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_PARTY (ID_PK NUMBER(38, 0) NOT NULL, VALUE VARCHAR2(255) NOT NULL, TYPE VARCHAR2(255), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_PARTY PRIMARY KEY (ID_PK))';
        v_table := 'TB_D_PARTY';
        create_table(v_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_MESSAGE_SUBTYPE (ID_PK NUMBER(38, 0) NOT NULL, SUBTYPE VARCHAR2(255) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_MESSAGE_SUBTYPE PRIMARY KEY (ID_PK))';
        v_table := 'TB_D_MESSAGE_SUBTYPE';
        create_table(v_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_MESSAGE_STATUS (ID_PK NUMBER(38, 0) NOT NULL, STATUS VARCHAR2(255) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_MESSAGE_STATUS PRIMARY KEY (ID_PK))';
        v_table := 'TB_D_MESSAGE_STATUS';
        create_table(v_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_MESSAGE_PROPERTY (ID_PK NUMBER(38, 0) NOT NULL, NAME VARCHAR2(255) NOT NULL, VALUE VARCHAR2(1024), TYPE VARCHAR2(255), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_MSG_PROPERTY PRIMARY KEY (ID_PK))';
        v_table := 'TB_D_MESSAGE_PROPERTY';
        create_table(v_table, v_sql);

        v_sql := 'CREATE TABLE TB_D_PART_PROPERTY (ID_PK NUMBER(38, 0) NOT NULL, NAME VARCHAR2(255) NOT NULL, VALUE VARCHAR2(1024), TYPE VARCHAR2(255), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_PART_PROPERTY PRIMARY KEY (ID_PK))';
        v_table := 'TB_D_PART_PROPERTY';
        create_table(v_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_NOTIFICATION_STATUS (ID_PK NUMBER(38, 0) NOT NULL, STATUS VARCHAR2(255) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_NOTIFICATION_STATUS PRIMARY KEY (ID_PK))';
        v_table := 'TB_D_NOTIFICATION_STATUS';
        create_table(v_table, v_sql);


    END migrate_pre;
    /** -- Helper procedures and functions end -*/

    /**-- TB_USER_MESSAGE migration --*/
    PROCEDURE migrate_user_message IS
        v_tab        VARCHAR2(30) := 'TB_USER_MESSAGE';
        v_tab_new    VARCHAR2(30) := 'MIGR_TB_USER_MESSAGE';
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
                   ML.MESSAGE_SUBTYPE
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
                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, CONVERSATION_ID, SOURCE_MESSAGE, MESSAGE_FRAGMENT, EBMS3_TIMESTAMP, MPC_ID_FK, FROM_ROLE_ID_FK, ' ||
                                          'TO_ROLE_ID_FK, SERVICE_ID_FK, AGREEMENT_ID_FK, ACTION_ID_FK, FROM_PARTY_ID_FK, TO_PARTY_ID_FK, MESSAGE_SUBTYPE_ID_FK) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16)'
                            USING user_message(i).ID_PK,
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
                            get_tb_d_agreement_rec(user_message(i).AGREEMENT_REF_TYPE,
                                                   user_message(i).AGREEMENT_REF_VALUE),
                            get_tb_d_action_rec(user_message(i).ACTION),
                            get_tb_d_party_rec(user_message(i).FROM_PARTY_TYPE, user_message(i).FROM_PARTY_VALUE),
                            get_tb_d_party_rec(user_message(i).TO_PARTY_TYPE, user_message(i).TO_PARTY_VALUE),
                            get_tb_d_msg_subtype_rec(user_message(i).MESSAGE_SUBTYPE);
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
    PROCEDURE migrate_message_fragment IS
        v_tab              VARCHAR2(30) := 'TB_MESSAGE_FRAGMENT';
        v_tab_new          VARCHAR2(30) := 'MIGR_TB_SJ_MESSAGE_FRAGMENT';
        v_tab_user_message VARCHAR2(30) := 'TB_USER_MESSAGE';
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
                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, GROUP_ID_FK, FRAGMENT_NUMBER, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7)'
                            USING message_fragment(i).ID_PK,
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
    PROCEDURE migrate_message_group IS
        v_tab                  VARCHAR2(30) := 'TB_MESSAGE_GROUP';
        v_tab_new              VARCHAR2(30) := 'MIGR_TB_SJ_MESSAGE_GROUP';
        v_tab_user_message_new VARCHAR2(30) := 'MIGR_TB_USER_MESSAGE';
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
                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, GROUP_ID, MESSAGE_SIZE, FRAGMENT_COUNT, SENT_FRAGMENTS, RECEIVED_FRAGMENTS, COMPRESSION_ALGORITHM, COMPRESSED_MESSAGE_SIZE,' ||
                                          'SOAP_ACTION, REJECTED, EXPIRED, MSH_ROLE_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY, SOURCE_MESSAGE_ID_FK) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16, :p_17)'
                            USING message_group(i).ID_PK,
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
    PROCEDURE migrate_message_header IS
        v_tab               VARCHAR2(30) := 'TB_MESSAGE_HEADER';
        v_tab_new           VARCHAR2(30) := 'MIGR_TB_SJ_MESSAGE_HEADER';
        v_tab_message_group VARCHAR2(30) := 'TB_MESSAGE_GROUP';
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
                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                          ' (ID_PK, BOUNDARY, "START", CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7)'
                            USING message_header(i).ID_PK,
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
    PROCEDURE migrate_signal_receipt IS
        v_tab_signal           VARCHAR2(30) := 'TB_SIGNAL_MESSAGE';
        v_tab_signal_new       VARCHAR2(30) := 'MIGR_TB_SIGNAL_MESSAGE';
        v_tab_messaging        VARCHAR2(30) := 'TB_MESSAGING';
        v_tab_user_message     VARCHAR2(30) := 'TB_USER_MESSAGE';
        v_tab_receipt          VARCHAR2(30) := 'TB_RECEIPT';
        v_tab_receipt_data     VARCHAR2(30) := 'TB_RECEIPT_DATA';
        v_tab_receipt_new      VARCHAR2(30) := 'MIGR_TB_RECEIPT';

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
                        -- new tb_signal_message table
                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_signal_new ||
                                          ' (ID_PK, SIGNAL_MESSAGE_ID, REF_TO_MESSAGE_ID, EBMS3_TIMESTAMP, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8)'
                            USING signal_message_receipt(i).ID_PK,
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
                            USING signal_message_receipt(i).ID_PK,
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
                        ' and ' ||
                        v_tab_receipt_new);
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
        v_count_user       NUMBER       := 0;
        v_count_signal     NUMBER       := 0;
        v_tab_migrated     VARCHAR2(30) := v_tab_signal_new;
        CURSOR c_raw_envelope IS
            SELECT UM.ID_PK, --  1:1 here
                   'USER' AS TYPE,
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
                   'SIGNAL' AS TYPE,
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
                        IF raw_envelope(i).TYPE = 'USER' THEN
                            v_count_user := v_count_user + 1;
                            BEGIN
                                EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_user_new ||
                                                  ' (ID_PK, RAW_XML, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                                  'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                                    USING raw_envelope(i).ID_PK,
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
                                    USING raw_envelope(i).ID_PK,
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
    PROCEDURE migrate_property IS
        v_tab              VARCHAR2(30) := 'TB_PROPERTY';
        v_tab_user_message VARCHAR2(30) := 'TB_USER_MESSAGE';
        v_tab_message_new  VARCHAR2(30) := 'MIGR_TB_MESSAGE_PROPERTIES';
        CURSOR c_property IS
            SELECT TP.ID_PK,
                   UM.ID_PK USER_MESSAGE_ID_FK,
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
                        EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_message_new ||
                                          ' (ID_PK, USER_MESSAGE_ID_FK, MESSAGE_PROPERTY_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                          'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7)'
                            USING
                            property(i).ID_PK,
                            property(i).USER_MESSAGE_ID_FK,
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
    PROCEDURE migrate_part_info_user IS
        v_tab              VARCHAR2(30) := 'TB_PART_INFO';
        v_tab_new          VARCHAR2(30) := 'MIGR_TB_PART_INFO';
        v_tab_user_message VARCHAR2(30) := 'TB_USER_MESSAGE';
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

                        BEGIN
                            EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                              ' (ID_PK, BINARY_DATA, DESCRIPTION_LANG, DESCRIPTION_VALUE, HREF, IN_BODY, FILENAME, MIME,' ||
                                              'PART_ORDER, ENCRYPTED, USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                              'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15)'
                                USING
                                part_info(i).ID_PK,
                                part_info(i).BINARY_DATA,
                                part_info(i).DESCRIPTION_LANG,
                                part_info(i).DESCRIPTION_VALUE,
                                part_info(i).HREF,
                                part_info(i).IN_BODY,
                                part_info(i).FILENAME,
                                part_info(i).MIME,
                                part_info(i).PART_ORDER,
                                part_info(i).ENCRYPTED,
                                part_info(i).USER_MESSAGE_ID_FK,
                                part_info(i).CREATION_TIME,
                                part_info(i).CREATED_BY,
                                part_info(i).MODIFICATION_TIME,
                                part_info(i).MODIFIED_BY;
                        EXCEPTION
                            WHEN OTHERS THEN
                                DBMS_OUTPUT.PUT_LINE('migrate_part_info_user for ' || v_tab_new ||
                                                     ' -> execute immediate error: ' ||
                                                     DBMS_UTILITY.FORMAT_ERROR_STACK);
                        END;

                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_part_info_user -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE(
                        'Migrated ' || part_info.COUNT || ' records in total into ' ||
                        v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_part_info;
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

                        BEGIN
                            EXECUTE IMMEDIATE 'INSERT INTO ' || v_tab_new ||
                                              ' (ID_PK, PART_INFO_ID_FK, PART_INFO_PROPERTY_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY ) ' ||
                                              'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7)'
                                USING
                                HIBERNATE_SEQUENCE.nextval,
                                part_prop(i).PART_INFO_ID_FK,
                                get_tb_d_part_property_rec(part_prop(i).NAME,
                                                           part_prop(i).VALUE,
                                                           part_prop(i).TYPE),
                                part_prop(i).CREATION_TIME,
                                part_prop(i).CREATED_BY,
                                part_prop(i).MODIFICATION_TIME,
                                part_prop(i).MODIFIED_BY;
                        EXCEPTION
                            WHEN OTHERS THEN
                                DBMS_OUTPUT.PUT_LINE('migrate_part_info_property for ' || v_tab_new || ' -> execute immediate error: ' ||DBMS_UTILITY.FORMAT_ERROR_STACK);
                        END;
                        IF i MOD BATCH_SIZE = 0 THEN
                            COMMIT;
                            DBMS_OUTPUT.PUT_LINE(
                                        v_tab_new || ': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                            v_batch_no := v_batch_no + 1;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('migrate_part_info_property -> execute immediate error: ' ||
                                                 DBMS_UTILITY.FORMAT_ERROR_STACK);
                    END;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE(
                        'Migrated ' || part_prop.COUNT || ' records in total into ' ||
                        v_tab_new);
        END LOOP;

        COMMIT;
        CLOSE c_part_prop;
    END migrate_part_info_property;

    /**-- TB_USER_MESSAGE migration post actions --*/
    PROCEDURE migrate_user_message_post IS
    BEGIN
        BEGIN
            -- TODO check if we can run this from Liquibase?
            -- put back the FKs
            EXECUTE IMMEDIATE 'ALTER TABLE MIGR_TB_USER_MESSAGE ADD CONSTRAINT FK_USER_MSG_ACTION_ID FOREIGN KEY (ACTION_ID_FK) REFERENCES TB_D_ACTION (ID_PK)';
            EXECUTE IMMEDIATE 'ALTER TABLE MIGR_TB_USER_MESSAGE ADD CONSTRAINT FK_USER_MSG_AGREEMENT_ID FOREIGN KEY (AGREEMENT_ID_FK) REFERENCES TB_D_AGREEMENT (ID_PK)';
            EXECUTE IMMEDIATE 'ALTER TABLE MIGR_TB_USER_MESSAGE ADD CONSTRAINT FK_USER_MSG_SERVICE_ID FOREIGN KEY (SERVICE_ID_FK) REFERENCES TB_D_SERVICE (ID_PK)';
            EXECUTE IMMEDIATE 'ALTER TABLE MIGR_TB_USER_MESSAGE ADD CONSTRAINT FK_USER_MSG_MPC_ID FOREIGN KEY (MPC_ID_FK) REFERENCES TB_D_MPC (ID_PK)';
            EXECUTE IMMEDIATE 'ALTER TABLE MIGR_TB_USER_MESSAGE ADD CONSTRAINT FK_USER_MSG_FROM_PARTY_ID FOREIGN KEY (FROM_PARTY_ID_FK) REFERENCES TB_D_PARTY (ID_PK)';
            EXECUTE IMMEDIATE 'ALTER TABLE MIGR_TB_USER_MESSAGE ADD CONSTRAINT FK_USER_MSG_FROM_ROLE_ID FOREIGN KEY (FROM_ROLE_ID_FK) REFERENCES TB_D_ROLE (ID_PK)';
            EXECUTE IMMEDIATE 'ALTER TABLE MIGR_TB_USER_MESSAGE ADD CONSTRAINT FK_USER_MSG_TO_PARTY_ID FOREIGN KEY (TO_PARTY_ID_FK) REFERENCES TB_D_PARTY (ID_PK)';
            EXECUTE IMMEDIATE 'ALTER TABLE MIGR_TB_USER_MESSAGE ADD CONSTRAINT FK_USER_MSG_TO_ROLE_ID FOREIGN KEY (TO_ROLE_ID_FK) REFERENCES TB_D_ROLE (ID_PK)';
            EXECUTE IMMEDIATE 'ALTER TABLE MIGR_TB_USER_MESSAGE ADD CONSTRAINT FK_USER_MSG_SUBTYPE_ID FOREIGN KEY (MESSAGE_SUBTYPE_ID_FK) REFERENCES TB_D_MESSAGE_SUBTYPE (ID_PK)';
            DBMS_OUTPUT.PUT_LINE('Added FK back on MIGR_TB_USER_MESSAGE table');
        EXCEPTION
            WHEN OTHERS THEN
                DBMS_OUTPUT.PUT_LINE('Execute immediate error: ' || DBMS_UTILITY.FORMAT_ERROR_STACK);
        END;
        --  drop_table_if_exists('TB_USER_MESSAGE');
    END migrate_user_message_post;

    /** -- Migration post actions --*/
    PROCEDURE migrate_post IS
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migration post actions start...');

        -- TODO add these to Liquibase?
        migrate_user_message_post;

        --  drop_table_if_exists('TB_MESSAGE_GROUP');
        --  drop_table_if_exists('TB_MESSAGE_FRAGMENT');
        --  drop_table_if_exists('TB_MESSAGE_HEADER');


    END migrate_post;

    /**-- main entry point for all migration --*/
    PROCEDURE migrate IS
    BEGIN
        -- pre migration actions
        migrate_pre;

        -- keep it in this order
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

        -- house keeping
        migrate_post;
    END migrate;


END MIGRATE_42_TO_50;
/