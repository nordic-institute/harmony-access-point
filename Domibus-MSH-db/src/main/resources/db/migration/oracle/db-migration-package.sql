CREATE OR REPLACE PACKAGE MIGRATE_42_TO_50 IS
    TEMP_PREFIX CONSTANT Varchar2(5) := 'TEMP_';
    BATCH_SIZE CONSTANT NUMBER := 3;

    PROCEDURE migrate;

END MIGRATE_42_TO_50;
/

CREATE OR REPLACE PACKAGE BODY MIGRATE_42_TO_50 IS

    /** -- Helper procedures start -*/
    PROCEDURE drop_table_if_exists(tab_name IN VARCHAR2) IS
        v_table_exists INT;
    BEGIN
        SELECT COUNT(*) INTO v_table_exists FROM USER_TABLES WHERE table_name = UPPER(tab_name);
        IF v_table_exists > 0 THEN
            EXECUTE IMMEDIATE 'DROP TABLE ' || tab_name;
            DBMS_OUTPUT.PUT_LINE('Table ' || tab_name || ' dropped');
        END IF;
    END drop_table_if_exists;

    FUNCTION check_table_exists(tab_name VARCHAR2) RETURN BOOLEAN IS
        v_table_exists INT;
    BEGIN
        SELECT COUNT(*) INTO v_table_exists FROM USER_TABLES WHERE table_name = UPPER(tab_name);
        RETURN v_table_exists > 0;
    END check_table_exists;

    PROCEDURE truncate_or_create_table(tab_name IN VARCHAR2, create_sql IN VARCHAR2) IS
    BEGIN
        IF check_table_exists(tab_name) THEN
            EXECUTE IMMEDIATE 'TRUNCATE TABLE ' || tab_name;
            DBMS_OUTPUT.PUT_LINE('Table ' || tab_name || ' truncated');
        ELSE
            EXECUTE IMMEDIATE create_sql;
            DBMS_OUTPUT.PUT_LINE('Table ' || tab_name || ' created');
        END IF;
    END truncate_or_create_table;

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
            DBMS_OUTPUT.PUT_LINE('No record added into TB_D_ROLE');
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

    FUNCTION get_tb_d_service_rec(service_type VARCHAR2, service_value VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF service_type IS NULL AND service_value IS NULL THEN
            DBMS_OUTPUT.PUT_LINE('No record added into TB_D_SERVICE');
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

    FUNCTION get_tb_d_agreement_rec(agreement_type VARCHAR2, agreement_value VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF agreement_type IS NULL AND agreement_value IS NULL THEN
            DBMS_OUTPUT.PUT_LINE('No record added into TB_D_AGREEMENT');
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
            DBMS_OUTPUT.PUT_LINE('No record added into TB_D_ACTION');
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
            DBMS_OUTPUT.PUT_LINE('No record added into TB_D_PARTY');
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
            DBMS_OUTPUT.PUT_LINE('No record added into TB_D_MESSAGE_SUBTYPE');
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

    FUNCTION get_tb_user_message_rec(message_id VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER;
    BEGIN
        IF message_id IS NULL THEN
            DBMS_OUTPUT.PUT_LINE('No record to look into TEMP_TB_USER_MESSAGE');
            RETURN v_id_pk;
        END IF;
        BEGIN
            -- TODO check index on message_id column?
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TEMP_TB_USER_MESSAGE WHERE MESSAGE_ID = :1' INTO v_id_pk USING message_id;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                DBMS_OUTPUT.PUT_LINE('No record found into TEMP_TB_USER_MESSAGE for MESSAGE_ID = ' || message_id);
        END;
        RETURN v_id_pk;
    END get_tb_user_message_rec;

    FUNCTION check_counts(tab_name1 VARCHAR2, tab_name2 VARCHAR2) RETURN BOOLEAN IS
        v_count_match BOOLEAN;
        v_count_tab1  NUMBER;
        v_count_tab2  NUMBER;
    BEGIN
        BEGIN
            EXECUTE IMMEDIATE 'SELECT /*+ PARALLEL(4) */ COUNT(*) FROM ' || tab_name1 INTO v_count_tab1;
            EXECUTE IMMEDIATE 'SELECT /*+ PARALLEL(4) */ COUNT(*) FROM ' || tab_name2 INTO v_count_tab2;
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

    PROCEDURE migrate_user_message_prereq(v_temp_table IN VARCHAR2) IS
        v_sql        VARCHAR2(1000);
        v_dict_table VARCHAR2(30);
    BEGIN
        v_sql := 'CREATE TABLE ' || v_temp_table ||
                 '(ID_PK NUMBER(38, 0) NOT NULL, MESSAGE_ID VARCHAR2(255), REF_TO_MESSAGE_ID VARCHAR2(255), CONVERSATION_ID VARCHAR2(255), SPLIT_AND_JOIN NUMBER(1), SOURCE_MESSAGE NUMBER(1), MESSAGE_FRAGMENT NUMBER(1), EBMS3_TIMESTAMP TIMESTAMP, ACTION_ID_FK NUMBER(38, 0), AGREEMENT_ID_FK NUMBER(38, 0), SERVICE_ID_FK NUMBER(38, 0), MPC_ID_FK NUMBER(38, 0), FROM_PARTY_ID_FK NUMBER(38, 0), FROM_ROLE_ID_FK NUMBER(38, 0), TO_PARTY_ID_FK NUMBER(38, 0), TO_ROLE_ID_FK NUMBER(38, 0), MESSAGE_SUBTYPE_ID_FK NUMBER(38, 0), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_USER_MESSAGE PRIMARY KEY (ID_PK))';
        truncate_or_create_table(v_temp_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_MPC (ID_PK NUMBER(38, 0) NOT NULL, VALUE VARCHAR2(255) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_MPC PRIMARY KEY (ID_PK))';
        v_dict_table := 'TB_D_MPC';
        truncate_or_create_table(v_dict_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_ROLE (ID_PK NUMBER(38, 0) NOT NULL, ROLE VARCHAR2(255) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_ROLE PRIMARY KEY (ID_PK))';
        v_dict_table := 'TB_D_ROLE';
        truncate_or_create_table(v_dict_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_SERVICE (ID_PK NUMBER(38, 0) NOT NULL, VALUE VARCHAR2(255) NOT NULL, TYPE VARCHAR2(255), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_SERVICE PRIMARY KEY (ID_PK))';
        v_dict_table := 'TB_D_SERVICE';
        truncate_or_create_table(v_dict_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_AGREEMENT (ID_PK NUMBER(38, 0) NOT NULL, VALUE VARCHAR2(255) NOT NULL, TYPE VARCHAR2(255), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_AGREEMENT PRIMARY KEY (ID_PK))';
        v_dict_table := 'TB_D_AGREEMENT';
        truncate_or_create_table(v_dict_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_ACTION (ID_PK NUMBER(38, 0) NOT NULL, ACTION VARCHAR2(255) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_ACTION PRIMARY KEY (ID_PK))';
        v_dict_table := 'TB_D_ACTION';
        truncate_or_create_table(v_dict_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_PARTY (ID_PK NUMBER(38, 0) NOT NULL, VALUE VARCHAR2(255) NOT NULL, TYPE VARCHAR2(255), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_PARTY PRIMARY KEY (ID_PK))';
        v_dict_table := 'TB_D_PARTY';
        truncate_or_create_table(v_dict_table, v_sql);

        v_sql :=
                'CREATE TABLE TB_D_MESSAGE_SUBTYPE (ID_PK NUMBER(38, 0) NOT NULL, SUBTYPE VARCHAR2(255) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_MESSAGE_SUBTYPE PRIMARY KEY (ID_PK))';
        v_dict_table := 'TB_D_MESSAGE_SUBTYPE';
        truncate_or_create_table(v_dict_table, v_sql);
    END migrate_user_message_prereq;
    /** -- Helper procedures end -*/

    /**-- TB_USER_MESSAGE migration --*/
    PROCEDURE migrate_tb_user_message IS
        v_temp_table VARCHAR2(30) := TEMP_PREFIX || 'TB_USER_MESSAGE';
        CURSOR c_user_message IS
            SELECT UM.ID_PK,
                   MI.MESSAGE_ID                MESSAGE_ID,
                   MI.REF_TO_MESSAGE_ID         REF_TO_MESSAGE_ID,
                   UM.COLL_INFO_CONVERS_ID      CONVERSATION_ID,
                   ML.SOURCE_MESSAGE            SOURCE_MESSAGE,
                   ML.MESSAGE_FRAGMENT          MESSAGE_FRAGMENT,
                   MI.TIME_STAMP                EBMS3_TIMESTAMP,
                   UM.MPC                       MPC,
                   UM.FROM_ROLE                 FROM_ROLE,
                   UM.TO_ROLE                   TO_ROLE,
                   UM.SERVICE_TYPE              SERVICE_TYPE,
                   UM.SERVICE_VALUE             SERVICE_VALUE,
                   UM.AGREEMENT_REF_TYPE        AGREEMENT_REF_TYPE,
                   UM.AGREEMENT_REF_VALUE       AGREEMENT_REF_VALUE,
                   UM.COLLABORATION_INFO_ACTION ACTION,
                   PA1.TYPE                     FROM_PARTY_TYPE,
                   PA1.VALUE                    FROM_PARTY_VALUE,
                   PA2.TYPE                     TO_PARTY_TYPE,
                   PA2.VALUE                    TO_PARTY_VALUE,
                   ML.MESSAGE_SUBTYPE           MESSAGE_SUBTYPE
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
        migrate_user_message_prereq(v_temp_table);

        /** migrate old columns and add data into dictionary tables */
        DBMS_OUTPUT.PUT_LINE('Start to migrate TB_USER_MESSAGE entries...');
        OPEN c_user_message;
        LOOP
            FETCH c_user_message BULK COLLECT INTO user_message;
            EXIT WHEN user_message.COUNT = 0;

            FOR i IN user_message.FIRST .. user_message.LAST
                LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO ' || v_temp_table ||
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
                        DBMS_OUTPUT.PUT_LINE('Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                        v_batch_no := v_batch_no + 1;
                    END IF;

                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Migrated ' || user_message.COUNT || ' records in total into ' || v_temp_table);
        END LOOP;

        COMMIT;
        CLOSE c_user_message;

        -- check counts
        IF check_counts('TB_USER_MESSAGE', v_temp_table) THEN
            DBMS_OUTPUT.PUT_LINE('TB_USER_MESSAGE migration is done');
            -- TODO drop old table and rename the temp one
        END IF;

    END migrate_tb_user_message;

    /**-- TB_MESSAGE_GROUP migration --*/
    PROCEDURE migrate_tb_message_group IS
        v_sql     VARCHAR2(1000);
        v_tab     VARCHAR2(30) := 'TB_MESSAGE_GROUP';
        v_tab_new VARCHAR2(30) := 'TB_SJ_MESSAGE_GROUP';
        v_tab_user_message_new VARCHAR2(30) := 'TEMP_TB_USER_MESSAGE';

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
        v_batch_no   INT          := 1;
    BEGIN
        IF NOT check_table_exists(v_tab_user_message_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab_user_message_new || ' should exists before starting '||v_tab||' migration');
        END IF;

        -- create the new table
        v_sql :=
                    'CREATE TABLE TB_SJ_MESSAGE_GROUP (ID_PK NUMBER(38, 0) NOT NULL, GROUP_ID VARCHAR2(255) NOT NULL, MESSAGE_SIZE NUMBER(38, 0), FRAGMENT_COUNT INTEGER, ' ||
                    'SENT_FRAGMENTS INTEGER, RECEIVED_FRAGMENTS INTEGER, COMPRESSION_ALGORITHM VARCHAR2(255), COMPRESSED_MESSAGE_SIZE NUMBER(38, 0), SOAP_ACTION VARCHAR2(255), ' ||
                    'REJECTED NUMBER(1), EXPIRED NUMBER(1), MSH_ROLE_ID_FK NUMBER(38, 0) NOT NULL, SOURCE_MESSAGE_ID_FK NUMBER(38, 0) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, ' ||
                    'CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_SJ_MESSAGE_GROUP PRIMARY KEY (ID_PK))';
        truncate_or_create_table(v_tab_new, v_sql);

        DBMS_OUTPUT.PUT_LINE('Start to migrate '||v_tab||' entries...');
        OPEN c_message_group;
        LOOP
            FETCH c_message_group BULK COLLECT INTO message_group;
            EXIT WHEN message_group.COUNT = 0;

            FOR i IN message_group.FIRST .. message_group.LAST
                LOOP
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
                        get_tb_d_role_rec(message_group(i).MSH_ROLE),
                        message_group(i).CREATION_TIME,
                        message_group(i).CREATED_BY,
                        message_group(i).MODIFICATION_TIME,
                        message_group(i).MODIFIED_BY,
                        get_tb_user_message_rec(message_group(i).SOURCE_MESSAGE_ID); -- we look into migrated table here
                    IF i MOD BATCH_SIZE = 0 THEN
                        COMMIT;
                        DBMS_OUTPUT.PUT_LINE(v_tab_new ||': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                        v_batch_no := v_batch_no + 1;
                    END IF;

                END LOOP;
            DBMS_OUTPUT.PUT_LINE(v_tab_new||': Migrated ' || message_group.COUNT || ' records in total');
        END LOOP;

        COMMIT;
        CLOSE c_message_group;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab||' migration is done');
            -- TODO drop old table
        END IF;

    END migrate_tb_message_group;

    /**-- TB_MESSAGE_FRAGMENT migration --*/
    PROCEDURE migrate_tb_message_fragment IS
        v_sql                     VARCHAR2(1000);
        v_tab VARCHAR2(30) := 'TB_MESSAGE_FRAGMENT';
        v_tab_new VARCHAR2(30) := 'TB_SJ_MESSAGE_FRAGMENT';
        v_tab_user_message VARCHAR2(30) := 'TB_USER_MESSAGE';

        CURSOR c_message_fragment IS
            SELECT UM.ID_PK,
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
        message_fragment T_MESSAGE_FRAGMENT;
        v_batch_no   INT          := 1;
    BEGIN
        IF NOT check_table_exists(v_tab_user_message) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab_user_message || ' should exists before starting TB_MESSAGE_FRAGMENT migration');
        END IF;

        -- create the new table
        v_sql :=
                'CREATE TABLE TB_SJ_MESSAGE_FRAGMENT (ID_PK NUMBER(38, 0) NOT NULL, FRAGMENT_NUMBER INTEGER NOT NULL, GROUP_ID_FK NUMBER(38, 0) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_MESSAGE_FRAGMENT PRIMARY KEY (ID_PK))';
        truncate_or_create_table(v_tab_new, v_sql);

        DBMS_OUTPUT.PUT_LINE('Start to migrate '||v_tab||' entries...');
        OPEN c_message_fragment;
        LOOP
            FETCH c_message_fragment BULK COLLECT INTO message_fragment;
            EXIT WHEN message_fragment.COUNT = 0;

            FOR i IN message_fragment.FIRST .. message_fragment.LAST
                LOOP
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
                        DBMS_OUTPUT.PUT_LINE(v_tab_new ||': Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                        v_batch_no := v_batch_no + 1;
                    END IF;

                END LOOP;
            DBMS_OUTPUT.PUT_LINE(v_tab_new||': Migrated ' || message_fragment.COUNT || ' records in total');
        END LOOP;

        COMMIT;
        CLOSE c_message_fragment;

        -- check counts
        IF check_counts(v_tab, v_tab_new) THEN
            DBMS_OUTPUT.PUT_LINE(v_tab||' migration is done');
            -- TODO drop old table
        END IF;

    END migrate_tb_message_fragment;

    /**-- main entry point for all migration --*/
    PROCEDURE migrate IS
    BEGIN
        migrate_tb_user_message;
        migrate_tb_message_fragment; -- keep it right after
        migrate_tb_message_group;
    END migrate;


END MIGRATE_42_TO_50;
/